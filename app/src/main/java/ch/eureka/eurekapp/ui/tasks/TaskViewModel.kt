package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Mock TaskRepository for testing and default instantiation Returns empty lists and successful
 * results
 */
private class MockTaskRepository : TaskRepository {
  override fun getTaskById(projectId: String, taskId: String): Flow<Task?> =
      kotlinx.coroutines.flow.flowOf(null)

  override fun getTasksInProject(projectId: String): Flow<List<Task>> =
      kotlinx.coroutines.flow.flowOf(emptyList())

  override fun getTasksForCurrentUser(): Flow<List<Task>> =
      kotlinx.coroutines.flow.flowOf(emptyList())

  override suspend fun createTask(task: Task): Result<String> = Result.success("mock-task-id")

  override suspend fun updateTask(task: Task): Result<Unit> = Result.success(Unit)

  override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> =
      Result.success(Unit)

  override suspend fun assignUser(projectId: String, taskId: String, userId: String): Result<Unit> =
      Result.success(Unit)

  override suspend fun unassignUser(
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = Result.success(Unit)
}

/** UI state data class for TasksScreen Contains all the data needed to render the tasks screen */
data class TaskUiState(
    val selectedFilter: TaskFilter = TaskFilter.MINE,
    val rawTasks: List<Task> = emptyList(), // Raw tasks from repository
    val isLoading: Boolean = false,
    val error: String? = null,
    val workspaceId: String? = null,
    val groupId: String? = null,
    val projectId: String? = null
)

/** Enum representing different task filtering options */
enum class TaskFilter {
  /** Show only tasks assigned to current user */
  MINE,
  /** Show tasks assigned to team members */
  TEAM,
  /** Show tasks due this week */
  THIS_WEEK,
  /** Show all tasks */
  ALL,
  /** Show tasks from specific project */
  PROJECT
}

/**
 * ViewModel for managing task state and data loading
 *
 * Connected to TaskRepository for real-time Firebase data updates. Handles task filtering, loading,
 * and completion status updates.
 *
 * @property uiState StateFlow containing current UI state
 * @property taskRepository Repository for task data operations
 */
class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

  constructor() : this(MockTaskRepository())

  companion object {
    /**
     * Factory for creating TaskViewModel instances Required for dependency injection in tests and
     * UI
     */
    fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(taskRepository) as T
          }
          throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
      }
    }
  }

  private val _uiState = MutableStateFlow(TaskUiState())
  val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

  private var currentJob: Job? = null

  init {
    loadTasks()
  }

  /**
   * Load tasks for the current user from Firebase Updates the UI state with raw task data (no UI
   * model conversion)
   */
  fun loadTasks() {
    currentJob?.cancel()
    currentJob =
        viewModelScope.launch {
          _uiState.value = _uiState.value.copy(isLoading = true, error = null)

          try {
            taskRepository.getTasksForCurrentUser().collect { tasks ->
              _uiState.value =
                  _uiState.value.copy(rawTasks = tasks, isLoading = false, error = null)
            }
          } catch (e: Exception) {
            _uiState.value =
                _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load tasks")
          }
        }
  }

  /**
   * Set the current task filter No need to reload data as filtering is handled by computed
   * properties
   *
   * @param filter The filter to apply
   */
  fun setFilter(filter: TaskFilter) {
    _uiState.value = _uiState.value.copy(selectedFilter = filter)
  }

  /** Get filtered tasks based on current filter This method handles all filtering logic */
  fun getFilteredTasks(): List<Task> {
    val state = _uiState.value
    return when (state.selectedFilter) {
      TaskFilter.MINE ->
          state.rawTasks.filter { task ->
            task.assignedUserIds.contains(FirebaseAuth.getInstance().currentUser?.uid)
          }
      TaskFilter.TEAM ->
          state.rawTasks.filter { task ->
            task.assignedUserIds.isNotEmpty() &&
                !task.assignedUserIds.contains(FirebaseAuth.getInstance().currentUser?.uid)
          }
      TaskFilter.THIS_WEEK ->
          state.rawTasks.filter { task -> TaskDateUtils.isDueThisWeek(task.dueDate) }
      TaskFilter.ALL -> state.rawTasks
      TaskFilter.PROJECT -> {
        if (state.projectId != null) {
          state.rawTasks.filter { task -> task.projectId == state.projectId }
        } else {
          emptyList()
        }
      }
    }
  }

  /** Get incomplete tasks from filtered results */
  fun getIncompleteTasks(): List<Task> {
    return getFilteredTasks().filter { task -> task.status != TaskStatus.COMPLETED }
  }

  /** Get completed tasks from filtered results */
  fun getCompletedTasks(): List<Task> {
    return getFilteredTasks().filter { task -> task.status == TaskStatus.COMPLETED }
  }

  /**
   * Update project context for filtering tasks by project
   *
   * @param workspaceId The workspace ID
   * @param groupId The group ID
   * @param projectId The project ID
   */
  fun updateProjectContext(workspaceId: String?, groupId: String?, projectId: String?) {
    _uiState.value =
        _uiState.value.copy(workspaceId = workspaceId, groupId = groupId, projectId = projectId)
  }

  /**
   * Toggle task completion status
   *
   * @param taskId The ID of the task to toggle
   */
  fun toggleTaskCompletion(taskId: String) {
    // Input validation
    if (taskId.isBlank()) {
      _uiState.value = _uiState.value.copy(error = "Task ID cannot be empty")
      return
    }

    viewModelScope.launch {
      try {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Find the task in current state
        val allTasks = _uiState.value.rawTasks
        val taskToUpdate = allTasks.find { it.taskID == taskId }

        if (taskToUpdate != null) {
          // Toggle the status
          val newStatus =
              if (taskToUpdate.status == TaskStatus.COMPLETED) {
                TaskStatus.TODO
              } else {
                TaskStatus.COMPLETED
              }

          val updatedTask = taskToUpdate.copy(status = newStatus)
          val result = taskRepository.updateTask(updatedTask)

          if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(isLoading = false)
          } else {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update task")
          }
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, error = "Task not found")
        }
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to update task")
      }
    }
  }
}

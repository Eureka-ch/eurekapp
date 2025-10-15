package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state data class for TasksScreen Contains all the data needed to render the tasks screen */
data class TaskUiState(
    val selectedFilter: TaskFilter = TaskFilter.MINE,
    val rawTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
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

  init {
    loadTasks()
  }

  /**
   * Load tasks for the current user from Firebase Simplified Flow usage - no manual job management
   */
  fun loadTasks() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      try {
        taskRepository.getTasksForCurrentUser().collect { tasks ->
          _uiState.value = _uiState.value.copy(rawTasks = tasks, isLoading = false, error = null)
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

  /** Get filtered tasks based on current filter */
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
          state.rawTasks.filter { task ->
            // Simple logic: tasks due within 7 days
            task.dueDate?.let { dueDate ->
              val now = System.currentTimeMillis()
              val taskTime = dueDate.toDate().time
              val diffInDays = (taskTime - now) / (1000 * 60 * 60 * 24)
              diffInDays in 0..7
            } ?: false
          }
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

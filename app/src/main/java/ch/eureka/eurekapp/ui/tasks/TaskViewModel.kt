package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.utils.TaskBusinessLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

  constructor() : this(MockTaskRepository())

  private val _uiState = MutableStateFlow(TaskUiState())
  val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

  // Additional data needed for TaskUiModel creation
  private val _assignees = MutableStateFlow<List<User>>(emptyList())
  private val _templates = MutableStateFlow<List<TaskTemplate>>(emptyList())

  init {
    loadTasks()
    loadAssignees() // TODO: Implement when UserRepository is available
    loadTemplates() // TODO: Implement when TemplateRepository is available
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

  /**
   * Computed properties for UI - replaces TaskUiModel Each property provides UI-ready data for
   * tasks
   */
  val taskTitles: StateFlow<List<String>> =
      combine(_uiState.map { it.rawTasks }, _templates) { tasks, templates ->
            tasks.map { task ->
              task.title.ifBlank {
                templates.find { it.templateID == task.templateId }?.title ?: "Untitled Task"
              }
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  val assigneeNames: StateFlow<List<String>> =
      combine(_uiState.map { it.rawTasks }, _assignees) { tasks, assignees ->
            tasks.map { task ->
              val assignee = assignees.find { it.uid in task.assignedUserIds }
              assignee?.displayName ?: "Unassigned"
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  val progressValues: StateFlow<List<Float>> =
      _uiState
          .map { state ->
            state.rawTasks.map { task ->
              when (task.status) {
                TaskStatus.COMPLETED -> 1.0f
                TaskStatus.IN_PROGRESS -> 0.5f
                TaskStatus.TODO -> 0.0f
                else -> 0.0f
              }
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  val progressTexts: StateFlow<List<String>> =
      progressValues
          .map { progressList -> progressList.map { progress -> "${(progress * 100).toInt()}%" } }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  val isCompletedList: StateFlow<List<Boolean>> =
      _uiState
          .map { state -> state.rawTasks.map { task -> TaskBusinessLogic.isTaskCompleted(task) } }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  val rawTasks: StateFlow<List<Task>> =
      _uiState
          .map { it.rawTasks }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyList())

  /** Load assignees (placeholder for now) */
  private fun loadAssignees() {
    // TODO: Implement when UserRepository is available
    // For now, keep empty list
  }

  /** Load templates (placeholder for now) */
  private fun loadTemplates() {
    // TODO: Implement when TemplateRepository is available
    // For now, keep empty list
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

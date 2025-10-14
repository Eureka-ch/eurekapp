package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskUiState(
    val selectedFilter: TaskFilter = TaskFilter.MINE,
    val myTasks: List<TaskUiModel> = emptyList(),
    val allTasks: List<TaskUiModel> = emptyList(),
    val recentCompleted: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class TaskFilter {
  MINE,
  ALL,
  PROJECT
}

/**
 * UI-only ViewModel with stub methods for future repository integration All data loading methods
 * are placeholders for now
 */
class TaskViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(TaskUiState())
  val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

  init {
    // TODO: Load initial tasks when repository is ready
    // loadTasks()
  }

  fun loadTasks() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      try {
        // TODO: Replace with real repository calls
        // val tasks = taskRepository.getTasksForCurrentUser()
        // val taskUiModels = tasks.map { createTaskUiModel(it) }

        // For now, show empty state
        _uiState.value = _uiState.value.copy(myTasks = emptyList(), isLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
      }
    }
  }

  fun setFilter(filter: TaskFilter) {
    _uiState.value = _uiState.value.copy(selectedFilter = filter)

    when (filter) {
      TaskFilter.MINE -> loadTasks()
      TaskFilter.ALL -> loadAllTasks()
      TaskFilter.PROJECT -> {
        // TODO: Get workspaceId, groupId, projectId from context
        // For now, use placeholder values
        loadProjectTasks("workspaceId", "groupId", "projectId")
      }
    }
  }

  private fun loadAllTasks() {
    viewModelScope.launch {
      try {
        // TODO: Replace with real repository calls
        // val allTasks = taskRepository.getAllTasks()
        // val taskUiModels = allTasks.map { createTaskUiModel(it) }

        // For now, use same data as myTasks
        _uiState.value = _uiState.value.copy(allTasks = _uiState.value.myTasks)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load all tasks")
      }
    }
  }

  fun refresh() {
    loadTasks()
  }

  private fun loadProjectTasks(workspaceId: String, groupId: String, projectId: String) {
    viewModelScope.launch {
      try {
        // TODO: Replace with real repository calls
        // val projectTasks = taskRepository.getTasksInProject(workspaceId, groupId, projectId)
        // val taskUiModels = projectTasks.map { createTaskUiModel(it) }

        // For now, use same data as myTasks (placeholder)
        _uiState.value = _uiState.value.copy(allTasks = _uiState.value.myTasks)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load project tasks")
      }
    }
  }

  // TODO: Add these methods when repository is ready
  /*
  private suspend fun createTaskUiModel(task: Task): TaskUiModel {
      // Get template
      val template = taskTemplateRepository.getTemplateById(task.templateId)

      // Get assignee (first assigned user)
      val assignee = if (task.assignedUserIds.isNotEmpty()) {
          userRepository.getUserById(task.assignedUserIds.first())
      } else {
          null
      }

      return TaskUiModel(
          task = task,
          template = template,
          assignee = assignee,
          progress = calculateProgress(task),
          isCompleted = isTaskCompleted(task)
      )
  }

  private fun calculateProgress(task: Task): Float {
      // Progress calculation logic
      return 0.5f // Placeholder
  }

  private fun isTaskCompleted(task: Task): Boolean {
      // Completion logic
      return false // Placeholder
  }
  */
}

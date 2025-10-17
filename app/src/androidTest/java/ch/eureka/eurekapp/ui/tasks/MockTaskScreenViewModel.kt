package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.screens.TaskAndUsers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mock implementation of TaskScreenViewModel for testing error states and edge cases
 *
 * Allows direct control over UI state without depending on repository flows
 */
class MockTaskScreenViewModel(
    taskRepository: TaskRepository,
    projectRepository: ProjectRepository,
    userRepository: UserRepository,
    currentUserId: String?,
    initialState: TaskScreenUiState = TaskScreenUiState()
) : TaskScreenViewModel(taskRepository, projectRepository, userRepository, currentUserId) {

  private val _mockUiState = MutableStateFlow(initialState)

  override val uiState: StateFlow<TaskScreenUiState>
    get() = _mockUiState

  /** Set a custom UI state for testing */
  fun setUiState(state: TaskScreenUiState) {
    _mockUiState.value = state
  }

  /** Set loading state */
  fun setLoading(isLoading: Boolean) {
    _mockUiState.value = _mockUiState.value.copy(isLoading = isLoading)
  }

  /** Set error state */
  fun setError(error: String?) {
    _mockUiState.value = _mockUiState.value.copy(error = error)
  }

  /** Set tasks and users */
  fun setTasksAndUsers(tasksAndUsers: List<TaskAndUsers>) {
    _mockUiState.value = _mockUiState.value.copy(tasksAndUsers = tasksAndUsers)
  }

  /** Set selected filter */
  override fun setFilter(filter: TaskScreenFilter) {
    _mockUiState.value = _mockUiState.value.copy(selectedFilter = filter)
  }

  /** Override toggle completion to track calls */
  var toggleCompletionCalls = mutableListOf<Task>()

  override fun toggleTaskCompletion(task: Task) {
    toggleCompletionCalls.add(task)
    // Simulate status change
    val newStatus =
        if (task.status == TaskStatus.COMPLETED) TaskStatus.TODO else TaskStatus.COMPLETED
    val updatedTask = task.copy(status = newStatus)

    // Update the tasks in UI state
    val updatedTasks =
        _mockUiState.value.tasksAndUsers.map { taskAndUsers ->
          if (taskAndUsers.task.taskID == task.taskID) {
            taskAndUsers.copy(task = updatedTask)
          } else {
            taskAndUsers
          }
        }
    _mockUiState.value = _mockUiState.value.copy(tasksAndUsers = updatedTasks)
  }
}

package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Data class combining Task with its assigned Users */

/** UI state data class for TasksScreen Contains all the data needed to render the tasks screen */
data class TaskScreenUiState(
    val tasksAndUsers: List<TaskAndUsers> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: TaskScreenFilter = TaskScreenFilter.Mine
)

/** Sealed class representing different task filtering options with their data */
sealed class TaskScreenFilter(val displayName: String) {
  /** Show only tasks assigned to current user */
  object Mine : TaskScreenFilter(MY_TASKS_DISPLAY_NAME)

  /** Show tasks assigned to team members */
  object Team : TaskScreenFilter(TEAM_DISPLAY_NAME)

  /** Show tasks due this week */
  object ThisWeek : TaskScreenFilter(THIS_WEEK_DISPLAY_NAME)

  /** Show all tasks */
  object All : TaskScreenFilter(ALL_DISPLAY_NAME)

  /** Show tasks from specific projects */
  /**
   * Constants for task filter options Centralizes filter option strings for better maintainability
   * and localization
   */
  companion object {
    const val MY_TASKS_DISPLAY_NAME = "My tasks"
    const val TEAM_DISPLAY_NAME = "Team"
    const val THIS_WEEK_DISPLAY_NAME = "This week"
    const val ALL_DISPLAY_NAME = "All"
    val values by lazy { listOf(Mine, Team, ThisWeek, All) }
  }
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
@OptIn(ExperimentalCoroutinesApi::class)
open class TaskScreenViewModel(
    private val taskRepository: TaskRepository =
        FirestoreTaskRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val projectRepository: ProjectRepository =
        FirestoreProjectRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val userRepository: UserRepository =
        FirestoreUserRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
) : ViewModel() {

  protected open val _uiState = MutableStateFlow(TaskScreenUiState())

  // Flow 1: Tasks assigned to current user
  private val userTasks = taskRepository.getTasksForCurrentUser()
  private val _selectedFilter = MutableStateFlow<TaskScreenFilter>(TaskScreenFilter.Mine)
  // Flow 2: All tasks in user's projects, filtered to exclude user's tasks (team tasks)
  @OptIn(ExperimentalCoroutinesApi::class)
  private val teamTasks: Flow<List<Task>> =
      projectRepository.getProjectsForCurrentUser().flatMapLatest { projects ->
        if (projects.isEmpty()) {
          flowOf(emptyList())
        } else {
          // Combine all task flows from all projects
          combine(
              projects.map { project -> taskRepository.getTasksInProject(project.projectId) }) {
                  tasksArrays ->
                tasksArrays
                    .flatMap { it.toList() }
                    .filter { task ->
                      currentUserId != null && !task.assignedUserIds.contains(currentUserId)
                    }
              }
        }
      }

  init {
    viewModelScope.launch {
      combine(userTasks, teamTasks, _selectedFilter) { userTasks, teamTasks, filter ->
            val tasks =
                when (filter) {
                  is TaskScreenFilter.Mine -> userTasks
                  is TaskScreenFilter.Team -> teamTasks
                  is TaskScreenFilter.All -> userTasks + teamTasks
                  is TaskScreenFilter.ThisWeek ->
                      (userTasks + teamTasks).filter { task ->
                        val now = Timestamp.now()
                        val daysUntilDue = getDaysUntilDue(task, now) ?: return@filter false
                        daysUntilDue in 0..7
                      }
                }
            Pair(filter, tasks)
          }
          .flatMapLatest { (filter, tasks) ->
            // For each task, fetch its assignees
            if (tasks.isEmpty()) {
              flowOf(Pair(filter, emptyList<TaskAndUsers>()))
            } else {
              combine(
                  tasks.map { task ->
                    getAssignees(task).map { users -> TaskAndUsers(task, users) }
                  }) { tasksAndUsersArray ->
                    Pair(filter, tasksAndUsersArray.toList())
                  }
            }
          }
          .collect { (filter, tasksAndUsers) ->
            _uiState.emit(
                _uiState.value.copy(
                    tasksAndUsers = tasksAndUsers,
                    selectedFilter = filter,
                    isLoading = false,
                    error = null))
          }
    }
  }

  open val uiState: StateFlow<TaskScreenUiState>
    get() = _uiState

  /**
   * Set the current task filter
   *
   * @param filter The filter to apply
   */
  open fun setFilter(filter: TaskScreenFilter) {
    _selectedFilter.value = filter
  }

  /** Determine priority based on due date */

  /**
   * Toggle task completion status
   *
   * @param taskId The ID of the task to toggle
   */
  open fun toggleTaskCompletion(task: Task) {
    viewModelScope.launch {
      val newStatus =
          if (task.status == TaskStatus.COMPLETED) TaskStatus.TODO else TaskStatus.COMPLETED
      val updatedTask = task.copy(status = newStatus)
      val result = taskRepository.updateTask(updatedTask)
      if (result.isFailure) {
        _uiState.value =
            _uiState.value.copy(
                error = "Failed to update task: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  fun getAssignees(task: Task): Flow<List<User>> {
    if (task.assignedUserIds.isEmpty()) return flowOf(emptyList())
    val users = task.assignedUserIds.map { userId -> userRepository.getUserById(userId) }
    return combine(users) { it.toList().filterNotNull() }
  }
}

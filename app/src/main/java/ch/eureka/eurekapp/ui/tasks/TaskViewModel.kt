package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.screens.TaskAndUsers
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Data class combining Task with its assigned Users Portions of this code were generated with the
 * help of AI. Portions added by Jiří Gebauer partially generated with the help of Grok.
 */

/** Internal data class for flow state management */
private data class TaskFlowState(
    val filter: TaskScreenFilter,
    val taskFlow: Flow<List<Task>>,
    val error: String?,
    val projects: List<Project>,
    val isConnected: Boolean
)

/** Internal data class for tasks state */
private data class TasksState(
    val filter: TaskScreenFilter,
    val tasks: List<Task>,
    val error: String?,
    val projects: List<Project>,
    val isConnected: Boolean
)

/** Internal data class for tasks with users state */
private data class TasksWithUsersState(
    val filter: TaskScreenFilter,
    val tasksAndUsers: List<TaskAndUsers>,
    val error: String?,
    val projects: List<Project>,
    val isConnected: Boolean
)

/** Internal data class for combined state */
private data class CombinedState(
    val userTasks: List<Task>,
    val teamTasks: List<Task>,
    val filter: TaskScreenFilter,
    val error: String?,
    val projects: List<Project>
)

/** UI state data class for TasksScreen Contains all the data needed to render the tasks screen */
data class TaskScreenUiState(
    val tasksAndUsers: List<TaskAndUsers> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: TaskScreenFilter = TaskScreenFilter.Mine,
    val availableProjects: List<Project> = emptyList(),
    val isConnected: Boolean = true
)

/** Sealed class representing different task filtering options with their data */
sealed class TaskScreenFilter(val displayName: String) {
  /** Show only tasks assigned to current user */
  object Mine : TaskScreenFilter(MY_TASKS_DISPLAY_NAME)

  /** Show tasks assigned to team members */
  object Team : TaskScreenFilter(TEAM_DISPLAY_NAME)

  /** Show tasks due today */
  object Today : TaskScreenFilter("Today")

  /** Show tasks due tomorrow */
  object Tomorrow : TaskScreenFilter("Tomorrow")

  /** Show tasks due this week */
  object ThisWeek : TaskScreenFilter(THIS_WEEK_DISPLAY_NAME)

  /** Show overdue tasks */
  object Overdue : TaskScreenFilter("Overdue")

  /** Show all tasks */
  object All : TaskScreenFilter(ALL_DISPLAY_NAME)

  /** Show tasks from a specific project */
  data class ByProject(val projectId: String, val projectName: String) :
      TaskScreenFilter(projectName)

  /**
   * Constants for task filter options Centralizes filter option strings for better maintainability
   * and localization
   */
  companion object {
    const val MY_TASKS_DISPLAY_NAME = "My tasks"
    const val TEAM_DISPLAY_NAME = "Team"
    const val THIS_WEEK_DISPLAY_NAME = "This week"
    const val ALL_DISPLAY_NAME = "All"
    val values by lazy { listOf(Mine, Team, Today, Tomorrow, ThisWeek, Overdue, All) }
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
    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid,
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  private val _selectedFilter = MutableStateFlow<TaskScreenFilter>(TaskScreenFilter.Mine)
  private val _error = MutableStateFlow<String?>(null)

  // Flow 0: Available projects
  private val availableProjects =
      projectRepository
          .getProjectsForCurrentUser()
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  // Flow 1: Tasks assigned to current user
  private val userTasks =
      taskRepository
          .getTasksForCurrentUser()
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  // Flow 2: All tasks in user's projects, filtered to exclude user's tasks (team tasks)
  @OptIn(ExperimentalCoroutinesApi::class)
  private val teamTasks: Flow<List<Task>> =
      projectRepository
          .getProjectsForCurrentUser()
          .flatMapLatest { projects ->
            if (projects.isEmpty()) {
              flowOf(emptyList())
            } else {
              // Combine all task flows from all projects
              combine(
                  projects.map { project ->
                    taskRepository.getTasksInProject(project.projectId)
                  }) { tasksArrays ->
                    tasksArrays
                        .flatMap { it.toList() }
                        .filter { task ->
                          currentUserId != null && !task.assignedUserIds.contains(currentUserId)
                        }
                  }
            }
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  /** Flow representing the current connectivity status (true if connected, false otherwise) */
  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  open val uiState: StateFlow<TaskScreenUiState> =
      combine(
              combine(userTasks, teamTasks, _selectedFilter, _error, availableProjects) {
                  userTasks,
                  teamTasks,
                  filter,
                  error,
                  projects ->
                CombinedState(userTasks, teamTasks, filter, error, projects)
              },
              _isConnected) { combined, isConnected ->
                // Extract tasks based on filter
                val allTasks = combined.userTasks + combined.teamTasks
                val now = Timestamp.now()

                val taskFlow =
                    when (combined.filter) {
                      is TaskScreenFilter.Mine -> flowOf(combined.userTasks)
                      is TaskScreenFilter.Team -> flowOf(combined.teamTasks)
                      is TaskScreenFilter.All -> flowOf(allTasks)
                      is TaskScreenFilter.Today ->
                          flowOf(
                              allTasks.filter { task ->
                                val daysUntilDue = getDaysUntilDue(task, now) ?: return@filter false
                                daysUntilDue == 0L
                              })
                      is TaskScreenFilter.Tomorrow ->
                          flowOf(
                              allTasks.filter { task ->
                                val daysUntilDue = getDaysUntilDue(task, now) ?: return@filter false
                                daysUntilDue == 1L
                              })
                      is TaskScreenFilter.ThisWeek ->
                          flowOf(
                              allTasks.filter { task ->
                                val daysUntilDue = getDaysUntilDue(task, now) ?: return@filter false
                                daysUntilDue in 0..7
                              })
                      is TaskScreenFilter.Overdue ->
                          flowOf(
                              allTasks.filter { task ->
                                val daysUntilDue = getDaysUntilDue(task, now) ?: return@filter false
                                daysUntilDue < 0
                              })
                      is TaskScreenFilter.ByProject ->
                          taskRepository.getTasksInProject(combined.filter.projectId)
                    }
                TaskFlowState(
                    combined.filter, taskFlow, combined.error, combined.projects, isConnected)
              }
          .flatMapLatest { flowState ->
            flowState.taskFlow.map { tasks ->
              TasksState(
                  flowState.filter,
                  tasks,
                  flowState.error,
                  flowState.projects,
                  flowState.isConnected)
            }
          }
          .flatMapLatest { tasksState ->
            // For each task, fetch its assignees
            if (tasksState.tasks.isEmpty()) {
              flowOf(
                  TasksWithUsersState(
                      tasksState.filter,
                      emptyList<TaskAndUsers>(),
                      tasksState.error,
                      tasksState.projects,
                      tasksState.isConnected))
            } else {
              combine(
                  tasksState.tasks.map { task ->
                    getAssignees(task).map { users -> TaskAndUsers(task, users) }
                  }) { tasksAndUsersArray ->
                    TasksWithUsersState(
                        tasksState.filter,
                        tasksAndUsersArray.toList(),
                        tasksState.error,
                        tasksState.projects,
                        tasksState.isConnected)
                  }
            }
          }
          .map { state ->
            TaskScreenUiState(
                tasksAndUsers = state.tasksAndUsers,
                selectedFilter = state.filter,
                isLoading = false,
                error = state.error,
                availableProjects = state.projects,
                isConnected = state.isConnected)
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, TaskScreenUiState(isLoading = true))

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
        _error.value = "Failed to update task: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun getAssignees(task: Task): Flow<List<User>> {
    if (task.assignedUserIds.isEmpty()) return flowOf(emptyList())
    val users = task.assignedUserIds.map { userId -> userRepository.getUserById(userId) }
    return combine(users) { it.toList().filterNotNull() }
  }
}

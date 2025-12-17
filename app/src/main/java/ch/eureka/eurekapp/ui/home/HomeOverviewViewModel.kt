/* Portions of this file where written with the help of Gemini. */
package ch.eureka.eurekapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Limit of items to display in the home overview sections. */
const val HOME_ITEMS_LIMIT = 3

data class TaskWithAssignees(val task: Task, val assigneeNames: List<String>)

data class HomeOverviewUiState(
    val currentUserName: String = "",
    val upcomingTasks: List<Task> = emptyList(),
    val upcomingTasksWithAssignees: List<TaskWithAssignees> = emptyList(),
    val upcomingMeetings: List<Meeting> = emptyList(),
    val recentProjects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean = true,
    val error: String? = null,
)

/**
 * View Model for the Home Overview screen. Manages data fetching for tasks, meetings, and projects
 * summary.
 *
 * @param taskRepository Repository for Task data.
 * @param projectRepository Repository for Project data.
 * @param meetingRepository Repository for Meeting data.
 * @param userRepository Repository for User data.
 * @param connectivityFlow Flow indicating network connectivity status.
 */
class HomeOverviewViewModel(
    taskRepository: TaskRepository = RepositoriesProvider.taskRepository,
    projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val meetingRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    userRepository: UserRepository = RepositoriesProvider.userRepository,
    connectivityFlow: Flow<Boolean> = ConnectivityObserverProvider.connectivityObserver.isConnected,
) : ViewModel() {

  private val _error = MutableStateFlow<String?>(null)

  /** Flow of the current user so we can reuse it for name and task filtering. */
  private val currentUser: Flow<User?> =
      userRepository.getCurrentUser().catch { throwable ->
        _error.value = throwable.message
        emit(null)
      }

  private val projectsFlow: StateFlow<List<Project>> =
      projectRepository
          .getProjectsForCurrentUser(skipCache = false)
          .catch { throwable ->
            _error.value = throwable.message
            emit(emptyList())
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val currentUserName: Flow<String> =
      currentUser
          .map { user -> user?.displayName ?: "" }
          .catch { throwable ->
            _error.value = throwable.message
            emit("")
          }

  private val upcomingTasks: Flow<List<Task>> =
      combine(currentUser, taskRepository.getTasksForCurrentUser()) { user, tasks ->
            val currentUserId = user?.uid
            tasks
                // Home overview: only show tasks assigned to the current user
                .filter { task ->
                  currentUserId != null &&
                      task.assignedUserIds.contains(currentUserId) &&
                      task.status != TaskStatus.COMPLETED
                }
                .sortedBy { it.dueDate.toEpochMillisOrMax() }
          }
          .catch { throwable ->
            _error.value = throwable.message
            emit(emptyList())
          }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val upcomingTasksWithAssignees: Flow<List<TaskWithAssignees>> =
      upcomingTasks.flatMapLatest { tasks ->
        if (tasks.isEmpty()) {
          flowOf(emptyList())
        } else {
          combine(
              tasks.map { task ->
                if (task.assignedUserIds.isEmpty()) {
                  flowOf(TaskWithAssignees(task, emptyList()))
                } else {
                  val userFlows =
                      task.assignedUserIds.map { userId ->
                        userRepository.getUserById(userId).map {
                          it?.displayName?.ifBlank { it.email } ?: ""
                        }
                      }
                  combine(userFlows) { names ->
                    TaskWithAssignees(task, names.filter { it.isNotEmpty() }.toList())
                  }
                }
              }) { taskWithAssigneesArray ->
                taskWithAssigneesArray.toList()
              }
        }
      }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val upcomingMeetings: Flow<List<Meeting>> =
      projectsFlow.flatMapLatest { projects ->
        if (projects.isEmpty()) {
          flowOf(emptyList())
        } else {
          combine(
                  projects.map { project ->
                    meetingRepository
                        .getMeetingsForCurrentUser(project.projectId, skipCache = false)
                        .catch { throwable ->
                          _error.value = throwable.message
                          emit(emptyList())
                        }
                  }) { meetingsArrays ->
                    meetingsArrays.flatMap { it.toList() }
                  }
              .map { meetings ->
                val now = Timestamp.now().toDate().time
                meetings
                    .filter { meeting ->
                      meeting.status != MeetingStatus.COMPLETED &&
                          (meeting.datetime?.toDate()?.time?.let { it >= now } == true ||
                              meeting.meetingProposals.any { proposal ->
                                proposal.votes.isNotEmpty() &&
                                    proposal.dateTime.toDate().time >= now
                              })
                    }
                    .sortedBy { meeting ->
                      meeting.datetime?.toDate()?.time
                          ?: meeting.meetingProposals
                              .filter { it.votes.isNotEmpty() }
                              .minOfOrNull { it.dateTime.toDate().time }
                          ?: Long.MAX_VALUE
                    }
              }
        }
      }

  private val recentProjects: Flow<List<Project>> =
      projectsFlow.map { projects ->
        projects.sortedByDescending { it.lastUpdated.toEpochMillisOrMin() }
      }

  private val connectivity: StateFlow<Boolean> =
      connectivityFlow.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  /** The UI state exposed to the view. Combines all data flows into a single state object. */
  val uiState: StateFlow<HomeOverviewUiState> =
      combine(
              currentUserName,
              upcomingTasks,
              upcomingTasksWithAssignees,
              upcomingMeetings,
              recentProjects,
              connectivity) { values ->
                val name = values[0] as String
                val tasks = values[1] as List<Task>
                val tasksWithAssignees = values[2] as List<TaskWithAssignees>
                val meetings = values[3] as List<Meeting>
                val projects = values[4] as List<Project>
                val isConnected = values[5] as Boolean
                HomeOverviewUiState(
                    currentUserName = name,
                    upcomingTasks = tasks,
                    upcomingTasksWithAssignees = tasksWithAssignees,
                    upcomingMeetings = meetings,
                    recentProjects = projects,
                    isConnected = isConnected,
                    error = _error.value,
                    isLoading = false)
              }
          .stateIn(
              viewModelScope,
              SharingStarted.Eagerly,
              HomeOverviewUiState(isLoading = true, isConnected = connectivity.value))
}

private fun Timestamp?.toEpochMillisOrMax(): Long {
  return this?.toDate()?.time ?: System.currentTimeMillis()
}

private fun Timestamp?.toEpochMillisOrMin(): Long {
  return this?.toDate()?.time ?: System.currentTimeMillis()
}

package ch.eureka.eurekapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
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

const val HOME_ITEMS_LIMIT = 3

data class HomeOverviewUiState(
    val currentUserName: String = "",
    val upcomingTasks: List<Task> = emptyList(),
    val upcomingMeetings: List<Meeting> = emptyList(),
    val recentProjects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean = true,
    val error: String? = null,
)

class HomeOverviewViewModel(
    private val taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val projectRepository: ProjectRepository =
        FirestoreRepositoriesProvider.projectRepository,
    private val meetingRepository: MeetingRepository =
        FirestoreRepositoriesProvider.meetingRepository,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    connectivityFlow: Flow<Boolean> = ConnectivityObserverProvider.connectivityObserver.isConnected,
) : ViewModel() {

  private val _error = MutableStateFlow<String?>(null)

  private val projectsFlow: StateFlow<List<Project>> =
      projectRepository
          .getProjectsForCurrentUser(skipCache = false)
          .catch { throwable ->
            _error.value = throwable.message
            emit(emptyList())
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val currentUserName: Flow<String> =
      userRepository
          .getCurrentUser()
          .map { user -> user?.displayName ?: "" }
          .catch { throwable ->
            _error.value = throwable.message
            emit("")
          }

  private val upcomingTasks: Flow<List<Task>> =
      taskRepository
          .getTasksForCurrentUser()
          .map { tasks ->
            tasks
                .filter { it.status != TaskStatus.COMPLETED }
                .sortedBy { it.dueDate.toEpochMillisOrMax() }
                .take(HOME_ITEMS_LIMIT)
          }
          .catch { throwable ->
            _error.value = throwable.message
            emit(emptyList())
          }

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
                meetings
                    .filter { it.status != MeetingStatus.COMPLETED }
                    .sortedBy { meeting -> meeting.datetime.toEpochMillisOrMax() }
                    .take(HOME_ITEMS_LIMIT)
              }
        }
      }

  private val recentProjects: Flow<List<Project>> =
      projectsFlow.map { projects ->
        projects.sortedByDescending { it.lastUpdated.toEpochMillisOrMin() }.take(HOME_ITEMS_LIMIT)
      }

  private val connectivity: StateFlow<Boolean> =
      connectivityFlow.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  val uiState: StateFlow<HomeOverviewUiState> =
      combine(currentUserName, upcomingTasks, upcomingMeetings, recentProjects, connectivity) {
              name,
              tasks,
              meetings,
              projects,
              isConnected ->
            HomeOverviewUiState(
                currentUserName = name,
                upcomingTasks = tasks,
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
  return this?.toDate()?.time ?: Long.MAX_VALUE
}

private fun Timestamp?.toEpochMillisOrMin(): Long {
  return this?.toDate()?.time ?: Long.MIN_VALUE
}

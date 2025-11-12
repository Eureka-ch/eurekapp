package ch.eureka.eurekapp.model.tasks

import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Portions of this code were generated with the help of Grok.
/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

/**
 * ViewModel for viewing task details. This ViewModel is responsible only for loading and displaying
 * task information.
 */
class ViewTaskViewModel(
    projectId: String,
    taskId: String,
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository
) : ReadTaskViewModel<ViewTaskState>(taskRepository, dispatcher) {

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  override val uiState: StateFlow<ViewTaskState> =
      combine(
              taskRepository
                  .getTaskById(projectId, taskId)
                  .flatMapLatest { task ->
                    if (task != null) {
                      val baseState =
                          ViewTaskState(
                              title = task.title,
                              description = task.description,
                              dueDate =
                                  task.dueDate?.let { date -> dateFormat.format(date.toDate()) }
                                      ?: "",
                              projectId = task.projectId,
                              taskId = task.taskID,
                              attachmentUrls = task.attachmentUrls,
                              status = task.status,
                              isLoading = false,
                              errorMsg = null,
                              assignedUsers = emptyList())

                      if (task.assignedUserIds.isEmpty()) {
                        flowOf(baseState)
                      } else {
                        // Combine all user flows to get assigned users
                        val userFlows =
                            task.assignedUserIds.map { userId ->
                              userRepository.getUserById(userId)
                            }
                        combine(userFlows) { users -> users.toList().filterNotNull() }
                            .map { assignedUsers -> baseState.copy(assignedUsers = assignedUsers) }
                      }
                    } else {
                      flowOf(ViewTaskState(isLoading = false, errorMsg = "Task not found."))
                    }
                  }
                  .catch { exception ->
                    emit(
                        ViewTaskState(
                            isLoading = false,
                            errorMsg = "Failed to load Task: ${exception.message}"))
                  },
              _isConnected) { taskState, isConnected ->
                taskState.copy(isConnected = isConnected)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ViewTaskState())
}

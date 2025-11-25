package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.downloads.DownloadService
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import java.io.File
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
import kotlinx.coroutines.launch

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
    private val downloadedFileDao: DownloadedFileDao,
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ReadTaskViewModel<ViewTaskState>(taskRepository, dispatcher) {

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val _downloadedFiles = downloadedFileDao.getAll()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun downloadFile(url: String, fileName: String, context: Context) {
    val downloadService = DownloadService(context)
    viewModelScope.launch {
      val uri = downloadService.downloadFile(url, fileName)
      if (uri != null) {
        downloadedFileDao.insert(
            DownloadedFile(
                url = url,
                localPath = uri.toString(),
                fileName = fileName
            )
        )
      }
    }
  }

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
              _isConnected,
              _downloadedFiles) { taskState, isConnected, downloadedFiles ->
                val urlToUriMap: Map<String, Uri> = downloadedFiles.associate { file ->
                  file.url to file.localPath.toUri()
                }

                val offlineAttachments = if (!isConnected) {
                  taskState.attachmentUrls.mapNotNull { url -> urlToUriMap[url] }
                } else {
                  emptyList()
                }

                val downloadedUrls = downloadedFiles.map { it.url }.toSet()
                val urlsToDownload = taskState.attachmentUrls.filter { it !in downloadedUrls }

                val effectiveAttachments = if (isConnected) {
                  taskState.attachmentUrls + taskState.attachmentUris
                } else {
                  offlineAttachments + taskState.attachmentUris + urlsToDownload
                }

                taskState.copy(
                    isConnected = isConnected,
                    urlsToDownload = urlsToDownload,
                    effectiveAttachments = effectiveAttachments)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ViewTaskState())
}

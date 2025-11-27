// Portions of this code were generated with the help of Grok and GPT-5.
package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.downloads.DownloadService
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
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

/**
 * ViewModel for viewing task details. This ViewModel is responsible only for loading and displaying
 * task information.
 */
class ViewTaskViewModel(
    projectId: String,
    taskId: String,
    taskRepository: TaskRepository = RepositoriesProvider.taskRepository,
    private val downloadedFileDao: DownloadedFileDao,
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ReadTaskViewModel<ViewTaskState>(taskRepository, dispatcher) {

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val _downloadedFiles =
      downloadedFileDao
          .getAll()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun downloadFile(url: String, fileName: String, context: Context) {
    viewModelScope.launch {
      if (downloadedFileDao.isDownloaded(url)) {
        // File already downloaded, skip
        return@launch
      }
      val downloadService = DownloadService(context)
      val result = downloadService.downloadFile(url, fileName)
      result.onSuccess { uri ->
        downloadedFileDao.insert(
            DownloadedFile(url = url, localPath = uri.toString(), fileName = fileName))
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
                val urlToUriMap: Map<String, Uri> =
                    downloadedFiles.associate { file -> file.url to file.localPath.toUri() }

                val downloadedUrls = downloadedFiles.map { it.url }.toSet()
                val urlsToDownload = taskState.attachmentUrls.filter { it !in downloadedUrls }

                val effectiveAttachments =
                    buildList<Attachment> {
                      if (isConnected) {
                        // Online: show all remote URLs
                        taskState.attachmentUrls.forEach { url -> add(Attachment.Remote(url)) }
                      } else {
                        // Offline: show downloaded files as Local, undownloaded as Remote
                        taskState.attachmentUrls.forEach { url ->
                          val localUri = urlToUriMap[url]
                          if (localUri != null) {
                            add(Attachment.Local(localUri))
                          } else {
                            add(Attachment.Remote(url))
                          }
                        }
                      }
                      // Add local URIs (e.g., newly taken photos)
                      taskState.attachmentUris.forEach { uri -> add(Attachment.Local(uri)) }
                    }

                taskState.copy(
                    isConnected = isConnected,
                    urlsToDownload = urlsToDownload,
                    effectiveAttachments = effectiveAttachments,
                    downloadedAttachmentUrls = downloadedUrls)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ViewTaskState())
}

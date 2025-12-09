// Portions of this code were generated with the help of Grok and GPT-5.
package ch.eureka.eurekapp.model.tasks

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.downloads.DownloadService
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for viewing task details. This ViewModel is responsible only for loading and displaying
 * task information.
 */
class ViewTaskViewModel(
    projectId: String,
    taskId: String,
    private val downloadedFileDao: DownloadedFileDao,
    taskRepository: TaskRepository = RepositoriesProvider.taskRepository,
    private val templateRepository: TaskTemplateRepository =
        RepositoriesProvider.taskTemplateRepository,
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

  @OptIn(ExperimentalCoroutinesApi::class)
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
                              assignedUsers = emptyList(),
                              customData = task.customData)

                      // Combine users and template flows
                      val userFlow =
                          if (task.assignedUserIds.isEmpty()) {
                            flowOf(emptyList())
                          } else {
                            val userFlows =
                                task.assignedUserIds.map { userId ->
                                  userRepository.getUserById(userId)
                                }
                            combine(userFlows) { users -> users.toList().filterNotNull() }
                          }

                      val templateFlow: kotlinx.coroutines.flow.Flow<TaskTemplate?> =
                          if (task.templateId.isNotEmpty()) {
                            templateRepository.getTemplateById(task.projectId, task.templateId)
                          } else {
                            flowOf(null)
                          }

                      combine(userFlow, templateFlow) { users, template ->
                        baseState.copy(assignedUsers = users, selectedTemplate = template)
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
                val downloadedUrls = downloadedFiles.map { it.url }.toSet()
                val urlsToDownload =
                    taskState.attachmentUrls
                        .map { it.substringBefore("|") }
                        .filter { it !in downloadedUrls }

                taskState.copy(
                    isConnected = isConnected,
                    urlsToDownload = urlsToDownload,
                    downloadedAttachmentUrls = downloadedUrls,
                    downloadedFiles = downloadedFiles)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ViewTaskState())
}

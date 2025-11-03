package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Portions of this code were generated with the help of Grok.

/**
 * ViewModel for viewing task details.
 * This ViewModel is responsible only for loading and displaying task information.
 */
class ViewTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseTaskViewModel<ViewTaskState>(taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val _uiState = MutableStateFlow(ViewTaskState())
  override val uiState: StateFlow<ViewTaskState> = _uiState.asStateFlow()

  override fun getState(): ViewTaskState = _uiState.value

  private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

  /**
   * Loads a task by its ID and updates the UI state.
   * @param projectId The ID of the project containing the task
   * @param taskId The ID of the task to load
   */
  fun loadTask(projectId: String, taskId: String) {
    _uiState.value = _uiState.value.copy(isLoading = true)

    viewModelScope.launch(dispatcher) {
      taskRepository
          .getTaskById(projectId, taskId)
          .catch { exception ->
            setErrorMsg("Failed to load Task: ${exception.message}")
            _uiState.value = _uiState.value.copy(isLoading = false)
          }
          .collect { task ->
            if (task != null) {
              _uiState.value =
                  _uiState.value.copy(
                      title = task.title,
                      description = task.description,
                      dueDate = task.dueDate?.let { date -> dateFormat.format(date.toDate()) } ?: "",
                      projectId = task.projectId,
                      taskId = task.taskID,
                      attachmentUrls = task.attachmentUrls,
                      status = task.status,
                      isLoading = false
                  )
            } else {
              setErrorMsg("Task not found.")
              _uiState.value = _uiState.value.copy(isLoading = false)
            }
          }
    }
  }

  // State update implementations
  override fun updateState(update: ViewTaskState.() -> ViewTaskState) {
    _uiState.value = _uiState.value.update()
  }

  override fun ViewTaskState.copyWithErrorMsg(errorMsg: String?) = copy(errorMsg = errorMsg)

  override fun ViewTaskState.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean) =
      copy(isSaving = isSaving, taskSaved = taskSaved)

  override fun ViewTaskState.copyWithTitle(title: String) = copy(title = title)

  override fun ViewTaskState.copyWithDescription(description: String) = copy(description = description)

  override fun ViewTaskState.copyWithDueDate(dueDate: String) = copy(dueDate = dueDate)

  override fun ViewTaskState.copyWithAttachmentUris(uris: List<Uri>) = copy(attachmentUris = uris)

  override fun ViewTaskState.copyWithProjectId(projectId: String) = copy(projectId = projectId)
}


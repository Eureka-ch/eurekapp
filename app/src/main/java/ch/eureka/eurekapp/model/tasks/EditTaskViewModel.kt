package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the EditTask screen. This ViewModel manages the state of input fields. */
class EditTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseTaskViewModel<EditTaskState>(taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

  private val _uiState = MutableStateFlow(EditTaskState())
  override val uiState: StateFlow<EditTaskState> = _uiState.asStateFlow()

  override fun getState(): EditTaskState = _uiState.value

  /** Resets the delete state after navigation or handling */
  fun resetDeleteState() {
    _uiState.value = _uiState.value.copy(taskDeleted = false)
  }

  /** Edits a Task */
  fun editTask(context: Context) {
    val state = uiState.value

    val timestampResult = parseDateString(state.dueDate)
    if (timestampResult.isFailure) {
      setErrorMsg(timestampResult.exceptionOrNull()?.message ?: "Invalid date")
      return
    }
    val timestamp = timestampResult.getOrThrow()

    val reminderTimestamp =
        if (state.reminderTime.isNotBlank() && state.dueDate.isNotBlank()) {
          parseReminderTime(state.dueDate, state.reminderTime).getOrNull()
        } else null

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, _ ->
      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT).show()
      }
      updateState { copy(isSaving = false) }
    }

    updateState { copy(isSaving = true) }

    saveFilesAsync(state.taskId, context, state.projectId, state.attachmentUris) { photoUrlsResult
      ->
      if (photoUrlsResult.isFailure) {
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT)
              .show()
        }
        updateState { copy(isSaving = false) }
        return@saveFilesAsync
      }

      val newPhotoUrls = photoUrlsResult.getOrThrow()

      viewModelScope.launch(dispatcher + handler) {
        val task =
            Task(
                taskID = state.taskId,
                projectId = state.projectId,
                title = state.title,
                description = state.description,
                assignedUserIds = listOf(currentUser),
                dueDate = timestamp,
                reminderTime = reminderTimestamp,
                attachmentUrls = state.attachmentUrls + newPhotoUrls,
                createdBy = currentUser,
                status = state.status)

        taskRepository.updateTask(task).onFailure { _ ->
          setErrorMsg("Failed to update Task.")
          updateState { copy(isSaving = false) }
          return@launch
        }

        clearErrorMsg()
        updateState { copy(isSaving = false, taskSaved = true) }
      }
    }
  }

  override fun removeAttachment(index: Int) {
    val state = _uiState.value
    val currentUris = state.attachmentUris
    val currentUrls = state.attachmentUrls

    if (index in currentUris.indices) {
      _uiState.value =
          _uiState.value.copy(
              attachmentUris = currentUris.toMutableList().apply { removeAt(index) })
    } else if (index - currentUris.size in currentUrls.indices) {
      val urlIndex = index - currentUris.size
      val urlToDelete = currentUrls[urlIndex]
      _uiState.value =
          _uiState.value.copy(
              attachmentUrls = currentUrls.toMutableList().apply { removeAt(urlIndex) },
              deletedAttachmentUrls = state.deletedAttachmentUrls + urlToDelete)
    }
  }

  fun removeAttachmentAndDelete(context: Context, index: Int) {
    val allAttachments = _uiState.value.attachmentUrls + _uiState.value.attachmentUris
    if (index !in allAttachments.indices) return

    val file = allAttachments[index]
    val uri =
        when (file) {
          is String -> file.toUri()
          else -> file as Uri
        }

    deletePhotoAsync(context, uri) { success ->
      if (success) {
        removeAttachment(index)
      }
    }
  }

  fun loadTask(projectId: String, taskId: String) {
    // Don't load task if it's being deleted or already deleted
    if (_uiState.value.isDeleting || _uiState.value.taskDeleted) {
      return
    }

    viewModelScope.launch(dispatcher) {
      taskRepository
          .getTaskById(projectId, taskId)
          .catch { exception ->
            // Only show error if we're not in the process of deleting
            if (!_uiState.value.isDeleting && !_uiState.value.taskDeleted) {
              setErrorMsg("Failed to load Task: ${exception.message}")
            }
          }
          .collect { task ->
            if (!_uiState.value.isDeleting && !_uiState.value.taskDeleted) {
              if (task != null) {
                val deletedUrls = _uiState.value.deletedAttachmentUrls
                val filteredAttachments = task.attachmentUrls.filterNot { it in deletedUrls }
                _uiState.value =
                    _uiState.value.copy(
                        title = task.title,
                        description = task.description,
                        dueDate =
                            task.dueDate?.let { date -> dateFormat.format(date.toDate()) } ?: "",
                        reminderTime =
                            task.reminderTime?.let { time -> timeFormat.format(time.toDate()) }
                                ?: "",
                        templateId = task.templateId,
                        projectId = task.projectId,
                        taskId = task.taskID,
                        assignedUserIds = task.assignedUserIds,
                        attachmentUrls = filteredAttachments,
                        status = task.status,
                        customData = task.customData,
                    )
              } else {
                setErrorMsg("Task not found.")
              }
            }
          }
    }
  }

  fun deleteTask(projectId: String, taskId: String) {
    _uiState.value = _uiState.value.copy(isDeleting = true)

    for (url in _uiState.value.attachmentUrls) {
      viewModelScope.launch(dispatcher) {
        fileRepository.deleteFile(url).onFailure { exception ->
          setErrorMsg("Failed to delete attachment: ${exception.message}")
        }
      }
    }

    viewModelScope.launch(dispatcher) {
      taskRepository
          .deleteTask(projectId, taskId)
          .onFailure { exception ->
            setErrorMsg("Failed to delete Task: ${exception.message}")
            _uiState.value = _uiState.value.copy(isDeleting = false)
            return@launch
          }
          .onSuccess { _ ->
            _uiState.value = _uiState.value.copy(isDeleting = false, taskDeleted = true)
          }
    }
  }

  fun setStatus(status: TaskStatus) {
    _uiState.value = _uiState.value.copy(status = status)
  }

  // State update implementations
  override fun updateState(update: EditTaskState.() -> EditTaskState) {
    _uiState.value = _uiState.value.update()
  }

  override fun EditTaskState.copyWithErrorMsg(errorMsg: String?) = copy(errorMsg = errorMsg)

  override fun EditTaskState.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean) =
      copy(isSaving = isSaving, taskSaved = taskSaved)

  override fun EditTaskState.copyWithTitle(title: String) = copy(title = title)

  override fun EditTaskState.copyWithDescription(description: String) =
      copy(description = description)

  override fun EditTaskState.copyWithDueDate(dueDate: String) = copy(dueDate = dueDate)

  override fun EditTaskState.copyWithAttachmentUris(uris: List<Uri>) = copy(attachmentUris = uris)

  override fun EditTaskState.copyWithProjectId(projectId: String) = copy(projectId = projectId)

  fun setReminderTime(reminderTime: String) {
    updateState { copy(reminderTime = reminderTime) }
  }
}

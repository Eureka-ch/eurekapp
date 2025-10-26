package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskCommonConstants
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskFileOperations
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskValidation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the EditTask screen. This ViewModel manages the state of input fields. */
class EditTaskViewModel(
    private val taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val fileRepository: FileStorageRepository =
        FirestoreRepositoriesProvider.fileRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
  // EditTask state
  private val _uiState = MutableStateFlow(EditTaskState())
  val uiState: StateFlow<EditTaskState> = _uiState.asStateFlow()

  val dateRegex = TaskCommonConstants.DATE_REGEX

  val inputValid: StateFlow<Boolean> =
      _uiState
          .map { state ->
            TaskValidation.isValidInput(state.title, state.description, state.dueDate)
          }
          .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = false)

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  private suspend fun saveFilesOnRepository(
      taskId: String,
      context: Context
  ): Result<List<String>> {
    val state = _uiState.value
    return TaskFileOperations.uploadAttachments(
        projectId = state.projectId,
        taskId = taskId,
        attachmentUris = state.attachmentUris,
        fileRepository = fileRepository,
        onDelete = { uri -> deletePhotoSuspend(context, uri) })
  }

  /** Resets the save state to allow creating another task */
  fun resetSaveState() {
    _uiState.value = _uiState.value.copy(isSaving = false, taskSaved = false)
  }

  /** Resets the delete state after navigation or handling */
  fun resetDeleteState() {
    _uiState.value = _uiState.value.copy(taskDeleted = false)
  }

  /** Edits a Task */
  fun editTask(context: Context) {
    val state = _uiState.value

    val timestampResult = TaskValidation.parseDateString(state.dueDate)
    if (timestampResult.isFailure) {
      setErrorMsg(timestampResult.exceptionOrNull()?.message ?: "Invalid date")
      return
    }
    val timestamp = timestampResult.getOrThrow()

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, exception ->
      Log.e("EditTaskViewModel", exception.message ?: "Unknown error")

      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT).show()
      }
      _uiState.value = _uiState.value.copy(isSaving = false)
    }

    _uiState.value = _uiState.value.copy(isSaving = true)

    viewModelScope.launch(dispatcher + handler) {
      val photoUrlsResult = saveFilesOnRepository(uiState.value.taskId, context)
      val newPhotoUrls =
          photoUrlsResult.getOrElse { exception ->
            handler.handleException(coroutineContext, exception)
            return@launch
          }

      val task =
          Task(
              taskID = uiState.value.taskId,
              projectId = state.projectId,
              title = state.title,
              description = state.description,
              assignedUserIds = listOf(currentUser),
              dueDate = timestamp,
              attachmentUrls = state.attachmentUrls + newPhotoUrls,
              createdBy = currentUser,
              status = state.status)

      taskRepository.updateTask(task).onFailure {
        setErrorMsg("Failed to update Task.")
        _uiState.value = _uiState.value.copy(isSaving = false)
        return@launch
      }

      clearErrorMsg()
      _uiState.value = _uiState.value.copy(isSaving = false, taskSaved = true)
    }
  }

  // Functions to update the UI state.

  fun setTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }

  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  fun setDueDate(dueDate: String) {
    _uiState.value = _uiState.value.copy(dueDate = dueDate)
  }

  fun addAttachment(uri: Uri) {
    if (!_uiState.value.attachmentUris.contains(uri)) {
      _uiState.value = _uiState.value.copy(attachmentUris = _uiState.value.attachmentUris + uri)
    }
  }

  fun removeAttachment(index: Int) {
    _uiState.value =
        _uiState.value.copy(
            attachmentUris =
                _uiState.value.attachmentUris.toMutableList().also {
                  if (index in it.indices) it.removeAt(index)
                },
            attachmentUrls =
                _uiState.value.attachmentUrls.toMutableList().also {
                  if (index in it.indices) it.removeAt(index)
                })
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

    viewModelScope.launch(dispatcher) {
      if (deletePhotoSuspend(context, uri)) {
        removeAttachment(index)
      }
    }
  }

  fun setProjectId(id: String) {
    _uiState.value = _uiState.value.copy(projectId = id)
  }

  private suspend fun deletePhotoSuspend(context: Context, photoUri: Uri): Boolean {
    return TaskFileOperations.deletePhoto(context, photoUri, fileRepository)
  }

  fun deletePhotosOnDispose(context: Context, photoUris: List<Uri>) {
    viewModelScope.launch(dispatcher) {
      photoUris.forEach { uri -> deletePhotoSuspend(context, uri) }
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
                _uiState.value =
                    _uiState.value.copy(
                        title = task.title,
                        description = task.description,
                        dueDate =
                            task.dueDate?.let { date ->
                              TaskCommonConstants.DATE_FORMAT.format(date.toDate())
                            } ?: "",
                        templateId = task.templateId,
                        projectId = task.projectId,
                        taskId = task.taskID,
                        assignedUserIds = task.assignedUserIds,
                        attachmentUrls = task.attachmentUrls,
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
          .onSuccess {
            _uiState.value = _uiState.value.copy(isDeleting = false, taskDeleted = true)
          }
    }
  }

  fun setStatus(status: TaskStatus) {
    _uiState.value = _uiState.value.copy(status = status)
  }
}

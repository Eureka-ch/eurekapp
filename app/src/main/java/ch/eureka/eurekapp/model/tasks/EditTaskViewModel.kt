package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class EditTaskViewModel(
    private val taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val fileRepository: FileStorageRepository =
        FirestoreRepositoriesProvider.fileRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
  // CreateTask state
  private val _uiState = MutableStateFlow(EditTaskState())
  val uiState: StateFlow<EditTaskState> = _uiState.asStateFlow()

  val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")

  val inputValid: StateFlow<Boolean> =
      _uiState
          .map { state ->
            state.title.isNotBlank() &&
                state.description.isNotBlank() &&
                dateRegex.matches(state.dueDate)
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
    return try {
      val state = _uiState.value
      val photoUrls = mutableListOf<String>()
      for (uri in state.attachmentUris) {
        val photoSaveResult =
            withTimeout(5000L) {
              fileRepository.uploadFile(
                  StoragePaths.taskAttachmentPath(
                      state.projectId, taskId, "${uri.lastPathSegment}.jpg"),
                  uri)
            }
        val photoUrl =
            photoSaveResult.getOrElse { exception ->
              return Result.failure(exception)
            }
        photoUrls.add(photoUrl)

        // Delete local file after successful upload
        deletePhoto(context, uri)
      }
      Result.success(photoUrls)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Resets the save state to allow creating another task */
  fun resetSaveState() {
    _uiState.value = _uiState.value.copy(isSaving = false, taskSaved = false)
  }

  /** Edits a Task */
  fun editTask(context: Context) {
    val state = _uiState.value
    val dateStr = state.dueDate

    if (!dateRegex.matches(dateStr)) {
      setErrorMsg("Invalid format, date must be DD/MM/YYYY.")
      return
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    dateFormat.isLenient = false
    val date =
        try {
          dateFormat.parse(dateStr)
        } catch (e: Exception) {
          setErrorMsg("Invalid date value: $dateStr")
          return
        }

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, exception ->
      Log.e("CreateTaskViewModel", exception.message ?: "Unknown error")

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
              dueDate = Timestamp(date),
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

  fun setProjectId(id: String) {
    _uiState.value = _uiState.value.copy(projectId = id)
  }

  fun deletePhoto(context: Context, photoUri: Uri): Boolean {
    return try {
      when (photoUri.scheme) {
        "content",
        "file" -> {
          val rowsDeleted = context.contentResolver.delete(photoUri, null, null)
          rowsDeleted > 0
        }
        "http",
        "https" -> {
          val result = runBlocking { fileRepository.deleteFile(photoUri.toString()) }
          result.isSuccess
        }
        else -> {
          Log.w("CreateTaskViewModel", "Unsupported URI scheme: ${photoUri.scheme}")
          false
        }
      }
    } catch (e: SecurityException) {
      Log.w("CreateTaskViewModel", "Failed to delete photo: ${e.message}")
      false
    } catch (e: Exception) {
      Log.w("CreateTaskViewModel", "Unexpected error: ${e.message}")
      false
    }
  }

  fun loadTask(projectId: String, taskId: String) {
    viewModelScope.launch(dispatcher) {
      taskRepository
          .getTaskById(projectId, taskId)
          .catch { exception -> setErrorMsg("Failed to load Task: ${exception.message}") }
          .collect { task ->
            if (task != null) {
              _uiState.value =
                  _uiState.value.copy(
                      title = task.title,
                      description = task.description,
                      dueDate =
                          task.dueDate?.let { date ->
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(date.toDate())
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

  fun deleteTask(projectId: String, taskId: String) {
    for (url in _uiState.value.attachmentUrls) {
      viewModelScope.launch(dispatcher) {
        fileRepository.deleteFile(url).onFailure { exception ->
          Log.w("EditTaskViewModel", "Failed to delete attachment: ${exception.message}")
        }
      }
    }

    viewModelScope.launch(dispatcher) {
      taskRepository.deleteTask(projectId, taskId).onFailure { exception ->
        setErrorMsg("Failed to delete Task: ${exception.message}")
      }
    }
  }

  fun setStatus(status: TaskStatus) {
    _uiState.value = _uiState.value.copy(status = status)
  }
}

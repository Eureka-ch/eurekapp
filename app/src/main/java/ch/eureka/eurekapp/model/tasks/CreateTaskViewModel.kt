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
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class CreateTaskViewModel(
    private val taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val fileRepository: FileStorageRepository =
        FirestoreRepositoriesProvider.fileRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
  // CreateTask state
  private val _uiState = MutableStateFlow(CreateTaskState())
  val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

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

  /** Adds a Task */
  fun addTask(context: Context) {
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
      val taskId = IdGenerator.generateTaskId()
      val photoUrlsResult = saveFilesOnRepository(taskId, context)
      val photoUrls =
          photoUrlsResult.getOrElse { exception ->
            handler.handleException(coroutineContext, exception)
            return@launch
          }

      val task =
          Task(
              taskID = taskId,
              projectId = state.projectId,
              title = state.title,
              description = state.description,
              assignedUserIds = listOf(currentUser),
              dueDate = Timestamp(date),
              attachmentUrls = photoUrls,
              createdBy = currentUser)

      taskRepository.createTask(task).onFailure {
        setErrorMsg("Failed to add Task.")
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
                })
  }

  fun setProjectId(id: String) {
    _uiState.value = _uiState.value.copy(projectId = id)
  }

  fun deletePhoto(context: Context, photoUri: Uri): Boolean {
    return try {
      val rowsDeleted = context.contentResolver.delete(photoUri, null, null)
      rowsDeleted > 0
    } catch (e: SecurityException) {
      Log.w("CreateTaskViewModel", "Failed to delete photo: ${e.message}")
      false
    }
  }
}

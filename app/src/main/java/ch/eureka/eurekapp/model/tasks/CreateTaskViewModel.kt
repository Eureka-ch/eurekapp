package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
*/

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class CreateTaskViewModel(
    private val taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val fileRepository: FileStorageRepository =
        FirestoreRepositoriesProvider.fileRepository,
    private val attachmentUris: List<Uri> = emptyList()
) : ViewModel() {
  // CreateTask state
  private val _uiState = MutableStateFlow(CreateTaskState(attachmentUris = attachmentUris))
  val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  private suspend fun saveFilesOnRepository(context: Context): List<String> {
    val state = _uiState.value
    val photoUrls = mutableListOf<String>()
    for (uri in state.attachmentUris) {
      val photoSaveResult =
          fileRepository.uploadFile(
              StoragePaths.taskAttachmentPath(
                  state.projectId, state.taskId, uri.lastPathSegment + ".jpg"),
              uri)

      val photoUrl =
          when {
            photoSaveResult.isSuccess -> photoSaveResult.getOrThrow()
            else -> {
              Toast.makeText(context, "Failed to save the photo", Toast.LENGTH_SHORT).show()
              continue
            }
          }
      photoUrls.add(photoUrl)
    }
    return photoUrls
  }

  /** Adds a Task */
  fun addTask(context: Context) {
    val state = _uiState.value
    val dateStr = state.dueDate
    val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")

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

    val currentUser = Firebase.auth.currentUser?.uid ?: throw Exception("User not logged in.")

    viewModelScope.launch {
      val photoUrls = saveFilesOnRepository(context)
      addTaskToRepository(
          Task(
              taskID = state.taskId,
              projectId = state.projectId,
              title = state.title,
              description = state.description,
              assignedUserIds = listOf(currentUser),
              dueDate = Timestamp(date),
              attachmentUrls = photoUrls,
              createdBy = currentUser))
      clearErrorMsg()
      for (uri in state.attachmentUris) {
        deletePhoto(context, uri)
      }
    }
  }

  private fun addTaskToRepository(task: Task) {
    viewModelScope.launch {
      taskRepository.createTask(task).onFailure { setErrorMsg("Failed to add Task.") }
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
    val rowsDeleted = context.contentResolver.delete(photoUri, null, null)
    return rowsDeleted > 0
  }
}

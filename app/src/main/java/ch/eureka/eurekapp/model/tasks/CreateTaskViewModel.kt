package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class CreateTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    ReadWriteTaskViewModel<CreateTaskState>(
        taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val _uiState = MutableStateFlow(CreateTaskState())
  override val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

  /** Adds a Task */
  fun addTask(context: Context) {
    val state = uiState.value

    val timestampResult = parseDateString(state.dueDate)
    if (timestampResult.isFailure) {
      setErrorMsg(timestampResult.exceptionOrNull()?.message ?: "Invalid date")
      return
    }
    val timestamp = timestampResult.getOrThrow()

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, exception ->
      Log.e("CreateTaskViewModel", exception.message ?: "Unknown error")

      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT).show()
      }
      updateState { copy(isSaving = false) }
    }

    updateState { copy(isSaving = true) }

    val taskId = IdGenerator.generateTaskId()
    val projectIdToUse = state.projectId

    saveFilesAsync(taskId, context, projectIdToUse, state.attachmentUris) { photoUrlsResult ->
      if (photoUrlsResult.isFailure) {
        val exception = photoUrlsResult.exceptionOrNull()
        Log.e("CreateTaskViewModel", exception?.message ?: "Unknown error")
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT)
              .show()
        }
        updateState { copy(isSaving = false) }
        return@saveFilesAsync
      }

      val photoUrls = photoUrlsResult.getOrThrow()

      viewModelScope.launch(dispatcher + handler) {
        val task =
            Task(
                taskID = taskId,
                projectId = projectIdToUse,
                title = state.title,
                description = state.description,
                assignedUserIds = listOf(currentUser),
                dueDate = timestamp,
                attachmentUrls = photoUrls,
                createdBy = currentUser)

        taskRepository.createTask(task).onFailure {
          setErrorMsg("Failed to add Task.")
          updateState { copy(isSaving = false) }
          return@launch
        }

        clearErrorMsg()
        updateState { copy(isSaving = false, taskSaved = true) }
      }
    }
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

  // State update implementations
  override fun CreateTaskState.copyWithErrorMsg(errorMsg: String?) = copy(errorMsg = errorMsg)

  override fun CreateTaskState.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean) =
      copy(isSaving = isSaving, taskSaved = taskSaved)

  override fun CreateTaskState.copyWithTitle(title: String) = copy(title = title)

  override fun CreateTaskState.copyWithDescription(description: String) =
      copy(description = description)

  override fun CreateTaskState.copyWithDueDate(dueDate: String) = copy(dueDate = dueDate)

  override fun CreateTaskState.copyWithAttachmentUris(uris: List<Uri>) = copy(attachmentUris = uris)

  override fun CreateTaskState.copyWithProjectId(projectId: String) = copy(projectId = projectId)

  override fun updateState(update: CreateTaskState.() -> CreateTaskState) {
    _uiState.value = _uiState.value.update()
  }
}

package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/*
Co-Authored-By: Claude <noreply@anthropic.com>
Portions of this code were generated with the help of Grok.
*/

/** Interface for common task state properties */
interface TaskStateCommon {
  val title: String
  val description: String
  val dueDate: String
  val projectId: String
  val availableProjects: List<Project>
  val attachmentUris: List<Uri>
  val isSaving: Boolean
  val taskSaved: Boolean
  val errorMsg: String?
}

/** Base ViewModel for task creation and editing with shared functionality */
abstract class BaseTaskViewModel<T : TaskStateCommon>(
    protected val taskRepository: TaskRepository,
    protected val fileRepository: FileStorageRepository,
    protected val getCurrentUserId: () -> String?,
    protected val dispatcher: CoroutineDispatcher
) : ViewModel() {

  abstract val uiState: StateFlow<T>

  protected abstract fun getState(): T

  val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
  private val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }

  val inputValid: StateFlow<Boolean> by lazy {
    uiState
        .map { state -> isValidInput(state.title, state.description, state.dueDate) }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = false)
  }

  /** Validates if the input fields are valid */
  fun isValidInput(title: String, description: String, dueDate: String): Boolean {
    return title.isNotBlank() && description.isNotBlank() && dateRegex.matches(dueDate)
  }

  /** Parses a date string to Timestamp */
  fun parseDateString(dateStr: String): Result<Timestamp> {
    if (!dateRegex.matches(dateStr)) {
      return Result.failure(IllegalArgumentException("Invalid format, date must be DD/MM/YYYY."))
    }

    return try {
      val date =
          dateFormat.parse(dateStr)
              ?: return Result.failure(IllegalArgumentException("Invalid date value: $dateStr"))
      Result.success(Timestamp(date))
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Invalid date value: $dateStr"))
    }
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    updateState { copyWithErrorMsg(null) }
  }

  /** Sets an error message in the UI state. */
  protected fun setErrorMsg(errorMsg: String) {
    updateState { copyWithErrorMsg(errorMsg) }
  }

  /** Uploads attachments to the repository */
  private suspend fun saveFilesOnRepository(
      taskId: String,
      context: Context,
      projectId: String,
      attachmentUris: List<Uri>
  ): Result<List<String>> {
    return try {
      val photoUrls =
          attachmentUris.map { uri ->
            uploadSingleFile(taskId, context, projectId, uri).getOrElse { exception ->
              return Result.failure(exception)
            }
          }
      Result.success(photoUrls)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Uploads a single file to the repository */
  private suspend fun uploadSingleFile(
      taskId: String,
      context: Context,
      projectId: String,
      uri: Uri
  ): Result<String> {
    return try {
      val path = StoragePaths.taskAttachmentPath(projectId, taskId, "${uri.lastPathSegment}.jpg")
      val photoUrl = withTimeout(5000L) { fileRepository.uploadFile(path, uri) }.getOrThrow()

      deletePhotoSuspend(context, uri)
      Result.success(photoUrl)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Resets the save state to allow creating another task */
  fun resetSaveState() {
    updateState { copyWithSaveState(isSaving = false, taskSaved = false) }
  }

  // Functions to update the UI state.

  fun setTitle(title: String) {
    updateState { copyWithTitle(title) }
  }

  fun setDescription(description: String) {
    updateState { copyWithDescription(description) }
  }

  fun setDueDate(dueDate: String) {
    updateState { copyWithDueDate(dueDate) }
  }

  fun addAttachment(uri: Uri) {
    val currentUris = getState().attachmentUris
    if (!currentUris.contains(uri)) {
      updateState { copyWithAttachmentUris(currentUris + uri) }
    }
  }

  open fun removeAttachment(index: Int) {
    val currentUris = getState().attachmentUris
    if (index in currentUris.indices) {
      updateState { copyWithAttachmentUris(currentUris.toMutableList().apply { removeAt(index) }) }
    }
  }

  fun setProjectId(id: String) {
    updateState { copyWithProjectId(id) }
  }

  /** Deletes a photo from storage */
  private suspend fun deletePhotoSuspend(context: Context, photoUri: Uri): Boolean {
    return try {
      when (photoUri.scheme) {
        "content",
        "file" -> {
          val rowsDeleted = context.contentResolver.delete(photoUri, null, null)
          rowsDeleted > 0
        }
        "http",
        "https" -> {
          val result = fileRepository.deleteFile(photoUri.toString())
          result.isSuccess
        }
        else -> {
          Log.w("BaseTaskViewModel", "Unsupported URI scheme: ${photoUri.scheme}")
          false
        }
      }
    } catch (e: Exception) {
      Log.w("BaseTaskViewModel", "Failed to delete photo: ${e.message}")
      false
    }
  }

  /** Deletes photos when composable is disposed */
  fun deletePhotosOnDispose(context: Context, photoUris: List<Uri>) {
    viewModelScope.launch(dispatcher) {
      photoUris.forEach { uri -> deletePhotoSuspend(context, uri) }
    }
  }

  /** Helper function for subclasses to save files - wraps the suspend function */
  protected fun saveFilesAsync(
      taskId: String,
      context: Context,
      projectId: String,
      attachmentUris: List<Uri>,
      onResult: (Result<List<String>>) -> Unit
  ) {
    viewModelScope.launch(dispatcher) {
      val result = saveFilesOnRepository(taskId, context, projectId, attachmentUris)
      onResult(result)
    }
  }

  /** Helper function for subclasses to delete photos - wraps the suspend function */
  protected fun deletePhotoAsync(context: Context, photoUri: Uri, onResult: (Boolean) -> Unit) {
    viewModelScope.launch(dispatcher) {
      val result = deletePhotoSuspend(context, photoUri)
      onResult(result)
    }
  }

  // Abstract methods for state updates
  protected abstract fun updateState(update: T.() -> T)

  protected abstract fun T.copyWithErrorMsg(errorMsg: String?): T

  protected abstract fun T.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean): T

  protected abstract fun T.copyWithTitle(title: String): T

  protected abstract fun T.copyWithDescription(description: String): T

  protected abstract fun T.copyWithDueDate(dueDate: String): T

  protected abstract fun T.copyWithAttachmentUris(uris: List<Uri>): T

  protected abstract fun T.copyWithProjectId(projectId: String): T
}

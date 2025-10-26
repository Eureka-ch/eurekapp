package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
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

  protected abstract val _uiState: MutableStateFlow<T>
  abstract val uiState: StateFlow<T>

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
  protected suspend fun saveFilesOnRepository(
      taskId: String,
      context: Context,
      projectId: String,
      attachmentUris: List<Uri>
  ): Result<List<String>> {
    return try {
      val photoUrls = mutableListOf<String>()
      for (uri in attachmentUris) {
        val photoSaveResult =
            withTimeout(5000L) {
              fileRepository.uploadFile(
                  StoragePaths.taskAttachmentPath(projectId, taskId, "${uri.lastPathSegment}.jpg"),
                  uri)
            }
        val photoUrl =
            photoSaveResult.getOrElse { exception ->
              return Result.failure(exception)
            }
        photoUrls.add(photoUrl)

        // Delete local file after successful upload
        deletePhotoSuspend(context, uri)
      }
      Result.success(photoUrls)
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
    val currentUris = _uiState.value.attachmentUris
    if (!currentUris.contains(uri)) {
      updateState { copyWithAttachmentUris(currentUris + uri) }
    }
  }

  open fun removeAttachment(index: Int) {
    val currentUris = _uiState.value.attachmentUris
    if (index in currentUris.indices) {
      updateState { copyWithAttachmentUris(currentUris.toMutableList().apply { removeAt(index) }) }
    }
  }

  fun setProjectId(id: String) {
    updateState { copyWithProjectId(id) }
  }

  /** Deletes a photo from storage */
  protected suspend fun deletePhotoSuspend(context: Context, photoUri: Uri): Boolean {
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

package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Portions of this code were generated with the help of Grok.

/** Lightweight base ViewModel for read-only task viewing */
abstract class ReadTaskViewModel<T>(
    protected val taskRepository: TaskRepository,
    protected val dispatcher: CoroutineDispatcher
) : ViewModel() {

  abstract val uiState: StateFlow<T>

  val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    updateState { copyWithErrorMsg(null) }
  }

  /** Sets an error message in the UI state. */
  protected fun setErrorMsg(errorMsg: String) {
    updateState { copyWithErrorMsg(errorMsg) }
  }

  /** Deletes photos when composable is disposed (simple, local-only attempt). */
  fun deletePhotosOnDispose(context: Context, photoUris: List<Uri>) {
    viewModelScope.launch(dispatcher) {
      photoUris.forEach { uri ->
        try {
          when (uri.scheme) {
            "content",
            "file" -> {
              try {
                context.contentResolver.delete(uri, null, null)
              } catch (_: Exception) {}
            }
          }
        } catch (_: Exception) {}
      }
    }
  }

  protected abstract fun updateState(update: T.() -> T)

  protected abstract fun T.copyWithErrorMsg(errorMsg: String?): T
}

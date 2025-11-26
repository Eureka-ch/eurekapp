// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.TaskRepository
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Interface for base task state properties shared by read-only views */
interface TaskStateRead {
  val errorMsg: String?
  val title: String
  val description: String
  val dueDate: String
  val projectId: String
  val attachmentUris: List<Uri>
}

/** Lightweight base ViewModel for read-only task viewing */
abstract class ReadTaskViewModel<T : TaskStateRead>(
    protected val taskRepository: TaskRepository,
    protected val dispatcher: CoroutineDispatcher
) : ViewModel() {

  abstract val uiState: StateFlow<T>

  protected val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }

  val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")

  /** Deletes photos when composable is disposed (simple, local-only attempt). */
  fun deletePhotosOnDispose(context: Context, photoUris: List<Uri>) {
    viewModelScope.launch(dispatcher) {
      photoUris.forEach { uri ->
        try {
          when (uri.scheme) {
            "content",
            "file" -> {
              context.contentResolver.delete(uri, null, null)
            }
          }
        } catch (_: Exception) {
          // Ignore errors during cleanup
          // This is a best-effort cleanup
          // File may not exist or may be inaccessible
        }
      }
    }
  }
}

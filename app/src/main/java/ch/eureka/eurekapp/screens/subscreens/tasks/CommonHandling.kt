package ch.eureka.eurekapp.screens.subscreens.tasks

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.withTimeout

// Portions of this code were generated with the help of Grok.

object CommonTaskTestTags {
  const val TITLE = "title"
  const val DESCRIPTION = "description"
  const val DUE_DATE = "due_date"
  const val ADD_PHOTO = "add_photo"
  const val SAVE_TASK = "save_task"
  const val PHOTO = "photo"
  const val DELETE_PHOTO = "delete_photo"
  const val ERROR_MSG = "error_msg"
}

@Composable
fun TaskTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Title") },
      placeholder = { Text("Name the task") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.TITLE))
  if (value.isBlank() && hasTouched) {
    Text(
        text = "Title cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun TaskDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Description") },
      placeholder = { Text("Describe the task") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DESCRIPTION))
  if (value.isBlank() && hasTouched) {
    Text(
        text = "Description cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun TaskDueDateField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    dateRegex: Regex,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Due date") },
      placeholder = { Text("01/01/1970") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DUE_DATE))
  if (value.isNotBlank() && !dateRegex.matches(value) && hasTouched) {
    Text(
        text = "Invalid format (must be dd/MM/yyyy)",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun AttachmentsList(
    attachments: List<Any>,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
  attachments.forEachIndexed { index, file ->
    Row(modifier = modifier) {
      Text("Photo ${index + 1}")
      IconButton(
          onClick = { onDelete(index) },
          modifier = Modifier.testTag(CommonTaskTestTags.DELETE_PHOTO)) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete file")
          }
      PhotoViewer(file, modifier = Modifier.size(100.dp).testTag(CommonTaskTestTags.PHOTO))
    }
  }
}

object TaskCommonConstants {
  val DATE_REGEX = Regex("""^\d{2}/\d{2}/\d{4}$""")
  val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
}

object TaskValidation {
  fun isValidInput(title: String, description: String, dueDate: String): Boolean {
    return title.isNotBlank() &&
        description.isNotBlank() &&
        TaskCommonConstants.DATE_REGEX.matches(dueDate)
  }

  fun parseDateString(dateStr: String): Result<Timestamp> {
    if (!TaskCommonConstants.DATE_REGEX.matches(dateStr)) {
      return Result.failure(IllegalArgumentException("Invalid format, date must be DD/MM/YYYY."))
    }

    return try {
      val date =
          TaskCommonConstants.DATE_FORMAT.parse(dateStr)
              ?: return Result.failure(IllegalArgumentException("Invalid date value: $dateStr"))
      Result.success(Timestamp(date))
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Invalid date value: $dateStr"))
    }
  }
}

object TaskFileOperations {
  suspend fun uploadAttachments(
      projectId: String,
      taskId: String,
      attachmentUris: List<Uri>,
      fileRepository: FileStorageRepository,
      onDelete: suspend (Uri) -> Unit
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
        onDelete(uri)
      }
      Result.success(photoUrls)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deletePhoto(
      context: Context,
      photoUri: Uri,
      fileRepository: FileStorageRepository
  ): Boolean {
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
          Log.w("TaskFileOperations", "Unsupported URI scheme: ${photoUri.scheme}")
          false
        }
      }
    } catch (e: Exception) {
      Log.w("TaskFileOperations", "Failed to delete photo: ${e.message}")
      false
    }
  }
}

package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.utils.Formatters
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/*
Co-Authored-By: Claude <noreply@anthropic.com>
Portions of this code were generated with the help of Grok.
Note: This file was partially written by GPT-5
Codex Co-author : GPT-5
*/

/** Interface for common task state properties */
interface TaskStateReadWrite : TaskStateRead {
  val isSaving: Boolean
  val taskSaved: Boolean
  val availableProjects: List<Project>
  val availableUsers: List<ch.eureka.eurekapp.model.data.user.User>
  val selectedAssignedUserIds: List<String>
  val dependingOnTasks: List<String>
}

/** Base ViewModel for task creation and editing with shared functionality */
abstract class ReadWriteTaskViewModel<T : TaskStateReadWrite>(
    taskRepository: TaskRepository,
    protected val fileRepository: FileStorageRepository,
    protected val projectRepository: ProjectRepository,
    protected val userRepository: UserRepository,
    protected val getCurrentUserId: () -> String?,
    dispatcher: CoroutineDispatcher
) : ReadTaskViewModel<T>(taskRepository, dispatcher) {

  companion object {
    const val MAX_FILE_SIZE_MB = 100L
    const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024L * 1024L
  }

  abstract override val uiState: StateFlow<T>

  val inputValid: StateFlow<Boolean> by lazy {
    uiState
        .map { state -> isValidInput(state.title, state.description, state.dueDate) }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = false)
  }

  protected val availableTasksMutable = MutableStateFlow<List<Task>>(emptyList())
  val availableTasks: StateFlow<List<Task>> = availableTasksMutable.asStateFlow()

  protected val cycleErrorMutable = MutableStateFlow<String?>(null)
  val cycleError: StateFlow<String?> = cycleErrorMutable.asStateFlow()

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    updateState { copyWithErrorMsg(null) }
  }

  /** Sets an error message in the UI state. */
  protected fun setErrorMsg(errorMsg: String) {
    updateState { copyWithErrorMsg(errorMsg) }
  }

  /** Validates if the input fields are valid */
  fun isValidInput(title: String, description: String, dueDate: String): Boolean {
    return title.isNotBlank() && description.isNotBlank() && dateRegex.matches(dueDate)
  }

  /** Loads tasks that are available as dependencies for the selected project. */
  open fun loadAvailableTasks(projectId: String) {
    if (projectId.isEmpty()) {
      availableTasksMutable.value = emptyList()
      return
    }
    viewModelScope.launch(dispatcher) {
      taskRepository.getTasksInProject(projectId).collect { tasks ->
        availableTasksMutable.value = tasks
      }
    }
  }

  /** Helper for subclasses to update the cycle dependency error message. */
  protected fun setCycleError(message: String?) {
    cycleErrorMutable.value = message
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

  /** Parses a reminder time string to Timestamp */
  protected fun parseReminderTime(dueDateStr: String, reminderTimeStr: String): Result<Timestamp> {
    if (!dateRegex.matches(dueDateStr)) {
      return Result.failure(IllegalArgumentException("Invalid due date format"))
    }

    if (!Formatters.timeRegex.matches(reminderTimeStr)) {
      return Result.failure(
          IllegalArgumentException("Invalid reminder time format (must be HH:mm)"))
    }

    return try {
      val dueDate =
          dateFormat.parse(dueDateStr)
              ?: return Result.failure(IllegalArgumentException("Invalid due date value"))

      val timeParts = reminderTimeStr.split(":")
      val hours = timeParts[0].toInt()
      val minutes = timeParts[1].toInt()

      val calendar =
          java.util.Calendar.getInstance().apply {
            time = dueDate
            set(java.util.Calendar.HOUR_OF_DAY, hours)
            set(java.util.Calendar.MINUTE, minutes)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
          }

      Result.success(Timestamp(calendar.time))
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Invalid reminder time: ${e.message}"))
    }
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
      val fileName =
          if (uri.scheme == "content") {
            // For content:// URIs, get the display name from the content resolver
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
              if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow("_display_name"))
                    ?: "attachment_${System.currentTimeMillis()}"
              } else {
                "attachment_${System.currentTimeMillis()}"
              }
            } ?: "attachment_${System.currentTimeMillis()}"
          } else {
            // For file:// URIs, use lastPathSegment
            uri.lastPathSegment ?: "attachment_${System.currentTimeMillis()}"
          }
      val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
      val extension =
          when {
            mimeType.startsWith("image/") -> ".jpg"
            mimeType.startsWith("video/") -> ".mp4"
            mimeType == "application/pdf" -> ".pdf"
            mimeType.startsWith("text/") -> ".txt"
            else -> ""
          }
      val finalFileName =
          if (extension.isNotEmpty() && !fileName.endsWith(extension)) {
            fileName + extension
          } else {
            fileName
          }
      val path = StoragePaths.taskAttachmentPath(projectId, taskId, finalFileName)

      // Open file descriptor once for size check and upload
      val fileDescriptor =
          context.contentResolver.openFileDescriptor(uri, "r")
              ?: return Result.failure(Exception("Cannot access file"))

      val fileSize = fileDescriptor.statSize
      if (fileSize > MAX_FILE_SIZE_BYTES) {
        fileDescriptor.close()
        return Result.failure(Exception("File too large (max $MAX_FILE_SIZE_MB MB)"))
      }

      // Upload using file descriptor to avoid reopening
      val fileUrl =
          withTimeout(60000L) { fileRepository.uploadFile(path, fileDescriptor) }.getOrThrow()
      fileDescriptor.close()

      // Store metadata as "url|name|mime"
      val metadata = "$fileUrl|$finalFileName|$mimeType"

      // Only delete temporary files (from camera), not files picked from storage
      // Files from file picker have content:// URI and should not be deleted
      if (uri.scheme == "file") {
        deletePhotoSuspend(context, uri)
      }

      Result.success(metadata)
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
    if (!uiState.value.attachmentUris.contains(uri)) {
      updateState { copyWithAttachmentUris(uiState.value.attachmentUris + uri) }
    }
  }

  open fun removeAttachment(index: Int) {
    if (index in uiState.value.attachmentUris.indices) {
      updateState {
        copyWithAttachmentUris(
            uiState.value.attachmentUris.toMutableList().apply { removeAt(index) })
      }
    }
  }

  fun setProjectId(id: String) {
    updateState { copyWithProjectId(id) }
  }

  /**
   * Replaces the current dependency list with the provided task identifiers.
   *
   * Subclasses may perform additional validation before calling this helper.
   */
  fun setDependencies(dependencyTaskIds: List<String>) {
    updateState { copyWithDependencies(dependencyTaskIds) }
  }

  /**
   * Adds a dependency to the state if it is not already present.
   *
   * Subclasses can override to inject extra validation (e.g. cycle detection) before persisting.
   */
  open fun addDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    if (!currentDependencies.contains(taskId)) {
      updateState { copyWithDependencies(currentDependencies + taskId) }
    }
  }

  /**
   * Removes the provided dependency identifier from the state if present.
   *
   * Subclasses should ensure any accompanying error state is cleared when invoking this method.
   */
  open fun removeDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    updateState { copyWithDependencies(currentDependencies.filter { it != taskId }) }
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
          false
        }
      }
    } catch (_: Exception) {
      false
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

  /** Sets the reminder time for the task */
  fun setReminderTime(reminderTime: String) {
    updateState { copyWithReminderTime(reminderTime) }
  }

  /** Loads available users from the selected project */
  fun loadProjectMembers(projectId: String) {
    if (projectId.isBlank()) {
      updateState { copyWithAvailableUsers(emptyList()) }
      return
    }

    viewModelScope.launch(dispatcher) {
      projectRepository.getMembers(projectId).collect { members ->
        if (members.isEmpty()) {
          updateState { copyWithAvailableUsers(emptyList()) }
        } else {
          // Collect all user flows
          val userFlows = members.map { member -> userRepository.getUserById(member.userId) }
          kotlinx.coroutines.flow
              .combine(userFlows) { users -> users.toList().filterNotNull() }
              .collect { users -> updateState { copyWithAvailableUsers(users) } }
        }
      }
    }
  }

  /** Sets the assigned user IDs for the task */
  fun setAssignedUsers(userIds: List<String>) {
    updateState { copyWithSelectedAssignedUserIds(userIds) }
  }

  /** Toggles a user in the assigned users list */
  fun toggleUserAssignment(userId: String) {
    val currentIds = uiState.value.selectedAssignedUserIds
    val newIds =
        if (currentIds.contains(userId)) {
          currentIds - userId
        } else {
          currentIds + userId
        }
    updateState { copyWithSelectedAssignedUserIds(newIds) }
  }

  // Abstract methods for state updates
  /** Updates the UI state with the given update function */
  protected abstract fun updateState(update: T.() -> T)

  /** Abstract method to copy state with new error message */
  protected abstract fun T.copyWithErrorMsg(errorMsg: String?): T

  protected abstract fun T.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean): T

  protected abstract fun T.copyWithTitle(title: String): T

  protected abstract fun T.copyWithDescription(description: String): T

  protected abstract fun T.copyWithDueDate(dueDate: String): T

  protected abstract fun T.copyWithAttachmentUris(uris: List<Uri>): T

  protected abstract fun T.copyWithProjectId(projectId: String): T

  protected abstract fun T.copyWithReminderTime(reminderTime: String): T

  protected abstract fun T.copyWithAvailableUsers(
      users: List<ch.eureka.eurekapp.model.data.user.User>
  ): T

  protected abstract fun T.copyWithSelectedAssignedUserIds(userIds: List<String>): T

  protected abstract fun T.copyWithDependencies(dependencies: List<String>): T
}

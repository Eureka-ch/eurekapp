package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
Note: This file was partially written by GPT-5 Codex Co-author : GPT-5
*/

/** ViewModel for the EditTask screen. This ViewModel manages the state of input fields. */
class EditTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    private val projectRepository: ProjectRepository = FirestoreRepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    ReadWriteTaskViewModel<EditTaskState>(
        taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

  private val _uiState = MutableStateFlow(EditTaskState())
  override val uiState: StateFlow<EditTaskState> = _uiState.asStateFlow()

  /** Resets the delete state after navigation or handling */
  fun resetDeleteState() {
    updateState { copy(taskDeleted = false) }
  }

  /** Edits a Task */
  fun editTask(context: Context) {
    val state = uiState.value

    val timestampResult = parseDateString(state.dueDate)
    if (timestampResult.isFailure) {
      setErrorMsg(timestampResult.exceptionOrNull()?.message ?: "Invalid date")
      return
    }
    val timestamp = timestampResult.getOrThrow()

    val reminderTimestamp =
        if (state.reminderTime.isNotBlank() && state.dueDate.isNotBlank()) {
          parseReminderTime(state.dueDate, state.reminderTime).getOrNull()
        } else null

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, _ ->
      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT).show()
      }
      updateState { copy(isSaving = false) }
    }

    updateState { copy(isSaving = true) }

    saveFilesAsync(state.taskId, context, state.projectId, state.attachmentUris) { photoUrlsResult
      ->
      if (photoUrlsResult.isFailure) {
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT)
              .show()
        }
        updateState { copy(isSaving = false) }
        return@saveFilesAsync
      }

      val newPhotoUrls = photoUrlsResult.getOrThrow()

      viewModelScope.launch(dispatcher + handler) {
        val task =
            Task(
                taskID = state.taskId,
                projectId = state.projectId,
                title = state.title,
                description = state.description,
                assignedUserIds = state.selectedAssignedUserIds.ifEmpty { listOf(currentUser) },
                dueDate = timestamp,
                reminderTime = reminderTimestamp,
                attachmentUrls = state.attachmentUrls + newPhotoUrls,
                createdBy = currentUser,
                status = state.status)

        taskRepository.updateTask(task).onFailure { _ ->
          setErrorMsg("Failed to update Task.")
          updateState { copy(isSaving = false) }
          return@launch
        }

        clearErrorMsg()
        updateState { copy(isSaving = false, taskSaved = true) }
      }
    }
  }

  override fun removeAttachment(index: Int) {
    val currentUris = uiState.value.attachmentUris
    val currentUrls = uiState.value.attachmentUrls

    if (index in currentUris.indices) {
      updateState { copy(attachmentUris = currentUris.toMutableList().apply { removeAt(index) }) }
    } else if (index - currentUris.size in currentUrls.indices) {
      val urlIndex = index - currentUris.size
      val urlToDelete = currentUrls[urlIndex]
      updateState {
        copy(
            attachmentUrls = currentUrls.toMutableList().apply { removeAt(urlIndex) },
            deletedAttachmentUrls = deletedAttachmentUrls + urlToDelete)
      }
    }
  }

  fun removeAttachmentAndDelete(context: Context, index: Int) {
    val allAttachments = _uiState.value.attachmentUrls + _uiState.value.attachmentUris
    if (index !in allAttachments.indices) return

    val file = allAttachments[index]
    val uri =
        when (file) {
          is String -> file.toUri()
          else -> file as Uri
        }

    deletePhotoAsync(context, uri) { success ->
      if (success) {
        removeAttachment(index)
      }
    }
  }

  fun loadTask(projectId: String, taskId: String) {
    // Don't load task if it's being deleted or already deleted
    if (_uiState.value.isDeleting || _uiState.value.taskDeleted) {
      return
    }

    viewModelScope.launch(dispatcher) {
      taskRepository
          .getTaskById(projectId, taskId)
          .catch { exception ->
            // Only show error if we're not in the process of deleting
            if (!_uiState.value.isDeleting && !_uiState.value.taskDeleted) {
              setErrorMsg("Failed to load Task: ${exception.message}")
            }
          }
          .collect { task ->
            if (!_uiState.value.isDeleting && !_uiState.value.taskDeleted) {
              if (task != null) {
                val deletedUrls = _uiState.value.deletedAttachmentUrls
                val filteredAttachments = task.attachmentUrls.filterNot { it in deletedUrls }
                updateState {
                  copy(
                      title = task.title,
                      description = task.description,
                      dueDate =
                          task.dueDate?.let { date -> dateFormat.format(date.toDate()) } ?: "",
                      reminderTime =
                          task.reminderTime?.let { time -> timeFormat.format(time.toDate()) } ?: "",
                      templateId = task.templateId,
                      projectId = task.projectId,
                      taskId = task.taskID,
                      assignedUserIds = task.assignedUserIds,
                      selectedAssignedUserIds = task.assignedUserIds,
                      attachmentUrls = filteredAttachments,
                      status = task.status,
                      customData = task.customData,
                  )
                }
                // Load project members after task is loaded
                loadProjectMembers(task.projectId)
              } else {
                setErrorMsg("Task not found.")
              }
            }
          }
    }
  }

  fun deleteTask(projectId: String, taskId: String) {
    updateState { copy(isDeleting = true) }

    for (url in _uiState.value.attachmentUrls) {
      viewModelScope.launch(dispatcher) {
        fileRepository.deleteFile(url).onFailure { exception ->
          setErrorMsg("Failed to delete attachment: ${exception.message}")
        }
      }
    }

    viewModelScope.launch(dispatcher) {
      taskRepository
          .deleteTask(projectId, taskId)
          .onFailure { exception ->
            setErrorMsg("Failed to delete Task: ${exception.message}")
            updateState { copy(isDeleting = false) }
            return@launch
          }
          .onSuccess { _ -> updateState { copy(isDeleting = false, taskDeleted = true) } }
    }
  }

  fun setStatus(status: TaskStatus) {
    updateState { copy(status = status) }
  }

  // State update implementations
  override fun EditTaskState.copyWithErrorMsg(errorMsg: String?) = copy(errorMsg = errorMsg)

  override fun EditTaskState.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean) =
      copy(isSaving = isSaving, taskSaved = taskSaved)

  override fun EditTaskState.copyWithTitle(title: String) = copy(title = title)

  override fun EditTaskState.copyWithDescription(description: String) =
      copy(description = description)

  override fun EditTaskState.copyWithDueDate(dueDate: String) = copy(dueDate = dueDate)

  override fun EditTaskState.copyWithAttachmentUris(uris: List<Uri>) = copy(attachmentUris = uris)

  override fun EditTaskState.copyWithProjectId(projectId: String) = copy(projectId = projectId)

  override fun updateState(update: EditTaskState.() -> EditTaskState) {
    _uiState.value = _uiState.value.update()
  }

  fun setReminderTime(reminderTime: String) {
    updateState { copy(reminderTime = reminderTime) }
  }

  /** Loads available users from the selected project */
  fun loadProjectMembers(projectId: String) {
    if (projectId.isBlank()) {
      updateState { copy(availableUsers = emptyList()) }
      return
    }

    viewModelScope.launch(dispatcher) {
      projectRepository.getMembers(projectId).collect { members ->
        val users = mutableListOf<User>()
        members.forEach { member ->
          userRepository.getUserById(member.userId).collect { user ->
            user?.let { users.add(it) }
          }
        }
        updateState { copy(availableUsers = users) }
      }
    }
  }

  /** Sets the assigned user IDs for the task */
  fun setAssignedUsers(userIds: List<String>) {
    updateState { copy(selectedAssignedUserIds = userIds) }
  }

  /** Toggles a user in the assigned users list */
  fun toggleUserAssignment(userId: String) {
    val currentIds = uiState.value.selectedAssignedUserIds
    val newIds = if (currentIds.contains(userId)) {
      currentIds - userId
    } else {
      currentIds + userId
    }
    updateState { copy(selectedAssignedUserIds = newIds) }
  }
}

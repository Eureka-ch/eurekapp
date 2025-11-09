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
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
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
Note: This file was partially written by GPT-5 Codex Co-author : GPT-5
*/

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class CreateTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    private val projectRepository: ProjectRepository = FirestoreRepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    ReadWriteTaskViewModel<CreateTaskState>(
        taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val _uiState = MutableStateFlow(CreateTaskState())
  override val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

  init {
    // Initialize with current user as default assignee
    getCurrentUserId()?.let { userId ->
      updateState { copy(selectedAssignedUserIds = listOf(userId)) }
    }
  }

  /** Adds a Task */
  fun addTask(context: Context) {
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
                assignedUserIds = state.selectedAssignedUserIds.ifEmpty { listOf(currentUser) },
                dueDate = timestamp,
                reminderTime = reminderTimestamp,
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

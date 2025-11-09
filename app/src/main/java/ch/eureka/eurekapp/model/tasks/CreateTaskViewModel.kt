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
import ch.eureka.eurekapp.utils.TaskDependencyCycleDetector

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

  private val _availableTasks = MutableStateFlow<List<Task>>(emptyList())
  val availableTasks: StateFlow<List<Task>> = _availableTasks.asStateFlow()

  private val _cycleError = MutableStateFlow<String?>(null)
  val cycleError: StateFlow<String?> = _cycleError.asStateFlow()

  fun loadAvailableTasks(projectId: String) {
    if (projectId.isEmpty()) {
      _availableTasks.value = emptyList()
      return
    }
    viewModelScope.launch(dispatcher) {
      taskRepository.getTasksInProject(projectId).collect { tasks ->
        _availableTasks.value = tasks
      }
    }
  }

  suspend fun validateDependency(dependencyTaskId: String): Boolean {
    val state = uiState.value
    if (state.projectId.isEmpty()) return true

    val taskId = IdGenerator.generateTaskId() // For new tasks, we use a placeholder
    val wouldCycle =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId, dependencyTaskId, state.projectId, taskRepository)
    if (wouldCycle) {
      _cycleError.value = "Adding this dependency would create a circular dependency"
      return false
    }
    _cycleError.value = null
    return true
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
        // Validate no cycles before saving
        val cycleValidation =
            TaskDependencyCycleDetector.validateNoCycles(
                taskId, state.dependingOnTasks, projectIdToUse, taskRepository)
        if (cycleValidation.isFailure) {
          setErrorMsg(cycleValidation.exceptionOrNull()?.message ?: "Circular dependency detected")
          updateState { copy(isSaving = false) }
          return@launch
        }

        val task =
            Task(
                taskID = taskId,
                projectId = projectIdToUse,
                title = state.title,
                description = state.description,
                assignedUserIds = listOf(currentUser),
                dueDate = timestamp,
                reminderTime = reminderTimestamp,
                attachmentUrls = photoUrls,
                createdBy = currentUser,
                dependingOnTasks = state.dependingOnTasks)

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

  override fun CreateTaskState.copyWithDependencies(dependencies: List<String>) =
      copy(dependingOnTasks = dependencies)

  override fun updateState(update: CreateTaskState.() -> CreateTaskState) {
    _uiState.value = _uiState.value.update()
  }

  fun setReminderTime(reminderTime: String) {
    updateState { copy(reminderTime = reminderTime) }
  }

  override fun addDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    if (!currentDependencies.contains(taskId)) {
      viewModelScope.launch(dispatcher) {
        // For new tasks, we can't validate cycles perfectly since task doesn't exist yet
        // But we can check if the dependency would create a cycle with existing tasks
        val newTaskId = IdGenerator.generateTaskId()
        val wouldCycle =
            TaskDependencyCycleDetector.wouldCreateCycle(
                newTaskId, taskId, uiState.value.projectId, taskRepository)
        if (wouldCycle) {
          _cycleError.value = "Adding this dependency would create a circular dependency"
        } else {
          _cycleError.value = null
          updateState { copyWithDependencies(currentDependencies + taskId) }
        }
      }
    }
  }

  override fun removeDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    updateState { copyWithDependencies(currentDependencies.filter { it != taskId }) }
    _cycleError.value = null
  }
}

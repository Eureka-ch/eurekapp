package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.utils.TaskDependencyCycleDetector
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
 * Portions of this code were generated with the help of Claude <noreply@anthropic.com>, Grok, and GPT-5 Codex.
 * Co-Authored-By: Claude Sonnet 4.5
 */

/** ViewModel for the CreateTask screen. This ViewModel manages the state of input fields. */
class CreateTaskViewModel(
    taskRepository: TaskRepository = RepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val templateRepository: TaskTemplateRepository =
        RepositoriesProvider.taskTemplateRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    ReadWriteTaskViewModel<CreateTaskState>(
        taskRepository,
        fileRepository,
        projectRepository,
        userRepository,
        getCurrentUserId,
        dispatcher) {

  private val _uiState = MutableStateFlow(CreateTaskState())
  override val uiState: StateFlow<CreateTaskState> = _uiState.asStateFlow()

  private val connectivityObserver = ConnectivityObserverProvider.connectivityObserver
  val isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  init {
    // Load available projects
    loadAvailableProjects()
  }

  /** Loads available projects for the current user */
  private fun loadAvailableProjects() {
    viewModelScope.launch(dispatcher) {
      projectRepository.getProjectsForCurrentUser().collect { projects ->
        updateState { copy(availableProjects = projects) }
      }
    }
  }

  /** Loads available templates for the given project */
  fun loadTemplatesForProject(projectId: String) {
    if (projectId.isEmpty()) {
      updateState { copy(availableTemplates = emptyList(), selectedTemplate = null) }
      return
    }
    viewModelScope.launch(dispatcher) {
      templateRepository.getTemplatesInProject(projectId).collect { templates ->
        updateState { copy(availableTemplates = templates) }
      }
    }
  }

  /** Selects a template and initializes customData with default values */
  fun selectTemplate(templateId: String?) {
    val templates = uiState.value.availableTemplates
    val template = templates.find { it.templateID == templateId }
    val initialData = initializeCustomData(template)
    updateState {
      copy(templateId = templateId, selectedTemplate = template, customData = initialData)
    }
  }

  /** Initializes customData with default values from the template */
  private fun initializeCustomData(
      template: ch.eureka.eurekapp.model.data.template.TaskTemplate?
  ): TaskCustomData {
    if (template == null) return TaskCustomData()
    val initialData = mutableMapOf<String, FieldValue>()
    template.definedFields.fields.forEach { field ->
      field.defaultValue?.let { initialData[field.id] = it }
    }
    return TaskCustomData(initialData)
  }

  /** Updates a custom field value */
  fun updateCustomFieldValue(fieldId: String, value: FieldValue) {
    val currentData = uiState.value.customData
    updateState { copy(customData = currentData.setValue(fieldId, value)) }
  }

  // Placeholder used for dependency validation before the task is persisted.
  private val placeholderTaskId = IdGenerator.generateTaskId()

  suspend fun validateDependency(dependencyTaskId: String): Boolean {
    val state = uiState.value
    if (state.projectId.isEmpty()) return true

    val wouldCycle =
        TaskDependencyCycleDetector.wouldCreateCycle(
            placeholderTaskId, dependencyTaskId, state.projectId, taskRepository)
    if (wouldCycle) {
      setCycleError("Adding this dependency would create a circular dependency")
      return false
    }
    setCycleError(null)
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
      Log.e("CreateTaskViewModel", exception.message ?: "Unknown error", exception)

      val errorMessage =
          when {
            exception.message?.contains("Timed out") == true ->
                "Upload timed out. Please check your connection and try again."
            exception.message?.contains("Unable to resolve host") == true ->
                "Network error. Please check your internet connection."
            else -> "Unable to save task: ${exception.message}"
          }

      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, errorMessage, Toast.LENGTH_LONG).show()
      }
      updateState { copy(isSaving = false) }
    }

    updateState { copy(isSaving = true) }

    val taskId = IdGenerator.generateTaskId()
    val projectIdToUse = state.projectId

    saveFilesAsync(taskId, context, projectIdToUse, state.attachmentUris) { photoUrlsResult ->
      if (photoUrlsResult.isFailure) {
        val exception = photoUrlsResult.exceptionOrNull()
        Log.e("CreateTaskViewModel", "Failed to upload files", exception)

        val errorMessage =
            when {
              exception?.message?.contains("Timed out") == true ->
                  "Upload timed out. Please check your connection and try again."
              exception?.message?.contains("Unable to.resolve host") == true ->
                  "Network error. Please check your internet connection."
              else -> "Unable to save task: ${exception?.message}"
            }

        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context.applicationContext, errorMessage, Toast.LENGTH_LONG).show()
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
                templateId = state.templateId ?: "",
                projectId = projectIdToUse,
                title = state.title,
                description = state.description,
                assignedUserIds = state.selectedAssignedUserIds,
                dueDate = timestamp,
                reminderTime = reminderTimestamp,
                attachmentUrls = photoUrls,
                customData = state.customData,
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

  fun removeAttachmentAndDelete(context: Context, index: Int) {
    val uri = uiState.value.attachmentUris[index]
    if (uiState.value.temporaryPhotoUris.contains(uri)) {
      deletePhotoAsync(context, uri) { success ->
        if (success) {
          removeAttachment(index)
          updateState { copyWithTemporaryPhotoUris(temporaryPhotoUris.filter { it != uri }) }
        }
      }
    } else {
      removeAttachment(index)
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

  override fun CreateTaskState.copyWithReminderTime(reminderTime: String) =
      copy(reminderTime = reminderTime)

  override fun CreateTaskState.copyWithAvailableUsers(
      users: List<ch.eureka.eurekapp.model.data.user.User>
  ) = copy(availableUsers = users)

  override fun CreateTaskState.copyWithSelectedAssignedUserIds(userIds: List<String>) =
      copy(selectedAssignedUserIds = userIds)

  override fun CreateTaskState.copyWithTemporaryPhotoUris(uris: List<Uri>) =
      copy(temporaryPhotoUris = uris)

  override fun addDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    if (!currentDependencies.contains(taskId)) {
      viewModelScope.launch(dispatcher) {
        // For new tasks, we can't validate cycles perfectly since task doesn't exist yet
        // But we can check if the dependency would create a cycle with existing tasks
        val wouldCycle =
            TaskDependencyCycleDetector.wouldCreateCycle(
                placeholderTaskId, taskId, uiState.value.projectId, taskRepository)
        if (wouldCycle) {
          setCycleError("Adding this dependency would create a circular dependency")
        } else {
          setCycleError(null)
          updateState { copyWithDependencies(currentDependencies + taskId) }
        }
      }
    }
  }

  override fun removeDependency(taskId: String) {
    val currentDependencies = uiState.value.dependingOnTasks
    updateState { copyWithDependencies(currentDependencies.filter { it != taskId }) }
    setCycleError(null)
  }
}

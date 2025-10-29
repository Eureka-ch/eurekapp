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
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Portions of this code were generated with the help of Grok.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** ViewModel for the EditTask screen. This ViewModel manages the state of input fields. */
class EditTaskViewModel(
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    fileRepository: FileStorageRepository = FirestoreRepositoriesProvider.fileRepository,
    projectRepository: ProjectRepository = FirestoreRepositoriesProvider.projectRepository,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseTaskViewModel<EditTaskState>(taskRepository, fileRepository, getCurrentUserId, dispatcher) {

  private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

  // Flow for available projects - using flow pattern like TaskScreenViewModel
  private val availableProjectsFlow =
      projectRepository
          .getProjectsForCurrentUser()
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Project>())

  private val _baseUiState = MutableStateFlow(EditTaskState())
  override val uiState: StateFlow<EditTaskState> =
      combine(_baseUiState, availableProjectsFlow) { state, projects ->
            state.copy(availableProjects = projects)
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, EditTaskState())

  override fun getState(): EditTaskState = _baseUiState.value

  /** Resets the delete state after navigation or handling */
  fun resetDeleteState() {
    _baseUiState.value = _baseUiState.value.copy(taskDeleted = false)
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

    val currentUser = getCurrentUserId() ?: throw Exception("User not logged in.")

    val handler = CoroutineExceptionHandler { _, _ ->
      Handler(Looper.getMainLooper()).post {
        Toast.makeText(context.applicationContext, "Unable to save task", Toast.LENGTH_SHORT).show()
      }
      updateState { copy(isSaving = false) }
    }

    updateState { copy(isSaving = true) }

    val projectIdToUse = state.selectedProjectId.takeIf { it.isNotEmpty() } ?: state.projectId
    saveFilesAsync(state.taskId, context, projectIdToUse, state.attachmentUris) { photoUrlsResult ->
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
                projectId = projectIdToUse,
                title = state.title,
                description = state.description,
                assignedUserIds = listOf(currentUser),
                dueDate = timestamp,
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
    val state = _baseUiState.value
    val currentUris = state.attachmentUris
    val currentUrls = state.attachmentUrls

    if (index in currentUris.indices) {
      _baseUiState.value =
          _baseUiState.value.copy(
              attachmentUris = currentUris.toMutableList().apply { removeAt(index) })
    } else if (index - currentUris.size in currentUrls.indices) {
      val urlIndex = index - currentUris.size
      val urlToDelete = currentUrls[urlIndex]
      _baseUiState.value =
          _baseUiState.value.copy(
              attachmentUrls = currentUrls.toMutableList().apply { removeAt(urlIndex) },
              deletedAttachmentUrls = state.deletedAttachmentUrls + urlToDelete)
    }
  }

  fun removeAttachmentAndDelete(context: Context, index: Int) {
    val allAttachments = _baseUiState.value.attachmentUrls + _baseUiState.value.attachmentUris
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
    if (_baseUiState.value.isDeleting || _baseUiState.value.taskDeleted) {
      return
    }

    viewModelScope.launch(dispatcher) {
      taskRepository
          .getTaskById(projectId, taskId)
          .catch { exception ->
            // Only show error if we're not in the process of deleting
            if (!_baseUiState.value.isDeleting && !_baseUiState.value.taskDeleted) {
              setErrorMsg("Failed to load Task: ${exception.message}")
            }
          }
          .collect { task ->
            if (!_baseUiState.value.isDeleting && !_baseUiState.value.taskDeleted) {
              if (task != null) {
                val deletedUrls = _baseUiState.value.deletedAttachmentUrls
                val filteredAttachments = task.attachmentUrls.filterNot { it in deletedUrls }
                _baseUiState.value =
                    _baseUiState.value.copy(
                        title = task.title,
                        description = task.description,
                        dueDate =
                            task.dueDate?.let { date -> dateFormat.format(date.toDate()) } ?: "",
                        templateId = task.templateId,
                        projectId = task.projectId,
                        selectedProjectId = task.projectId,
                        taskId = task.taskID,
                        assignedUserIds = task.assignedUserIds,
                        attachmentUrls = filteredAttachments,
                        status = task.status,
                        customData = task.customData,
                    )
              } else {
                setErrorMsg("Task not found.")
              }
            }
          }
    }
  }

  fun deleteTask(projectId: String, taskId: String) {
    _baseUiState.value = _baseUiState.value.copy(isDeleting = true)

    for (url in _baseUiState.value.attachmentUrls) {
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
            _baseUiState.value = _baseUiState.value.copy(isDeleting = false)
            return@launch
          }
          .onSuccess { _ ->
            _baseUiState.value = _baseUiState.value.copy(isDeleting = false, taskDeleted = true)
          }
    }
  }

  fun setStatus(status: TaskStatus) {
    _baseUiState.value = _baseUiState.value.copy(status = status)
  }

  // State update implementations
  override fun updateState(update: EditTaskState.() -> EditTaskState) {
    _baseUiState.value = _baseUiState.value.update()
  }

  override fun EditTaskState.copyWithErrorMsg(errorMsg: String?) = copy(errorMsg = errorMsg)

  override fun EditTaskState.copyWithSaveState(isSaving: Boolean, taskSaved: Boolean) =
      copy(isSaving = isSaving, taskSaved = taskSaved)

  override fun EditTaskState.copyWithTitle(title: String) = copy(title = title)

  override fun EditTaskState.copyWithDescription(description: String) =
      copy(description = description)

  override fun EditTaskState.copyWithDueDate(dueDate: String) = copy(dueDate = dueDate)

  override fun EditTaskState.copyWithAttachmentUris(uris: List<Uri>) = copy(attachmentUris = uris)

  override fun EditTaskState.copyWithProjectId(projectId: String) = copy(projectId = projectId)

  override fun EditTaskState.copyWithSelectedProjectId(projectId: String) =
      copy(selectedProjectId = projectId)

  override fun EditTaskState.copyWithAvailableProjects(projects: List<Project>) =
      copy(availableProjects = projects)
}

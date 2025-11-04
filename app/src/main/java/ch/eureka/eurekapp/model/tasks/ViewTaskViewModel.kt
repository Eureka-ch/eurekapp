package ch.eureka.eurekapp.model.tasks

import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Portions of this code were generated with the help of Grok.

/**
 * ViewModel for viewing task details. This ViewModel is responsible only for loading and displaying
 * task information.
 */
class ViewTaskViewModel(
    projectId: String,
    taskId: String,
    taskRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReadTaskViewModel<ViewTaskState>(taskRepository, dispatcher) {

  override val uiState: StateFlow<ViewTaskState> =
      taskRepository
          .getTaskById(projectId, taskId)
          .map { task ->
            if (task != null) {
              ViewTaskState(
                  title = task.title,
                  description = task.description,
                  dueDate = task.dueDate?.let { date -> dateFormat.format(date.toDate()) } ?: "",
                  projectId = task.projectId,
                  taskId = task.taskID,
                  attachmentUrls = task.attachmentUrls,
                  status = task.status,
                  isLoading = false,
                  errorMsg = null)
            } else {
              ViewTaskState(isLoading = false, errorMsg = "Task not found.")
            }
          }
          .catch { exception ->
            emit(
                ViewTaskState(
                    isLoading = false, errorMsg = "Failed to load Task: ${exception.message}"))
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ViewTaskState())

  override fun ViewTaskState.copyWithErrorMsg(errorMsg: String?): ViewTaskState {
    return copy(errorMsg = errorMsg)
  }
}

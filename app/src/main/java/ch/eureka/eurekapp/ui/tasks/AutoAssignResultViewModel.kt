package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// portions of this code and documentation were generated with the help of AI.
/**
 * Data class representing a proposed assignment for a task.
 *
 * @property task The task to be assigned
 * @property proposedAssignee The user proposed to be assigned to the task
 * @property projectId The project ID containing the task
 * @property isAccepted Whether the assignment has been accepted
 * @property isRejected Whether the assignment has been rejected
 */
data class ProposedAssignment(
    val task: Task,
    val proposedAssignee: User,
    val projectId: String,
    val isAccepted: Boolean = false,
    val isRejected: Boolean = false
)

/** UI state for the auto-assign result screen */
data class AutoAssignResultUiState(
    val proposedAssignments: List<ProposedAssignment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isApplying: Boolean = false,
    val appliedCount: Int = 0
)

/**
 * ViewModel for managing auto-assignment results and user actions.
 *
 * Handles displaying proposed assignments and applying accepted assignments.
 */
class AutoAssignResultViewModel(
    private val projectRepository: ProjectRepository =
        FirestoreProjectRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val taskRepository: TaskRepository =
        FirestoreTaskRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            projectRepository = projectRepository),
    private val userRepository: UserRepository =
        FirestoreUserRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val autoAssignResultLoader: AutoAssignResultLoader =
        AutoAssignResultLoader(taskRepository, projectRepository, userRepository)
) : ViewModel() {

  private val _uiState = MutableStateFlow(AutoAssignResultUiState(isLoading = true))
  val uiState: StateFlow<AutoAssignResultUiState> = _uiState.asStateFlow()

  private val _proposedAssignments = MutableStateFlow<List<ProposedAssignment>>(emptyList())

  init {
    loadAutoAssignResults()
  }

  /** Loads auto-assignment results by running the algorithm and fetching user data. */
  private fun loadAutoAssignResults() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      when (val result = autoAssignResultLoader.load()) {
        is AutoAssignResultLoader.Result.Success -> {
          _proposedAssignments.value = result.proposedAssignments
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, proposedAssignments = result.proposedAssignments, error = null)
        }
        is AutoAssignResultLoader.Result.Error.NoProjects -> {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "No projects available for auto-assignment")
        }
        is AutoAssignResultLoader.Result.Error.NoMembers -> {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "No team members available for assignment")
        }
        is AutoAssignResultLoader.Result.Error.NoAssignableTasks -> {
          val skipped = result.skippedTasks
          val message =
              if (skipped > 0) {
                "No tasks could be assigned. $skipped tasks skipped."
              } else {
                "No unassigned tasks found."
              }
          _uiState.value = _uiState.value.copy(isLoading = false, error = message)
        }
        is AutoAssignResultLoader.Result.Error.Generic -> {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false,
                  error = "Failed to load assignments: ${result.throwable.message}")
        }
      }
    }
  }

  /**
   * Accepts a proposed assignment.
   *
   * @param assignment The assignment to accept
   */
  fun acceptAssignment(assignment: ProposedAssignment) {
    val updatedAssignments =
        _proposedAssignments.value.map {
          if (it.task.taskID == assignment.task.taskID) {
            it.copy(isAccepted = true, isRejected = false)
          } else {
            it
          }
        }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }

  /**
   * Rejects a proposed assignment.
   *
   * @param assignment The assignment to reject
   */
  fun rejectAssignment(assignment: ProposedAssignment) {
    val updatedAssignments =
        _proposedAssignments.value.map {
          if (it.task.taskID == assignment.task.taskID) {
            it.copy(isAccepted = false, isRejected = true)
          } else {
            it
          }
        }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }

  /** Applies all accepted assignments. */
  fun applyAcceptedAssignments() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isApplying = true, error = null)

      try {
        val acceptedAssignments =
            _proposedAssignments.value.filter { it.isAccepted && !it.isRejected }

        if (acceptedAssignments.isEmpty()) {
          _uiState.value =
              _uiState.value.copy(isApplying = false, error = "No assignments selected to apply")
          return@launch
        }

        var successCount = 0
        var failureCount = 0

        for (assignment in acceptedAssignments) {
          val result =
              taskRepository.assignUser(
                  assignment.projectId, assignment.task.taskID, assignment.proposedAssignee.uid)
          if (result.isSuccess) {
            successCount++
          } else {
            failureCount++
          }
        }

        _uiState.value =
            _uiState.value.copy(
                isApplying = false,
                appliedCount = successCount,
                error =
                    if (failureCount > 0) {
                      "$successCount assignments applied. $failureCount failed."
                    } else {
                      null
                    })
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isApplying = false, error = "Failed to apply assignments: ${e.message}")
      }
    }
  }

  /** Accepts all proposed assignments. */
  fun acceptAll() {
    val updatedAssignments =
        _proposedAssignments.value.map { it.copy(isAccepted = true, isRejected = false) }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }

  /** Rejects all proposed assignments. */
  fun rejectAll() {
    val updatedAssignments =
        _proposedAssignments.value.map { it.copy(isAccepted = false, isRejected = true) }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }
}

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
import ch.eureka.eurekapp.model.tasks.TaskAutoAssignmentService
import ch.eureka.eurekapp.screens.TaskAndUsers
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val taskRepository: TaskRepository =
        FirestoreTaskRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val projectRepository: ProjectRepository =
        FirestoreProjectRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance()),
    private val userRepository: UserRepository =
        FirestoreUserRepository(
            firestore = FirebaseFirestore.getInstance(), auth = FirebaseAuth.getInstance())
) : ViewModel() {

  private val _uiState = MutableStateFlow(AutoAssignResultUiState(isLoading = true))
  val uiState: StateFlow<AutoAssignResultUiState> = _uiState.asStateFlow()

  private val _proposedAssignments = MutableStateFlow<List<ProposedAssignment>>(emptyList())

  init {
    loadAutoAssignResults()
  }

  /**
   * Loads auto-assignment results by running the algorithm and fetching user data.
   */
  private fun loadAutoAssignResults() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      try {
        // Get all projects for current user
        val projects = projectRepository.getProjectsForCurrentUser().first()

        if (projects.isEmpty()) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "No projects available for auto-assignment")
          return@launch
        }

        // Collect all tasks from all projects
        val allTasks = mutableListOf<Task>()
        val projectTasksMap = mutableMapOf<String, List<Task>>()

        for (project in projects) {
          val tasks = taskRepository.getTasksInProject(project.projectId).first()
          allTasks.addAll(tasks)
          projectTasksMap[project.projectId] = tasks
        }

        // Collect all members from all projects
        val allMembers = mutableListOf<ch.eureka.eurekapp.model.data.project.Member>()
        for (project in projects) {
          val members = projectRepository.getMembers(project.projectId).first()
          allMembers.addAll(members)
        }

        // Remove duplicate members
        val uniqueMembers = allMembers.distinctBy { it.userId }

        if (uniqueMembers.isEmpty()) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "No team members available for assignment")
          return@launch
        }

        // Run auto-assignment algorithm
        val assignmentResult =
            TaskAutoAssignmentService.assignTasks(allTasks, uniqueMembers)

        if (assignmentResult.assignments.isEmpty()) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false,
                  error =
                      if (assignmentResult.skippedTasks.isNotEmpty()) {
                        "No tasks could be assigned. ${assignmentResult.skippedTasks.size} tasks skipped."
                      } else {
                        "No unassigned tasks found."
                      })
          return@launch
        }

        // Fetch user data for proposed assignees
        val proposedAssignments = mutableListOf<ProposedAssignment>()

        for ((taskId, userId) in assignmentResult.assignments) {
          // Find the project for this task
          val taskProject =
              projects.find { project ->
                projectTasksMap[project.projectId]?.any { it.taskID == taskId } == true
              }

          if (taskProject != null) {
            val task = allTasks.find { it.taskID == taskId }
            val user = userRepository.getUserById(userId).first()

            if (task != null && user != null) {
              proposedAssignments.add(
                  ProposedAssignment(
                      task = task,
                      proposedAssignee = user,
                      projectId = taskProject.projectId))
            }
          }
        }

        _proposedAssignments.value = proposedAssignments
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, proposedAssignments = proposedAssignments, error = null)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, error = "Failed to load assignments: ${e.message}")
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

  /**
   * Applies all accepted assignments.
   */
  fun applyAcceptedAssignments() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isApplying = true, error = null)

      try {
        val acceptedAssignments =
            _proposedAssignments.value.filter { it.isAccepted && !it.isRejected }

        if (acceptedAssignments.isEmpty()) {
          _uiState.value =
              _uiState.value.copy(
                  isApplying = false, error = "No assignments selected to apply")
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

  /**
   * Accepts all proposed assignments.
   */
  fun acceptAll() {
    val updatedAssignments =
        _proposedAssignments.value.map { it.copy(isAccepted = true, isRejected = false) }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }

  /**
   * Rejects all proposed assignments.
   */
  fun rejectAll() {
    val updatedAssignments =
        _proposedAssignments.value.map { it.copy(isAccepted = false, isRejected = true) }
    _proposedAssignments.value = updatedAssignments
    _uiState.value = _uiState.value.copy(proposedAssignments = updatedAssignments)
  }
}


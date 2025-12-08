/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.ideas.IdeasRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** UI state for CreateIdea bottom sheet */
data class CreateIdeaState(
    val title: String = "",
    val selectedProject: Project? = null,
    val selectedParticipantIds: Set<String> = emptySet(),
    val availableProjects: List<Project> = emptyList(),
    val availableUsers: List<User> = emptyList(),
    val isCreating: Boolean = false,
    val errorMsg: String? = null,
    val navigateToIdea: Idea? = null
)

/**
 * ViewModel for creating a new Idea.
 *
 * Manages the state for the Create Idea bottom sheet, including:
 * - Project selection from user's projects
 * - Loading available users from selected project
 * - Participant selection
 * - Creating the idea with selected data
 */
open class CreateIdeaViewModel
@JvmOverloads
constructor(
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val ideasRepository: IdeasRepository = RepositoriesProvider.ideasRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  companion object {
    private const val TAG = "CreateIdeaViewModel"
  }

  private val _title = MutableStateFlow("")
  private val _selectedProject = MutableStateFlow<Project?>(null)
  private val _selectedParticipantIds = MutableStateFlow<Set<String>>(emptySet())
  private val _availableUsers = MutableStateFlow<List<User>>(emptyList())
  private val _isCreating = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _navigateToIdea = MutableStateFlow<Idea?>(null)

  private val projectsFlow =
      projectRepository
          .getProjectsForCurrentUser()
          .catch { e ->
            Log.e(TAG, "Error loading projects", e)
            _errorMsg.value = "Error loading projects: ${e.message}"
            emit(emptyList())
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  @Suppress("UNCHECKED_CAST")
  open val uiState: StateFlow<CreateIdeaState> =
      combine(
              _title,
              _selectedProject,
              _selectedParticipantIds,
              projectsFlow,
              _availableUsers,
              _isCreating,
              _errorMsg,
              _navigateToIdea) { args ->
                val title = args[0] as String
                val project = args[1] as Project?
                val participants = args[2] as Set<String>
                val projects = args[3] as List<Project>
                val users = args[4] as List<User>
                val creating = args[5] as Boolean
                val error = args[6] as String?
                val navigate = args[7] as Idea?
                CreateIdeaState(
                    title = title,
                    selectedProject = project,
                    selectedParticipantIds = participants,
                    availableProjects = projects,
                    availableUsers = users,
                    isCreating = creating,
                    errorMsg = error,
                    navigateToIdea = navigate)
              }
          .stateIn(viewModelScope, SharingStarted.Eagerly, CreateIdeaState())

  open fun updateTitle(title: String) {
    _title.value = title
  }

  open fun selectProject(project: Project) {
    _selectedProject.value = project
    _selectedParticipantIds.value = emptySet()
    loadUsersForProject(project.projectId)
  }

  open fun toggleParticipant(userId: String) {
    val currentSet = _selectedParticipantIds.value.toMutableSet()
    if (currentSet.contains(userId)) {
      currentSet.remove(userId)
    } else {
      currentSet.add(userId)
    }
    _selectedParticipantIds.value = currentSet
  }

  open fun createIdea() {
    val projectId = _selectedProject.value?.projectId
    val currentUserId = getCurrentUserId()

    if (projectId == null) {
      _errorMsg.value = "Please select a project"
      return
    }

    if (currentUserId == null) {
      _errorMsg.value = "User not authenticated"
      return
    }

    if (_isCreating.value) return

    _isCreating.value = true

    viewModelScope.launch(dispatcher) {
      val allParticipantIds = (listOf(currentUserId) + _selectedParticipantIds.value).distinct()

      val newIdea =
          Idea(
              ideaId = IdGenerator.generateIdeaId(),
              projectId = projectId,
              title = _title.value.takeIf { it.isNotBlank() },
              createdBy = currentUserId,
              participantIds = allParticipantIds,
              createdAt = Timestamp.now(),
              lastUpdated = Timestamp.now())

      ideasRepository
          .createIdea(newIdea)
          .fold(
              onSuccess = {
                _navigateToIdea.value = newIdea
                _isCreating.value = false
              },
              onFailure = { error ->
                Log.e(TAG, "Error creating idea", error)
                _errorMsg.value = "Error creating idea: ${error.message}"
                _isCreating.value = false
              })
    }
  }

  private fun loadUsersForProject(projectId: String) {
    if (projectId.isBlank()) {
      _availableUsers.value = emptyList()
      return
    }

    viewModelScope.launch(dispatcher) {
      try {
        val members = projectRepository.getMembers(projectId).first()
        if (members.isEmpty()) {
          _availableUsers.value = emptyList()
        } else {
          val userFlows = members.map { member -> userRepository.getUserById(member.userId) }
          val users = combine(userFlows) { userArray -> userArray.toList().filterNotNull() }.first()
          _availableUsers.value = users
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error loading users", e)
        _errorMsg.value = "Error loading users: ${e.message}"
      }
    }
  }

  open fun clearError() {
    _errorMsg.value = null
  }

  open fun resetNavigation() {
    _navigateToIdea.value = null
  }

  open fun reset() {
    _title.value = ""
    _selectedProject.value = null
    _selectedParticipantIds.value = emptySet()
    _availableUsers.value = emptyList()
    _isCreating.value = false
    _errorMsg.value = null
    _navigateToIdea.value = null
  }
}

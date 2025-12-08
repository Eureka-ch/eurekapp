/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.ideas.IdeasRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/** UI state data class for Ideas Screen. */
data class IdeasUIState(
    val selectedProject: Project? = null,
    val availableProjects: List<Project> = emptyList(),
    val ideas: List<Idea> = emptyList(),
    val selectedIdea: Idea? = null,
    val messages: List<Message> = emptyList(),
    val viewMode: IdeasViewMode = IdeasViewMode.LIST,
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

/** Placeholder UI state function for when ViewModel is not yet implemented. */
fun IdeasUIStatePlaceholder(): IdeasUIState = IdeasUIState()

/**
 * ViewModel for the Ideas Screen.
 *
 * Manages the state for ideas (conversations with AI) including:
 * - Loading and displaying ideas for a selected project
 * - Creating new ideas
 * - Selecting an idea to view its conversation
 * - Sending messages in an idea conversation
 * - Sharing ideas by adding participants
 * - Hiding ideas locally (deletion is local only, not global)
 *
 * @property projectRepository Repository for project data operations.
 * @property ideasRepository Repository for ideas data operations.
 * @property getCurrentUserId Function to get the current authenticated user's ID.
 * @property dispatcher Coroutine dispatcher for background operations.
 */
open class IdeasViewModel
@JvmOverloads
constructor(
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val ideasRepository: IdeasRepository = RepositoriesProvider.ideasRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  companion object {
    private const val MAX_MESSAGE_LENGTH = 5000
    private const val TAG = "IdeasViewModel"
  }

  private val _selectedProject = MutableStateFlow<Project?>(null)
  private val _selectedIdea = MutableStateFlow<Idea?>(null)
  private val _viewMode = MutableStateFlow<IdeasViewMode>(IdeasViewMode.LIST)
  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _hiddenIdeaIds = MutableStateFlow<Set<String>>(emptySet())

  private val projectsFlow =
      projectRepository
          .getProjectsForCurrentUser()
          .catch { e ->
            Log.e(TAG, "Error loading projects", e)
            _errorMsg.value = "Error loading projects: ${e.message}"
            emit(emptyList())
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val ideasFlow =
      combine(
          _selectedProject.flatMapLatest { project ->
            if (project != null) {
              ideasRepository.getIdeasForProject(project.projectId).catch { e ->
                Log.e(TAG, "Error loading ideas", e)
                _errorMsg.value = "Error loading ideas: ${e.message}"
                emit(emptyList())
              }
            } else {
              flowOf(emptyList())
            }
          },
          _hiddenIdeaIds) { ideas, hiddenIds ->
            val currentUserId = getCurrentUserId()
            ideas.filter { idea ->
              currentUserId != null &&
                  idea.participantIds.contains(currentUserId) &&
                  !hiddenIds.contains(idea.ideaId)
            }
          }

  // Messages flow removed - chat functionality in separate PR

  /** The single source of truth for the UI state. */
  @Suppress("UNCHECKED_CAST")
  open val uiState: StateFlow<IdeasUIState> =
      combine(projectsFlow, _selectedProject, ideasFlow, _selectedIdea, _viewMode, _errorMsg) { args
            ->
            val projects = args[0] as List<Project>
            val selectedProject = args[1] as Project?
            val ideas = args[2] as List<Idea>
            val selectedIdea = args[3] as Idea?
            val viewMode = args[4] as IdeasViewMode
            val errorMsg = args[5] as String?

            IdeasUIState(
                selectedProject = selectedProject,
                availableProjects = projects,
                ideas = ideas,
                selectedIdea = selectedIdea,
                messages = emptyList(), // Chat functionality in separate PR
                viewMode = viewMode,
                currentMessage = "",
                isSending = false,
                isLoading = false,
                errorMsg = errorMsg)
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = IdeasUIState(isLoading = true))

  open fun selectProject(project: Project) {
    _selectedProject.value = project
    _viewMode.value = IdeasViewMode.LIST
    _selectedIdea.value = null
  }

  open fun selectIdea(idea: Idea) {
    _selectedIdea.value = idea
    _viewMode.value = IdeasViewMode.LIST // Conversation mode in separate PR
  }

  /** Called when a new idea is created from CreateIdeaViewModel */
  open fun onIdeaCreated(idea: Idea) {
    val project = projectsFlow.value.find { it.projectId == idea.projectId }
    if (project != null) {
      _selectedProject.value = project
    }
    _selectedIdea.value = idea
    // Conversation navigation in separate PR
  }

  open fun deleteIdea(ideaId: String) {
    val currentUserId = getCurrentUserId()
    if (currentUserId == null) return

    val hiddenIds = _hiddenIdeaIds.value.toMutableSet()
    hiddenIds.add(ideaId)
    _hiddenIdeaIds.value = hiddenIds

    if (_selectedIdea.value?.ideaId == ideaId) {
      _selectedIdea.value = null
      _viewMode.value = IdeasViewMode.LIST
    }
  }

  // Chat functionality (sendMessage, updateMessage, addParticipant) will be in separate PR

  open fun clearError() {
    _errorMsg.value = null
  }

  open fun getCurrentUserId(): String? = getCurrentUserId.invoke()
}

/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val getCurrentUserId: () -> String? = { null },
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

  private val messagesFlow =
      _selectedIdea.flatMapLatest { idea ->
        if (idea != null) {
          ideasRepository.getMessagesForIdea(idea.ideaId).catch { e ->
            Log.e(TAG, "Error loading messages", e)
            _errorMsg.value = "Error loading messages: ${e.message}"
            emit(emptyList())
          }
        } else {
          flowOf(emptyList())
        }
      }

  /** The single source of truth for the UI state. */
  @Suppress("UNCHECKED_CAST")
  open val uiState: StateFlow<IdeasUIState> =
      combine(
              projectsFlow,
              _selectedProject,
              ideasFlow,
              _selectedIdea,
              messagesFlow,
              _viewMode,
              _currentMessage,
              _isSending,
              _errorMsg) { args ->
                val projects = args[0] as List<Project>
                val selectedProject = args[1] as Project?
                val ideas = args[2] as List<Idea>
                val selectedIdea = args[3] as Idea?
                val messages = args[4] as List<Message>
                val viewMode = args[5] as IdeasViewMode
                val currentMessage = args[6] as String
                val isSending = args[7] as Boolean
                val errorMsg = args[8] as String?

                IdeasUIState(
                    selectedProject = selectedProject,
                    availableProjects = projects,
                    ideas = ideas,
                    selectedIdea = selectedIdea,
                    messages = messages,
                    viewMode = viewMode,
                    currentMessage = currentMessage,
                    isSending = isSending,
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
    _currentMessage.value = ""
  }

  open fun selectIdea(idea: Idea) {
    _selectedIdea.value = idea
    _viewMode.value = IdeasViewMode.CONVERSATION
    _currentMessage.value = ""
  }

  /** Called when a new idea is created from CreateIdeaViewModel */
  open fun onIdeaCreated(idea: Idea) {
    val project = projectsFlow.value.find { it.projectId == idea.projectId }
    if (project != null) {
      _selectedProject.value = project
    }
    _selectedIdea.value = idea
    _viewMode.value = IdeasViewMode.CONVERSATION
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
      _currentMessage.value = ""
    }
  }

  open fun addParticipantToIdea(ideaId: String, userId: String) {
    val projectId = _selectedProject.value?.projectId
    if (projectId == null) {
      _errorMsg.value = "No project selected"
      return
    }

    viewModelScope.launch(dispatcher) {
      ideasRepository
          .addParticipant(projectId, ideaId, userId)
          .fold(
              onSuccess = {},
              onFailure = { error ->
                Log.e(TAG, "Error adding participant", error)
                _errorMsg.value = "Error sharing idea: ${error.message}"
              })
    }
  }

  open fun updateMessage(message: String) {
    if (message.length <= MAX_MESSAGE_LENGTH) {
      _currentMessage.value = message
    } else {
      _errorMsg.value = "Message too long (max $MAX_MESSAGE_LENGTH characters)"
    }
  }

  open fun sendMessage() {
    val ideaId = _selectedIdea.value?.ideaId
    val messageText = _currentMessage.value.trim()
    val currentUserId = getCurrentUserId()

    if (ideaId == null) {
      _errorMsg.value = "Please select an idea first"
      return
    }

    if (messageText.isEmpty()) {
      return
    }

    if (_isSending.value) {
      return
    }

    if (currentUserId == null) {
      _errorMsg.value = "User not authenticated"
      return
    }

    _isSending.value = true

    viewModelScope.launch(dispatcher) {
      val message =
          Message(
              messageID = IdGenerator.generateMessageId(),
              text = messageText,
              senderId = currentUserId,
              createdAt = Timestamp.now())

      ideasRepository
          .sendMessage(ideaId, message)
          .fold(
              onSuccess = {
                _currentMessage.value = ""
                _isSending.value = false
              },
              onFailure = { error ->
                Log.e(TAG, "Error sending message", error)
                _errorMsg.value = "Error sending message: ${error.message}"
                _isSending.value = false
              })
    }
  }

  open fun clearError() {
    _errorMsg.value = null
  }

  open fun getCurrentUserId(): String? = getCurrentUserId.invoke()
}

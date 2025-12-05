/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
 * @property ideasRepository Repository for ideas data operations (to be implemented).
 * @property getCurrentUserId Function to get the current authenticated user's ID.
 * @property dispatcher Coroutine dispatcher for background operations.
 */
class IdeasViewModel
@JvmOverloads
constructor(
    private val projectRepository: ProjectRepository =
        RepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val ideasRepository: IdeasRepository =
        IdeasRepositoryPlaceholder(), // Placeholder until repository is implemented
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), IdeasViewModelInterface {

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
  private val _hiddenIdeaIds = MutableStateFlow<Set<String>>(emptySet()) // Local hiding
  private val _availableUsersForProject = MutableStateFlow<List<User>>(emptyList())
  private val _isLoadingUsers = MutableStateFlow(false)

  // Flow of available projects
  private val projectsFlow =
      projectRepository.getProjectsForCurrentUser().catch { e ->
        Log.e(TAG, "Error loading projects", e)
        _errorMsg.value = "Error loading projects: ${e.message}"
        emit(emptyList())
      }

  // Flow of ideas for selected project (excluding hidden ones)
  private val ideasFlow =
      _selectedProject.flatMapLatest { project ->
        if (project != null) {
          ideasRepository
              .getIdeasForProject(project.projectId)
              .map { ideas ->
                // Filter out ideas hidden by current user
                val hiddenIds = _hiddenIdeaIds.value
                ideas.filter { idea ->
                  // Show idea if user is participant AND not hidden locally
                  val currentUserId = getCurrentUserId()
                  currentUserId != null &&
                      idea.participantIds.contains(currentUserId) &&
                      !hiddenIds.contains(idea.ideaId)
                }
              }
              .catch { e ->
                Log.e(TAG, "Error loading ideas", e)
                _errorMsg.value = "Error loading ideas: ${e.message}"
                emit(emptyList())
              }
        } else {
          flowOf(emptyList())
        }
      }

  // Flow of messages for selected idea
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
  override val uiState: StateFlow<IdeasUIState> =
      combine(
              projectsFlow,
              _selectedProject,
              ideasFlow,
              _selectedIdea,
              messagesFlow,
              _viewMode,
              _currentMessage,
              _isSending,
              _errorMsg,
              _availableUsersForProject,
              _isLoadingUsers) { args ->
                val projects = args[0] as List<Project>
                val selectedProject = args[1] as Project?
                val ideas = args[2] as List<Idea>
                val selectedIdea = args[3] as Idea?
                val messages = args[4] as List<Message>
                val viewMode = args[5] as IdeasViewMode
                val currentMessage = args[6] as String
                val isSending = args[7] as Boolean
                val errorMsg = args[8] as String?
                val availableUsers = args[9] as List<User>
                val isLoadingUsers = args[10] as Boolean

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
                    errorMsg = errorMsg,
                    availableUsers = availableUsers,
                    isLoadingUsers = isLoadingUsers)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = IdeasUIState(isLoading = true))

  override fun selectProject(project: Project) {
    viewModelScope.launch(dispatcher) {
      _selectedProject.value = project
      _viewMode.value = IdeasViewMode.LIST
      _selectedIdea.value = null
      _currentMessage.value = ""
    }
  }

  override fun selectIdea(idea: Idea) {
    viewModelScope.launch(dispatcher) {
      _selectedIdea.value = idea
      _viewMode.value = IdeasViewMode.CONVERSATION
      _currentMessage.value = ""
    }
  }

  override fun createNewIdea(title: String?, projectId: String, participantIds: List<String>) {
    val currentUserId = getCurrentUserId()

    if (currentUserId == null) {
      _errorMsg.value = "User not authenticated"
      return
    }

    viewModelScope.launch(dispatcher) {
      // Combine creator with selected participants
      val allParticipantIds = (listOf(currentUserId) + participantIds).distinct()

      val newIdea =
          Idea(
              ideaId = IdGenerator.generateIdeaId(),
              projectId = projectId,
              title = title,
              createdBy = currentUserId,
              participantIds = allParticipantIds,
              createdAt = Timestamp.now(),
              lastUpdated = Timestamp.now())

      ideasRepository.createIdea(newIdea).fold(
          onSuccess = {
            // Select the project and the newly created idea
            // Find project from current projects list
            val currentProjects = projectsFlow.first()
            val project = currentProjects.find { it.projectId == projectId }
            if (project != null) {
              _selectedProject.value = project
            }
            _selectedIdea.value = newIdea
            _viewMode.value = IdeasViewMode.CONVERSATION
          },
          onFailure = { error ->
            Log.e(TAG, "Error creating idea", error)
            _errorMsg.value = "Error creating idea: ${error.message}"
          })
    }
  }

  /**
   * Load users for a project (for participant selection).
   * Uses first() to get a snapshot instead of continuous collection.
   */
  fun loadUsersForProject(projectId: String) {
    if (projectId.isBlank()) {
      _availableUsersForProject.value = emptyList()
      return
    }

    viewModelScope.launch(dispatcher) {
      _isLoadingUsers.value = true
      try {
        val members = projectRepository.getMembers(projectId).first()
        if (members.isEmpty()) {
          _availableUsersForProject.value = emptyList()
          _isLoadingUsers.value = false
        } else {
          // Convert members to users
          val userFlows = members.map { member -> userRepository.getUserById(member.userId) }
          val users = combine(userFlows) { userArray -> userArray.toList().filterNotNull() }.first()
          _availableUsersForProject.value = users
          _isLoadingUsers.value = false
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error loading users", e)
        _errorMsg.value = "Error loading users: ${e.message}"
        _isLoadingUsers.value = false
      }
    }
  }

  override fun deleteIdea(ideaId: String) {
    val currentUserId = getCurrentUserId()
    if (currentUserId == null) return

    viewModelScope.launch(dispatcher) {
      // Hide the idea locally (local deletion only)
      val hiddenIds = _hiddenIdeaIds.value.toMutableSet()
      hiddenIds.add(ideaId)
      _hiddenIdeaIds.value = hiddenIds

      // If this was the selected idea, go back to list mode
      if (_selectedIdea.value?.ideaId == ideaId) {
        _selectedIdea.value = null
        _viewMode.value = IdeasViewMode.LIST
        _currentMessage.value = ""
      }

      // Note: We don't actually delete from Firestore, just hide locally
      // The idea will still be visible to other participants
    }
  }

  override fun addParticipantToIdea(ideaId: String, userId: String) {
    val projectId = _selectedProject.value?.projectId
    if (projectId == null) {
      _errorMsg.value = "No project selected"
      return
    }

    viewModelScope.launch(dispatcher) {
      ideasRepository.addParticipant(projectId, ideaId, userId).fold(
          onSuccess = {
            // Update the idea in the list by reloading
            // The flow will automatically update
          },
          onFailure = { error ->
            Log.e(TAG, "Error adding participant", error)
            _errorMsg.value = "Error sharing idea: ${error.message}"
          })
    }
  }

  override fun updateMessage(message: String) {
    if (message.length <= MAX_MESSAGE_LENGTH) {
      _currentMessage.value = message
    } else {
      _errorMsg.value = "Message too long (max $MAX_MESSAGE_LENGTH characters)"
    }
  }

  override fun sendMessage() {
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

      ideasRepository.sendMessage(ideaId, message).fold(
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

  override fun clearError() {
    _errorMsg.value = null
  }

  override fun getCurrentUserId(): String? = getCurrentUserId.invoke()
}

/**
 * Placeholder repository interface for Ideas.
 * This will be replaced with actual Firestore implementation.
 */
interface IdeasRepository {
  fun getIdeasForProject(projectId: String): Flow<List<Idea>>
  suspend fun createIdea(idea: Idea): Result<String>
  suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit>
  fun getMessagesForIdea(ideaId: String): Flow<List<Message>>
  suspend fun sendMessage(ideaId: String, message: Message): Result<Unit>
  suspend fun addParticipant(projectId: String, ideaId: String, userId: String): Result<Unit>
}

/**
 * Placeholder implementation of IdeasRepository.
 * Returns empty data until the real repository is implemented.
 */
private class IdeasRepositoryPlaceholder : IdeasRepository {
  override fun getIdeasForProject(projectId: String): Flow<List<Idea>> = flowOf(emptyList())

  override suspend fun createIdea(idea: Idea): Result<String> =
      Result.failure(Exception("IdeasRepository not yet implemented"))

  override suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit> =
      Result.failure(Exception("IdeasRepository not yet implemented"))

  override fun getMessagesForIdea(ideaId: String): Flow<List<Message>> = flowOf(emptyList())

  override suspend fun sendMessage(ideaId: String, message: Message): Result<Unit> =
      Result.failure(Exception("IdeasRepository not yet implemented"))

  override suspend fun addParticipant(projectId: String, ideaId: String, userId: String): Result<Unit> =
      Result.failure(Exception("IdeasRepository not yet implemented"))
}

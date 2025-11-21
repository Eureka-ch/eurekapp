package ch.eureka.eurekapp.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * UI state for the conversation list screen.
 *
 * @property conversations List of conversations with resolved display data.
 * @property isLoading Whether conversations are being loaded.
 * @property errorMsg Error message to display, or null if no error.
 * @property isConnected Whether the device is connected to the internet.
 */
data class ConversationListState(
    val conversations: List<ConversationDisplayData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val isConnected: Boolean = true
)

/**
 * Display data for a conversation item in the list.
 *
 * @property conversation The underlying conversation data.
 * @property otherMemberName Display name of the other member in the conversation.
 * @property otherMemberPhotoUrl Photo URL of the other member (from Google profile).
 * @property projectName Name of the project this conversation belongs to.
 */
data class ConversationDisplayData(
    val conversation: Conversation,
    val otherMemberName: String,
    val otherMemberPhotoUrl: String,
    val projectName: String
)

/**
 * ViewModel for the conversation list screen.
 *
 * Loads all conversations for the current user and resolves member names and project names for
 * display. Uses caching to minimize redundant Firestore queries when resolving user and project
 * data.
 *
 * @property conversationRepository Repository for conversation data operations.
 * @property userRepository Repository for user data lookups.
 * @property projectRepository Repository for project data lookups.
 * @property getCurrentUserId Function to get the current authenticated user's ID.
 * @property connectivityObserver Observer for network connectivity status.
 */
class ConversationListViewModel(
    private val conversationRepository: ConversationRepository =
        FirestoreRepositoriesProvider.conversationRepository,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    private val projectRepository: ProjectRepository =
        FirestoreRepositoriesProvider.projectRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  // Internal mutable state for the UI
  private val _uiState = MutableStateFlow(ConversationListState())

  // Public read-only state exposed to the UI layer
  val uiState: StateFlow<ConversationListState> = _uiState

  // Network connectivity state, starts optimistically as connected
  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  // Cache for resolved users and projects to avoid redundant Firestore queries.
  // This improves performance when displaying multiple conversations with overlapping members.
  private val userCache = mutableMapOf<String, User>()
  private val projectCache = mutableMapOf<String, Project>()

  init {
    // Observe connectivity changes and update UI state accordingly
    viewModelScope.launch {
      _isConnected.collect { isConnected -> _uiState.update { it.copy(isConnected = isConnected) } }
    }
    // Load conversations on ViewModel creation
    loadConversations()
  }

  /**
   * Load all conversations for the current user.
   *
   * Fetches conversations from Firestore and resolves display data (member names, project names)
   * for each conversation. Results are emitted to [uiState] as they become available.
   */
  fun loadConversations() {
    viewModelScope.launch {
      conversationRepository
          .getConversationsForCurrentUser()
          .onStart {
            // Show loading indicator while fetching
            _uiState.update { it.copy(isLoading = true) }
          }
          .catch { e ->
            // Handle errors gracefully and display to user
            _uiState.update { it.copy(isLoading = false, errorMsg = e.message) }
          }
          .collect { conversations ->
            // Transform raw conversations into display-ready data
            val displayDataList =
                conversations.map { conversation -> resolveConversationDisplayData(conversation) }
            // Update UI with resolved conversations
            _uiState.update {
              it.copy(isLoading = false, conversations = displayDataList, errorMsg = null)
            }
          }
    }
  }

  /**
   * Resolve display data for a conversation by fetching user and project information.
   *
   * Uses caching to avoid redundant lookups. The other member (not the current user) is identified
   * and their display name is fetched from the user repository.
   *
   * @param conversation The conversation to resolve display data for.
   * @return Display data containing the other member's name and project name.
   */
  private suspend fun resolveConversationDisplayData(
      conversation: Conversation
  ): ConversationDisplayData {
    val currentUserId = getCurrentUserId() ?: ""

    // Find the other participant in the conversation (not the current user)
    val otherUserId = conversation.memberIds.firstOrNull { it != currentUserId } ?: ""

    // Try to get user from cache first, otherwise fetch from Firestore and cache it
    val otherUser =
        userCache[otherUserId]
            ?: run {
              val user = userRepository.getUserById(otherUserId).first()
              user?.let { userCache[otherUserId] = it }
              user
            }

    // Try to get project from cache first, otherwise fetch from Firestore and cache it
    val project =
        projectCache[conversation.projectId]
            ?: run {
              val proj = projectRepository.getProjectById(conversation.projectId).first()
              proj?.let { projectCache[conversation.projectId] = it }
              proj
            }

    // Return display data with fallback values for missing data
    return ConversationDisplayData(
        conversation = conversation,
        otherMemberName = otherUser?.displayName ?: "Unknown User",
        otherMemberPhotoUrl = otherUser?.photoUrl ?: "",
        projectName = project?.name ?: "Unknown Project")
  }

  /** Clear the error message. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }
}

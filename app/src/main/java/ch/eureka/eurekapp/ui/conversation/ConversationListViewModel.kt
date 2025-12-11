package ch.eureka.eurekapp.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
 * @property otherMembers Display name of the other members in the conversation.
 * @property otherMembersPhotoUrl Photo URL of the other members (from Google profile).
 * @property projectName Name of the project this conversation belongs to.
 * @property lastMessagePreview Preview of the last message in the conversation.
 * @property lastMessageTime Formatted time of the last message.
 * @property hasUnread Whether there are unread messages in this conversation.
 */
data class ConversationDisplayData(
    val conversation: Conversation,
    val otherMembers: List<String>,
    val otherMembersPhotoUrl: List<String>,
    val projectName: String,
    val lastMessagePreview: String? = null,
    val lastMessageTime: String? = null,
    val hasUnread: Boolean = false
)

/**
 * ViewModel for the conversation list screen.
 *
 * Loads all conversations for the current user and resolves member names and project names for
 * display.
 *
 * @property conversationRepository Repository for conversation data operations.
 * @property userRepository Repository for user data lookups.
 * @property projectRepository Repository for project data lookups.
 * @property getCurrentUserId Function to get the current authenticated user's ID.
 * @property connectivityObserver Observer for network connectivity status.
 */
open class ConversationListViewModel(
    private val conversationRepository: ConversationRepository =
        RepositoriesProvider.conversationRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  private val _conversationsState =
      MutableStateFlow<ConversationsDataState>(ConversationsDataState.Loading)

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  open val uiState: StateFlow<ConversationListState> =
      combine(_conversationsState, _isConnected) { conversationsState, isConnected ->
            when (conversationsState) {
              is ConversationsDataState.Loading ->
                  ConversationListState(isLoading = true, isConnected = isConnected)
              is ConversationsDataState.Success ->
                  ConversationListState(
                      conversations = conversationsState.conversations,
                      isLoading = false,
                      isConnected = isConnected)
              is ConversationsDataState.Error ->
                  ConversationListState(
                      isLoading = false,
                      errorMsg = conversationsState.message,
                      isConnected = isConnected)
            }
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, ConversationListState())

  init {
    loadConversations()
  }

  /** Internal sealed class to represent the conversations data state */
  private sealed class ConversationsDataState {
    object Loading : ConversationsDataState()

    data class Success(val conversations: List<ConversationDisplayData>) : ConversationsDataState()

    data class Error(val message: String) : ConversationsDataState()
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
          .onStart { _conversationsState.value = ConversationsDataState.Loading }
          .catch { e ->
            _conversationsState.value = ConversationsDataState.Error(e.message ?: "Unknown error")
          }
          .collect { conversations ->
            val displayDataList =
                conversations.map { conversation -> resolveConversationDisplayData(conversation) }
            _conversationsState.value = ConversationsDataState.Success(displayDataList)
          }
    }
  }

  /**
   * Resolve display data for a conversation by fetching user and project information.
   *
   * The other member (not the current user) is identified and their display name is fetched from
   * the user repository. Firestore's built-in caching handles performance optimization for repeated
   * queries.
   *
   * @param conversation The conversation to resolve display data for.
   * @return Display data containing the other member's name and project name.
   */
  private suspend fun resolveConversationDisplayData(
      conversation: Conversation
  ): ConversationDisplayData {
    val currentUserId = getCurrentUserId() ?: ""
    val otherUserIds = conversation.memberIds.filter { it != currentUserId }
    val otherUsers = if (otherUserIds.isNotEmpty()) getUserIds(otherUserIds).first() else listOf()
    val project = projectRepository.getProjectById(conversation.projectId).first()

    // Calculate unread status (only unread if someone else sent the last message)
    val lastReadAt = conversation.lastReadAt[currentUserId]
    val hasUnread =
        conversation.lastMessageAt != null &&
            conversation.lastMessageSenderId != currentUserId &&
            (lastReadAt == null || conversation.lastMessageAt > lastReadAt)

    // Format last message time
    val lastMessageTime =
        conversation.lastMessageAt?.let { timestamp ->
          ch.eureka.eurekapp.utils.Formatters.formatRelativeTime(timestamp.toDate())
        }

    return ConversationDisplayData(
        conversation = conversation,
        otherMembers = otherUsers.map { user -> user.displayName },
        otherMembersPhotoUrl = otherUsers.map { user -> user.photoUrl },
        projectName = project?.name ?: "Unknown Project",
        lastMessagePreview = conversation.lastMessagePreview,
        lastMessageTime = lastMessageTime,
        hasUnread = hasUnread)
  }

  /** Clear the error message. */
  fun clearErrorMsg() {
    _conversationsState.value =
        when (val current = _conversationsState.value) {
          is ConversationsDataState.Error -> ConversationsDataState.Success(emptyList())
          else -> current
        }
  }

  private fun getUserIds(userIds: List<String>): Flow<List<User>> {
    return combine(userIds.map { id -> userRepository.getUserById(id) }) { list ->
      list.filterNotNull().toList()
    }
  }
}

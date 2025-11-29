package ch.eureka.eurekapp.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * UI state for the conversation detail screen.
 *
 * @property conversation The conversation data.
 * @property messages List of messages in the conversation.
 * @property otherMemberName Display name of the other member.
 * @property projectName Name of the project this conversation belongs to.
 * @property currentMessage The message being composed.
 * @property isLoading Whether data is loading.
 * @property isSending Whether a message is being sent.
 * @property errorMsg Error message to display.
 * @property isConnected Whether the device is connected.
 */
data class ConversationDetailState(
    val conversation: Conversation? = null,
    val messages: List<ConversationMessage> = emptyList(),
    val otherMemberName: String = "",
    val projectName: String = "",
    val currentMessage: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMsg: String? = null,
    val isConnected: Boolean = true
)

/**
 * ViewModel for the conversation detail screen.
 *
 * Manages loading messages, sending new messages, and marking messages as read.
 */
open class ConversationDetailViewModel(
    private val conversationId: String,
    private val conversationRepository: ConversationRepository =
        RepositoriesProvider.conversationRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  private val _uiState = MutableStateFlow(ConversationDetailState())
  open val uiState: StateFlow<ConversationDetailState> = _uiState

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  open val currentUserId: String?
    get() = getCurrentUserId()

  init {
    loadConversation()
    loadMessages()
    observeConnectivity()
  }

  private fun observeConnectivity() {
    viewModelScope.launch {
      _isConnected.collect { connected ->
        _uiState.value = _uiState.value.copy(isConnected = connected)
      }
    }
  }

  private fun loadConversation() {
    viewModelScope.launch {
      conversationRepository
          .getConversationById(conversationId)
          .catch { e -> _uiState.value = _uiState.value.copy(errorMsg = e.message) }
          .collect { conversation ->
            _uiState.value = _uiState.value.copy(conversation = conversation, isLoading = false)
            if (conversation != null) {
              resolveDisplayData(conversation)
            }
          }
    }
  }

  private fun loadMessages() {
    viewModelScope.launch {
      conversationRepository
          .getMessages(conversationId)
          .catch { e -> _uiState.value = _uiState.value.copy(errorMsg = e.message) }
          .collect { messages -> _uiState.value = _uiState.value.copy(messages = messages) }
    }
  }

  private suspend fun resolveDisplayData(conversation: Conversation) {
    val currentUserId = getCurrentUserId() ?: ""
    val otherUserId = conversation.memberIds.firstOrNull { it != currentUserId } ?: ""
    val otherUser = userRepository.getUserById(otherUserId).first()
    val project = projectRepository.getProjectById(conversation.projectId).first()

    _uiState.value =
        _uiState.value.copy(
            otherMemberName = otherUser?.displayName ?: "Unknown User",
            projectName = project?.name ?: "Unknown Project")
  }

  fun updateMessage(text: String) {
    _uiState.value = _uiState.value.copy(currentMessage = text)
  }

  fun sendMessage() {
    val text = _uiState.value.currentMessage.trim()
    if (text.isEmpty()) return
    if (text.length > 5000) {
      _uiState.value = _uiState.value.copy(errorMsg = "Message too long (max 5000 characters)")
      return
    }

    _uiState.value = _uiState.value.copy(isSending = true)

    viewModelScope.launch {
      conversationRepository
          .sendMessage(conversationId, text)
          .fold(
              onSuccess = {
                _uiState.value = _uiState.value.copy(currentMessage = "", isSending = false)
              },
              onFailure = { error ->
                _uiState.value =
                    _uiState.value.copy(isSending = false, errorMsg = "Error: ${error.message}")
              })
    }
  }

  fun markAsRead() {
    viewModelScope.launch { conversationRepository.markMessagesAsRead(conversationId) }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
}

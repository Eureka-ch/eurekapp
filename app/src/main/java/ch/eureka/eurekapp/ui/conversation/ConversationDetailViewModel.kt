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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

private data class DisplayData(val otherMemberName: String, val projectName: String)

/**
 * ViewModel for the conversation detail screen.
 *
 * Manages loading messages, sending new messages, and marking messages as read.
 */
@OptIn(ExperimentalCoroutinesApi::class)
open class ConversationDetailViewModel(
    private val conversationId: String,
    private val conversationRepository: ConversationRepository =
        RepositoriesProvider.conversationRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)

  private val conversationFlow =
      conversationRepository.getConversationById(conversationId).catch { e ->
        _errorMsg.value = e.message
      }

  private val messagesFlow =
      conversationRepository.getMessages(conversationId).catch { e -> _errorMsg.value = e.message }

  private val isConnectedFlow =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val displayDataFlow =
      conversationFlow.flatMapLatest { conversation ->
        if (conversation == null) {
          flowOf(DisplayData("", ""))
        } else {
          val currentUserId = getCurrentUserId() ?: ""
          val otherUserId = conversation.memberIds.firstOrNull { it != currentUserId } ?: ""
          combine(
              userRepository.getUserById(otherUserId),
              projectRepository.getProjectById(conversation.projectId)) { user, project ->
                DisplayData(
                    otherMemberName = user?.displayName ?: "Unknown User",
                    projectName = project?.name ?: "Unknown Project")
              }
        }
      }

  private val inputStateFlow =
      combine(_currentMessage, _isSending, _errorMsg) { currentMessage, isSending, errorMsg ->
        Triple(currentMessage, isSending, errorMsg)
      }

  open val uiState: StateFlow<ConversationDetailState> =
      combine(conversationFlow, messagesFlow, displayDataFlow, inputStateFlow, isConnectedFlow) {
              conversation,
              messages,
              displayData,
              inputState,
              isConnected ->
            ConversationDetailState(
                conversation = conversation,
                messages = messages,
                otherMemberName = displayData.otherMemberName,
                projectName = displayData.projectName,
                currentMessage = inputState.first,
                isLoading = conversation == null,
                isSending = inputState.second,
                errorMsg = inputState.third,
                isConnected = isConnected)
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ConversationDetailState())

  open val currentUserId: String?
    get() = getCurrentUserId()

  fun updateMessage(text: String) {
    _currentMessage.value = text
  }

  fun sendMessage() {
    val text = _currentMessage.value.trim()
    if (text.isEmpty()) return
    if (text.length > 5000) {
      _errorMsg.value = "Message too long (max 5000 characters)"
      return
    }

    _isSending.value = true

    viewModelScope.launch {
      conversationRepository
          .sendMessage(conversationId, text)
          .fold(
              onSuccess = {
                _currentMessage.value = ""
                _isSending.value = false
              },
              onFailure = { error ->
                _isSending.value = false
                _errorMsg.value = "Error: ${error.message}"
              })
    }
  }

  fun markAsRead() {
    viewModelScope.launch { conversationRepository.markMessagesAsRead(conversationId) }
  }

  fun clearError() {
    _errorMsg.value = null
  }
}

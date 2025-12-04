package ch.eureka.eurekapp.ui.conversation

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
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
Co-author: Grok
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
 * @property isUploadingFile Whether a file is currently being uploaded.
 * @property selectedFileUri The URI of the selected file for sending.
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
    val isConnected: Boolean = true,
    val isUploadingFile: Boolean = false,
    val selectedFileUri: Uri? = null
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
    private val fileStorageRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  companion object {
    const val MAX_MESSAGE_LENGTH = 5000
    const val MAX_FILE_SIZE_MB = 100L
    const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024L * 1024L
    const val TIMESTAMP_LENGTH = 13
    const val SUBSCRIPTION_TIMEOUT = 5000L
  }

  private data class Quintuple<A, B, C, D, E>(
      val first: A,
      val second: B,
      val third: C,
      val fourth: D,
      val fifth: E
  )

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _isUploadingFile = MutableStateFlow(false)
  private val _selectedFileUri = MutableStateFlow<Uri?>(null)
  private val _snackbarMessage = MutableStateFlow<String?>(null)

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
      combine(_currentMessage, _isSending, _errorMsg, _isUploadingFile, _selectedFileUri) {
          currentMessage,
          isSending,
          errorMsg,
          isUploadingFile,
          selectedFileUri ->
        Quintuple(currentMessage, isSending, errorMsg, isUploadingFile, selectedFileUri)
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
                isConnected = isConnected,
                isUploadingFile = inputState.fourth,
                selectedFileUri = inputState.fifth)
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
              initialValue = ConversationDetailState())

  open val currentUserId: String?
    get() = getCurrentUserId()

  val snackbarMessage: StateFlow<String?> =
      _snackbarMessage.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
          initialValue = null)

  fun updateMessage(text: String) {
    _currentMessage.value = text
  }

  fun setSelectedFile(uri: Uri?) {
    _selectedFileUri.value = uri
  }

  fun clearSelectedFile() {
    _selectedFileUri.value = null
  }

  fun sendMessage() {
    val text = _currentMessage.value.trim()
    if (text.isEmpty()) return
    if (text.length > MAX_MESSAGE_LENGTH) {
      _errorMsg.value = "Message too long (max $MAX_MESSAGE_LENGTH characters)"
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

  fun sendFileMessage(fileUri: Uri, context: Context) {
    val text = _currentMessage.value.trim()
    val messageText = text.ifEmpty { "File attached" }

    _isSending.value = true
    _isUploadingFile.value = true

    viewModelScope.launch {
      // Get original filename with extension from ContentResolver
      val originalFilename =
          context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
              cursor.getString(nameIndex) ?: "file_${System.currentTimeMillis()}"
            } else {
              "file_${System.currentTimeMillis()}"
            }
          } ?: "file_${System.currentTimeMillis()}"

      // Open file descriptor once for size check and upload
      val fileDescriptor = context.contentResolver.openFileDescriptor(fileUri, "r")
      if (fileDescriptor == null) {
        _errorMsg.value = "Cannot access file"
        _isSending.value = false
        _isUploadingFile.value = false
        return@launch
      }

      val fileSize = fileDescriptor.statSize
      val maxSize = MAX_FILE_SIZE_BYTES
      if (fileSize > maxSize) {
        fileDescriptor.close()
        _isSending.value = false
        _isUploadingFile.value = false
        _errorMsg.value = "File too large (max $MAX_FILE_SIZE_MB MB)"
        return@launch
      }

      // Split filename into name and extension
      val lastDotIndex = originalFilename.lastIndexOf('.')
      val (baseName, extension) =
          if (lastDotIndex > 0) {
            val name = originalFilename.take(lastDotIndex)
            val ext = originalFilename.drop(lastDotIndex) // includes the dot
            name to ext
          } else {
            originalFilename to ""
          }

      // Sanitize base name (remove special characters, keep only safe chars)
      val sanitizedBaseName = baseName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

      // Create filename with timestamp before extension
      val filename = "${sanitizedBaseName}_${System.currentTimeMillis()}${extension}"
      val storagePath = StoragePaths.conversationFilePath(conversationId, filename)
      fileStorageRepository
          .uploadFile(storagePath, fileDescriptor)
          .fold(
              onSuccess = { downloadUrl ->
                fileDescriptor.close()
                _isUploadingFile.value = false
                conversationRepository
                    .sendFileMessage(conversationId, messageText, downloadUrl)
                    .fold(
                        onSuccess = {
                          _currentMessage.value = ""
                          _selectedFileUri.value = null
                          _isSending.value = false
                        },
                        onFailure = { error ->
                          _isSending.value = false
                          _errorMsg.value = "Error sending file: ${error.message}"
                        })
              },
              onFailure = { error ->
                fileDescriptor.close()
                _isUploadingFile.value = false
                _isSending.value = false
                _errorMsg.value = "Error uploading file: ${error.message}"
              })
    }
  }

  fun openUrl(url: String, context: Context) {
    val uri =
        try {
          url.toUri()
        } catch (e: Exception) {
          null
        }

    if (uri == null || uri.scheme == null || uri.scheme !in listOf("http", "https")) {
      _snackbarMessage.value = "Invalid URL"
      return
    }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
      context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      _snackbarMessage.value = "Cannot open file"
    }
  }

  fun downloadFile(url: String, context: Context) {
    // Extract filename from Firebase Storage URL by decoding the path
    val fileName =
        url.substringAfter("/o/")
            .substringBefore("?")
            .let { java.net.URLDecoder.decode(it, "UTF-8") }
            .substringAfterLast("/")
    // Remove timestamp from display name (format: name_timestamp.ext)
    val displayName = fileName.replace(Regex("_\\d{${TIMESTAMP_LENGTH}}(?=\\.[^.]+$|$)"), "")

    val request = DownloadManager.Request(url.toUri())
    request.setTitle(displayName)
    request.setDescription("Downloading file from chat")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: run {
              _snackbarMessage.value = "Download manager not available"
              return
            }
    downloadManager.enqueue(request)

    // Show feedback to user
    _snackbarMessage.value = "Download started"
  }

  fun markAsRead() {
    viewModelScope.launch { conversationRepository.markMessagesAsRead(conversationId) }
  }

  fun clearError() {
    _errorMsg.value = null
  }

  fun clearSnackbarMessage() {
    _snackbarMessage.value = null
  }
}

package ch.eureka.eurekapp.ui.conversation

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
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
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
 * @property otherMemberNames Display name of the other member.
 * @property projectName Name of the project this conversation belongs to.
 * @property currentMessage The message being composed.
 * @property isLoading Whether data is loading.
 * @property isSending Whether a message is being sent.
 * @property errorMsg Error message to display.
 * @property isConnected Whether the device is connected.
 * @property isUploadingFile Whether a file is currently being uploaded.
 * @property selectedFileUri The URI of the selected file for sending.
 * @property editingMessageId The ID of the message being edited, or null if not editing.
 * @property selectedMessageId The ID of the message selected for actions (edit/delete menu).
 * @property showDeleteConfirmation Whether to show the delete confirmation dialog.
 */
data class ConversationDetailState(
    val conversation: Conversation? = null,
    val messages: List<ConversationMessage> = emptyList(),
    val otherMemberNames: List<String> = emptyList(),
    val projectName: String = "",
    val currentMessage: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMsg: String? = null,
    val isConnected: Boolean = true,
    val isUploadingFile: Boolean = false,
    val selectedFileUri: Uri? = null,
    val editingMessageId: String? = null,
    val selectedMessageId: String? = null,
    val showDeleteConfirmation: Boolean = false
) {
  /** Whether the user is currently in edit mode. */
  val isEditing: Boolean
    get() = editingMessageId != null
}

private data class DisplayData(val otherMemberNames: List<String>, val projectName: String)

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

  private data class InputState(
      val currentMessage: String,
      val isSending: Boolean,
      val errorMsg: String?,
      val isUploadingFile: Boolean,
      val selectedFileUri: Uri?,
      val editingMessageId: String?,
      val selectedMessageId: String?,
      val showDeleteConfirmation: Boolean
  )

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _isUploadingFile = MutableStateFlow(false)
  private val _selectedFileUri = MutableStateFlow<Uri?>(null)
  private val _snackbarMessage = MutableStateFlow<String?>(null)
  private val _editingMessageId = MutableStateFlow<String?>(null)
  private val _selectedMessageId = MutableStateFlow<String?>(null)
  private val _showDeleteConfirmation = MutableStateFlow(false)
  private val _pendingDeleteFileUrl = MutableStateFlow<String?>(null)

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
          flowOf(DisplayData(emptyList(), ""))
        } else {
          val currentUserId = getCurrentUserId() ?: ""
          val otherUserIds = conversation.memberIds.filter { id -> id != currentUserId }

          val projectFlow: Flow<Project?> = projectRepository.getProjectById(conversation.projectId)
          val combinedUsersFlow: Flow<List<User>> = combine(otherUserIds.map { id ->
              userRepository.getUserById(id) }) { usersArray ->
            usersArray.filterNotNull().toList()
          }

            combine(
              combinedUsersFlow,
              projectFlow) { users, project ->
                DisplayData(
                    otherMemberNames = users.map { user -> user.displayName },
                    projectName = project?.name ?: "Unknown Project")
              }
        }
      }

  @Suppress("UNCHECKED_CAST")
  private val inputStateFlow =
      combine(
          _currentMessage,
          _isSending,
          _errorMsg,
          _isUploadingFile,
          _selectedFileUri,
          _editingMessageId,
          _selectedMessageId,
          _showDeleteConfirmation) { args ->
            InputState(
                currentMessage = args[0] as String,
                isSending = args[1] as Boolean,
                errorMsg = args[2] as String?,
                isUploadingFile = args[3] as Boolean,
                selectedFileUri = args[4] as Uri?,
                editingMessageId = args[5] as String?,
                selectedMessageId = args[6] as String?,
                showDeleteConfirmation = args[7] as Boolean)
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
                otherMemberNames = displayData.otherMemberNames,
                projectName = displayData.projectName,
                currentMessage = inputState.currentMessage,
                isLoading = conversation == null,
                isSending = inputState.isSending,
                errorMsg = inputState.errorMsg,
                isConnected = isConnected,
                isUploadingFile = inputState.isUploadingFile,
                selectedFileUri = inputState.selectedFileUri,
                editingMessageId = inputState.editingMessageId,
                selectedMessageId = inputState.selectedMessageId,
                showDeleteConfirmation = inputState.showDeleteConfirmation)
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

  // --- Edit/Delete Message Methods ---

  /** Select a message for showing the action menu. Pass null to clear selection. */
  fun selectMessage(messageId: String?) {
    _selectedMessageId.value = messageId
  }

  /** Clear the selected message (close action menu). */
  fun clearMessageSelection() {
    _selectedMessageId.value = null
  }

  /** Enter edit mode for a message. Populates the input field with the message text. */
  fun startEditing(message: ConversationMessage) {
    _selectedMessageId.value = null
    _editingMessageId.value = message.messageId
    _currentMessage.value = message.text
  }

  /** Cancel the current edit operation and clear the input field. */
  fun cancelEditing() {
    _editingMessageId.value = null
    _currentMessage.value = ""
  }

  /** Save the edited message. Called when user submits while in edit mode. */
  fun saveEditedMessage() {
    val messageId = _editingMessageId.value ?: return
    val newText = _currentMessage.value.trim()

    if (newText.isEmpty()) {
      _errorMsg.value = "Message cannot be empty"
      return
    }
    if (newText.length > MAX_MESSAGE_LENGTH) {
      _errorMsg.value = "Message too long (max $MAX_MESSAGE_LENGTH characters)"
      return
    }

    _isSending.value = true

    viewModelScope.launch {
      conversationRepository
          .updateMessage(conversationId, messageId, newText)
          .fold(
              onSuccess = {
                _currentMessage.value = ""
                _editingMessageId.value = null
                _isSending.value = false
                _snackbarMessage.value = "Message updated"
              },
              onFailure = { error ->
                _isSending.value = false
                _errorMsg.value = "Error: ${error.message}"
              })
    }
  }

  /** Request to delete a message. Shows the confirmation dialog. */
  fun requestDeleteMessage(messageId: String, fileUrl: String? = null) {
    _selectedMessageId.value = messageId
    _pendingDeleteFileUrl.value = fileUrl
    _showDeleteConfirmation.value = true
  }

  /** Confirm deletion of the selected message. Also deletes attachment from storage if present. */
  fun confirmDeleteMessage() {
    val messageId = _selectedMessageId.value ?: return
    val fileUrl = _pendingDeleteFileUrl.value
    _showDeleteConfirmation.value = false
    _selectedMessageId.value = null
    _pendingDeleteFileUrl.value = null

    viewModelScope.launch {
      // Delete the file from storage if the message has an attachment
      var fileDeleteFailed = false
      if (!fileUrl.isNullOrEmpty()) {
        fileStorageRepository.deleteFile(fileUrl).onFailure { fileDeleteFailed = true }
      }

      conversationRepository
          .deleteMessage(conversationId, messageId)
          .fold(
              onSuccess = {
                _snackbarMessage.value =
                    if (fileDeleteFailed) "Message deleted, but could not delete attachment file"
                    else "Message deleted"
              },
              onFailure = { error -> _errorMsg.value = "Error: ${error.message}" })
    }
  }

  /** Cancel the delete confirmation dialog. */
  fun cancelDeleteMessage() {
    _showDeleteConfirmation.value = false
    _selectedMessageId.value = null
    _pendingDeleteFileUrl.value = null
  }

  /** Remove the attachment from a message and delete the file from storage. */
  fun removeAttachment(messageId: String, fileUrl: String) {
    _selectedMessageId.value = null

    viewModelScope.launch {
      // First remove the attachment reference from Firestore, then delete the file
      // This order ensures we don't have broken references if file deletion fails
      conversationRepository
          .removeAttachment(conversationId, messageId)
          .fold(
              onSuccess = {
                // Then delete the file from storage (best effort)
                fileStorageRepository.deleteFile(fileUrl).onFailure {
                  _snackbarMessage.value = "Attachment removed, but file deletion failed"
                  return@fold
                }
                _snackbarMessage.value = "Attachment removed"
              },
              onFailure = { error -> _errorMsg.value = "Error: ${error.message}" })
    }
  }

    fun getUser(userId: String): Flow<User?>{
        return userRepository.getUserById(userId)
    }
}

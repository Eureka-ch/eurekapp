package ch.eureka.eurekapp.ui.conversation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.DeleteConfirmationDialog
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.MessageActionMenu
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.components.MessageBubbleFileAttachment
import ch.eureka.eurekapp.ui.components.MessageBubbleInteractions
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

object ConversationDetailScreenTestTags {
  const val SCREEN = "conversationDetailScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val MESSAGES_LIST = "messagesList"
  const val BACK_BUTTON = "backButton"
  const val SELECTED_FILE_TEXT = "selectedFileText"
  const val UPLOADING_TEXT = "uploadingText"
  const val EDITING_TOP_BAR = "editingTopBar"
  const val CANCEL_EDIT_BUTTON = "cancelEditButton"
}

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

/** Callbacks for message-level interactions in the conversation. */
data class MessageCallbacks(
    val onDownloadFile: (String, Context) -> Unit,
    val onOpenUrl: (String, Context) -> Unit,
    val onSelectMessage: (String) -> Unit,
    val onClearSelection: () -> Unit,
    val onStartEditing: (ConversationMessage) -> Unit,
    val onRequestDelete: (String, String?) -> Unit,
    val onRemoveAttachment: (String, String) -> Unit
)

/**
 * Screen for displaying and composing messages in a conversation.
 *
 * Shows messages between two users in a chat-like interface with the ability to send new messages.
 *
 * @param conversationId The ID of the conversation to display.
 * @param onNavigateBack Callback when the back button is pressed.
 * @param viewModel The ViewModel managing conversation state.
 * @param modifier Optional modifier for the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationDetailViewModel =
        remember(conversationId) { ConversationDetailViewModel(conversationId) }
) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarMessage by viewModel.snackbarMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()
  val context = LocalContext.current

  val filePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri -> viewModel.setSelectedFile(uri) })

  // Handle back button to exit edit mode
  BackHandler(enabled = uiState.isEditing) { viewModel.cancelEditing() }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbarMessage()
    }
  }

  LaunchedEffect(uiState.messages.size) {
    if (uiState.messages.isNotEmpty() && !uiState.isLoading) {
      listState.animateScrollToItem(0)
    }
  }

  LaunchedEffect(Unit) { viewModel.markAsRead() }

  // Show delete confirmation dialog
  if (uiState.showDeleteConfirmation) {
    DeleteConfirmationDialog(
        onConfirm = viewModel::confirmDeleteMessage, onDismiss = viewModel::cancelDeleteMessage)
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(ConversationDetailScreenTestTags.SCREEN),
      topBar = {
        if (uiState.isEditing) {
          // Contextual top bar for editing mode
          TopAppBar(
              title = { Text("Editing Message") },
              modifier = Modifier.testTag(ConversationDetailScreenTestTags.EDITING_TOP_BAR),
              colors =
                  TopAppBarDefaults.topAppBarColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
              navigationIcon = {
                IconButton(
                    onClick = viewModel::cancelEditing,
                    modifier =
                        Modifier.testTag(ConversationDetailScreenTestTags.CANCEL_EDIT_BUTTON)) {
                      Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                    }
              })
        } else {
          // Standard top bar
          EurekaTopBar(
              title = uiState.otherMemberName.ifEmpty { "Chat" },
              navigationIcon = {
                BackButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag(ConversationDetailScreenTestTags.BACK_BUTTON))
              },
              actions = {
                if (uiState.projectName.isNotEmpty()) {
                  Text(
                      text = uiState.projectName,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                }
              })
        }
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        ConversationBottomBar(
            uiState = uiState,
            context = context,
            onClearSelectedFile = viewModel::clearSelectedFile,
            onAttachFile = { filePickerLauncher.launch("*/*") },
            onMessageChange = viewModel::updateMessage,
            onSend = {
              if (uiState.isEditing) {
                viewModel.saveEditedMessage()
              } else {
                uiState.selectedFileUri?.let { uri -> viewModel.sendFileMessage(uri, context) }
                    ?: viewModel.sendMessage()
              }
            })
      }) { paddingValues ->
        val messageCallbacks =
            MessageCallbacks(
                onDownloadFile = viewModel::downloadFile,
                onOpenUrl = viewModel::openUrl,
                onSelectMessage = viewModel::selectMessage,
                onClearSelection = viewModel::clearMessageSelection,
                onStartEditing = viewModel::startEditing,
                onRequestDelete = viewModel::requestDeleteMessage,
                onRemoveAttachment = viewModel::removeAttachment)

        ConversationContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            listState = listState,
            currentUserId = viewModel.currentUserId ?: "",
            context = context,
            callbacks = messageCallbacks)
      }
}

@Composable
private fun ConversationBottomBar(
    uiState: ConversationDetailState,
    context: Context,
    onClearSelectedFile: () -> Unit,
    onAttachFile: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    SelectedFileIndicator(
        selectedFileUri = uiState.selectedFileUri,
        context = context,
        onClearSelectedFile = onClearSelectedFile)

    if (uiState.isUploadingFile) {
      UploadingIndicator()
    }

    MessageInputRow(
        uiState = uiState,
        onAttachFile = onAttachFile,
        onMessageChange = onMessageChange,
        onSend = onSend)
  }
}

@Composable
private fun SelectedFileIndicator(
    selectedFileUri: Uri?,
    context: Context,
    onClearSelectedFile: () -> Unit
) {
  selectedFileUri?.let { uri ->
    val selectedFileName =
        remember(uri) {
          context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
          } ?: "Unknown file"
        }
    Row(
        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "Selected file: $selectedFileName",
              style = MaterialTheme.typography.bodySmall,
              modifier =
                  Modifier.weight(1f).testTag(ConversationDetailScreenTestTags.SELECTED_FILE_TEXT))
          IconButton(onClick = onClearSelectedFile) {
            Icon(Icons.Default.Close, contentDescription = "Remove selected file")
          }
        }
  }
}

@Composable
private fun UploadingIndicator() {
  Row(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
    CircularProgressIndicator(modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(Spacing.sm))
    Text(
        "Uploading file...",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(ConversationDetailScreenTestTags.UPLOADING_TEXT))
  }
}

@Composable
private fun MessageInputRow(
    uiState: ConversationDetailState,
    onAttachFile: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
  Row {
    if (!uiState.isEditing) {
      IconButton(onClick = onAttachFile) {
        Icon(Icons.Default.AttachFile, contentDescription = "Attach file")
      }
    }
    MessageInputField(
        message = uiState.currentMessage,
        onMessageChange = onMessageChange,
        onSend = onSend,
        isSending = uiState.isSending,
        placeholder = if (uiState.isEditing) "Edit your message..." else "Write a message...",
        canSend =
            uiState.currentMessage.isNotBlank() ||
                (!uiState.isEditing && uiState.selectedFileUri != null))
  }
}

@Composable
private fun ConversationContent(
    modifier: Modifier = Modifier,
    uiState: ConversationDetailState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentUserId: String,
    context: Context,
    callbacks: MessageCallbacks
) {
  Box(modifier = modifier.fillMaxSize()) {
    when {
      uiState.isLoading -> {
        CircularProgressIndicator(
            modifier =
                Modifier.align(Alignment.Center)
                    .testTag(ConversationDetailScreenTestTags.LOADING_INDICATOR))
      }
      uiState.messages.isEmpty() -> {
        Text(
            text = "No messages yet. Start the conversation!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(Spacing.lg)
                    .testTag(ConversationDetailScreenTestTags.EMPTY_STATE))
      }
      else -> {
        MessagesList(
            messages = uiState.messages,
            selectedMessageId = uiState.selectedMessageId,
            listState = listState,
            currentUserId = currentUserId,
            context = context,
            callbacks = callbacks)
      }
    }
  }
}

@Composable
private fun MessagesList(
    messages: List<ConversationMessage>,
    selectedMessageId: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentUserId: String,
    context: Context,
    callbacks: MessageCallbacks
) {
  LazyColumn(
      state = listState,
      modifier =
          Modifier.fillMaxSize()
              .padding(horizontal = Spacing.md)
              .testTag(ConversationDetailScreenTestTags.MESSAGES_LIST),
      reverseLayout = true,
      verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items(items = messages.reversed(), key = { it.messageId }) { message ->
          MessageItem(
              message = message,
              isSelected = selectedMessageId == message.messageId,
              currentUserId = currentUserId,
              context = context,
              callbacks = callbacks)
        }
      }
}

@Composable
private fun MessageItem(
    message: ConversationMessage,
    isSelected: Boolean,
    currentUserId: String,
    context: Context,
    callbacks: MessageCallbacks
) {
  val isFromCurrentUser = message.senderId == currentUserId

  Box {
    MessageBubble(
        text = message.text,
        timestamp = message.createdAt,
        isFromCurrentUser = isFromCurrentUser,
        fileAttachment =
            MessageBubbleFileAttachment(
                isFile = message.isFile,
                fileUrl = message.fileUrl,
                onDownloadClick = { url -> callbacks.onDownloadFile(url, context) }),
        editedAt = message.editedAt,
        interactions =
            MessageBubbleInteractions(
                onLinkClick = { url -> callbacks.onOpenUrl(url, context) },
                onLongClick =
                    if (isFromCurrentUser) {
                      { callbacks.onSelectMessage(message.messageId) }
                    } else null))

    if (isSelected && isFromCurrentUser) {
      MessageActionMenu(
          expanded = true,
          onDismiss = callbacks.onClearSelection,
          onEdit = { callbacks.onStartEditing(message) },
          onDelete = {
            callbacks.onRequestDelete(
                message.messageId, if (message.isFile) message.fileUrl else null)
          },
          onRemoveAttachment = { callbacks.onRemoveAttachment(message.messageId, message.fileUrl) },
          hasAttachment = message.isFile)
    }
  }
}

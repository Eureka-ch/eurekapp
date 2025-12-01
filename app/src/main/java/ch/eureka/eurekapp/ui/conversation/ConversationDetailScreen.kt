package ch.eureka.eurekapp.ui.conversation

import android.net.Uri
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

object ConversationDetailScreenTestTags {
  const val SCREEN = "conversationDetailScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val MESSAGES_LIST = "messagesList"
  const val BACK_BUTTON = "backButton"
}

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

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
  val selectedFileUri = remember { mutableStateOf<Uri?>(null) }

  val filePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri -> selectedFileUri.value = uri })

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

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearError()
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(ConversationDetailScreenTestTags.SCREEN),
      topBar = {
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
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        Column(modifier = Modifier.fillMaxWidth()) {
          selectedFileUri.value?.let { uri ->
            val selectedFileName =
                remember(uri) {
                  context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex =
                        cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                  } ?: "Unknown file"
                }
            Text(
                text = "Selected file: $selectedFileName",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs))
          }
          if (uiState.isUploadingFile) {
            Row(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(Spacing.sm))
              Text("Uploading file...", style = MaterialTheme.typography.bodySmall)
            }
          }
          Row {
            IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
              Icon(Icons.Default.AttachFile, contentDescription = "Attach file")
            }
            MessageInputField(
                message = uiState.currentMessage,
                onMessageChange = viewModel::updateMessage,
                onSend = {
                  selectedFileUri.value?.let { uri ->
                    viewModel.sendFileMessage(uri, context)
                    selectedFileUri.value = null
                  } ?: viewModel.sendMessage()
                },
                isSending = uiState.isSending,
                placeholder = "Write a message...",
                canSend = uiState.currentMessage.isNotBlank() || selectedFileUri.value != null)
          }
        }
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
              LazyColumn(
                  state = listState,
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(horizontal = Spacing.md)
                          .testTag(ConversationDetailScreenTestTags.MESSAGES_LIST),
                  reverseLayout = true,
                  verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(items = uiState.messages.reversed(), key = { it.messageId }) { message ->
                      MessageBubble(
                          text = message.text,
                          timestamp = message.createdAt,
                          isFromCurrentUser = message.senderId == viewModel.currentUserId,
                          isFile = message.isFile,
                          fileUrl = message.fileUrl,
                          onLinkClick = { url -> viewModel.openUrl(url, context) },
                          onDownloadClick = { url -> viewModel.downloadFile(url, context) })
                    }
                  }
            }
          }
        }
      }
}

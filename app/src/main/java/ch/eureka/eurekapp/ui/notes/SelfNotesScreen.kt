/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Test tags for the Self Notes Screen. */
object SelfNotesScreenTestTags {
  const val SCREEN = "selfNotesScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val ERROR_MESSAGE = "errorMessage"
  const val NOTES_LIST = "notesList"
  const val TOGGLE_SWITCH = "toggleSwitch"
}

/**
 * Screen for displaying and composing self-notes in a chat-like interface.
 *
 * This screen serves as the main UI for the "Note to Self" feature. It implements an offline-first
 * architecture where users can seamlessly switch between local-only storage and cloud
 * synchronization.
 *
 * Features:
 * - **Chat Interface:** Displays notes as message bubbles in a reverse-chronological list.
 * - **Storage Toggle:** Allows users to switch between "Local" (private, on-device) and "Cloud"
 *   (synced across devices) modes via a toggle switch in the Top Bar.
 * - **Real-time Updates:** Observes the local database (Room) which is kept in sync with Firestore.
 * - **Message Composition:** Provides an input field to write and send new notes.
 * - **Auto-scrolling:** Automatically scrolls to the newest note upon sending.
 * - **Feedback:** Displays loading indicators, empty states, and snackbar notifications for sync
 *   status.
 *
 * @param modifier Optional modifier for customizing the layout behavior of the screen root.
 * @param viewModel The [SelfNotesViewModel] that manages the UI state and business logic. Defaults
 *   to using the standard `viewModel()` factory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfNotesScreen(modifier: Modifier = Modifier, viewModel: SelfNotesViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      snackbarHostState.showSnackbar(errorMsg)
      viewModel.clearError()
    }
  }

  LaunchedEffect(uiState.notes.size) {
    if (uiState.notes.isNotEmpty() && !uiState.isLoading) {
      listState.animateScrollToItem(0)
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(SelfNotesScreenTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = { Text("Notes") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary),
            actions = {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = if (uiState.isCloudStorageEnabled) "Cloud" else "Local",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 8.dp))
                    Switch(
                        checked = uiState.isCloudStorageEnabled,
                        onCheckedChange = { viewModel.toggleStorageMode(it) },
                        modifier = Modifier.testTag(SelfNotesScreenTestTags.TOGGLE_SWITCH),
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                  }
            })
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        MessageInputField(
            message = uiState.currentMessage,
            onMessageChange = viewModel::updateMessage,
            onSend = { viewModel.sendNote() },
            isSending = uiState.isSending,
            placeholder = "Write a note to yourself...")
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          when {
            uiState.isLoading -> {
              CircularProgressIndicator(
                  modifier =
                      Modifier.align(Alignment.Center)
                          .testTag(SelfNotesScreenTestTags.LOADING_INDICATOR))
            }
            uiState.notes.isEmpty() -> {
              Text(
                  text = "No notes yet. Start writing!",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                  modifier =
                      Modifier.align(Alignment.Center)
                          .padding(Spacing.lg)
                          .testTag(SelfNotesScreenTestTags.EMPTY_STATE))
            }
            else -> {
              LazyColumn(
                  state = listState,
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(horizontal = Spacing.md)
                          .testTag(SelfNotesScreenTestTags.NOTES_LIST),
                  reverseLayout = true,
                  verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(items = uiState.notes, key = { it.messageID }) { message ->
                      SelfNoteMessageBubble(message = message)
                    }
                  }
            }
          }
        }
      }
}

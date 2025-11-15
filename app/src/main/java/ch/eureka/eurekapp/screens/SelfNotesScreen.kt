package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.notes.SelfNotesViewModel
import ch.eureka.eurekapp.screens.subscreens.tasks.MessageInputField
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.notes.SelfNoteMessageBubble

/** Test tags for the Self Notes Screen. */
object SelfNotesScreenTestTags {
  const val SCREEN = "selfNotesScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val ERROR_MESSAGE = "errorMessage"
  const val NOTES_LIST = "notesList"
}

/*
Co-author: GPT-5 Codex
*/

/**
 * Screen for displaying and composing self-notes in a chat-like interface.
 *
 * Features:
 * - Chat-style message bubbles for notes
 * - Real-time updates from Firestore
 * - Input field at the bottom for composing new notes
 * - Auto-scroll to latest note when sent
 * - Loading and error states
 *
 * @param viewModel ViewModel managing the notes state.
 * @param modifier Optional modifier.
 */
@Composable
fun SelfNotesScreen(viewModel: SelfNotesViewModel = viewModel(), modifier: Modifier = Modifier) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  // Show error message in snackbar
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      snackbarHostState.showSnackbar(errorMsg)
      viewModel.clearError()
    }
  }

  // Auto-scroll to top (latest message) when a new note is sent
  LaunchedEffect(uiState.notes.size) {
    if (uiState.notes.isNotEmpty() && !uiState.isLoading) {
      listState.animateScrollToItem(0)
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(SelfNotesScreenTestTags.SCREEN),
      topBar = { EurekaTopBar(title = "Notes") },
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

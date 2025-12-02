/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.ScreenWithHelp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfNotesScreen(modifier: Modifier = Modifier, viewModel: SelfNotesViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  // Handle hardware "Back" button to exit selection mode or edit mode
  BackHandler(enabled = uiState.isSelectionMode || uiState.editingMessageId != null) {
    if (uiState.isSelectionMode) viewModel.clearSelection()
    if (uiState.editingMessageId != null) viewModel.cancelEditing()
  }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      snackbarHostState.showSnackbar(errorMsg)
      viewModel.clearError()
    }
  }

  // Auto-scroll when new notes arrive (only if NOT in selection mode to avoid jumping)
  LaunchedEffect(uiState.notes.size) {
    if (uiState.notes.isNotEmpty() && !uiState.isLoading && !uiState.isSelectionMode) {
      listState.animateScrollToItem(0)
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(SelfNotesScreenTestTags.SCREEN),
      topBar = {
        if (uiState.isSelectionMode) {
          TopAppBar(
              title = { Text("${uiState.selectedNoteIds.size} Selected") },
              colors =
                  TopAppBarDefaults.topAppBarColors(
                      containerColor =
                          MaterialTheme.colorScheme
                              .surfaceVariant, // Different color for context mode
                      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
              navigationIcon = {
                IconButton(onClick = { viewModel.clearSelection() }) {
                  Icon(Icons.Default.Close, contentDescription = "Cancel Selection")
                }
              },
              actions = {
                if (uiState.selectedNoteIds.size == 1) {
                  IconButton(
                      onClick = {
                        val selectedId = uiState.selectedNoteIds.first()
                        val noteToEdit = uiState.notes.find { it.messageID == selectedId }
                        if (noteToEdit != null) viewModel.startEditing(noteToEdit)
                      }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                      }
                }
                IconButton(onClick = { viewModel.deleteSelectedNotes() }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                }
              })
        } else {
          TopAppBar(
              title = { Text(if (uiState.editingMessageId != null) "Editing Note" else "Notes") },
              colors =
                  TopAppBarDefaults.topAppBarColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      titleContentColor = MaterialTheme.colorScheme.onPrimary,
                      actionIconContentColor = MaterialTheme.colorScheme.onPrimary),
              navigationIcon = {
                if (uiState.editingMessageId != null) {
                  IconButton(onClick = { viewModel.cancelEditing() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                  }
                }
              },
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
        }
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!uiState.isSelectionMode) {
          MessageInputField(
              message = uiState.currentMessage,
              onMessageChange = viewModel::updateMessage,
              onSend = { viewModel.sendNote() },
              isSending = uiState.isSending,
              placeholder =
                  if (uiState.editingMessageId != null) "Edit your note..."
                  else "Write a note to yourself...")
        }
      }) { paddingValues ->
        ScreenWithHelp(
            helpContext = HelpContext.NOTES,
            content = {
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
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                          items(items = uiState.notes, key = { it.messageID }) { message ->
                            val isSelected = uiState.selectedNoteIds.contains(message.messageID)

                            SelfNoteMessageBubble(
                                message = message,
                                isSelected = isSelected,
                                onClick = {
                                  if (uiState.isSelectionMode) {
                                    viewModel.toggleSelection(message.messageID)
                                  }
                                },
                                onLongClick = { viewModel.toggleSelection(message.messageID) })
                          }
                        }
                  }
                }
              }
            })
      }
}

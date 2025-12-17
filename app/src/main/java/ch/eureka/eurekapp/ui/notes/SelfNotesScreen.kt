/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.ScreenWithHelp
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
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

@Composable
fun SelfNotesScreen(
    modifier: Modifier = Modifier,
    viewModel: SelfNotesViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
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
        SelfNotesTopBar(
            uiState = uiState,
            onClearSelection = viewModel::clearSelection,
            onEditSelected = { id ->
              val note = uiState.notes.find { it.messageID == id }
              if (note != null) viewModel.startEditing(note)
            },
            onDeleteSelected = viewModel::deleteSelectedNotes,
            onCancelEditing = viewModel::cancelEditing,
            onToggleStorage = viewModel::toggleStorageMode,
            onNavigateBack = onNavigateBack)
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        SelfNotesBottomBar(
            uiState = uiState,
            onMessageChange = viewModel::updateMessage,
            onSend = viewModel::sendNote)
      }) { paddingValues ->
        ScreenWithHelp(
            helpContext = HelpContext.NOTES,
            helpPadding =
                PaddingValues(
                    start = Spacing.md, end = Spacing.md, top = Spacing.md, bottom = 100.dp),
            content = {
              SelfNotesContent(
                  uiState = uiState,
                  listState = listState,
                  paddingValues = paddingValues,
                  onToggleSelection = viewModel::toggleSelection)
            })
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelfNotesTopBar(
    uiState: SelfNotesUIState,
    onClearSelection: () -> Unit,
    onEditSelected: (String) -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelEditing: () -> Unit,
    onToggleStorage: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
  if (uiState.isSelectionMode) {
    ContextualSelectionTopBar(
        selectionCount = uiState.selectedNoteIds.size,
        selectedIds = uiState.selectedNoteIds,
        onClearSelection = onClearSelection,
        onEditSelected = onEditSelected,
        onDeleteSelected = onDeleteSelected)
  } else {
    StandardTopBar(
        isEditing = uiState.editingMessageId != null,
        isCloudEnabled = uiState.isCloudStorageEnabled,
        onCancelEditing = onCancelEditing,
        onToggleStorage = onToggleStorage,
        onNavigateBack = onNavigateBack)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextualSelectionTopBar(
    selectionCount: Int,
    selectedIds: Set<String>,
    onClearSelection: () -> Unit,
    onEditSelected: (String) -> Unit,
    onDeleteSelected: () -> Unit
) {
  EurekaTopBar(
      title = stringResource(R.string.selfnotes_selection_count, selectionCount),
      navigationIcon = {
        IconButton(onClick = onClearSelection) {
          Icon(
              Icons.Default.Close,
              contentDescription = stringResource(R.string.cancel_selection_desc),
              tint = EColors.WhiteTextColor)
        }
      },
      actions = {
        if (selectionCount == 1) {
          IconButton(onClick = { onEditSelected(selectedIds.first()) }) {
            Icon(
                Icons.Default.Edit, contentDescription = stringResource(R.string.edit_note_desc), tint = EColors.WhiteTextColor)
          }
        }
        IconButton(onClick = onDeleteSelected) {
          Icon(
              Icons.Default.Delete,
              contentDescription = stringResource(R.string.delete_selected_desc),
              tint = EColors.WhiteTextColor)
        }
      })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardTopBar(
    isEditing: Boolean,
    isCloudEnabled: Boolean,
    onCancelEditing: () -> Unit,
    onToggleStorage: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
  EurekaTopBar(
      title = if (isEditing) stringResource(R.string.selfnotes_editing_title) else stringResource(R.string.selfnotes_title),
      navigationIcon = {
        if (isEditing) {
          IconButton(onClick = onCancelEditing) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.cancel_edit_desc),
                tint = EColors.WhiteTextColor)
          }
        } else {
          BackButton(onClick = onNavigateBack)
        }
      },
      actions = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)) {
              Text(
                  text = if (isCloudEnabled) stringResource(R.string.selfnotes_storage_cloud) else stringResource(R.string.selfnotes_storage_local),
                  style = MaterialTheme.typography.labelMedium,
                  color = EColors.WhiteTextColor,
                  modifier = Modifier.padding(end = 8.dp))
              Switch(
                  checked = isCloudEnabled,
                  onCheckedChange = onToggleStorage,
                  modifier = Modifier.testTag(SelfNotesScreenTestTags.TOGGLE_SWITCH),
                  colors =
                      SwitchDefaults.colors(
                          checkedThumbColor = EColors.WhiteTextColor,
                          checkedTrackColor = EColors.WhiteTextColor.copy(alpha = 0.5f),
                          uncheckedThumbColor = EColors.WhiteTextColor.copy(alpha = 0.8f),
                          uncheckedTrackColor = EColors.WhiteTextColor.copy(alpha = 0.3f)))
            }
      })
}

@Composable
private fun SelfNotesBottomBar(
    uiState: SelfNotesUIState,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
  if (!uiState.isSelectionMode) {
    MessageInputField(
        message = uiState.currentMessage,
        onMessageChange = onMessageChange,
        onSend = onSend,
        isSending = uiState.isSending,
        placeholder =
            if (uiState.editingMessageId != null) stringResource(R.string.selfnotes_edit_placeholder)
            else stringResource(R.string.selfnotes_message_placeholder))
  }
}

@Composable
private fun SelfNotesContent(
    uiState: SelfNotesUIState,
    listState: LazyListState,
    paddingValues: PaddingValues,
    onToggleSelection: (String) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    when {
      uiState.isLoading -> {
        CircularProgressIndicator(
            modifier =
                Modifier.align(Alignment.Center).testTag(SelfNotesScreenTestTags.LOADING_INDICATOR))
      }
      uiState.notes.isEmpty() -> {
        Text(
            text = stringResource(R.string.selfnotes_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(Spacing.lg)
                    .testTag(SelfNotesScreenTestTags.EMPTY_STATE))
      }
      else -> {
        NotesList(uiState = uiState, listState = listState, onToggleSelection = onToggleSelection)
      }
    }
  }
}

@Composable
private fun NotesList(
    uiState: SelfNotesUIState,
    listState: LazyListState,
    onToggleSelection: (String) -> Unit
) {
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
                  onToggleSelection(message.messageID)
                }
              },
              onLongClick = { onToggleSelection(message.messageID) })
        }
      }
}

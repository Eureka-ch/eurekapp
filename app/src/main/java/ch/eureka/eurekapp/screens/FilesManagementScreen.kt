// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.downloads.AppDatabase
import ch.eureka.eurekapp.model.downloads.FileItem
import ch.eureka.eurekapp.model.downloads.FilesManagementViewModel
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.ScreenWithHelp

object FilesManagementScreenTestTags {
  const val NO_FILES_MESSAGE = "no_files_message"
  const val TOP_BAR_TITLE = "top_bar_title"
  const val FILE_DISPLAY_NAME = "file_display_name"
  const val OPEN_BUTTON = "open_button"
  const val DELETE_BUTTON = "delete_button"
  const val DELETE_DIALOG_TITLE = "delete_dialog_title"
  const val DELETE_DIALOG_TEXT = "delete_dialog_text"
  const val CONFIRM_DELETE_BUTTON = "confirm_delete_button"
  const val CANCEL_DELETE_BUTTON = "cancel_delete_button"
}

@Composable
fun FilesManagementContent(
    files: List<FileItem>,
    onOpenFile: (FileItem) -> Unit,
    onDeleteFile: (FileItem) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    if (files.isEmpty()) {
      Text(
          stringResource(R.string.files_management_no_files),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.testTag(FilesManagementScreenTestTags.NO_FILES_MESSAGE))
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(files) { fileItem ->
          FileItemRow(fileItem = fileItem, onOpenFile = onOpenFile, onDeleteFile = onDeleteFile)
        }
      }
    }
  }
}

@Composable
fun FileItemRow(
    fileItem: FileItem,
    onOpenFile: (FileItem) -> Unit,
    onDeleteFile: (FileItem) -> Unit
) {
  Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      if (fileItem.isImage) {
        PhotoViewer(image = fileItem.uri, modifier = Modifier.size(64.dp))
      }
      Column {
        Text(
            fileItem.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier.testTag(
                    "${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_${fileItem.displayName}"))
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(
          onClick = { onOpenFile(fileItem) },
          modifier = Modifier.testTag(FilesManagementScreenTestTags.OPEN_BUTTON)) {
            Text(stringResource(R.string.files_management_open_button))
          }
      Button(
          onClick = { onDeleteFile(fileItem) },
          modifier = Modifier.testTag(FilesManagementScreenTestTags.DELETE_BUTTON)) {
            Text(stringResource(R.string.files_management_delete_button))
          }
    }
  }
}

@Composable
fun DeleteConfirmationDialog(
    fileToDelete: FileItem?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  if (fileToDelete != null) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
              stringResource(R.string.files_management_delete_dialog_title),
              modifier = Modifier.testTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE))
        },
        text = {
          Text(
              stringResource(R.string.files_management_delete_dialog_message, fileToDelete.displayName),
              modifier = Modifier.testTag(FilesManagementScreenTestTags.DELETE_DIALOG_TEXT))
        },
        confirmButton = {
          TextButton(
              onClick = onConfirm,
              modifier = Modifier.testTag(FilesManagementScreenTestTags.CONFIRM_DELETE_BUTTON)) {
                Text(stringResource(R.string.files_management_delete_confirm))
              }
        },
        dismissButton = {
          TextButton(
              onClick = onDismiss,
              modifier = Modifier.testTag(FilesManagementScreenTestTags.CANCEL_DELETE_BUTTON)) {
                Text(stringResource(R.string.files_management_delete_cancel))
              }
        })
  }
}

@Composable
fun FilesManagementScreen(
    onBackClick: () -> Unit = {},
    viewModel: FilesManagementViewModel = run {
      val context = LocalContext.current
      remember {
        FilesManagementViewModel(
            AppDatabase.getDatabase(context).downloadedFileDao(),
            context.applicationContext as android.app.Application)
      }
    }
) {
  val state by viewModel.uiState.collectAsState()
  val files = state.files
  var showDeleteDialog by remember { mutableStateOf(false) }
  var fileToDelete by remember { mutableStateOf<FileItem?>(null) }
  var openFileIntent by remember { mutableStateOf<android.content.Intent?>(null) }
  val context = LocalContext.current

  LaunchedEffect(openFileIntent) {
    openFileIntent?.let { intent ->
      try {
        val chooser =
            android.content.Intent.createChooser(intent, "Open with").apply {
              addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(chooser)
      } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT)
            .show()
      }
      openFileIntent = null
    }
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.files_management_title),
            navigationIcon = { BackButton(onClick = onBackClick) },
            modifier = Modifier.testTag(FilesManagementScreenTestTags.TOP_BAR_TITLE))
      },
      content = { padding ->
        ScreenWithHelp(
            helpContext = HelpContext.FILES_MANAGEMENT,
            content = {
              Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                FilesManagementContent(
                    files = files,
                    onOpenFile = { openFileIntent = viewModel.getOpenFileIntent(it) },
                    onDeleteFile = {
                      fileToDelete = it
                      showDeleteDialog = true
                    })
              }
            })
      })

  DeleteConfirmationDialog(
      fileToDelete = if (showDeleteDialog) fileToDelete else null,
      onConfirm = {
        viewModel.deleteFile(fileToDelete!!) { success ->
          if (!success) {
            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
          }
        }
        showDeleteDialog = false
        fileToDelete = null
      },
      onDismiss = {
        showDeleteDialog = false
        fileToDelete = null
      })
}

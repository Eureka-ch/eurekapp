// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.downloads.AppDatabase
import ch.eureka.eurekapp.model.downloads.FileItem
import ch.eureka.eurekapp.model.downloads.FilesManagementViewModel
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar

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

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Downloaded Files",
            navigationIcon = { BackButton(onClick = onBackClick) },
            modifier = Modifier.testTag(FilesManagementScreenTestTags.TOP_BAR_TITLE))
      },
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
          if (files.isEmpty()) {
            Text(
                "No downloaded files.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(FilesManagementScreenTestTags.NO_FILES_MESSAGE))
          } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(files) { fileItem ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Row(
                          modifier = Modifier.weight(1f),
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            onClick = { viewModel.openFile(fileItem) },
                            modifier =
                                Modifier.testTag(FilesManagementScreenTestTags.OPEN_BUTTON)) {
                              Text("Open")
                            }
                        Button(
                            onClick = {
                              fileToDelete = fileItem
                              showDeleteDialog = true
                            },
                            modifier =
                                Modifier.testTag(FilesManagementScreenTestTags.DELETE_BUTTON)) {
                              Text("Delete")
                            }
                      }
                    }
              }
            }
          }
        }
      })

  if (showDeleteDialog && fileToDelete != null) {
    AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = {
          Text(
              "Confirm Deletion",
              modifier = Modifier.testTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE))
        },
        text = {
          Text(
              "Are you sure you want to delete '${fileToDelete!!.displayName}'?",
              modifier = Modifier.testTag(FilesManagementScreenTestTags.DELETE_DIALOG_TEXT))
        },
        confirmButton = {
          TextButton(
              onClick = {
                viewModel.deleteFile(fileToDelete!!)
                showDeleteDialog = false
                fileToDelete = null
              },
              modifier = Modifier.testTag(FilesManagementScreenTestTags.CONFIRM_DELETE_BUTTON)) {
                Text("Delete")
              }
        },
        dismissButton = {
          TextButton(
              onClick = {
                showDeleteDialog = false
                fileToDelete = null
              },
              modifier = Modifier.testTag(FilesManagementScreenTestTags.CANCEL_DELETE_BUTTON)) {
                Text("Cancel")
              }
        })
  }
}

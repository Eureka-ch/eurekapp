package ch.eureka.eurekapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.ui.camera.LocalPhotoViewer
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object CreateTaskScreenTestTags {
  const val TITLE = "title"
  const val DESCRIPTION = "description"
  const val DUE_DATE = "due_date"
  const val ADD_PHOTO = "add_photo"
  const val SAVE_TASK = "save_task"
  const val PHOTO = "photo"
  const val DELETE_PHOTO = "delete_photo"
  const val ERROR_MSG = "error_msg"
}

const val SAVE_BUTTON_SIZE = 0.7f

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/**
 * A composable screen for creating a new task within a project.
 *
 * @param projectId The ID of the project to which the new task will be added.
 * @param navigationController The NavHostController for handling navigation actions.
 * @param createTaskViewModel The CreateTaskViewModel instance responsible for managing task
 *   creation state.
 */
@Composable
fun CreateTaskScreen(
    projectId: String,
    navigationController: NavHostController = rememberNavController(),
    createTaskViewModel: CreateTaskViewModel = viewModel(),
) {
  val createTaskState by createTaskViewModel.uiState.collectAsState()
  val inputValid by createTaskViewModel.inputValid.collectAsState()
  val errorMsg = createTaskState.errorMsg
  var hasTouchedTitle by remember { mutableStateOf(false) }
  var hasTouchedDescription by remember { mutableStateOf(false) }
  var hasTouchedDate by remember { mutableStateOf(false) }
  val savedStateHandle = navigationController.currentBackStackEntry?.savedStateHandle
  val photoUri by
      savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
          ?: remember { mutableStateOf("") }

  val context = LocalContext.current
  val scrollState = rememberScrollState()

  LaunchedEffect(projectId) { createTaskViewModel.setProjectId(projectId) }

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      createTaskViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(photoUri) {
    if (photoUri.isNotEmpty()) {
      createTaskViewModel.addAttachment(photoUri.toUri())
    }
  }

  LaunchedEffect(createTaskState.taskSaved) {
    if (createTaskState.taskSaved) {
      navigationController.popBackStack()
      createTaskViewModel.resetSaveState()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      // Clean up photos when navigating away if task wasn't saved
      if (!createTaskState.taskSaved) {
        createTaskState.attachmentUris.forEach { uri ->
          createTaskViewModel.deletePhoto(context, uri)
        }
      }
    }
  }

  Scaffold(
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Title Input
              OutlinedTextField(
                  value = createTaskState.title,
                  onValueChange = { createTaskViewModel.setTitle(it) },
                  label = { Text("Title") },
                  placeholder = { Text("Name the task") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .onFocusChanged({ focusState ->
                            if (focusState.isFocused) {
                              hasTouchedTitle = true
                            }
                          })
                          .testTag(CreateTaskScreenTestTags.TITLE))
              if (createTaskState.title.isBlank() && hasTouchedTitle) {
                Text(
                    text = "Title cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
              }

              // Description Input
              OutlinedTextField(
                  value = createTaskState.description,
                  onValueChange = { createTaskViewModel.setDescription(it) },
                  label = { Text("Description") },
                  placeholder = { Text("Describe the task") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .onFocusChanged({ focusState ->
                            if (focusState.isFocused) {
                              hasTouchedDescription = true
                            }
                          })
                          .testTag(CreateTaskScreenTestTags.DESCRIPTION))
              if (createTaskState.description.isBlank() && hasTouchedDescription) {
                Text(
                    text = "Description cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
              }

              // Due Date Input
              OutlinedTextField(
                  value = createTaskState.dueDate,
                  onValueChange = { createTaskViewModel.setDueDate(it) },
                  label = { Text("Due date") },
                  placeholder = { Text("01/01/1970") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .onFocusChanged({ focusState ->
                            if (focusState.isFocused) {
                              hasTouchedDate = true
                            }
                          })
                          .testTag(CreateTaskScreenTestTags.DUE_DATE))
              val dateRegex = createTaskViewModel.dateRegex
              if (createTaskState.dueDate.isNotBlank() &&
                  !dateRegex.matches(createTaskState.dueDate) &&
                  hasTouchedDate) {
                Text(
                    text = "Invalid format (must be dd/MM/yyyy)",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                OutlinedButton(
                    onClick = { navigationController.navigate(Route.Camera) },
                    colors = EurekaStyles.OutlinedButtonColors(),
                    modifier = Modifier.testTag(CreateTaskScreenTestTags.ADD_PHOTO)) {
                      Text("Add Photo")
                    }

                // Save Button
                Button(
                    onClick = { createTaskViewModel.addTask(context) },
                    enabled = inputValid && !createTaskState.isSaving,
                    modifier =
                        Modifier.fillMaxWidth(SAVE_BUTTON_SIZE)
                            .testTag(CreateTaskScreenTestTags.SAVE_TASK),
                    colors = EurekaStyles.PrimaryButtonColors()) {
                      Text(if (createTaskState.isSaving) "Saving..." else "Save")
                    }
              }
              createTaskState.attachmentUris.forEachIndexed { index, file ->
                Row {
                  Text("Photo ${index + 1}")
                  IconButton(
                      onClick = {
                        if (createTaskViewModel.deletePhoto(context, file)) {
                          createTaskViewModel.removeAttachment(index)
                        }
                      },
                      modifier = Modifier.testTag(CreateTaskScreenTestTags.DELETE_PHOTO)) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete file")
                      }
                  LocalPhotoViewer(
                      file,
                      modifier = Modifier.size(100.dp).testTag(CreateTaskScreenTestTags.PHOTO))
                }
              }
            }
      })
}

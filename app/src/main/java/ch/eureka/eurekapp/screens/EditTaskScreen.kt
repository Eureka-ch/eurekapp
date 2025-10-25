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
import ch.eureka.eurekapp.model.tasks.EditTaskViewModel
import ch.eureka.eurekapp.navigation.SharedScreens
import ch.eureka.eurekapp.navigation.navigationFunction
import ch.eureka.eurekapp.ui.camera.LocalPhotoViewer
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object EditTaskScreenTestTags {
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val DUE_DATE = "due_date"
    const val ADD_PHOTO = "add_photo"
    const val SAVE_TASK = "save_task"
    const val PHOTO = "photo"
    const val DELETE_PHOTO = "delete_photo"
    const val ERROR_MSG = "error_msg"
}

const val EDIT_SCREEN_SAVE_BUTTON_SIZE = 0.7f

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/**
 * A composable screen for editing an existing task within a project.
 *
 * @param projectId The ID of the project that contains the task.
 * @param taskId The ID of the task to be edited
 * @param navigationController The NavHostController for handling navigation actions.
 * @param editTaskViewModel The EditTaskViewModel instance responsible for managing task
 *   editing state.
 */
@Composable
fun EditTaskScreen(
    projectId: String,
    taskId: String,
    navigationController: NavHostController = rememberNavController(),
    editTaskViewModel: EditTaskViewModel = viewModel(),
) {
    val editTaskState by editTaskViewModel.uiState.collectAsState()
    val inputValid by editTaskViewModel.inputValid.collectAsState()
    val errorMsg = editTaskState.errorMsg
    var hasTouchedTitle by remember { mutableStateOf(false) }
    var hasTouchedDescription by remember { mutableStateOf(false) }
    var hasTouchedDate by remember { mutableStateOf(false) }
    val savedStateHandle = navigationController.currentBackStackEntry?.savedStateHandle
    val photoUri by
    savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
        ?: remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(projectId) { editTaskViewModel.setProjectId(projectId) }

    LaunchedEffect(taskId) { editTaskViewModel.loadTask(projectId, taskId) }

    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            editTaskViewModel.clearErrorMsg()
        }
    }

    LaunchedEffect(photoUri) {
        if (photoUri.isNotEmpty()) {
            editTaskViewModel.addAttachment(photoUri.toUri())
        }
    }

    LaunchedEffect(editTaskState.taskSaved) {
        if (editTaskState.taskSaved) {
            navigationFunction(navigationController, true, null)
            editTaskViewModel.resetSaveState()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Clean up photos when navigating away if task wasn't saved
            if (!editTaskState.taskSaved) {
                editTaskState.attachmentUris.forEach { uri ->
                    editTaskViewModel.deletePhoto(context, uri)
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
                    value = editTaskState.title,
                    onValueChange = { editTaskViewModel.setTitle(it) },
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
                if (editTaskState.title.isBlank() && hasTouchedTitle) {
                    Text(
                        text = "Title cannot be empty",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
                }

                // Description Input
                OutlinedTextField(
                    value = editTaskState.description,
                    onValueChange = { editTaskViewModel.setDescription(it) },
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
                if (editTaskState.description.isBlank() && hasTouchedDescription) {
                    Text(
                        text = "Description cannot be empty",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
                }

                // Due Date Input
                OutlinedTextField(
                    value = editTaskState.dueDate,
                    onValueChange = { editTaskViewModel.setDueDate(it) },
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
                val dateRegex = editTaskViewModel.dateRegex
                if (editTaskState.dueDate.isNotBlank() &&
                    !dateRegex.matches(editTaskState.dueDate) &&
                    hasTouchedDate) {
                    Text(
                        text = "Invalid format (must be dd/MM/yyyy)",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(CreateTaskScreenTestTags.ERROR_MSG))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    OutlinedButton(
                        onClick = {
                            navigationFunction(
                                navigationController = navigationController,
                                destination = SharedScreens.CameraScreen)
                        },
                        colors = EurekaStyles.OutlinedButtonColors(),
                        modifier = Modifier.testTag(CreateTaskScreenTestTags.ADD_PHOTO)) {
                        Text("Add Photo")
                    }

                    // Save Button
                    Button(
                        onClick = { editTaskViewModel.addTask(context) },
                        enabled = inputValid && !editTaskState.isSaving,
                        modifier =
                            Modifier.fillMaxWidth(EDIT_SCREEN_SAVE_BUTTON_SIZE)
                                .testTag(CreateTaskScreenTestTags.SAVE_TASK),
                        colors = EurekaStyles.PrimaryButtonColors()) {
                        Text(if (editTaskState.isSaving) "Saving..." else "Save")
                    }
                }
                editTaskState.attachmentUris.forEachIndexed { index, file ->
                    Row {
                        Text("Photo ${index + 1}")
                        IconButton(
                            onClick = {
                                if (editTaskViewModel.deletePhoto(context, file)) {
                                    editTaskViewModel.removeAttachment(index)
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

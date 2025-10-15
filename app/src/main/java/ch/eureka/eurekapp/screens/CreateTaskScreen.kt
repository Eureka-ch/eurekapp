package ch.eureka.eurekapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.ui.camera.RemotePhotoViewer
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.SharedScreens
import ch.eureka.eurekapp.navigation.navigationFunction
import ch.eureka.eurekapp.ui.camera.LocalPhotoViewer

object CreateTaskScreenTestTags {
  const val CREATE_TASK_TEXT = "CreateTaskText"
}

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
*/

@Composable
fun CreateTaskScreen(
    projectId: String,
    navigationController: NavHostController = rememberNavController(),
    createTaskViewModel: CreateTaskViewModel = viewModel()
) {
  val createTaskState by createTaskViewModel.uiState.collectAsState()
  val errorMsg = createTaskState.errorMsg
  var hasTouchedTitle by remember { mutableStateOf(false) }
  var hasTouchedDescription by remember { mutableStateOf(false) }
  var hasTouchedDate by remember { mutableStateOf(false) }
  val savedStateHandle = navigationController.currentBackStackEntry?.savedStateHandle
  val photoUri by
      savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
          ?: remember { mutableStateOf("") }

  val context = LocalContext.current

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

  Scaffold(
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                          }))
              if (createTaskState.title.isBlank() && hasTouchedTitle) {
                Text(
                    text = "Title cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall)
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
                          }))
              if (createTaskState.description.isBlank() && hasTouchedDescription) {
                Text(
                    text = "Description cannot be empty",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                )
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
                          }))
              val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
              if (createTaskState.dueDate.isNotBlank() &&
                  !dateRegex.matches(createTaskState.dueDate) &&
                  hasTouchedDate) {
                Text(
                    text = "Invalid format (must be dd/MM/yyyy)",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall)
              }

              Button(
                  onClick = {
                    navigationFunction(
                        navigationController = navigationController,
                        destination = SharedScreens.CameraScreen,
                        args = arrayOf(projectId, createTaskState.taskId))
                  }) {
                    Text("Add Photo")
                  }

              for ((index, file) in createTaskState.attachmentUrls.withIndex()) {
                Row {
                  Text("Photo${index + 1}")
                  IconButton(
                      onClick = { createTaskViewModel.removeAttachment(file) },
                  ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete file")
                  }
                    LocalPhotoViewer(file, modifier = Modifier.size(100.dp))
                }
              }

              // Save Button
              Button(
                  onClick = { createTaskViewModel.addTask(context) },
                  enabled =
                      createTaskState.title.isNotBlank() &&
                          createTaskState.description.isNotBlank() &&
                          dateRegex.matches(createTaskState.dueDate)) {
                    Text("Save")
                  }
            }
      })
}

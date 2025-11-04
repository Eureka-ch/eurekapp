package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.camera.PhotoViewer

// Portions of this code were generated with the help of AI.

object CommonTaskTestTags {
  const val TITLE = "title"
  const val DESCRIPTION = "description"
  const val DUE_DATE = "due_date"
  const val REMINDER_TIME = "reminder_time"
  const val ADD_PHOTO = "add_photo"
  const val SAVE_TASK = "save_task"
  const val PHOTO = "photo"
  const val DELETE_PHOTO = "delete_photo"
  const val ERROR_MSG = "error_msg"
  const val PROJECT_SELECTION_TITLE = "project_selection_title"
  const val PROJECT_RADIO = "project_radio"
  const val PROJECT_NAME = "project_name"
  const val PROJECT_SELECTION_ERROR = "project_selection_error"
  const val NO_PROJECTS_AVAILABLE = "no_projects_available"
}

@Composable
fun TaskTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Title") },
      placeholder = { Text("Name the task") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.TITLE))
  if (value.isBlank() && hasTouched) {
    Text(
        text = "Title cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun TaskDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Description") },
      placeholder = { Text("Describe the task") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DESCRIPTION))
  if (value.isBlank() && hasTouched) {
    Text(
        text = "Description cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun TaskDueDateField(
    value: String,
    onValueChange: (String) -> Unit,
    hasTouched: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    dateRegex: Regex,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Due date") },
      placeholder = { Text("01/01/1970") },
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DUE_DATE))
  if (value.isNotBlank() && !dateRegex.matches(value) && hasTouched) {
    Text(
        text = "Invalid format (must be dd/MM/yyyy)",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun TaskReminderField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  val timeRegex = Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")

  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Reminder time") },
      placeholder = { Text("HH:mm") },
      modifier = modifier.fillMaxWidth().testTag(CommonTaskTestTags.REMINDER_TIME))

  if (value.isNotBlank() && !timeRegex.matches(value)) {
    Text(
        text = "Invalid format (must be HH:mm)",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun AttachmentsList(
    attachments: List<Any>,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
  attachments.forEachIndexed { index, file ->
    Row(modifier = modifier) {
      Text("Photo ${index + 1}")
      IconButton(
          onClick = { onDelete(index) },
          modifier = Modifier.testTag(CommonTaskTestTags.DELETE_PHOTO)) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete file")
          }
      PhotoViewer(file, modifier = Modifier.size(100.dp).testTag(CommonTaskTestTags.PHOTO))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectionField(
    projects: List<Project>,
    selectedProjectId: String,
    onProjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedProject = projects.firstOrNull { it.projectId == selectedProjectId }
  val selectedProjectName = selectedProject?.name ?: "No project selected yet"

  Column(modifier = modifier) {
    Text(text = "Select Project", style = MaterialTheme.typography.titleMedium)

    if (projects.isEmpty()) {
      Text(
          text = "No projects available",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.testTag(CommonTaskTestTags.NO_PROJECTS_AVAILABLE))
    } else {
      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedProjectName,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("No project selected yet") },
            modifier =
                Modifier.fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .testTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("${CommonTaskTestTags.PROJECT_RADIO}_menu")) {
              projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                      onProjectSelected(project.projectId)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${CommonTaskTestTags.PROJECT_RADIO}_${project.projectId}"))
              }
            }
      }
    }

    if (projects.isNotEmpty() && selectedProjectId.isEmpty()) {
      Text(
          text = "Please select a project",
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(CommonTaskTestTags.PROJECT_SELECTION_ERROR))
    }
  }
}

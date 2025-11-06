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

/**
 * Helper function to determine if an error should be shown for a field.
 *
 * @param readOnly Whether the field is read-only
 * @param value The current value of the field
 * @param hasTouched Whether the field has been touched/focused
 * @return true if the error should be displayed
 */
private fun shouldShowError(
    readOnly: Boolean = false,
    value: String,
    hasTouched: Boolean
): Boolean {
  return !readOnly && value.isBlank() && hasTouched
}

/**
 * Helper function to determine if a date format error should be shown.
 *
 * @param readOnly Whether the field is read-only
 * @param value The current value of the field
 * @param dateRegex The regex pattern for valid date format
 * @param hasTouched Whether the field has been touched/focused
 * @return true if the date format error should be displayed
 */
private fun shouldShowDateFormatError(
    readOnly: Boolean,
    value: String,
    dateRegex: Regex,
    hasTouched: Boolean
): Boolean {
  return !readOnly && value.isNotBlank() && !dateRegex.matches(value) && hasTouched
}

object CommonTaskTestTags {
  const val TITLE = "title"
  const val DESCRIPTION = "description"
  const val DUE_DATE = "due_date"
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
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Title") },
      placeholder = { Text("Name the task") },
      readOnly = readOnly,
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.TITLE))
  if (shouldShowError(readOnly, value, hasTouched)) {
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
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Description") },
      placeholder = { Text("Describe the task") },
      readOnly = readOnly,
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DESCRIPTION))
  if (shouldShowError(readOnly, value, hasTouched)) {
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
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Due date") },
      placeholder = { Text("01/01/1970") },
      readOnly = readOnly,
      modifier =
          modifier
              .fillMaxWidth()
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onFocusChanged(true)
                }
              }
              .testTag(CommonTaskTestTags.DUE_DATE))
  if (shouldShowError(readOnly, value, hasTouched)) {
    Text(
        text = "Due date cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  } else if (shouldShowDateFormatError(readOnly, value, dateRegex, hasTouched)) {
    Text(
        text = "Invalid format (must be dd/MM/yyyy)",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CommonTaskTestTags.ERROR_MSG))
  }
}

@Composable
fun AttachmentsList(
    attachments: List<Any>,
    modifier: Modifier = Modifier,
    onDelete: ((Int) -> Unit)? = null,
    isReadOnly: Boolean = false
) {
  attachments.forEachIndexed { index, file ->
    Row(modifier = modifier) {
      Text("Photo ${index + 1}")
      if (!isReadOnly && onDelete != null) {
        IconButton(
            onClick = { onDelete(index) },
            modifier = Modifier.testTag(CommonTaskTestTags.DELETE_PHOTO)) {
              Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete file")
            }
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

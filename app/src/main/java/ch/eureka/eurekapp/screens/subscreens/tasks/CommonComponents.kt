package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.utils.Formatters

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
  const val TASK_DEPENDENCIES_FIELD = "task_dependencies_field"
  const val TASK_DEPENDENCY_ITEM = "task_dependency_item"
  const val REMOVE_DEPENDENCY = "remove_dependency"
  const val ADD_DEPENDENCY_BUTTON = "add_dependency_button"
  const val DEPENDENCY_CYCLE_ERROR = "dependency_cycle_error"
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
fun TaskReminderField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text("Reminder time") },
      placeholder = { Text("HH:mm") },
      modifier = modifier.fillMaxWidth().testTag(CommonTaskTestTags.REMINDER_TIME))

  if (value.isNotBlank() && !Formatters.timeRegex.matches(value)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDependenciesSelectionField(
    availableTasks: List<ch.eureka.eurekapp.model.data.task.Task>,
    selectedDependencyIds: List<String>,
    onDependencyAdded: (String) -> Unit,
    onDependencyRemoved: (String) -> Unit,
    currentTaskId: String = "",
    cycleError: String? = null,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  
  // Filter out the current task from available tasks (can't depend on itself)
  val selectableTasks = availableTasks.filter { it.taskID != currentTaskId }
  
  Column(modifier = modifier) {
    Text(text = "Task Dependencies", style = MaterialTheme.typography.titleMedium)
    
    // Show selected dependencies
    if (selectedDependencyIds.isNotEmpty()) {
      Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedDependencyIds.forEach { dependencyId ->
              val task = availableTasks.firstOrNull { it.taskID == dependencyId }
              if (task != null) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = task.title,
                          style = MaterialTheme.typography.bodyMedium,
                          modifier = Modifier.weight(1f))
                      IconButton(
                          onClick = { onDependencyRemoved(dependencyId) },
                          modifier = Modifier.testTag("${CommonTaskTestTags.REMOVE_DEPENDENCY}_$dependencyId")) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove dependency")
                      }
                    }
              }
            }
          }
    }
    
    // Add dependency button/dropdown
    if (selectableTasks.isNotEmpty()) {
      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON)) {
              Text("Add Dependency")
            }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_menu")) {
              selectableTasks.forEach { task ->
                val isSelected = selectedDependencyIds.contains(task.taskID)
                DropdownMenuItem(
                    text = { Text(task.title) },
                    onClick = {
                      if (isSelected) {
                        onDependencyRemoved(task.taskID)
                      } else {
                        onDependencyAdded(task.taskID)
                      }
                      expanded = false
                    },
                    enabled = !isSelected,
                    modifier =
                        Modifier.testTag(
                            "${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_${task.taskID}"))
              }
            }
      }
    } else if (availableTasks.isEmpty()) {
      Text(
          text = "No tasks available in this project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    // Show cycle error if present
    if (cycleError != null) {
      Text(
          text = cycleError,
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR))
    }
  }
}

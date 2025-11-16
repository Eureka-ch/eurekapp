package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters

// Portions of this code were generated with the help of AI.
// Portions added by Jiří Gebauer partially generated with the help of Grok.

/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/
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
  const val USER_ASSIGNMENT_TITLE = "user_assignment_title"
  const val USER_ASSIGNMENT_MENU = "user_assignment_menu"
  const val USER_CHECKBOX = "user_checkbox"
  const val NO_USERS_AVAILABLE = "no_users_available"
  const val TASK_DEPENDENCIES_FIELD = "task_dependencies_field"
  const val TASK_DEPENDENCY_ITEM = "task_dependency_item"
  const val REMOVE_DEPENDENCY = "remove_dependency"
  const val ADD_DEPENDENCY_BUTTON = "add_dependency_button"
  const val DEPENDENCY_CYCLE_ERROR = "dependency_cycle_error"
  const val OFFLINE_MESSAGE = "offline_message"

  const val BACK_BUTTON = "back_button"

  const val NOTE_INPUT_FIELD = "noteInputField"
  const val SEND_BUTTON = "sendButton"
}

// Note: NoteInputField composable was removed. The input field is now inlined in SelfNotesScreen.

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
    isReadOnly: Boolean = false,
    isConnected: Boolean = true
) {
  attachments.forEachIndexed { index, file ->
    Row(modifier = modifier) {
      if (!isConnected) {
        Text(
            "Photo ${index + 1}: Attachment available, but cannot be visualized offline.",
            modifier = Modifier.testTag(CommonTaskTestTags.OFFLINE_MESSAGE))
      } else {
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
      Box(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedProjectName,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("No project selected yet") },
            modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE),
            enabled = false)

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
fun UserAssignmentField(
    availableUsers: List<User>,
    selectedUserIds: List<String>,
    onUserToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedCount = selectedUserIds.size
  val displayText =
      when {
        selectedCount == 0 -> "No users assigned"
        selectedCount == 1 -> {
          val user = availableUsers.firstOrNull { it.uid == selectedUserIds.first() }
          user?.displayName?.ifBlank { user.email } ?: "1 user assigned"
        }
        else -> "$selectedCount users assigned"
      }

  Column(modifier = modifier) {
    Text(text = "Assign Users", style = MaterialTheme.typography.titleMedium)

    if (availableUsers.isEmpty()) {
      Text(
          text = "No users available in this project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.testTag(CommonTaskTestTags.NO_USERS_AVAILABLE))
    } else {
      Box(
          modifier =
              Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = !expanded }) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select users to assign") },
                modifier =
                    Modifier.fillMaxWidth().testTag(CommonTaskTestTags.USER_ASSIGNMENT_TITLE),
                enabled = false)

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.testTag(CommonTaskTestTags.USER_ASSIGNMENT_MENU)) {
                  availableUsers.forEach { user ->
                    DropdownMenuItem(
                        text = {
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = user.displayName.ifBlank { user.email },
                                    style = MaterialTheme.typography.bodyMedium)
                                Checkbox(
                                    checked = selectedUserIds.contains(user.uid),
                                    onCheckedChange = null,
                                    modifier =
                                        Modifier.testTag(
                                            "${CommonTaskTestTags.USER_CHECKBOX}_${user.uid}"))
                              }
                        },
                        onClick = { onUserToggled(user.uid) },
                        modifier = Modifier.testTag("user_item_${user.uid}"))
                  }
                }
          }
    }
  }
}

@Composable
fun TaskDependenciesSelectionField(
    availableTasks: List<ch.eureka.eurekapp.model.data.task.Task>,
    selectedDependencyIds: List<String>,
    onDependencyAdded: (String) -> Unit,
    onDependencyRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentTaskId: String = "",
    cycleError: String? = null
) {
  val selectableTasks =
      remember(availableTasks, currentTaskId) {
        availableTasks.filter { it.taskID != currentTaskId }
      }

  Column(modifier = modifier) {
    Text(text = "Task Dependencies", style = MaterialTheme.typography.titleMedium)
    DependencyList(
        selectedDependencyIds = selectedDependencyIds,
        availableTasks = availableTasks,
        onDependencyRemoved = onDependencyRemoved)
    DependencyPicker(
        selectableTasks = selectableTasks,
        selectedDependencyIds = selectedDependencyIds,
        onDependencyAdded = onDependencyAdded,
        onDependencyRemoved = onDependencyRemoved,
        hasProjectTasks = availableTasks.isNotEmpty())
    cycleError?.let {
      Text(
          text = it,
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR))
    }
  }
}

@Composable
private fun DependencyList(
    selectedDependencyIds: List<String>,
    availableTasks: List<ch.eureka.eurekapp.model.data.task.Task>,
    onDependencyRemoved: (String) -> Unit
) {
  if (selectedDependencyIds.isEmpty()) return

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    selectedDependencyIds.forEach { dependencyId ->
      val task = availableTasks.firstOrNull { it.taskID == dependencyId } ?: return@forEach
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
                modifier =
                    Modifier.testTag("${CommonTaskTestTags.REMOVE_DEPENDENCY}_$dependencyId")) {
                  Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove dependency")
                }
          }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DependencyPicker(
    selectableTasks: List<ch.eureka.eurekapp.model.data.task.Task>,
    selectedDependencyIds: List<String>,
    onDependencyAdded: (String) -> Unit,
    onDependencyRemoved: (String) -> Unit,
    hasProjectTasks: Boolean
) {
  var expanded by remember { mutableStateOf(false) }

  when {
    selectableTasks.isNotEmpty() -> {
      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON)) {
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
    }
    !hasProjectTasks -> {
      Text(
          text = "No tasks available in this project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

/**
 * Reusable input field for composing and sending messages.
 *
 * Displays a text field with a send button. The send button is disabled when the message is empty
 * or when a message is currently being sent.
 *
 * @param message Current message text.
 * @param onMessageChange Callback when the message text changes.
 * @param onSend Callback when the send button is clicked or Enter is pressed.
 * @param isSending Whether a message is currently being sent.
 * @param placeholder Placeholder text for the input field.
 * @param modifier Optional modifier.
 */
@Composable
fun MessageInputField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Write a message..."
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(Spacing.md),
      verticalAlignment = Alignment.Bottom) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f).testTag(CommonTaskTestTags.NOTE_INPUT_FIELD),
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions =
                KeyboardActions(onSend = { if (message.isNotBlank() && !isSending) onSend() }),
            maxLines = 4,
            shape = MaterialTheme.shapes.medium,
            colors = EurekaStyles.textFieldColors())

        IconButton(
            onClick = onSend,
            enabled = message.isNotBlank() && !isSending,
            modifier =
                Modifier.padding(start = Spacing.sm).testTag(CommonTaskTestTags.SEND_BUTTON)) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint =
                      if (message.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
            }
      }
}

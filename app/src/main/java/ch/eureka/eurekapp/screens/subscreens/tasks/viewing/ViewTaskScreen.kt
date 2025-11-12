package ch.eureka.eurekapp.screens.subscreens.tasks.viewing

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.tasks.ViewTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.AttachmentsList
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDescriptionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDueDateField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskTitleField
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object ViewTaskScreenTestTags {
  const val EDIT_TASK = "edit_task"
  const val ASSIGNED_USERS_SECTION = "assigned_users_section"
  const val ASSIGNED_USER_ITEM = "assigned_user_item"

  fun assignedUserItem(index: Int) = "${ASSIGNED_USER_ITEM}_$index"
}

/*
Portions of this code were generated with the help of Grok.
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

/**
 * A composable screen for viewing an existing task's details.
 *
 * @param projectId The ID of the project that contains the task.
 * @param taskId The ID of the task to be viewed.
 * @param navigationController The NavHostController for handling navigation actions.
 * @param viewTaskViewModel The ViewTaskViewModel instance for loading and displaying task details.
 */
@Composable
fun ViewTaskScreen(
    projectId: String,
    taskId: String,
    navigationController: NavHostController = rememberNavController(),
    viewTaskViewModel: ViewTaskViewModel = remember { ViewTaskViewModel(projectId, taskId) },
) {
  val viewTaskState by viewTaskViewModel.uiState.collectAsState()
  val errorMsg = viewTaskState.errorMsg
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      // Clean up photos when navigating away, if any (usually none in view mode as everything
      // should be already uploaded)
      viewTaskViewModel.deletePhotosOnDispose(context, viewTaskState.attachmentUris)
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
              TaskTitleField(
                  value = viewTaskState.title,
                  onValueChange = {},
                  hasTouched = false,
                  onFocusChanged = {},
                  readOnly = true)

              TaskDescriptionField(
                  value = viewTaskState.description,
                  onValueChange = {},
                  hasTouched = false,
                  onFocusChanged = {},
                  readOnly = true)

              TaskDueDateField(
                  value = viewTaskState.dueDate,
                  onValueChange = {},
                  hasTouched = false,
                  onFocusChanged = {},
                  dateRegex = viewTaskViewModel.dateRegex,
                  readOnly = true)

              Text(text = "Status: ${viewTaskState.status.name.replace("_", " ")}")

              // Display assigned users
              if (viewTaskState.assignedUsers.isNotEmpty()) {
                Column(modifier = Modifier.testTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)) {
                  Text(
                      text = "Assigned Users:",
                      style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                  viewTaskState.assignedUsers.forEachIndexed { index, user ->
                    Text(
                        text = "• ${user.displayName.ifBlank { user.email }}",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(ViewTaskScreenTestTags.assignedUserItem(index)))
                  }
                }
              }

              Button(
                  onClick = {
                    navigationController.navigate(Route.TasksSection.EditTask(projectId, taskId))
                  },
                  modifier = Modifier.testTag(ViewTaskScreenTestTags.EDIT_TASK),
                  colors = EurekaStyles.PrimaryButtonColors()) {
                    Text("Edit Task")
                  }

              AttachmentsList(
                  attachments = viewTaskState.attachmentUrls + viewTaskState.attachmentUris,
                  onDelete = null, // Pass null to indicate read-only mode
                  isReadOnly = true)
            }
      })
}

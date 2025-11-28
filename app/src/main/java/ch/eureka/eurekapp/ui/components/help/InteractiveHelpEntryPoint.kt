package ch.eureka.eurekapp.ui.components.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.defaultValuesNotificationSettingsKeys
import ch.eureka.eurekapp.model.notifications.NotificationSettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Partially written using AI.
enum class HelpContext {
  HOME_OVERVIEW,
  TASKS,
  MEETINGS,
  PROJECTS,
  CREATE_TASK
}

@Composable
fun InteractiveHelpEntryPoint(
    helpContext: HelpContext,
    modifier: Modifier = Modifier,
    userProvidedName: String? = null,
    chipShape: Shape = MaterialTheme.shapes.large,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel()
) {
  // Early return: if help is disabled, don't render anything at all
  val helpEnabledDefault =
      defaultValuesNotificationSettingsKeys.getOrDefault(
          UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP.name, true)
  val isHelpEnabled by
      notificationSettingsViewModel
          .getUserSetting(UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP)
          .collectAsState(helpEnabledDefault)

  // If help is disabled, return early - no UI will be rendered
  if (!isHelpEnabled) {
    return
  }

  val resolvedName =
      remember(userProvidedName) {
            when {
              !userProvidedName.isNullOrBlank() -> userProvidedName
              else -> Firebase.auth.currentUser?.displayName.orEmpty()
            }
          }
          .ifBlank { "there" }

  var isDialogOpen by rememberSaveable { mutableStateOf(false) }

  val helpContent = remember(resolvedName, helpContext) { helpContext.toHelpContent(resolvedName) }

  AssistChip(
      onClick = { isDialogOpen = true },
      label = { Text("Guide") },
      leadingIcon = {
        Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null)
      },
      modifier = modifier,
      shape = chipShape)

  if (isDialogOpen) {
    AlertDialog(
        onDismissRequest = { isDialogOpen = false },
        confirmButton = { TextButton(onClick = { isDialogOpen = false }) { Text("Got it!") } },
        title = { Text(helpContent.title) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(helpContent.intro)
            helpContent.steps.forEach { step ->
              Surface(
                  tonalElevation = 1.dp,
                  shape = MaterialTheme.shapes.medium,
                  color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                          Text(step.highlight, style = MaterialTheme.typography.titleSmall)
                          Text(step.detail, style = MaterialTheme.typography.bodyMedium)
                        }
                  }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You can disable this help from Preferences > Notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        })
  }
}

internal data class HelpContent(val title: String, val intro: String, val steps: List<HelpStep>)

internal data class HelpStep(val highlight: String, val detail: String)

internal fun HelpContext.toHelpContent(userName: String): HelpContent {
  return when (this) {
    HelpContext.HOME_OVERVIEW ->
        HelpContent(
            title = "Welcome $userName 👋",
            intro = "Hey $userName, let's take a quick tour of the important overview.",
            steps =
                listOf(
                    HelpStep(
                        "Summary cards",
                        "The three cards at the top give you a quick glance at your tasks, meetings, and active projects."),
                    HelpStep(
                        "Quick actions",
                        "Use the 'View all' buttons to open the full sections (Tasks, Meetings, Projects)."),
                    HelpStep(
                        "Interactive sections",
                        "Tap on a task, meeting, or project card to open the detailed view directly.")))
    HelpContext.TASKS ->
        HelpContent(
            title = "Task management",
            intro = "Hello $userName! Here's how to master your tasks quickly.",
            steps =
                listOf(
                    HelpStep(
                        "Filter bar",
                        "The chips at the top let you switch from 'My tasks' to 'Team', 'Today', etc."),
                    HelpStep(
                        "Action buttons",
                        "The 'Create task' and 'Auto assign' buttons help you start or distribute work."),
                    HelpStep(
                        "Interactive cards",
                        "Tap on a card to open the task; use the folder icon to manage files.")))
    HelpContext.MEETINGS ->
        HelpContent(
            title = "Mastering meetings",
            intro = "$userName, let's review what you can do here.",
            steps =
                listOf(
                    HelpStep(
                        "Upcoming/Past tabs",
                        "Navigate between your upcoming and past meetings to stay on track."),
                    HelpStep(
                        "Meeting card",
                        "Each card provides access to voting, directions, and follow-up actions."),
                    HelpStep(
                        "+ Button",
                        "The floating 'Add' button instantly creates a new meeting (if you're online).")))
    HelpContext.PROJECTS ->
        HelpContent(
            title = "Project view",
            intro = "Hello $userName, here's how to make the most of the project overview.",
            steps =
                listOf(
                    HelpStep(
                        "Project context",
                        "The view displays key information about the selected project to help you stay focused."),
                    HelpStep(
                        "Quick navigation",
                        "You can launch the camera or other project-specific actions from this page."),
                    HelpStep(
                        "Back to home",
                        "Use the bottom bar to quickly return to associated tasks or meetings.")))
    HelpContext.CREATE_TASK ->
        HelpContent(
            title = "Guided creation",
            intro = "$userName, let's go step by step to create your task.",
            steps =
                listOf(
                    HelpStep(
                        "Essential fields",
                        "Start with the title, description, and due date to provide context."),
                    HelpStep(
                        "Project & team",
                        "Choose a project and assign members. Once a project is selected, you can assign users."),
                    HelpStep(
                        "Task dependencies",
                        "Dependencies allow you to define the execution order: this task cannot start until its dependent tasks are completed. Select them from the list after choosing a project. The app automatically detects forbidden cycles."),
                    HelpStep(
                        "Attachments",
                        "Add attachments or photos from the bottom of the screen before saving.")))
  }
}

package ch.eureka.eurekapp.ui.components.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.defaultValuesNotificationSettingsKeys
import ch.eureka.eurekapp.model.notifications.NotificationSettingsViewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

// Partially written using AI.
enum class HelpContext {
  HOME_OVERVIEW,
  TASKS,
  MEETINGS,
  PROJECTS,
  CREATE_TASK,
  FILES_MANAGEMENT,
  MEETING_VOTES,
  TOKEN_ENTRY,
  VIEW_TASK,
  NOTES
}

/**
 * Interactive help entry point that displays a "Guide" chip and shows contextual help when clicked.
 *
 * @param helpContext The context for which help content should be displayed.
 * @param modifier Modifier for the chip.
 * @param userProvidedName Optional user name to personalize the help content. If not provided, the
 *   ViewModel will resolve it from Firebase.
 * @param chipShape Shape for the help chip.
 * @param notificationSettingsViewModel ViewModel for notification settings. Defaults to
 *   [viewModel()].
 * @param helpViewModel ViewModel for help state management. Defaults to [viewModel()].
 */
@Composable
fun InteractiveHelpEntryPoint(
    helpContext: HelpContext,
    modifier: Modifier = Modifier,
    userProvidedName: String? = null,
    chipShape: Shape = MaterialTheme.shapes.large,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(),
    helpViewModel: InteractiveHelpViewModel = viewModel()
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

  val resolvedName = remember(userProvidedName) { helpViewModel.resolveUserName(userProvidedName) }
  val isDialogOpen by helpViewModel.isDialogOpen.collectAsState()

  val helpContent = remember(resolvedName, helpContext) { helpContext.toHelpContent(resolvedName) }

  val contentColor =
      if (LocalContentColor.current == EColors.WhiteTextColor) EColors.WhiteTextColor
      else MaterialTheme.colorScheme.onSurface
  AssistChip(
      onClick = { helpViewModel.openDialog() },
      label = {
        CompositionLocalProvider(LocalContentColor provides contentColor) { Text("Guide") }
      },
      leadingIcon = {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
          Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null)
        }
      },
      modifier = modifier,
      shape = chipShape,
      colors =
          AssistChipDefaults.assistChipColors(
              leadingIconContentColor = contentColor, labelColor = contentColor))

  if (isDialogOpen) {
    AlertDialog(
        onDismissRequest = { helpViewModel.closeDialog() },
        confirmButton = {
          TextButton(onClick = { helpViewModel.closeDialog() }) { Text("Got it!") }
        },
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

/**
 * Wrapper composable that provides a Box layout with the help entry point positioned at the bottom
 * end. This eliminates the need to manually create Box and alignment in each screen.
 *
 * @param helpContext The context for which help content should be displayed.
 * @param content The main content of the screen.
 * @param modifier Modifier for the Box container.
 * @param userProvidedName Optional user name to personalize the help content.
 * @param helpPadding Padding around the help chip (defaults to Spacing.md).
 * @param position Position of the help button: BottomEnd (default) or TopEnd.
 */
@Composable
fun ScreenWithHelp(
    helpContext: HelpContext,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    userProvidedName: String? = null,
    helpPadding: PaddingValues = PaddingValues(Spacing.md),
    position: Alignment = Alignment.BottomEnd
) {
  Box(modifier = modifier.fillMaxSize()) {
    content()
    InteractiveHelpEntryPoint(
        helpContext = helpContext,
        userProvidedName = userProvidedName,
        modifier = Modifier.align(position).padding(helpPadding))
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
                        "The floating 'Add' button instantly creates a new meeting (if you're online)."),
                    HelpStep(
                        "File attachments",
                        "Upload files and documents to meetings. Use the 'Pick a File' button in the meeting details to attach relevant files that participants can view and download.")))
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
                        "Task templates",
                        "Use templates to speed up task creation. Select a template from the dropdown to automatically add custom fields (text, numbers, dates, selections). You can also create new templates with the '+ Create Template' button to standardize your workflow."),
                    HelpStep(
                        "Task dependencies",
                        "Dependencies allow you to define the execution order: this task cannot start until its dependent tasks are completed. Select them from the list after choosing a project. The app automatically detects forbidden cycles.")))
    HelpContext.FILES_MANAGEMENT ->
        HelpContent(
            title = "File management",
            intro = "Hello $userName! Here's how to manage your downloaded files.",
            steps =
                listOf(
                    HelpStep(
                        "View files",
                        "All your downloaded files are listed here. Images show a preview, other files show their name."),
                    HelpStep(
                        "Open files",
                        "Tap the 'Open' button to open a file with an appropriate app on your device."),
                    HelpStep(
                        "Delete files",
                        "Use the 'Delete' button to remove files you no longer need. You'll be asked to confirm before deletion.")))
    HelpContext.MEETING_VOTES ->
        HelpContent(
            title = "Meeting proposals voting",
            intro = "$userName, let's learn how to vote on meeting proposals.",
            steps =
                listOf(
                    HelpStep(
                        "View proposals",
                        "Each card shows a date/time and format proposal. Review all options before voting."),
                    HelpStep(
                        "Select preferences",
                        "Check the boxes for the proposals that work for you. You can select multiple options."),
                    HelpStep(
                        "Add new proposal",
                        "Use the '+' button to propose a new date/time or format if none of the existing options suit you."),
                    HelpStep(
                        "Confirm votes",
                        "Once you've made your selections, tap the checkmark button to save your votes.")))
    HelpContext.TOKEN_ENTRY ->
        HelpContent(
            title = "Join with token",
            intro = "Welcome $userName! Here's how to join a project using an invitation token.",
            steps =
                listOf(
                    HelpStep(
                        "Get your token",
                        "Ask your project administrator for an invitation token. It usually looks like: 7F4A-93KD-XX12"),
                    HelpStep(
                        "Enter token",
                        "Paste or type the token in the field above. Make sure there are no extra spaces."),
                    HelpStep(
                        "Validate",
                        "Tap the 'Validate' button to join the project. You'll be redirected once the token is accepted.")))
    HelpContext.VIEW_TASK ->
        HelpContent(
            title = "Viewing task details",
            intro = "$userName, here's what you can do on this task detail screen.",
            steps =
                listOf(
                    HelpStep(
                        "Task information",
                        "View all task details including title, description, due date, assignees, and status."),
                    HelpStep(
                        "Edit task",
                        "Use the 'Edit' button to modify task details, change assignees, or update dependencies."),
                    HelpStep(
                        "View dependencies",
                        "Tap 'View Dependencies' to see which tasks depend on this one and which tasks this one depends on."),
                    HelpStep(
                        "Attachments",
                        "View and download any files or photos attached to this task.")))
    HelpContext.NOTES ->
        HelpContent(
            title = "Personal notes",
            intro = "Hello $userName! Here's how to use your personal notes.",
            steps =
                listOf(
                    HelpStep(
                        "Cloud vs Local",
                        "Toggle the switch at the top to choose between cloud storage (syncs across devices) or local-only storage. In cloud mode, your notes are saved locally first and automatically synchronized when you're online. Even without internet, all your notes are safe and will sync once connection is restored."),
                    HelpStep(
                        "Add notes",
                        "Type your note in the input field at the bottom and send it. Notes appear in a chat-like interface."),
                    HelpStep(
                        "View history",
                        "Scroll through your notes to see your previous entries. The most recent notes appear at the bottom.")))
  }
}

package ch.eureka.eurekapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.determinePriority
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.task.getDueDateTag
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.components.IconTextRow
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.InteractiveHelpEntryPoint
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.home.HOME_ITEMS_LIMIT
import ch.eureka.eurekapp.ui.home.HomeOverviewUiState
import ch.eureka.eurekapp.ui.home.HomeOverviewViewModel
import ch.eureka.eurekapp.ui.meeting.MeetingCard
import ch.eureka.eurekapp.ui.meeting.MeetingCardConfig
import ch.eureka.eurekapp.ui.tasks.components.TaskSectionHeader
import com.google.firebase.Timestamp

// Part of this code and documentation were generated with the help of AI (ChatGPT 5.1).
object HomeOverviewTestTags {
  const val SCREEN = "homeOverviewScreen"
  const val LOADING_INDICATOR = "homeOverviewLoading"
  const val CTA_TASKS = "homeOverviewCtaTasks"
  const val CTA_MEETINGS = "homeOverviewCtaMeetings"
  const val CTA_PROJECTS = "homeOverviewCtaProjects"
  const val TASK_ITEM_PREFIX = "homeOverviewTask_"
  const val MEETING_ITEM_PREFIX = "homeOverviewMeeting_"
  const val PROJECT_ITEM_PREFIX = "homeOverviewProject_"
  const val PROJECT_LINK_PREFIX = "homeOverviewProjectLink_"

  fun getTaskItemTestTag(taskId: String): String {
    return "${TASK_ITEM_PREFIX}$taskId"
  }

  fun getMeetingItemTestTag(meetingId: String): String {
    return "${MEETING_ITEM_PREFIX}$meetingId"
  }

  fun getProjectItemTestTag(projectId: String): String {
    return "${PROJECT_ITEM_PREFIX}$projectId"
  }

  fun getProjectLinkTestTag(projectId: String): String {
    return "${PROJECT_LINK_PREFIX}$projectId"
  }
}

/**
 * Data class grouping all navigation callbacks for HomeOverviewScreen. This reduces the number of
 * parameters to comply with SonarQube rules.
 */
data class HomeOverviewActions(
    val onOpenProjects: () -> Unit = {},
    val onOpenTasks: () -> Unit = {},
    val onOpenMeetings: () -> Unit = {},
    val onTaskSelected: (projectId: String, taskId: String) -> Unit = { _, _ -> },
    val onMeetingSelected: (projectId: String, meetingId: String) -> Unit = { _, _ -> },
    val onProjectSelected: (projectId: String) -> Unit = {}
)

/**
 * Home overview entry screen.
 *
 * Provides a summary of tasks, meetings, and projects leveraging [HomeOverviewViewModel].
 */
internal object HomeOverviewTestOverrides {
  var uiState: HomeOverviewUiState? = null
}

@Composable
fun HomeOverviewScreen(
    modifier: Modifier = Modifier,
    actions: HomeOverviewActions = HomeOverviewActions(),
    homeOverviewViewModel: HomeOverviewViewModel = viewModel(),
) {
  val overrideState = HomeOverviewTestOverrides.uiState
  if (overrideState != null) {
    HomeOverviewScreenContainer(modifier = modifier, uiState = overrideState, actions = actions)
  } else {
    val uiState by homeOverviewViewModel.uiState.collectAsState()
    HomeOverviewScreenContainer(modifier = modifier, uiState = uiState, actions = actions)
  }
}

@Composable
private fun HomeOverviewScreenContainer(
    modifier: Modifier,
    uiState: HomeOverviewUiState,
    actions: HomeOverviewActions
) {
  Box(modifier = modifier.fillMaxSize()) {
    HomeOverviewLayout(modifier = Modifier.fillMaxSize(), uiState = uiState, actions = actions)
    InteractiveHelpEntryPoint(
        helpContext = HelpContext.HOME_OVERVIEW,
        userProvidedName = uiState.currentUserName,
        modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.md))
  }
}

/** Visible-for-testing layout that renders the actual home overview content based on [uiState]. */
@Composable
internal fun HomeOverviewLayout(
    modifier: Modifier = Modifier,
    uiState: HomeOverviewUiState,
    actions: HomeOverviewActions = HomeOverviewActions()
) {
  if (uiState.isLoading) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator(modifier = Modifier.testTag(HomeOverviewTestTags.LOADING_INDICATOR))
    }
    return
  }

  LazyColumn(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = Spacing.lg, vertical = Spacing.md)
              .testTag(HomeOverviewTestTags.SCREEN),
      verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        item {
          GreetingHeader(uiState.currentUserName, uiState.isConnected, uiState.error)
          Spacer(modifier = Modifier.height(Spacing.md))
          SummaryCardsRow(
              tasksCount = uiState.upcomingTasks.size,
              meetingsCount = uiState.upcomingMeetings.size,
              projectsCount = uiState.recentProjects.size,
          )
        }

        item {
          HomeSectionHeader(
              title = "Upcoming tasks",
              count = uiState.upcomingTasks.size,
              actionLabel = "View all",
              actionTestTag = HomeOverviewTestTags.CTA_TASKS,
              onActionClick = actions.onOpenTasks)
        }
        if (uiState.upcomingTasks.isEmpty()) {
          item { EmptyState(text = "No tasks assigned yet. Create one to get started.") }
        } else {
          items(uiState.upcomingTasks.take(HOME_ITEMS_LIMIT)) { task ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(HomeOverviewTestTags.getTaskItemTestTag(task.taskID))) {
                  TaskPreviewCard(
                      task = task,
                      onTaskClick = { actions.onTaskSelected(task.projectId, task.taskID) })
                }
          }
        }

        item {
          HomeSectionHeader(
              title = "Next meetings",
              count = uiState.upcomingMeetings.size,
              actionLabel = "Open meetings",
              actionTestTag = HomeOverviewTestTags.CTA_MEETINGS,
              onActionClick = actions.onOpenMeetings)
        }
        if (uiState.upcomingMeetings.isEmpty()) {
          item { EmptyState(text = "No upcoming meetings. Schedule one to keep your team synced.") }
        } else {
          items(uiState.upcomingMeetings.take(HOME_ITEMS_LIMIT)) { meeting ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(HomeOverviewTestTags.getMeetingItemTestTag(meeting.meetingID))) {
                  MeetingCard(
                      meeting = meeting,
                      config =
                          MeetingCardConfig(
                              isCurrentUserId = { false },
                              onClick = {
                                actions.onMeetingSelected(meeting.projectId, meeting.meetingID)
                              },
                              isConnected = uiState.isConnected))
                }
          }
        }

        item {
          HomeSectionHeader(
              title = "Recent projects",
              count = uiState.recentProjects.size,
              actionLabel = "Browse projects",
              actionTestTag = HomeOverviewTestTags.CTA_PROJECTS,
              onActionClick = actions.onOpenProjects)
        }
        if (uiState.recentProjects.isEmpty()) {
          item { EmptyState(text = "No projects yet. Create a project to organize your work.") }
        } else {
          items(uiState.recentProjects.take(HOME_ITEMS_LIMIT)) { project ->
            ProjectSummaryCard(
                project = project,
                onClick = { actions.onProjectSelected(project.projectId) },
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(vertical = Spacing.xs)
                        .testTag(HomeOverviewTestTags.getProjectItemTestTag(project.projectId)))
          }
        }
      }
}

@Composable
private fun GreetingHeader(name: String, isConnected: Boolean, error: String?) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = if (name.isNotEmpty()) "Hello $name" else "Welcome back",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface)
    val statusMessage =
        when {
          !isConnected -> "You are offline. Some data may be outdated."
          error != null -> error
          else -> null
        }
    statusMessage?.let {
      Text(
          text = it,
          style = MaterialTheme.typography.bodyMedium,
          color =
              if (!isConnected) MaterialTheme.colorScheme.tertiary
              else MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(top = Spacing.xs))
    }
  }
}

@Composable
private fun SummaryCardsRow(tasksCount: Int, meetingsCount: Int, projectsCount: Int) {
  Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
    EurekaInfoCard(
        title = "Upcoming tasks",
        primaryValue = "$tasksCount",
        secondaryValue = "Assigned to you",
        iconText = "✓")
    EurekaInfoCard(
        title = "Next meetings",
        primaryValue = "$meetingsCount",
        secondaryValue = "Scheduled",
        iconText = "🗓")
    EurekaInfoCard(
        title = "Recent projects",
        primaryValue = "$projectsCount",
        secondaryValue = "Active teams",
        iconText = "📁")
  }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    count: Int,
    actionLabel: String,
    actionTestTag: String?,
    onActionClick: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        TaskSectionHeader(modifier = Modifier.weight(1f), title = title, taskCount = count)
        val buttonModifier = actionTestTag?.let { Modifier.testTag(it) } ?: Modifier
        TextButton(onClick = onActionClick, modifier = buttonModifier) { Text(actionLabel) }
      }
}

@Composable
private fun EmptyState(text: String) {
  Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm))
}

@Composable
private fun TaskPreviewCard(task: Task, onTaskClick: () -> Unit) {
  val now = Timestamp.now()
  val daysUntilDue = getDaysUntilDue(task, now)
  val dueDate = daysUntilDue?.let { formatDueDate(it) } ?: "No due date"
  val dueDateTag = getDueDateTag(task, now)
  val priority = determinePriority(task, now)
  val progressValue =
      when (task.status) {
        TaskStatus.COMPLETED -> 1f
        TaskStatus.IN_PROGRESS -> 0.5f
        else -> 0f
      }
  val progressText =
      when (task.status) {
        TaskStatus.COMPLETED -> "100%"
        TaskStatus.IN_PROGRESS -> "50%"
        else -> "0%"
      }

  EurekaTaskCard(
      title = task.title.ifEmpty { "Untitled task" },
      dueDate = dueDate,
      dueDateTag = dueDateTag,
      assignee = if (task.assignedUserIds.isNotEmpty()) "Multiple assignees" else "Unassigned",
      priority = priority,
      progressText = progressText,
      progressValue = progressValue,
      isCompleted = task.status == TaskStatus.COMPLETED,
      onToggleComplete = {},
      onClick = onTaskClick,
      canToggleCompletion = false)
}

@Composable
private fun ProjectSummaryCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier,
      shape = CardDefaults.shape,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(Spacing.md)) {
          Text(
              text = project.name.ifEmpty { "Untitled project" },
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.height(Spacing.xs))
          IconTextRow(
              text = project.description.ifEmpty { "No description provided" },
              iconVector = Icons.Default.Description,
              iconColor = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(Spacing.xs))
          IconTextRow(
              text = "${project.memberIds.size} members",
              iconVector = Icons.Default.Person,
              iconColor = MaterialTheme.colorScheme.secondary)
          Spacer(modifier = Modifier.height(Spacing.sm))
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                ProjectStatusDisplay(project.status)
                TextButton(onClick = onClick) { Text("Open project") }
              }
          Text(
              text = "Go to overview",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              modifier =
                  Modifier.padding(top = Spacing.xs)
                      .clickable(onClick = onClick)
                      .testTag(HomeOverviewTestTags.getProjectLinkTestTag(project.projectId)))
        }
      }
}

package ch.eureka.eurekapp.screens

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
}

/**
 * Home overview entry screen.
 *
 * Provides a summary of tasks, meetings, and projects leveraging [HomeOverviewViewModel].
 */
@Composable
fun HomeOverviewScreen(
    modifier: Modifier = Modifier,
    onOpenProjects: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenMeetings: () -> Unit = {},
    onTaskSelected: (projectId: String, taskId: String) -> Unit = { _, _ -> },
    onMeetingSelected: (projectId: String, meetingId: String) -> Unit = { _, _ -> },
    onProjectSelected: (projectId: String) -> Unit = {},
    homeOverviewViewModel: HomeOverviewViewModel = viewModel(),
) {
  val uiState by homeOverviewViewModel.uiState.collectAsState()
  HomeOverviewLayout(
      modifier = modifier,
      uiState = uiState,
      onOpenProjects = onOpenProjects,
      onOpenTasks = onOpenTasks,
      onOpenMeetings = onOpenMeetings,
      onTaskSelected = onTaskSelected,
      onMeetingSelected = onMeetingSelected,
      onProjectSelected = onProjectSelected)
}

/** Visible-for-testing layout that renders the actual home overview content based on [uiState]. */
@Composable
internal fun HomeOverviewLayout(
    modifier: Modifier = Modifier,
    uiState: HomeOverviewUiState,
    onOpenProjects: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenMeetings: () -> Unit = {},
    onTaskSelected: (projectId: String, taskId: String) -> Unit = { _, _ -> },
    onMeetingSelected: (projectId: String, meetingId: String) -> Unit = { _, _ -> },
    onProjectSelected: (projectId: String) -> Unit = {}
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
              onActionClick = onOpenTasks)
        }
        if (uiState.upcomingTasks.isEmpty()) {
          item { EmptyState(text = "No tasks assigned yet. Create one to get started.") }
        } else {
          items(uiState.upcomingTasks.take(HOME_ITEMS_LIMIT)) { task ->
            TaskPreviewCard(
                task = task, onTaskClick = { onTaskSelected(task.projectId, task.taskID) })
          }
        }

        item {
          HomeSectionHeader(
              title = "Next meetings",
              count = uiState.upcomingMeetings.size,
              actionLabel = "Open meetings",
              onActionClick = onOpenMeetings)
        }
        if (uiState.upcomingMeetings.isEmpty()) {
          item { EmptyState(text = "No upcoming meetings. Schedule one to keep your team synced.") }
        } else {
          items(uiState.upcomingMeetings.take(HOME_ITEMS_LIMIT)) { meeting ->
            MeetingCard(
                meeting = meeting,
                config =
                    MeetingCardConfig(
                        isCurrentUserId = { false },
                        onClick = { onMeetingSelected(meeting.projectId, meeting.meetingID) },
                        isConnected = uiState.isConnected))
          }
        }

        item {
          HomeSectionHeader(
              title = "Recent projects",
              count = uiState.recentProjects.size,
              actionLabel = "Browse projects",
              onActionClick = onOpenProjects)
        }
        if (uiState.recentProjects.isEmpty()) {
          item { EmptyState(text = "No projects yet. Create a project to organize your work.") }
        } else {
          items(uiState.recentProjects.take(HOME_ITEMS_LIMIT)) { project ->
            ProjectSummaryCard(
                project = project, onClick = { onProjectSelected(project.projectId) })
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
    onActionClick: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        TaskSectionHeader(modifier = Modifier.weight(1f), title = title, taskCount = count)
        TextButton(onClick = onActionClick) { Text(actionLabel) }
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
private fun ProjectSummaryCard(project: Project, onClick: () -> Unit) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
      shape = CardDefaults.shape,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(Spacing.md)) {
          Text(
              text = project.name.ifEmpty { "Untitled project" },
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.height(Spacing.xs))
          InformationContainer(
              text = project.description.ifEmpty { "No description provided" },
              iconVector = Icons.Default.Description,
              iconColor = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(Spacing.xs))
          InformationContainer(
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
        }
      }
}

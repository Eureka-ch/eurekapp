package ch.eureka.eurekapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.determinePriority
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.task.getDueDateTag
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.ProjectSummaryCard
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.InteractiveHelpEntryPoint
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.home.HOME_ITEMS_LIMIT
import ch.eureka.eurekapp.ui.home.HomeOverviewUiState
import ch.eureka.eurekapp.ui.home.HomeOverviewViewModel
import ch.eureka.eurekapp.ui.meeting.MeetingCard
import ch.eureka.eurekapp.ui.meeting.MeetingCardConfig
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
  Scaffold(
      modifier = modifier.fillMaxSize(),
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.home_overview_title),
            actions = {
              InteractiveHelpEntryPoint(
                  helpContext = HelpContext.HOME_OVERVIEW,
                  userProvidedName = uiState.currentUserName)
            })
      }) { padding ->
        HomeOverviewLayout(
            modifier = Modifier.fillMaxSize().padding(padding),
            uiState = uiState,
            actions = actions)
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

  val listState = rememberLazyListState()

  LazyColumn(
      state = listState,
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
              .testTag(HomeOverviewTestTags.SCREEN),
      verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        item(key = "header") {
          GreetingHeader(uiState.currentUserName, uiState.isConnected, uiState.error)
        }

        item(key = "cards") {
          SummaryCardsRow(
              tasksCount = uiState.upcomingTasks.size,
              meetingsCount = uiState.upcomingMeetings.size,
              projectsCount = uiState.recentProjects.size)
        }

        item {
          HomeSectionHeader(
              title = stringResource(R.string.home_overview_section_upcoming_tasks),
              count = uiState.upcomingTasks.size,
              actionLabel = stringResource(R.string.home_overview_section_view_all),
              actionTestTag = HomeOverviewTestTags.CTA_TASKS,
              onActionClick = actions.onOpenTasks)
        }
        if (uiState.upcomingTasks.isEmpty()) {
          item { EmptyState(text = stringResource(R.string.home_overview_empty_tasks)) }
        } else {
          items(items = uiState.upcomingTasks.take(HOME_ITEMS_LIMIT), key = { it.taskID }) { task ->
            AnimatedVisibility(
                visible = true,
                enter =
                    fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(400, delayMillis = 100))) {
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
        }

        item {
          HomeSectionHeader(
              title = stringResource(R.string.home_overview_section_next_meetings),
              count = uiState.upcomingMeetings.size,
              actionLabel = stringResource(R.string.home_overview_section_open_meetings),
              actionTestTag = HomeOverviewTestTags.CTA_MEETINGS,
              onActionClick = actions.onOpenMeetings)
        }
        if (uiState.upcomingMeetings.isEmpty()) {
          item { EmptyState(text = stringResource(R.string.home_overview_empty_meetings)) }
        } else {
          items(items = uiState.upcomingMeetings.take(HOME_ITEMS_LIMIT), key = { it.meetingID }) {
              meeting ->
            AnimatedVisibility(
                visible = true,
                enter =
                    fadeIn(animationSpec = tween(400, delayMillis = 150)) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(400, delayMillis = 150))) {
                  Box(
                      modifier =
                          Modifier.fillMaxWidth()
                              .testTag(
                                  HomeOverviewTestTags.getMeetingItemTestTag(meeting.meetingID))) {
                        MeetingCard(
                            meeting = meeting,
                            config =
                                MeetingCardConfig(
                                    isCurrentUserId = { false },
                                    onClick = {
                                      actions.onMeetingSelected(
                                          meeting.projectId, meeting.meetingID)
                                    },
                                    isConnected = uiState.isConnected))
                      }
                }
          }
        }

        item {
          HomeSectionHeader(
              title = stringResource(R.string.home_overview_section_recent_projects),
              count = uiState.recentProjects.size,
              actionLabel = stringResource(R.string.home_overview_section_browse_projects),
              actionTestTag = HomeOverviewTestTags.CTA_PROJECTS,
              onActionClick = actions.onOpenProjects)
        }
        if (uiState.recentProjects.isEmpty()) {
          item { EmptyState(text = stringResource(R.string.home_overview_empty_projects)) }
        } else {
          items(items = uiState.recentProjects.take(HOME_ITEMS_LIMIT), key = { it.projectId }) {
              project ->
            AnimatedVisibility(
                visible = true,
                enter =
                    fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(400, delayMillis = 200))) {
                  ProjectSummaryCard(
                      project = project,
                      onClick = { actions.onProjectSelected(project.projectId) },
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(vertical = Spacing.xs)
                              .testTag(
                                  HomeOverviewTestTags.getProjectItemTestTag(project.projectId)),
                      actionButtonTestTag =
                          HomeOverviewTestTags.getProjectLinkTestTag(project.projectId))
                }
          }
        }
      }
}

@Composable
private fun GreetingHeader(name: String, isConnected: Boolean, error: String?) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = if (name.isNotEmpty()) stringResource(R.string.home_overview_greeting_hello, name) else stringResource(R.string.home_overview_greeting_welcome),
        style = MaterialTheme.typography.headlineLarge,
        color = Color(0xFF0F172A),
        fontWeight = FontWeight.Bold)
    val statusMessage =
        when {
          !isConnected -> stringResource(R.string.home_overview_offline_message)
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
  Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
    EurekaInfoCard(
        title = stringResource(R.string.home_overview_upcoming_tasks_title),
        primaryValue = "$tasksCount",
        secondaryValue = stringResource(R.string.home_overview_upcoming_tasks_subtitle),
        icon = Icons.Default.AssignmentTurnedIn,
        gradientStart = Color.White,
        gradientEnd = Color.White)
    EurekaInfoCard(
        title = stringResource(R.string.home_overview_upcoming_meetings_title),
        primaryValue = "$meetingsCount",
        secondaryValue = stringResource(R.string.home_overview_upcoming_meetings_subtitle),
        icon = Icons.Default.CalendarToday,
        gradientStart = Color.White,
        gradientEnd = Color.White)
    EurekaInfoCard(
        title = stringResource(R.string.home_overview_recent_projects_title),
        primaryValue = "$projectsCount",
        secondaryValue = stringResource(R.string.home_overview_recent_projects_subtitle),
        icon = Icons.Default.Folder,
        gradientStart = Color.White,
        gradientEnd = Color.White)
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
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold)
            if (count > 0) {
              Text(
                  text = stringResource(R.string.home_overview_items_count, count),
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF64748B),
                  modifier = Modifier.padding(top = 4.dp))
            }
          }
          val buttonModifier = actionTestTag?.let { Modifier.testTag(it) } ?: Modifier
          TextButton(
              onClick = onActionClick,
              modifier = buttonModifier,
              colors =
                  androidx.compose.material3.ButtonDefaults.textButtonColors(
                      contentColor = MaterialTheme.colorScheme.primary)) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
              }
        }
    Spacer(modifier = Modifier.height(16.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
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
  val dueDate = daysUntilDue?.let { formatDueDate(it) } ?: stringResource(R.string.home_overview_task_due_no_date)
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
        TaskStatus.COMPLETED -> stringResource(R.string.home_overview_task_progress_100)
        TaskStatus.IN_PROGRESS -> stringResource(R.string.home_overview_task_progress_50)
        else -> stringResource(R.string.home_overview_task_progress_0)
      }

  EurekaTaskCard(
      title = task.title.ifEmpty { stringResource(R.string.home_overview_task_untitled) },
      dueDate = dueDate,
      dueDateTag = dueDateTag,
      assignee = if (task.assignedUserIds.isNotEmpty()) stringResource(R.string.home_overview_task_multiple_assignees) else stringResource(R.string.home_overview_task_unassigned),
      priority = priority,
      progressText = progressText,
      progressValue = progressValue,
      isCompleted = task.status == TaskStatus.COMPLETED,
      onToggleComplete = {},
      onClick = onTaskClick,
      canToggleCompletion = false)
}

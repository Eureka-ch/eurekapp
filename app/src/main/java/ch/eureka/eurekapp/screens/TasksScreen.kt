package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.determinePriority
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.components.NavItem
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.tasks.TaskScreenFilter
import ch.eureka.eurekapp.ui.tasks.TaskScreenViewModel
import ch.eureka.eurekapp.ui.tasks.components.TaskActionButtons
import ch.eureka.eurekapp.ui.tasks.components.TaskSectionHeader
import com.google.firebase.Timestamp

/** Test tags used by UI tests. */
object TasksScreenTestTags {
  const val TASKS_SCREEN_CONTENT = "tasksScreenContent"
  const val TASKS_SCREEN_TEXT = "tasksScreenText"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val ERROR_MESSAGE = "errorMessage"
  const val EMPTY_STATE = "emptyState"
  const val TASK_LIST = "taskList"
  const val CREATE_TASK_BUTTON = "createTaskButton"
  const val AUTO_ASSIGN_BUTTON = "autoAssignButton"
  const val TASK_CARD = "taskCard"
}

data class TaskAndUsers(val task: Task, val users: List<User>)

/**
 * Render a task card with individual properties Uses ViewModel computed properties directly for
 * better performance Portions of this code were generated with the help of IA.
 */
@Composable
private fun TaskCard(
    taskAndUsers: TaskAndUsers,
    onToggleComplete: () -> Unit,
    onTaskClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
  val (task, users) = taskAndUsers
  val progressValue =
      when (task.status) {
        TaskStatus.COMPLETED -> 1.0f
        TaskStatus.IN_PROGRESS -> 0.5f
        TaskStatus.TODO -> 0.0f
        else -> 0.0f
      }
  val now = Timestamp.now()
  val daysUntilDue = getDaysUntilDue(task, now)

  EurekaTaskCard(
      title = task.title,
      assignee = if (users.isNotEmpty()) users[0].displayName else "Unassigned",
      progressText = "${(progressValue * 100).toInt()}%",
      progressValue = progressValue,
      isCompleted = task.status == TaskStatus.COMPLETED,
      dueDate = daysUntilDue?.let { formatDueDate(it) } ?: "No due date",
      priority = determinePriority(task, now),
      onToggleComplete = onToggleComplete,
      onClick = { onTaskClick(task.taskID, task.projectId) },
      modifier = modifier.testTag(TasksScreenTestTags.TASK_CARD))
}

/** Format due date for display */
fun formatDueDate(diffInDays: Long): String {

  return when {
    diffInDays < 0 -> "Overdue"
    diffInDays == 0L -> "Due today"
    diffInDays == 1L -> "Due tomorrow"
    diffInDays <= 7L -> "Due in $diffInDays days"
    else -> "Due in more than a week"
  }
}
/**
 * Main Tasks screen composable Displays user tasks with filtering and management capabilities
 *
 * @param onTaskClick Callback when a task is clicked
 * @param onCreateTaskClick Callback when create task button is clicked
 * @param onAutoAssignClick Callback when auto-assign button is clicked
 * @param onNavigate Callback for navigation
 * @param modifier Modifier for the composable
 * @param viewModel ViewModel for task state management
 */
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (String, String) -> Unit = { _, _ -> },
    onCreateTaskClick: () -> Unit = {},
    onAutoAssignClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: TaskScreenViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val selectedFilter by remember { derivedStateOf { uiState.selectedFilter } }
  val tasksAndUsers by remember { derivedStateOf { uiState.tasksAndUsers } }
  val errorMsg by remember { derivedStateOf { uiState.error } }

  fun setFilter(filter: TaskScreenFilter) {
    viewModel.setFilter(filter)
  }

  // Bottom navigation items
  val navItems =
      listOf(
          NavItem("Tasks", null),
          NavItem("Ideas", null),
          NavItem("Home", null),
          NavItem("Meetings", null),
          NavItem("Profile", null))

  Scaffold(modifier = modifier.fillMaxSize().testTag(TasksScreenTestTags.TASKS_SCREEN_CONTENT)) {
      innerPadding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = Spacing.md)) {
          // Header section
          Column(modifier = Modifier.padding(vertical = Spacing.md)) {
            Text(
                text = "Task",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))

            Text(
                text = "Manage and track your project tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs))

            // Action buttons
            TaskActionButtons(
                onCreateTaskClick = onCreateTaskClick,
                onAutoAssignClick = onAutoAssignClick,
                modifier = Modifier.padding(top = Spacing.md))
          }

          // Horizontal scrollable filter bar
          LazyRow(
              horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
              modifier = Modifier.padding(bottom = Spacing.sm)) {
                // Standard filters (Mine, Team, ThisWeek, All)
                items(TaskScreenFilter.Companion.values) { filter ->
                  FilterChip(
                      onClick = { setFilter(filter) },
                      label = { Text(filter.displayName) },
                      selected = filter == selectedFilter,
                      modifier = Modifier.testTag(getFilterTag(filter)),
                      colors =
                          FilterChipDefaults.filterChipColors(
                              selectedContainerColor = MaterialTheme.colorScheme.primary,
                              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                              containerColor = MaterialTheme.colorScheme.surface,
                              labelColor = MaterialTheme.colorScheme.onSurface))
                }

                // Project filters
                items(uiState.availableProjects) { project ->
                  val projectFilter = TaskScreenFilter.ByProject(project.projectId, project.name)
                  FilterChip(
                      onClick = { setFilter(projectFilter) },
                      label = { Text(project.name) },
                      selected =
                          uiState.selectedFilter is TaskScreenFilter.ByProject &&
                              (uiState.selectedFilter as TaskScreenFilter.ByProject).projectId ==
                                  project.projectId,
                      modifier = Modifier.testTag("filter_${project.projectId}"),
                      colors =
                          FilterChipDefaults.filterChipColors(
                              selectedContainerColor = MaterialTheme.colorScheme.secondary,
                              selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                              containerColor = MaterialTheme.colorScheme.surfaceVariant,
                              labelColor = MaterialTheme.colorScheme.onSurfaceVariant))
                }
              }

          // Loading state
          if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(TasksScreenTestTags.LOADING_INDICATOR))
                  Text(
                      text = "Loading tasks...",
                      modifier = Modifier.padding(top = Spacing.md),
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
          } else if (uiState.error != null) {
            // Error state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                  Text(
                      text = "Error: ${uiState.error}",
                      color = MaterialTheme.colorScheme.error,
                      modifier =
                          Modifier.padding(Spacing.md).testTag(TasksScreenTestTags.ERROR_MESSAGE))
                }
          } else {

            val currentTaskAndUsers =
                tasksAndUsers.filter { it.task.status != TaskStatus.COMPLETED }
            val completedTaskAndUsers =
                tasksAndUsers.filter { it.task.status == TaskStatus.COMPLETED }

            // Task count
            Text(
                text = "${currentTaskAndUsers.size} tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.md))

            // Task lists
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.testTag(TasksScreenTestTags.TASK_LIST)) {
                  taskSection(
                      title = "Current Tasks",
                      tasksAndUsers = currentTaskAndUsers,
                      viewModel = viewModel,
                      onTaskClick = onTaskClick)
                  taskSection(
                      title = "Recently Completed",
                      tasksAndUsers = completedTaskAndUsers,
                      viewModel = viewModel,
                      modifier = Modifier.padding(top = Spacing.lg),
                      onTaskClick = onTaskClick)
                  if (currentTaskAndUsers.isEmpty() && completedTaskAndUsers.isEmpty()) {
                    item {
                      Column(
                          modifier = Modifier.fillMaxSize(),
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "No tasks found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier.padding(Spacing.lg)
                                        .testTag(TasksScreenTestTags.EMPTY_STATE))
                            Text(
                                text = "Tasks will appear here when repository is connected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(Spacing.sm))
                          }
                    }
                  }
                }
          }
        }
  }
}

private fun LazyListScope.taskSection(
    title: String,
    tasksAndUsers: List<TaskAndUsers>,
    viewModel: TaskScreenViewModel,
    modifier: Modifier = Modifier,
    onTaskClick: (String, String) -> Unit = { _, _ -> }
) {
  if (tasksAndUsers.isEmpty()) return
  item { TaskSectionHeader(title = title, taskCount = tasksAndUsers.size, modifier = modifier) }
  items(tasksAndUsers, key = { it.task.taskID }) { taskAndUsers ->
    TaskCard(
        taskAndUsers,
        onToggleComplete = { viewModel.toggleTaskCompletion(taskAndUsers.task) },
        onTaskClick = onTaskClick)
  }
}

fun getFilterTag(filter: TaskScreenFilter): String {
  return "filter_${filter.displayName.lowercase().replace(" ", "_")}"
}

package ch.eureka.eurekapp.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.utils.TaskBusinessLogic
import ch.eureka.eurekapp.ui.components.EurekaBottomNav
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.NavItem
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.tasks.components.TaskActionButtons
import ch.eureka.eurekapp.ui.tasks.components.TaskSectionHeader

/** Test tags used by UI tests. */
object TasksScreenTestTags {
  const val TASKS_SCREEN_CONTENT = "tasksScreenContent"
  const val TASKS_SCREEN_TEXT = "tasksScreenText"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val ERROR_MESSAGE = "errorMessage"
  const val EMPTY_STATE = "emptyState"
  const val TASK_LIST = "taskList"
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
/**
 * Convert a Task to TaskUiModel for UI display This function handles the UI model conversion in the
 * UI layer
 */
@Composable
private fun Task.toTaskUiModel(): TaskUiModel {
  return TaskUiModel(
      task = this,
      template = null, // TODO: Load template if needed
      assignee = null, // TODO: Load assignee if needed
      progress = 0.0f,
      isCompleted = TaskBusinessLogic.isTaskCompleted(this) // Business logic in UI layer for now
      )
}

/**
 * Render a task card with proper UI model conversion Wraps EurekaTaskCard instantiation as
 * requested in code review
 */
@Composable
private fun TaskCard(task: Task, onToggleComplete: () -> Unit, modifier: Modifier = Modifier) {
  val taskUiModel = task.toTaskUiModel()

  EurekaTaskCard(
      title = taskUiModel.title,
      dueDate = TaskBusinessLogic.formatDueDate(taskUiModel.task.dueDate),
      assignee = taskUiModel.assigneeName,
      priority = TaskBusinessLogic.determinePriority(taskUiModel.task),
      progressText = taskUiModel.progressText,
      progressValue = taskUiModel.progressValue,
      isCompleted = taskUiModel.isCompleted,
      onToggleComplete = onToggleComplete,
      modifier = modifier)
}

@Composable
fun TasksScreen(
    onCreateTaskClick: () -> Unit = {},
    onAutoAssignClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel =
        viewModel(factory = TaskViewModel.provideFactory(MockTaskRepository()))
) {
  val uiState by viewModel.uiState.collectAsState()

  // Bottom navigation items
  val navItems =
      listOf(
          NavItem("Tasks", null),
          NavItem("Ideas", null),
          NavItem("Home", null),
          NavItem("Meetings", null),
          NavItem("Profile", null))

  Scaffold(
      topBar = { EurekaTopBar(title = "Tasks", modifier = Modifier.testTag("tasksTopBar")) },
      bottomBar = {
        EurekaBottomNav(currentRoute = "Tasks", onNavigate = onNavigate, navItems = navItems)
      },
      modifier = modifier.fillMaxSize().testTag(TasksScreenTestTags.TASKS_SCREEN_CONTENT)) {
          innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = Spacing.md)) {
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
                    val filterOptions = TaskFilterConstants.FILTER_OPTIONS
                    items(filterOptions) { option ->
                      val isSelected =
                          when (option) {
                            TaskFilterConstants.FILTER_MY_TASKS ->
                                uiState.selectedFilter == TaskFilter.MINE
                            TaskFilterConstants.FILTER_TEAM ->
                                uiState.selectedFilter == TaskFilter.TEAM
                            TaskFilterConstants.FILTER_THIS_WEEK ->
                                uiState.selectedFilter == TaskFilter.THIS_WEEK
                            TaskFilterConstants.FILTER_ALL ->
                                uiState.selectedFilter == TaskFilter.ALL
                            TaskFilterConstants.FILTER_PROJECT ->
                                uiState.selectedFilter == TaskFilter.PROJECT
                            else -> false
                          }

                      FilterChip(
                          onClick = {
                            when (option) {
                              TaskFilterConstants.FILTER_MY_TASKS ->
                                  viewModel.setFilter(TaskFilter.MINE)
                              TaskFilterConstants.FILTER_TEAM ->
                                  viewModel.setFilter(TaskFilter.TEAM)
                              TaskFilterConstants.FILTER_THIS_WEEK ->
                                  viewModel.setFilter(TaskFilter.THIS_WEEK)
                              TaskFilterConstants.FILTER_ALL -> viewModel.setFilter(TaskFilter.ALL)
                              TaskFilterConstants.FILTER_PROJECT ->
                                  viewModel.setFilter(TaskFilter.PROJECT)
                            }
                          },
                          label = { Text(option) },
                          selected = isSelected,
                          modifier =
                              Modifier.testTag("filter_${option.lowercase().replace(" ", "_")}"),
                          colors =
                              FilterChipDefaults.filterChipColors(
                                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                  containerColor = MaterialTheme.colorScheme.surface,
                                  labelColor = MaterialTheme.colorScheme.onSurface))
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
                              Modifier.padding(Spacing.md)
                                  .testTag(TasksScreenTestTags.ERROR_MESSAGE))
                    }
              } else {
                // Content
                val currentTasks = viewModel.getIncompleteTasks()
                val completedTasks = viewModel.getCompletedTasks()

                // Task count
                Text(
                    text = "${currentTasks.size} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md))

                // Task lists
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.testTag(TasksScreenTestTags.TASK_LIST)) {
                      // Current tasks section
                      if (currentTasks.isNotEmpty()) {
                        item {
                          TaskSectionHeader(title = "Current Tasks", taskCount = currentTasks.size)
                        }

                        items(currentTasks, key = { it.taskID }) { task ->
                          TaskCard(
                              task = task,
                              onToggleComplete = { viewModel.toggleTaskCompletion(task.taskID) })
                        }
                      }

                      // Completed tasks section
                      if (completedTasks.isNotEmpty()) {
                        item {
                          TaskSectionHeader(
                              title = "Recently Completed",
                              taskCount = completedTasks.size,
                              modifier = Modifier.padding(top = Spacing.lg))
                        }

                        items(completedTasks, key = { it.taskID }) { task ->
                          TaskCard(
                              task = task,
                              onToggleComplete = { viewModel.toggleTaskCompletion(task.taskID) })
                        }
                      }

                      // Empty state
                      if (currentTasks.isEmpty() && completedTasks.isEmpty()) {
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

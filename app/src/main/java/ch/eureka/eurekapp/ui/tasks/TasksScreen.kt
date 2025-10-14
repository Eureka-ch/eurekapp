package ch.eureka.eurekapp.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.EurekaBottomNav
import ch.eureka.eurekapp.ui.components.EurekaFilterBar
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.NavItem
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.tasks.components.EnhancedTaskCard
import ch.eureka.eurekapp.ui.tasks.components.TaskActionButtons
import ch.eureka.eurekapp.ui.tasks.components.TaskSectionHeader

/** Test tags used by UI tests. */
object TasksScreenTestTags {
  const val TASKS_SCREEN_CONTENT = "tasksScreenContent"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val ERROR_MESSAGE = "errorMessage"
  const val EMPTY_STATE = "emptyState"
  const val TASK_LIST = "taskList"
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreviewLight() {
  EurekaTheme(darkTheme = false) {
    Column(
        modifier = Modifier.padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          Text(
              text = "Task",
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Text(
              text = "Manage and track your project tasks",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = Spacing.xs))

          TaskActionButtons(
              onCreateTaskClick = {},
              onAutoAssignClick = {},
              modifier = Modifier.padding(top = Spacing.md))

          EurekaFilterBar(
              options = listOf("Me", "Team", "This week", "All", "Project"),
              selectedOption = "Me",
              onOptionSelected = {},
              modifier = Modifier.padding(bottom = Spacing.sm))

          Text(
              text = "0 tasks",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = Spacing.md))

          Text(
              text = "No tasks found",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(Spacing.lg))
        }
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
    onTaskClick: (String) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onAutoAssignClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
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
      topBar = { EurekaTopBar(title = "EUREKA") },
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)

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

              // Filter bar
              EurekaFilterBar(
                  options = listOf("Me", "Team", "This week", "All", "Project"),
                  selectedOption =
                      when (uiState.selectedFilter) {
                        TaskFilter.MINE -> "Me"
                        TaskFilter.ALL -> "All"
                        TaskFilter.PROJECT -> "Project"
                      },
                  onOptionSelected = { option ->
                    when (option) {
                      "Me" -> viewModel.setFilter(TaskFilter.MINE)
                      "All" -> viewModel.setFilter(TaskFilter.ALL)
                      "Project" -> viewModel.setFilter(TaskFilter.PROJECT)
                    // TODO: Implement other filters
                    }
                  },
                  modifier = Modifier.padding(bottom = Spacing.sm))

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
                val currentTasks =
                    when (uiState.selectedFilter) {
                      TaskFilter.MINE -> uiState.myTasks.filter { !it.isCompleted }
                      TaskFilter.ALL -> uiState.allTasks.filter { !it.isCompleted }
                      TaskFilter.PROJECT -> uiState.allTasks.filter { !it.isCompleted }
                    }

                val completedTasks =
                    when (uiState.selectedFilter) {
                      TaskFilter.MINE -> uiState.myTasks.filter { it.isCompleted }
                      TaskFilter.ALL -> uiState.allTasks.filter { it.isCompleted }
                      TaskFilter.PROJECT -> uiState.allTasks.filter { it.isCompleted }
                    }

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

                        items(currentTasks, key = { it.id }) { task ->
                          EnhancedTaskCard(task = task, onTaskClick = onTaskClick)
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

                        items(completedTasks, key = { it.id }) { task ->
                          EnhancedTaskCard(task = task, onTaskClick = onTaskClick)
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

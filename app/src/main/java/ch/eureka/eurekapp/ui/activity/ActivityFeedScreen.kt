/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity feed screen with filtering and real-time updates.
 *
 * @param projectId Project ID to show activities for
 * @param onActivityClick Callback when activity clicked (receives entityId)
 * @param onNavigateBack Back button callback
 * @param modifier Screen modifier
 * @param viewModel ActivityFeedViewModel (injected for testing)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    projectId: String,
    onActivityClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ActivityFeedViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Always use "global-activities" collection for cross-project visibility
  val globalProjectId = "global-activities"

  // Set mode but don't load activities until user selects filter
  LaunchedEffect(projectId) {
    viewModel.setCompactMode(false) // Full mode, not compact
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag("ActivityFeedScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Activity Feed",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            })
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Filter buttons
          Row(
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
              horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = uiState.filterEntityType == EntityType.PROJECT,
                    onClick = {
                      if (uiState.filterEntityType == EntityType.PROJECT) {
                        viewModel.clearFilters(globalProjectId)
                      } else {
                        viewModel.loadActivitiesByEntityType(globalProjectId, EntityType.PROJECT)
                      }
                    },
                    label = { Text("Projects") },
                    modifier = Modifier.testTag("ProjectsFilterChip"))

                FilterChip(
                    selected = uiState.filterEntityType == EntityType.MEETING,
                    onClick = {
                      if (uiState.filterEntityType == EntityType.MEETING) {
                        viewModel.clearFilters(globalProjectId)
                      } else {
                        viewModel.loadActivitiesByEntityType(globalProjectId, EntityType.MEETING)
                      }
                    },
                    label = { Text("Meetings") },
                    modifier = Modifier.testTag("MeetingsFilterChip"))
              }

          Box(modifier = Modifier.fillMaxSize()) {
            when {
              uiState.isLoading && uiState.activities.isEmpty() -> {
                // Initial loading state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
                      Spacer(modifier = Modifier.height(Spacing.md))
                      Text(
                          text = "Loading activities...",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
              }
              uiState.activities.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      if (uiState.filterEntityType == null) {
                        // No filter selected
                        Text(
                            text = "Select a filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("EmptyState"))
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Choose 'Projects' or 'Meetings' above to view activities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      } else {
                        // Filter selected but no results
                        Text(
                            text = "No activities yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("EmptyState"))
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Activities will appear here when actions are performed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }
              }
              else -> {
                // Activities list with date grouping
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("ActivitiesList"),
                    contentPadding = PaddingValues(vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                      // Group activities by date
                      val activitiesByDate =
                          uiState.activities.groupBy { activity ->
                            val calendar = Calendar.getInstance()
                            calendar.time = activity.timestamp.toDate()
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            calendar.timeInMillis
                          }

                      activitiesByDate.forEach { (dateMillis, activities) ->
                        // Date header
                        item(key = "header_$dateMillis") {
                          Text(
                              text = formatDateHeader(dateMillis),
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.SemiBold,
                              color = MaterialTheme.colorScheme.primary,
                              modifier =
                                  Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                                      .testTag("DateHeader_$dateMillis"))
                        }

                        // Activities for this date
                        items(items = activities, key = { it.activityId }) { activity ->
                          ActivityCard(
                              activity = activity,
                              onClick = { onActivityClick(activity.entityId) },
                              onDelete = {
                                viewModel.deleteActivity(globalProjectId, activity.activityId)
                              },
                              modifier = Modifier.padding(horizontal = Spacing.md))
                        }
                      }
                    }
              }
            }

            // Error message overlay
            uiState.errorMsg?.let { error ->
              Box(
                  modifier =
                      Modifier.fillMaxWidth().padding(Spacing.md).align(Alignment.BottomCenter)) {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().testTag("ErrorMessage")) {
                          Row(
                              modifier = Modifier.padding(Spacing.md),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Error: $error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f))
                              }
                        }
                  }
            }
          }
        }
      }
}

/** Format date for headers (Today, Yesterday, or MMM dd, yyyy). */
private fun formatDateHeader(dateMillis: Long): String {
  val today = Calendar.getInstance()
  today.set(Calendar.HOUR_OF_DAY, 0)
  today.set(Calendar.MINUTE, 0)
  today.set(Calendar.SECOND, 0)
  today.set(Calendar.MILLISECOND, 0)

  val yesterday = Calendar.getInstance()
  yesterday.add(Calendar.DAY_OF_YEAR, -1)
  yesterday.set(Calendar.HOUR_OF_DAY, 0)
  yesterday.set(Calendar.MINUTE, 0)
  yesterday.set(Calendar.SECOND, 0)
  yesterday.set(Calendar.MILLISECOND, 0)

  return when (dateMillis) {
    today.timeInMillis -> "Today"
    yesterday.timeInMillis -> "Yesterday"
    else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(dateMillis)
  }
}

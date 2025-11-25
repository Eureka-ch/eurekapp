/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/**
 * Compact activity feed widget for dashboard embedding.
 *
 * Shows recent activities in a compact format suitable for dashboard display. Includes a "See All"
 * button to navigate to the full activity feed screen.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @param projectId ID of the project to show activities for
 * @param maxItems Maximum number of activities to display (default 5)
 * @param onSeeAllClick Callback when "See All" button is clicked
 * @param onActivityClick Callback when an activity card is clicked
 * @param modifier Modifier for the widget
 * @param viewModel ViewModel for activity feed (injected for testing)
 */
@Composable
fun ActivityFeedWidget(
    projectId: String,
    maxItems: Int = 5,
    onSeeAllClick: () -> Unit = {},
    onActivityClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ActivityFeedViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Load activities in compact mode when composable is first displayed
  LaunchedEffect(projectId) {
    viewModel.setCompactMode(true)
    viewModel.loadActivities(projectId)
  }

  Column(modifier = modifier.fillMaxWidth().testTag("ActivityFeedWidget")) {
    // Header with title and "See All" button
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "Recent Activity",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          TextButton(
              onClick = onSeeAllClick, modifier = Modifier.testTag("SeeAllButton")) {
                Text(text = "See All")
              }
        }

    // Content: Activities list or loading/empty state
    when {
      uiState.isLoading -> {
        // Loading state
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            horizontalArrangement = Arrangement.Center) {
              CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
            }
      }
      uiState.activities.isEmpty() -> {
        // Empty state
        Text(
            text = "No recent activity",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg).testTag("EmptyState"))
      }
      else -> {
        // Activities list
        LazyColumn(
            modifier = Modifier.testTag("ActivitiesList"),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
              items(
                  items = uiState.activities.take(maxItems),
                  key = { activity -> activity.activityId }) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.entityId) },
                        modifier = Modifier.padding(horizontal = Spacing.md))
                  }
            }
      }
    }

    // Error message if any
    uiState.errorMsg?.let { error ->
      Text(
          text = "Error: $error",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(Spacing.md).testTag("ErrorMessage"))
    }
  }
}

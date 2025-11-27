/*
 * This file was co-authored by Claude Code and Gemini.
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
 * Activity feed screen. Shows a unified feed of activities for the current user.
 *
 * @param modifier Modifier used in the whole screen.
 * @param onActivityClick Callback executed when an activity is clicked.
 * @param viewModel The view model associated with that screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    modifier: Modifier = Modifier,
    onActivityClick: (String) -> Unit = {},
    viewModel: ActivityFeedViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Load the global feed on entry
  LaunchedEffect(Unit) {
    viewModel.setCompactMode(false)
    viewModel.loadActivities()
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

                // PROJECTS FILTER
                FilterChip(
                    selected = uiState.filterEntityType == EntityType.PROJECT,
                    onClick = {
                      if (uiState.filterEntityType == EntityType.PROJECT) {
                        viewModel.clearFilters()
                      } else {
                        viewModel.applyFilter(EntityType.PROJECT)
                      }
                    },
                    label = { Text("Projects") },
                    modifier = Modifier.testTag("ProjectsFilterChip"))

                // MEETINGS FILTER
                FilterChip(
                    selected = uiState.filterEntityType == EntityType.MEETING,
                    onClick = {
                      if (uiState.filterEntityType == EntityType.MEETING) {
                        viewModel.clearFilters()
                      } else {
                        viewModel.applyFilter(EntityType.MEETING)
                      }
                    },
                    label = { Text("Meetings") },
                    modifier = Modifier.testTag("MeetingsFilterChip"))
              }

          Box(modifier = Modifier.fillMaxSize()) {
            when {
              uiState.isLoading && uiState.activities.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
                      Spacer(modifier = Modifier.height(Spacing.md))
                      Text("Loading activities...")
                    }
              }
              uiState.activities.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                          text = "No activities found",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.testTag("EmptyState"))
                    }
              }
              else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("ActivitiesList"),
                    contentPadding = PaddingValues(vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                      uiState.activitiesByDate.forEach { (dateMillis, activities) ->
                        item(key = "header_$dateMillis") {
                          Text(
                              text = formatDateHeader(dateMillis),
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.SemiBold,
                              color = MaterialTheme.colorScheme.primary,
                              modifier =
                                  Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm))
                        }

                        items(items = activities, key = { it.activityId }) { activity ->
                          ActivityCard(
                              activity = activity,
                              onClick = { onActivityClick(activity.entityId) },
                              onDelete = { viewModel.deleteActivity(activity.activityId) },
                              modifier = Modifier.padding(horizontal = Spacing.md))
                        }
                      }
                    }
              }
            }

            uiState.errorMsg?.let { error ->
              Box(
                  modifier =
                      Modifier.fillMaxWidth().padding(Spacing.md).align(Alignment.BottomCenter)) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                  }
            }
          }
        }
      }
}

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

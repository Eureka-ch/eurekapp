/*
 * This file was co-authored by Claude Code and Gemini.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity feed screen. Shows a unified feed of activities for the current user.
 *
 * @param modifier Modifier used in the whole screen.
 * @param onActivityClick Callback executed when an activity is clicked (activityId, projectId).
 * @param viewModel The view model associated with that screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    modifier: Modifier = Modifier,
    onActivityClick: (String, String) -> Unit = { _, _ -> },
    viewModel: ActivityFeedViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  var searchExpanded by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { viewModel.setCompactMode(false) }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag("ActivityFeedScreen"),
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.activity_feed_title),
            actions = {
              IconButton(
                  onClick = { viewModel.refresh() }, modifier = Modifier.testTag("RefreshButton")) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.activity_feed_refresh_button),
                        tint = EColors.WhiteTextColor)
                  }
              IconButton(
                  onClick = { searchExpanded = !searchExpanded },
                  modifier = Modifier.testTag("SearchButton")) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.activity_feed_search_button),
                        tint = EColors.WhiteTextColor)
                  }
              if (uiState.activities.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.markAllAsRead() },
                    modifier = Modifier.testTag("MarkAllReadButton")) {
                      Icon(
                          imageVector = Icons.Default.DoneAll,
                          contentDescription = stringResource(R.string.activity_feed_mark_all_read_button),
                          tint = EColors.WhiteTextColor)
                    }
              }
            })
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Search bar
          ActivitySearchBar(
              query = uiState.searchQuery,
              onQueryChange = { viewModel.applySearch(it) },
              expanded = searchExpanded)

          // Entity type filter chips
          FilterChipsRow(
              currentEntityFilter = uiState.filterEntityType,
              onEntityFilterClick = { entityType ->
                if (uiState.filterEntityType == entityType) viewModel.clearFilters()
                else viewModel.applyEntityTypeFilter(entityType)
              })

          // Activity content
          ActivityContent(
              uiState = uiState,
              onActivityClick = onActivityClick,
              onDeleteActivity = { viewModel.deleteActivity(it) },
              onMarkAsRead = { viewModel.markAsRead(it) })
        }
      }
}

@Composable
private fun FilterChipsRow(
    currentEntityFilter: EntityType?,
    onEntityFilterClick: (EntityType) -> Unit
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = Spacing.md, vertical = Spacing.sm),
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        ActivityTypeFilterChip(
            label = stringResource(R.string.activity_filter_projects),
            selected = currentEntityFilter == EntityType.PROJECT,
            onClick = { onEntityFilterClick(EntityType.PROJECT) },
            modifier = Modifier.testTag("ProjectsFilterChip"))

        ActivityTypeFilterChip(
            label = stringResource(R.string.activity_filter_meetings),
            selected = currentEntityFilter == EntityType.MEETING,
            onClick = { onEntityFilterClick(EntityType.MEETING) },
            modifier = Modifier.testTag("MeetingsFilterChip"))

        ActivityTypeFilterChip(
            label = stringResource(R.string.activity_filter_messages),
            selected = currentEntityFilter == EntityType.MESSAGE,
            onClick = { onEntityFilterClick(EntityType.MESSAGE) },
            modifier = Modifier.testTag("MessagesFilterChip"))

        ActivityTypeFilterChip(
            label = stringResource(R.string.activity_filter_files),
            selected = currentEntityFilter == EntityType.FILE,
            onClick = { onEntityFilterClick(EntityType.FILE) },
            modifier = Modifier.testTag("FilesFilterChip"))

        ActivityTypeFilterChip(
            label = stringResource(R.string.activity_filter_tasks),
            selected = currentEntityFilter == EntityType.TASK,
            onClick = { onEntityFilterClick(EntityType.TASK) },
            modifier = Modifier.testTag("TasksFilterChip"))
      }
}

@Composable
private fun ActivityContent(
    uiState: ActivityFeedUIState,
    onActivityClick: (String, String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onMarkAsRead: (String) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize()) {
    when {
      uiState.isLoading && uiState.activities.isEmpty() -> LoadingState()
      uiState.activities.isEmpty() ->
          EmptyState(
              hasFilters =
                  uiState.filterEntityType != null ||
                      uiState.filterActivityType != null ||
                      uiState.searchQuery.isNotBlank())
      else ->
          ActivitiesList(
              activitiesByDate = uiState.activitiesByDate,
              readActivityIds = uiState.readActivityIds,
              onActivityClick = onActivityClick,
              onDeleteActivity = onDeleteActivity,
              onMarkAsRead = onMarkAsRead)
    }

    uiState.errorMsg?.let { error -> ErrorMessage(error, Modifier.align(Alignment.BottomCenter)) }
  }
}

@Composable
private fun LoadingState() {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(stringResource(R.string.activity_feed_loading))
      }
}

@Composable
private fun EmptyState(hasFilters: Boolean = false) {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text =
                if (hasFilters) stringResource(R.string.activity_feed_empty_with_filters)
                else stringResource(R.string.activity_feed_empty_no_filters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("EmptyState"))

        if (hasFilters) {
          Spacer(modifier = Modifier.height(Spacing.sm))
          Text(
              text = stringResource(R.string.activity_feed_empty_hint_with_filters),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          Spacer(modifier = Modifier.height(Spacing.sm))
          Text(
              text = stringResource(R.string.activity_feed_empty_hint_no_filters),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

@Composable
private fun ActivitiesList(
    activitiesByDate: Map<Long, List<ch.eureka.eurekapp.model.data.activity.Activity>>,
    readActivityIds: Set<String>,
    onActivityClick: (String, String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onMarkAsRead: (String) -> Unit
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().testTag("ActivitiesList"),
      contentPadding = PaddingValues(vertical = Spacing.md),
      verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        activitiesByDate.forEach { (dateMillis, activities) ->
          item(key = "header_$dateMillis") {
            Text(
                text = formatDateHeader(dateMillis),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm))
          }

          items(items = activities, key = { it.activityId }) { activity ->
            val isRead = readActivityIds.contains(activity.activityId)
            ActivityCard(
                activity = activity,
                isRead = isRead,
                modifier = Modifier.padding(horizontal = Spacing.md),
                onClick = { activityId ->
                  // Mark as read on click
                  if (!isRead) {
                    onMarkAsRead(activityId)
                  }
                  onActivityClick(activityId, activity.projectId)
                },
                onDelete = { onDeleteActivity(activity.activityId) })
          }
        }
      }
}

@Composable
private fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth().padding(Spacing.md)) {
    Text(stringResource(R.string.activity_feed_error_prefix) + error, color = MaterialTheme.colorScheme.error)
  }
}

private fun formatDateHeader(dateMillis: Long): String {
  val today = Calendar.getInstance()
  today[Calendar.HOUR_OF_DAY] = 0
  today[Calendar.MINUTE] = 0
  today[Calendar.SECOND] = 0
  today[Calendar.MILLISECOND] = 0

  val yesterday = Calendar.getInstance()
  yesterday.add(Calendar.DAY_OF_YEAR, -1)
  yesterday[Calendar.HOUR_OF_DAY] = 0
  yesterday[Calendar.MINUTE] = 0
  yesterday[Calendar.SECOND] = 0
  yesterday[Calendar.MILLISECOND] = 0

  return when (dateMillis) {
    today.timeInMillis -> "Today"
    yesterday.timeInMillis -> "Yesterday"
    else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(dateMillis)
  }
}

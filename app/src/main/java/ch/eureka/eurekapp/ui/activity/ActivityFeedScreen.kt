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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

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
  var searchExpanded by remember { mutableStateOf(false) }

  // Set compact mode on entry, but don't load activities until filter is selected
  LaunchedEffect(Unit) { viewModel.setCompactMode(false) }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag("ActivityFeedScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Activity Feed",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            actions = {
              // Refresh button
              IconButton(
                  onClick = { viewModel.refresh() }, modifier = Modifier.testTag("RefreshButton")) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                  }

              // Search toggle button
              IconButton(
                  onClick = { searchExpanded = !searchExpanded },
                  modifier = Modifier.testTag("SearchButton")) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Toggle search")
                  }

              // Mark all as read button
              if (uiState.activities.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.markAllAsRead() },
                    modifier = Modifier.testTag("MarkAllReadButton")) {
                      Icon(
                          imageVector = Icons.Default.DoneAll,
                          contentDescription = "Mark all as read")
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
        listOf(
                EntityType.PROJECT,
                EntityType.MEETING,
                EntityType.MESSAGE,
                EntityType.FILE,
                EntityType.TASK)
            .forEach { entityType ->
              ActivityTypeFilterChip(
                  label = entityType.toDisplayString(),
                  selected = currentEntityFilter == entityType,
                  onClick = { onEntityFilterClick(entityType) },
                  modifier = Modifier.testTag("${entityType.toDisplayString()}FilterChip"))
            }
      }
}

@Composable
private fun ActivityContent(
    uiState: ActivityFeedUIState,
    onActivityClick: (String) -> Unit,
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
        Text("Loading activities...")
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
                if (hasFilters) "No activities match your filters"
                else "Select a filter to view activities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("EmptyState"))

        if (hasFilters) {
          Spacer(modifier = Modifier.height(Spacing.sm))
          Text(
              text = "Try adjusting your filters or search query",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          Spacer(modifier = Modifier.height(Spacing.sm))
          Text(
              text = "Choose Projects, Meetings, Messages, Files, or Tasks above",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

@Composable
private fun ActivitiesList(
    activitiesByDate: Map<Long, List<ch.eureka.eurekapp.model.data.activity.Activity>>,
    readActivityIds: Set<String>,
    onActivityClick: (String) -> Unit,
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
                onClick = {
                  // Mark as read on click
                  if (!isRead) {
                    onMarkAsRead(activity.activityId)
                  }
                  onActivityClick(activity.activityId)
                },
                onDelete = { onDeleteActivity(activity.activityId) })
          }
        }
      }
}

@Composable
private fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth().padding(Spacing.md)) {
    Text("Error: $error", color = MaterialTheme.colorScheme.error)
  }
}

/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType

/**
 * A filter chip for filtering activities by type.
 *
 * This component displays a clickable chip that can be selected/deselected to filter
 * activities. It shows a visual indication when selected.
 *
 * @param label The text to display on the chip.
 * @param selected Whether this filter is currently selected.
 * @param onClick Callback when the chip is clicked.
 * @param modifier Optional modifier for styling.
 */
@Composable
fun ActivityTypeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  FilterChip(
      selected = selected,
      onClick = onClick,
      label = { Text(label) },
      modifier = modifier.padding(horizontal = 4.dp),
      colors =
          FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
}

/**
 * Helper function to get display label for EntityType.
 */
fun EntityType.toDisplayString(): String {
  return when (this) {
    EntityType.MEETING -> "Meetings"
    EntityType.MESSAGE -> "Messages"
    EntityType.FILE -> "Files"
    EntityType.TASK -> "Tasks"
    EntityType.PROJECT -> "Projects"
    EntityType.MEMBER -> "Members"
  }
}

/**
 * Helper function to get display label for ActivityType.
 */
fun ActivityType.toDisplayString(): String {
  return when (this) {
    ActivityType.CREATED -> "Created"
    ActivityType.UPDATED -> "Updated"
    ActivityType.DELETED -> "Deleted"
    ActivityType.UPLOADED -> "Uploaded"
    ActivityType.SHARED -> "Shared"
    ActivityType.COMMENTED -> "Commented"
    ActivityType.STATUS_CHANGED -> "Status Changed"
    ActivityType.JOINED -> "Joined"
    ActivityType.LEFT -> "Left"
    ActivityType.ASSIGNED -> "Assigned"
    ActivityType.UNASSIGNED -> "Unassigned"
    ActivityType.ROLE_CHANGED -> "Role Changed"
    ActivityType.DOWNLOADED -> "Downloaded"
  }
}

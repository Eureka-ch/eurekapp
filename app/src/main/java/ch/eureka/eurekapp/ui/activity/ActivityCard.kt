/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
*/
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Card component for displaying a single activity item.
 *
 * Shows the activity type, entity, timestamp, and relevant metadata in a compact card format.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @param activity The activity to display
 * @param onClick Callback when the card is clicked
 * @param onDelete Callback when the delete button is clicked
 * @param modifier Modifier for the card
 */
@Composable
fun ActivityCard(
    activity: Activity,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = EurekaStyles.taskCardColors(),
      border = EurekaStyles.taskCardBorder(),
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(role = Role.Button, onClick = onClick)
              .testTag("ActivityCard_${activity.activityId}")) {
        Row(
            modifier = Modifier.padding(Spacing.md).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top) {
              // Icon indicator
              ActivityIcon(
                  activityType = activity.activityType,
                  entityType = activity.entityType,
                  modifier = Modifier.align(Alignment.Top))

              // Activity content
              Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Activity description
                    Text(
                        text = getActivityDescription(activity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis)

                    // Metadata (entity title if available)
                    activity.metadata["title"]?.let { title ->
                      Text(
                          text = title.toString(),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                    }

                    // Timestamp
                    Text(
                        text = formatTimestamp(activity.timestamp.toDate().time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }

              // Delete button
              IconButton(
                  onClick = onDelete,
                  modifier =
                      Modifier.align(Alignment.Top)
                          .testTag("DeleteButton_${activity.activityId}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete activity",
                        tint = MaterialTheme.colorScheme.error)
                  }
            }
      }
}

/**
 * Icon component showing the activity and entity type.
 *
 * @param activityType Type of activity (CREATED, UPDATED, etc.)
 * @param entityType Type of entity (MEETING, MESSAGE, etc.)
 * @param modifier Modifier for the icon
 */
@Composable
private fun ActivityIcon(
    activityType: ActivityType,
    entityType: EntityType,
    modifier: Modifier = Modifier
) {
  val (icon, color) = getIconAndColor(activityType, entityType)

  Surface(modifier = modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.12f)) {
    Icon(
        imageVector = icon,
        contentDescription = "$activityType $entityType",
        tint = color,
        modifier = Modifier.padding(8.dp))
  }
}

/**
 * Get the appropriate icon and color for the activity.
 *
 * @param activityType Type of activity
 * @param entityType Type of entity
 * @return Pair of icon and color
 */
private fun getIconAndColor(
    activityType: ActivityType,
    entityType: EntityType
): Pair<ImageVector, Color> {
  return when (activityType) {
    ActivityType.CREATED ->
        when (entityType) {
          EntityType.MEETING -> Icons.Default.CalendarToday to EColors.light.primary
          EntityType.MESSAGE -> Icons.AutoMirrored.Filled.Message to Color(0xFF4CAF50)
          EntityType.FILE -> Icons.Default.FileUpload to Color(0xFF2196F3)
          else -> Icons.AutoMirrored.Filled.Article to EColors.light.primary
        }
    ActivityType.UPDATED -> Icons.Default.Edit to Color(0xFFFF9800)
    ActivityType.DELETED -> Icons.Default.Delete to Color(0xFFF44336)
    ActivityType.UPLOADED -> Icons.Default.Upload to Color(0xFF2196F3)
    ActivityType.JOINED -> Icons.Default.PersonAdd to Color(0xFF4CAF50)
    ActivityType.LEFT -> Icons.AutoMirrored.Filled.ExitToApp to Color(0xFFFF9800)
    else -> Icons.AutoMirrored.Filled.Article to EColors.light.onSurfaceVariant
  }
}

/**
 * Generate a human-readable description of the activity.
 *
 * @param activity The activity to describe
 * @return Description string
 */
private fun getActivityDescription(activity: Activity): String {
  // Get user name from metadata, or use a placeholder
  val userName = activity.metadata["userName"]?.toString() ?: "Someone"

  val action =
      when (activity.activityType) {
        ActivityType.CREATED -> "created"
        ActivityType.UPDATED -> "updated"
        ActivityType.DELETED -> "deleted"
        ActivityType.UPLOADED -> "uploaded"
        ActivityType.SHARED -> "shared"
        ActivityType.COMMENTED -> "commented on"
        ActivityType.STATUS_CHANGED -> "changed status of"
        ActivityType.JOINED -> "joined"
        ActivityType.LEFT -> "left"
      }

  val entity =
      when (activity.entityType) {
        EntityType.MEETING -> "a meeting"
        EntityType.MESSAGE -> "a message"
        EntityType.FILE -> "a file"
        EntityType.TASK -> "a task"
        EntityType.PROJECT -> "the project"
        EntityType.MEMBER -> "the project"
      }

  return "$userName $action $entity"
}

/**
 * Format timestamp to relative time (e.g., "2 hours ago", "Yesterday")
 *
 * @param timestamp Timestamp in milliseconds
 * @return Formatted time string
 */
private fun formatTimestamp(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp

  return when {
    diff < 60_000 -> "Just now"
    diff < 3600_000 -> "${diff / 60_000}m ago"
    diff < 86400_000 -> "${diff / 3600_000}h ago"
    diff < 172800_000 -> "Yesterday"
    diff < 604800_000 -> "${diff / 86400_000}d ago"
    else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp)
  }
}

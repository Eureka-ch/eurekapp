/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Get the appropriate icon and color for an activity type and entity type combination.
 *
 * @param activityType The type of activity (CREATED, UPDATED, etc.)
 * @param entityType The type of entity (PROJECT, MEETING, etc.)
 * @return Pair of ImageVector icon and Color
 */
fun getActivityIconAndColor(
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
    ActivityType.ASSIGNED -> Icons.Default.PersonAdd to Color(0xFF4CAF50)
    ActivityType.UNASSIGNED -> Icons.Default.PersonRemove to Color(0xFFFF9800)
    ActivityType.ROLE_CHANGED -> Icons.Default.ChangeCircle to Color(0xFF2196F3)
    ActivityType.DOWNLOADED -> Icons.Default.Download to Color(0xFF4CAF50)
    else -> Icons.AutoMirrored.Filled.Article to EColors.light.onSurfaceVariant
  }
}

/**
 * Format a timestamp in milliseconds to a human-readable date header.
 *
 * @param timestamp The timestamp in milliseconds
 * @return Formatted date string (e.g., "Today", "Yesterday", or "Mon, Jan 15")
 */
fun formatDateHeader(timestamp: Long): String {
  val calendar = Calendar.getInstance()
  val today = calendar.clone() as Calendar
  today[Calendar.HOUR_OF_DAY] = 0
  today[Calendar.MINUTE] = 0
  today[Calendar.SECOND] = 0
  today[Calendar.MILLISECOND] = 0

  calendar.timeInMillis = timestamp
  calendar[Calendar.HOUR_OF_DAY] = 0
  calendar[Calendar.MINUTE] = 0
  calendar[Calendar.SECOND] = 0
  calendar[Calendar.MILLISECOND] = 0

  val diff = today.timeInMillis - calendar.timeInMillis
  val daysDiff = diff / (1000 * 60 * 60 * 24)

  return when (daysDiff.toInt()) {
    0 -> "Today"
    1 -> "Yesterday"
    else -> {
      val format = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
      format.format(calendar.time)
    }
  }
}

fun EntityType.toDisplayString() =
    when (this) {
      EntityType.MEETING -> "Meetings"
      EntityType.MESSAGE -> "Messages"
      EntityType.FILE -> "Files"
      EntityType.TASK -> "Tasks"
      EntityType.PROJECT -> "Projects"
      EntityType.MEMBER -> "Members"
    }

fun ActivityType.toDisplayString() =
    when (this) {
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

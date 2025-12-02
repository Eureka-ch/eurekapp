/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity detail screen showing comprehensive information about a specific activity.
 *
 * @param activityId The unique ID of the activity to display.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onNavigateToEntity Callback to navigate to the related entity.
 * @param viewModel The view model for this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEntity: (String, EntityType) -> Unit,
    viewModel: ActivityDetailViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Load activity on entry
  LaunchedEffect(activityId) { viewModel.loadActivity(activityId) }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("ActivityDetailScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Activity Details") },
            navigationIcon = {
              IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("BackButton")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back")
              }
            },
            actions = {
              // Delete button
              if (uiState.activity != null) {
                IconButton(
                    onClick = {
                      viewModel.deleteActivity()
                      onNavigateBack()
                    },
                    modifier = Modifier.testTag("DeleteButton")) {
                      Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete activity",
                          tint = MaterialTheme.colorScheme.error)
                    }
              }
            })
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          when {
            uiState.isLoading -> LoadingState()
            uiState.activity == null -> ErrorState(uiState.errorMsg ?: "Activity not found")
            else ->
                ActivityDetailContent(
                    activity = uiState.activity!!,
                    entityDetails = uiState.entityDetails,
                    onNavigateToEntity = { onNavigateToEntity(it, uiState.activity!!.entityType) })
          }
        }
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
        Text("Loading activity details...")
      }
}

@Composable
private fun ErrorState(message: String) {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("ErrorState"))
      }
}

@Composable
private fun ActivityDetailContent(
    activity: Activity,
    entityDetails: Map<String, Any>,
    onNavigateToEntity: (String) -> Unit
) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(Spacing.md)
              .testTag("ActivityDetailContent"),
      verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        // Activity header with icon and type
        ActivityHeader(activity)

        // User and timestamp info
        UserInfoCard(activity)

        // Entity details
        EntityDetailsCard(activity, entityDetails, onNavigateToEntity)

        // Metadata
        if (activity.metadata.isNotEmpty()) {
          MetadataCard(activity.metadata)
        }
      }
}

@Composable
private fun ActivityHeader(activity: Activity) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically) {
              // Activity icon
              val (icon, color) = getIconAndColor(activity.activityType, activity.entityType)
              Surface(
                  modifier = Modifier.size(56.dp),
                  shape = CircleShape,
                  color = color.copy(alpha = 0.12f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "${activity.activityType} ${activity.entityType}",
                        tint = color,
                        modifier = Modifier.padding(12.dp))
                  }

              Column(modifier = Modifier.weight(1f)) {
                // Activity type
                Text(
                    text = activity.activityType.toDisplayString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)

                // Entity type
                Text(
                    text = activity.entityType.toDisplayString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
              }
            }
      }
}

@Composable
private fun UserInfoCard(activity: Activity) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          Text(
              text = "Activity Information",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold)

          HorizontalDivider()

          // User
          InfoRow(
              label = "Performed by",
              value = activity.metadata["userName"]?.toString() ?: "Unknown User")

          // Timestamp
          val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
          val formattedDate = dateFormat.format(activity.timestamp.toDate())
          InfoRow(label = "Timestamp", value = formattedDate)

          // Project name
          InfoRow(
              label = "Project",
              value = activity.metadata["projectName"]?.toString() ?: activity.projectId)
        }
  }
}

@Composable
private fun EntityDetailsCard(
    activity: Activity,
    entityDetails: Map<String, Any>,
    onNavigateToEntity: (String) -> Unit
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Related ${activity.entityType.toDisplayString()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))

                // Navigate to entity button
                IconButton(
                    onClick = { onNavigateToEntity(activity.entityId) },
                    modifier = Modifier.testTag("NavigateToEntityButton")) {
                      Icon(
                          imageVector = Icons.Default.OpenInNew,
                          contentDescription = "Open ${activity.entityType}",
                          tint = MaterialTheme.colorScheme.primary)
                    }
              }

          HorizontalDivider()

          // Display entity-specific details
          when (activity.entityType) {
            EntityType.MEETING -> DisplayMeetingDetails(entityDetails)
            EntityType.TASK -> DisplayTaskDetails(entityDetails)
            EntityType.FILE -> DisplayFileDetails(entityDetails, activity)
            EntityType.PROJECT -> DisplayProjectDetails(entityDetails)
            EntityType.MEMBER -> DisplayMemberDetails(entityDetails)
            EntityType.MESSAGE -> DisplayMessageDetails(entityDetails)
          }
        }
  }
}

@Composable
private fun DisplayMeetingDetails(details: Map<String, Any>) {
  details["title"]?.let { InfoRow(label = "Title", value = it.toString()) }
  details["description"]?.let { InfoRow(label = "Description", value = it.toString()) }
  details["status"]?.let { InfoRow(label = "Status", value = it.toString()) }
  details["location"]?.let { InfoRow(label = "Location", value = it.toString()) }
  details["duration"]?.let { InfoRow(label = "Duration", value = "${it} minutes") }
}

@Composable
private fun DisplayTaskDetails(details: Map<String, Any>) {
  details["title"]?.let { InfoRow(label = "Title", value = it.toString()) }
  details["description"]?.let { InfoRow(label = "Description", value = it.toString()) }
  details["status"]?.let { InfoRow(label = "Status", value = it.toString()) }
}

@Composable
private fun DisplayFileDetails(details: Map<String, Any>, activity: Activity) {
  activity.metadata["title"]?.let { InfoRow(label = "File Name", value = it.toString()) }
  activity.metadata["size"]?.let { InfoRow(label = "Size", value = "${it} bytes") }
  activity.metadata["type"]?.let { InfoRow(label = "Type", value = it.toString()) }
  details["downloadUrl"]?.let { InfoRow(label = "URL", value = it.toString()) }
}

@Composable
private fun DisplayProjectDetails(details: Map<String, Any>) {
  details["name"]?.let { InfoRow(label = "Name", value = it.toString()) }
  details["description"]?.let { InfoRow(label = "Description", value = it.toString()) }
}

@Composable
private fun DisplayMemberDetails(details: Map<String, Any>) {
  details["displayName"]?.let { InfoRow(label = "Name", value = it.toString()) }
  details["email"]?.let { InfoRow(label = "Email", value = it.toString()) }
  details["role"]?.let { InfoRow(label = "Role", value = it.toString()) }
}

@Composable
private fun DisplayMessageDetails(details: Map<String, Any>) {
  details["content"]?.let { InfoRow(label = "Content", value = it.toString()) }
}

@Composable
private fun MetadataCard(metadata: Map<String, Any>) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          Text(
              text = "Additional Metadata",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold)

          HorizontalDivider()

          metadata.forEach { (key, value) ->
            // Skip userName and title as they're already shown
            if (key != "userName" && key != "title") {
              InfoRow(label = key.replaceFirstChar { it.uppercase() }, value = value.toString())
            }
          }
        }
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f))
      }
}

// Reuse the icon helper from ActivityCard
private fun getIconAndColor(
    activityType: ActivityType,
    entityType: EntityType
): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
  return when (activityType) {
    ActivityType.CREATED ->
        when (entityType) {
          EntityType.MEETING -> Icons.Default.CalendarToday to Color(0xFF6200EA)
          EntityType.MESSAGE -> Icons.AutoMirrored.Filled.Message to Color(0xFF4CAF50)
          EntityType.FILE -> Icons.Default.FileUpload to Color(0xFF2196F3)
          else -> Icons.AutoMirrored.Filled.Article to Color(0xFF6200EA)
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
    else -> Icons.AutoMirrored.Filled.Article to Color(0xFF757575)
  }
}

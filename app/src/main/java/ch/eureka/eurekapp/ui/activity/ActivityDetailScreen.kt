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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.activity.Activity
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
              val (icon, color) =
                  getActivityIconAndColor(activity.activityType, activity.entityType)
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
          DisplayEntityDetails(activity, entityDetails)
        }
  }
}

@Composable
private fun DisplayEntityDetails(activity: Activity, details: Map<String, Any>) {
  val fieldMappings =
      when (activity.entityType) {
        EntityType.FILE ->
            mapOf(
                "title" to "File Name", "size" to "Size", "type" to "Type", "downloadUrl" to "URL")
        EntityType.MEMBER -> mapOf("displayName" to "Name", "email" to "Email", "role" to "Role")
        EntityType.MESSAGE -> mapOf("content" to "Content")
        EntityType.PROJECT -> mapOf("name" to "Name", "description" to "Description")
        else ->
            mapOf(
                "title" to "Title",
                "description" to "Description",
                "status" to "Status",
                "location" to "Location",
                "duration" to "Duration")
      }

  fieldMappings.forEach { (key, label) ->
    val value =
        if (activity.entityType == EntityType.FILE && key in listOf("title", "size", "type")) {
          activity.metadata[key]
        } else {
          details[key]
        }
    value?.let {
      val displayValue =
          if (key == "size") "${it} bytes"
          else if (key == "duration") "${it} minutes" else it.toString()
      InfoRow(label = label, value = displayValue)
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

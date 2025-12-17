/*
 * Co-Authored-By: Claude Sonnet 4.5
 */
package ch.eureka.eurekapp.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.meeting.InfoRow
import ch.eureka.eurekapp.utils.Formatters

/** Helper function to get alpha value based on connection status. */
private fun getAlpha(isConnected: Boolean): Float = if (isConnected) 1f else 0.6f

/** Test tags for ActivityDetailScreen. */
object ActivityDetailScreenTestTags {
  const val ACTIVITY_DETAIL_SCREEN = "ActivityDetailScreen"
  const val LOADING_INDICATOR = "LoadingIndicator"
  const val ERROR_MESSAGE = "ErrorMessage"
  const val ACTIVITY_HEADER = "ActivityHeader"
  const val ACTIVITY_INFO_CARD = "ActivityInfoCard"
  const val ENTITY_BUTTON = "EntityButton"
  const val RELATED_ACTIVITIES_SECTION = "RelatedActivitiesSection"
  const val SHARE_BUTTON = "ShareButton"
  const val DELETE_BUTTON = "DeleteButton"
  const val DELETE_DIALOG = "DeleteDialog"
  const val ACTIVITY_TYPE = "ActivityType"
  const val ENTITY_TYPE = "EntityType"
  const val USER_NAME = "UserName"
  const val TIMESTAMP = "Timestamp"
  const val ENTITY_TITLE = "EntityTitle"
  const val RELATED_ACTIVITY_ITEM = "RelatedActivityItem"
  const val NO_RELATED_ACTIVITIES = "NoRelatedActivities"
  const val OFFLINE_MESSAGE = "OfflineMessage"
}

/**
 * Main activity detail screen.
 *
 * Displays detailed information about an activity, including related activities, entity navigation,
 * and action buttons.
 *
 * @param activityId The ID of the activity to display.
 * @param viewModel The ViewModel for managing screen state.
 * @param onNavigateBack Callback when the back button is clicked.
 * @param onNavigateToEntity Callback for navigating to the source entity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    viewModel: ActivityDetailViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToEntity: (EntityType, String, String) -> Unit = { _, _, _ -> }
) {
  val vm: ActivityDetailViewModel =
      viewModel
          ?: androidx.lifecycle.viewmodel.compose.viewModel(
              factory =
                  object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                      return ActivityDetailViewModel(activityId) as T
                    }
                  })
  val uiState by vm.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.deleteSuccess) {
    if (uiState.deleteSuccess) {
      Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
      onNavigateBack()
    }
  }

  Scaffold(
      modifier = Modifier.testTag(ActivityDetailScreenTestTags.ACTIVITY_DETAIL_SCREEN),
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.activity_detail_screen_title),
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.activity_detail_navigate_back),
                    tint = EColors.WhiteTextColor)
              }
            })
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(ActivityDetailScreenTestTags.LOADING_INDICATOR))
                }
          }
          uiState.errorMsg != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = uiState.errorMsg ?: "Unknown error",
                      color = MaterialTheme.colorScheme.error,
                      modifier = Modifier.testTag(ActivityDetailScreenTestTags.ERROR_MESSAGE))
                }
          }
          uiState.activity != null -> {
            ActivityDetailContent(
                activity = uiState.activity!!,
                relatedActivities = uiState.relatedActivities,
                isConnected = uiState.isConnected,
                onNavigateToEntity = onNavigateToEntity,
                onShare = {
                  val shareText = vm.getShareText()
                  if (shareText != null) {
                    shareToClipboard(context, shareText)
                    vm.markShareSuccess()
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                  }
                },
                onDelete = { vm.deleteActivity() },
                modifier = Modifier.padding(paddingValues))
          }
        }
      }
}

/**
 * Content section of the activity detail screen.
 *
 * Contains all the detailed information, related activities, and action buttons.
 */
@Composable
private fun ActivityDetailContent(
    activity: Activity,
    relatedActivities: List<Activity>,
    isConnected: Boolean,
    onNavigateToEntity: (EntityType, String, String) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ActivityHeader(activity) }
        item { ActivityInformationCard(activity) }
        item {
          val hasDetailScreen =
              activity.entityType in
                  listOf(
                      EntityType.MEETING, EntityType.TASK, EntityType.MESSAGE, EntityType.PROJECT)

          if (hasDetailScreen) {
            Button(
                onClick = {
                  onNavigateToEntity(activity.entityType, activity.entityId, activity.projectId)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .alpha(getAlpha(isConnected))
                        .testTag(ActivityDetailScreenTestTags.ENTITY_BUTTON),
                enabled = isConnected) {
                  Text(stringResource(R.string.activity_detail_view_entity_button, activity.entityType.name))
                }
          }
        }
        item { RelatedActivitiesSection(relatedActivities) }
        item { ActionButtonsSection(isConnected, onShare, onDelete) }
        if (!isConnected) {
          item {
            Text(
                text = stringResource(R.string.activity_detail_offline_message),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(ActivityDetailScreenTestTags.OFFLINE_MESSAGE))
          }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
}

/**
 * Activity header with type badge.
 *
 * Displays a colored badge with the activity type.
 */
@Composable
private fun ActivityHeader(activity: Activity) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag(ActivityDetailScreenTestTags.ACTIVITY_HEADER),
      horizontalArrangement = Arrangement.Center) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = getActivityTypeColor(activity.activityType.name),
            modifier = Modifier.padding(8.dp)) {
              Text(
                  text = activity.activityType.name,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = EColors.WhiteTextColor)
            }
      }
}

/**
 * Activity information card with all details.
 *
 * Displays activity type, entity type, user, timestamp, and entity title.
 */
@Composable
private fun ActivityInformationCard(activity: Activity) {
  val formattedTimestamp =
      remember(activity.timestamp) { Formatters.formatFullTimestamp(activity.timestamp.toDate()) }

  Card(
      modifier = Modifier.fillMaxWidth().testTag(ActivityDetailScreenTestTags.ACTIVITY_INFO_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = stringResource(R.string.activity_detail_information_title),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              HorizontalDivider()

              InfoRow(
                  icon = Icons.Default.Category,
                  label = stringResource(R.string.activity_detail_activity_type_label),
                  value = activity.activityType.name,
                  testTag = ActivityDetailScreenTestTags.ACTIVITY_TYPE)

              InfoRow(
                  icon = Icons.Default.Description,
                  label = stringResource(R.string.activity_detail_entity_type_label),
                  value = activity.entityType.name,
                  testTag = ActivityDetailScreenTestTags.ENTITY_TYPE)

              InfoRow(
                  icon = Icons.Default.Person,
                  label = stringResource(R.string.activity_detail_user_label),
                  value = activity.metadata["userName"]?.toString() ?: stringResource(R.string.activity_detail_unknown_user),
                  testTag = ActivityDetailScreenTestTags.USER_NAME)

              InfoRow(
                  icon = Icons.Default.AccessTime,
                  label = stringResource(R.string.activity_detail_timestamp_label),
                  value = formattedTimestamp,
                  testTag = ActivityDetailScreenTestTags.TIMESTAMP)

              if (activity.metadata.containsKey("title")) {
                InfoRow(
                    icon = Icons.Default.Title,
                    label = stringResource(R.string.activity_detail_entity_label),
                    value = activity.metadata["title"]?.toString() ?: stringResource(R.string.activity_detail_untitled_entity),
                    testTag = ActivityDetailScreenTestTags.ENTITY_TITLE)
              }
            }
      }
}
/**
 * Related activities section.
 *
 * Displays a timeline of other activities for the same entity.
 */
@Composable
private fun RelatedActivitiesSection(relatedActivities: List<Activity>) {
  Card(
      modifier =
          Modifier.fillMaxWidth().testTag(ActivityDetailScreenTestTags.RELATED_ACTIVITIES_SECTION),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = stringResource(R.string.activity_detail_related_activities_title, relatedActivities.size),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              HorizontalDivider()

              if (relatedActivities.isEmpty()) {
                Text(
                    text = stringResource(R.string.activity_detail_no_related_activities),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EColors.SecondaryTextColor,
                    modifier = Modifier.testTag(ActivityDetailScreenTestTags.NO_RELATED_ACTIVITIES))
              } else {
                relatedActivities.forEach { activity -> RelatedActivityItem(activity) }
              }
            }
      }
}

/**
 * Compact activity item for the related activities list.
 *
 * Shows activity icon, description, and timestamp.
 */
@Composable
private fun RelatedActivityItem(activity: Activity) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(
                  ActivityDetailScreenTestTags.RELATED_ACTIVITY_ITEM + "_${activity.activityId}"),
      verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(32.dp)
                    .background(
                        getActivityTypeColor(activity.activityType.name), shape = CircleShape),
            contentAlignment = Alignment.Center) {
              Text(
                  text = activity.activityType.name.first().toString(),
                  color = EColors.WhiteTextColor,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold)
            }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          val userName = activity.metadata["userName"]?.toString() ?: "Someone"
          val description = "$userName ${activity.activityType.name.lowercase()}"

          Text(text = description, style = MaterialTheme.typography.bodyMedium)

          Text(
              text = Formatters.formatRelativeTime(activity.timestamp.toDate()),
              style = MaterialTheme.typography.bodySmall,
              color = EColors.SecondaryTextColor)
        }
      }
}

/**
 * Action buttons section.
 *
 * Contains Share and Delete buttons.
 */
@Composable
private fun ActionButtonsSection(isConnected: Boolean, onShare: () -> Unit, onDelete: () -> Unit) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    OutlinedButton(
        onClick = onShare,
        modifier =
            Modifier.fillMaxWidth()
                .alpha(getAlpha(isConnected))
                .testTag(ActivityDetailScreenTestTags.SHARE_BUTTON),
        enabled = isConnected) {
          Icon(Icons.Default.Share, stringResource(R.string.activity_detail_share_button), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.activity_detail_share_button))
        }

    OutlinedButton(
        onClick = { showDeleteDialog = true },
        modifier =
            Modifier.fillMaxWidth()
                .alpha(getAlpha(isConnected))
                .testTag(ActivityDetailScreenTestTags.DELETE_BUTTON),
        enabled = isConnected,
        colors =
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
          Icon(Icons.Default.Delete, stringResource(R.string.activity_detail_delete_button), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.activity_detail_delete_button))
        }
  }

  if (showDeleteDialog) {
    AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text(stringResource(R.string.activity_detail_delete_dialog_title)) },
        text = { Text(stringResource(R.string.activity_detail_delete_dialog_message)) },
        confirmButton = {
          TextButton(
              onClick = {
                showDeleteDialog = false
                onDelete()
              }) {
                Text(stringResource(R.string.activity_detail_delete_confirm), color = MaterialTheme.colorScheme.error)
              }
        },
        dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.activity_detail_delete_cancel)) } },
        modifier = Modifier.testTag(ActivityDetailScreenTestTags.DELETE_DIALOG))
  }
}

/** Helper function to get color for activity type. */
private fun getActivityTypeColor(activityType: String): Color {
  return when (activityType) {
    "CREATED" -> EColors.ActivityCreated
    "UPDATED" -> EColors.ActivityUpdated
    "DELETED" -> EColors.ActivityDeleted
    "UPLOADED" -> EColors.ActivityUploaded
    "SHARED" -> EColors.ActivityShared
    "COMMENTED" -> EColors.ActivityCommented
    "STATUS_CHANGED" -> EColors.ActivityStatusChanged
    "JOINED" -> EColors.ActivityJoined
    "LEFT" -> EColors.ActivityLeft
    "ASSIGNED" -> EColors.ActivityAssigned
    "UNASSIGNED" -> EColors.ActivityUnassigned
    "ROLE_CHANGED" -> EColors.ActivityRoleChanged
    "DOWNLOADED" -> EColors.ActivityDownloaded
    else -> EColors.ActivityDefault
  }
}

/** Helper function to copy text to clipboard. */
private fun shareToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("Activity Details", text)
  clipboard.setPrimaryClip(clip)
}

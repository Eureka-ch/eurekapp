package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.utils.Formatters

/**
 * Test tags for MeetingDetailScreen composable.
 *
 * Provides semantic identifiers for UI testing with Compose UI Test framework. Each constant
 * represents a unique testTag applied to composables in MeetingDetailScreen, enabling reliable and
 * maintainable UI test assertions.
 */
object MeetingDetailScreenTestTags {
  const val MEETING_DETAIL_SCREEN = "MeetingDetailScreen"
  const val LOADING_INDICATOR = "LoadingIndicator"
  const val ERROR_MESSAGE = "ErrorMessage"
  const val MEETING_TITLE = "MeetingDetailTitle"
  const val MEETING_STATUS = "MeetingDetailStatus"
  const val MEETING_DATETIME = "MeetingDetailDateTime"
  const val MEETING_FORMAT = "MeetingDetailFormat"
  const val MEETING_LOCATION = "MeetingDetailLocation"
  const val MEETING_LINK = "MeetingDetailLink"
  const val PARTICIPANTS_SECTION = "ParticipantsSection"
  const val PARTICIPANT_ITEM = "ParticipantItem"
  const val PARTICIPANT_NAME = "ParticipantName"
  const val PARTICIPANT_ROLE = "ParticipantRole"
  const val ATTACHMENTS_SECTION = "AttachmentsSection"
  const val ATTACHMENT_ITEM = "AttachmentItem"
  const val NO_ATTACHMENTS_MESSAGE = "NoAttachmentsMessage"
  const val ACTION_BUTTONS_SECTION = "ActionButtonsSection"
  const val JOIN_MEETING_BUTTON = "JoinMeetingButton"
  const val RECORD_BUTTON = "RecordButton"
  const val VIEW_TRANSCRIPT_BUTTON = "ViewTranscriptButton"
  const val DELETE_BUTTON = "DeleteButton"
  const val DELETE_CONFIRMATION_DIALOG = "DeleteConfirmationDialog"
  const val CONFIRM_DELETE_BUTTON = "ConfirmDeleteButton"
  const val CANCEL_DELETE_BUTTON = "CancelDeleteButton"
  const val VOTE_TIME_BUTTON = "VoteForTimeButton"
  const val VOTE_FORMAT_BUTTON = "VoteForFormatButton"
}

/**
 * Main composable for the meeting detail screen.
 *
 * Displays comprehensive information about a meeting including title, date/time, format,
 * location/link, participants, and attachments. Provides action buttons for joining, recording,
 * viewing transcripts, and deleting the meeting.
 *
 * @param projectId The ID of the project containing the meeting.
 * @param meetingId The ID of the meeting to display.
 * @param viewModel The ViewModel managing the meeting detail state.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onJoinMeeting Callback when user clicks join meeting button.
 * @param onRecordMeeting Callback when user clicks record button.
 * @param onViewTranscript Callback when user clicks view transcript button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    projectId: String,
    meetingId: String,
    viewModel: MeetingDetailViewModel = viewModel { MeetingDetailViewModel(projectId, meetingId) },
    onNavigateBack: () -> Unit = {},
    onJoinMeeting: (String) -> Unit = {},
    onRecordMeeting: () -> Unit = {},
    onViewTranscript: () -> Unit = {},
    onVoteForTime: () -> Unit = {},
    onVoteForFormat: () -> Unit = {},
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var showDeleteDialog by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uiState.deleteSuccess) {
    if (uiState.deleteSuccess) {
      Toast.makeText(context, "Meeting deleted successfully", Toast.LENGTH_SHORT).show()
      onNavigateBack()
    }
  }

  Scaffold(
      modifier = Modifier.testTag(MeetingDetailScreenTestTags.MEETING_DETAIL_SCREEN),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = uiState.meeting?.title ?: "Meeting",
                  modifier = Modifier.testTag(MeetingDetailScreenTestTags.MEETING_TITLE))
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
              }
            })
      },
      content = { padding ->
        if (uiState.isLoading) {
          LoadingScreen()
        } else {
          uiState.meeting?.let { meeting ->
            MeetingDetailContent(
                modifier = Modifier.padding(padding),
                meeting = meeting,
                participants = uiState.participants,
                onJoinMeeting = onJoinMeeting,
                onRecordMeeting = onRecordMeeting,
                onViewTranscript = onViewTranscript,
                onDeleteMeeting = { showDeleteDialog = true },
                onVoteForTime = onVoteForTime,
                onVoteForFormat = onVoteForFormat)
          } ?: ErrorScreen(message = uiState.errorMsg ?: "Meeting not found")
        }
      })

  if (showDeleteDialog) {
    DeleteConfirmationDialog(
        onConfirm = {
          showDeleteDialog = false
          viewModel.deleteMeeting(projectId, meetingId)
        },
        onDismiss = { showDeleteDialog = false })
  }
}

/** Loading indicator screen. */
@Composable
private fun LoadingScreen() {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(16.dp)
              .testTag(MeetingDetailScreenTestTags.LOADING_INDICATOR),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading meeting details...", style = MaterialTheme.typography.bodyMedium)
      }
}

/**
 * Error message screen.
 *
 * @param message The error message to display to the user.
 */
@Composable
private fun ErrorScreen(message: String) {
  Column(
      modifier =
          Modifier.fillMaxSize().padding(16.dp).testTag(MeetingDetailScreenTestTags.ERROR_MESSAGE),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error)
      }
}

/**
 * Main content displaying meeting details.
 *
 * @param meeting The meeting to display.
 * @param participants List of participants in the meeting.
 * @param onJoinMeeting Callback invoked when user clicks join meeting button, receives meeting
 *   link.
 * @param onRecordMeeting Callback invoked when user clicks record meeting button.
 * @param onViewTranscript Callback invoked when user clicks view transcript button.
 * @param onDeleteMeeting Callback invoked when user clicks delete meeting button.
 * @param modifier Modifier to be applied to the root composable.
 */
@Composable
private fun MeetingDetailContent(
    meeting: Meeting,
    participants: List<Participant>,
    onJoinMeeting: (String) -> Unit,
    onRecordMeeting: () -> Unit,
    onViewTranscript: () -> Unit,
    onDeleteMeeting: () -> Unit,
    onVoteForTime: () -> Unit,
    onVoteForFormat: () -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().then(modifier),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { MeetingHeader(meeting = meeting) }

        item { MeetingInformationCard(meeting = meeting) }

        item { ParticipantsSection(participants = participants) }

        item { AttachmentsSection(attachments = meeting.attachmentUrls) }

        item {
          ActionButtonsSection(
              meeting = meeting,
              onJoinMeeting = onJoinMeeting,
              onRecordMeeting = onRecordMeeting,
              onViewTranscript = onViewTranscript,
              onDeleteMeeting = onDeleteMeeting,
              onVoteForTime = onVoteForTime,
              onVoteForFormat = onVoteForFormat)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
}

/**
 * Meeting status badge.
 *
 * @param meeting The meeting to display the status badge for.
 */
@Composable
private fun MeetingHeader(meeting: Meeting) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color =
            when (meeting.status) {
              MeetingStatus.OPEN_TO_VOTES -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
              MeetingStatus.SCHEDULED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
              MeetingStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
              MeetingStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
            },
        modifier = Modifier.testTag(MeetingDetailScreenTestTags.MEETING_STATUS)) {
          Text(
              text = meeting.status.description,
              style = MaterialTheme.typography.labelMedium,
              color =
                  when (meeting.status) {
                    MeetingStatus.OPEN_TO_VOTES -> Color.Blue
                    MeetingStatus.SCHEDULED -> Color.Red
                    MeetingStatus.IN_PROGRESS -> Color.Green
                    MeetingStatus.COMPLETED -> Color.DarkGray
                  },
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
  }
}

/**
 * Card displaying meeting information (date, time, format, location/link).
 *
 * @param meeting The meeting to display information for.
 */
@Composable
private fun MeetingInformationCard(meeting: Meeting) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = "Meeting Information",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              HorizontalDivider()

              if (meeting.datetime != null) {
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Date & Time",
                    value = Formatters.formatDateTime(meeting.datetime.toDate()),
                    testTag = MeetingDetailScreenTestTags.MEETING_DATETIME)
              }

              meeting.format?.let { format ->
                InfoRow(
                    icon =
                        when (format) {
                          MeetingFormat.VIRTUAL -> Icons.Default.VideoCall
                          MeetingFormat.IN_PERSON -> Icons.Default.Place
                        },
                    label = "Format",
                    value = format.description,
                    testTag = MeetingDetailScreenTestTags.MEETING_FORMAT)
              }

              if (meeting.format == MeetingFormat.IN_PERSON && meeting.location != null) {
                InfoRow(
                    icon = Icons.Default.Place,
                    label = "Location",
                    value = meeting.location.name,
                    testTag = MeetingDetailScreenTestTags.MEETING_LOCATION)
              }

              if (meeting.format == MeetingFormat.VIRTUAL && meeting.link != null) {
                InfoRow(
                    icon = Icons.Default.VideoCall,
                    label = "Meeting Link",
                    value = meeting.link,
                    testTag = MeetingDetailScreenTestTags.MEETING_LINK)
              }
            }
      }
}

/**
 * Reusable information row component.
 *
 * @param icon The icon to display at the start of the row.
 * @param label The label text describing the information.
 * @param value The value text to display.
 * @param testTag The test tag for UI testing.
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    testTag: String,
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    Icon(
        imageVector = icon,
        contentDescription = label,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
      Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.testTag(testTag))
    }
  }
}

/**
 * Section displaying meeting participants.
 *
 * @param participants The list of participants to display.
 */
@Composable
private fun ParticipantsSection(participants: List<Participant>) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.PARTICIPANTS_SECTION),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Participants",
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Participants (${participants.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
              }

              HorizontalDivider()

              if (participants.isEmpty()) {
                Text(
                    text = "No participants yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray)
              } else {
                participants.forEach { ParticipantItem(participant = it) }
              }
            }
      }
}

/**
 * Individual participant item.
 *
 * @param participant The participant to display.
 */
@Composable
private fun ParticipantItem(participant: Participant) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.PARTICIPANT_ITEM),
      verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)) {
              Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Participant",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp))
                  }
            }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = participant.userId,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag(MeetingDetailScreenTestTags.PARTICIPANT_NAME))
          Text(
              text = participant.role.name,
              style = MaterialTheme.typography.labelSmall,
              color = Color.Gray,
              modifier = Modifier.testTag(MeetingDetailScreenTestTags.PARTICIPANT_ROLE))
        }
      }
}

/**
 * Section displaying meeting attachments.
 *
 * @param attachments The list of attachment URLs to display.
 */
@Composable
private fun AttachmentsSection(attachments: List<String>) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.ATTACHMENTS_SECTION),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attachments",
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attachments (${attachments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
              }

              HorizontalDivider()

              if (attachments.isEmpty()) {
                Text(
                    text = "No attachments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.testTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE))
              } else {
                attachments.forEach { attachment -> AttachmentItem(attachmentUrl = attachment) }
              }
            }
      }
}

/**
 * Individual attachment item.
 *
 * @param attachmentUrl The URL of the attachment to display.
 */
@Composable
private fun AttachmentItem(attachmentUrl: String) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.ATTACHMENT_ITEM),
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Attachment",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = attachmentUrl, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
      }
}

/**
 * Action buttons section (join, record, transcript, delete).
 *
 * @param meeting The meeting for which to display action buttons.
 * @param onJoinMeeting Callback invoked when user clicks join meeting button, receives meeting
 *   link.
 * @param onRecordMeeting Callback invoked when user clicks record meeting button.
 * @param onViewTranscript Callback invoked when user clicks view transcript button.
 * @param onDeleteMeeting Callback invoked when user clicks delete meeting button.
 */
@Composable
private fun ActionButtonsSection(
    meeting: Meeting,
    onJoinMeeting: (String) -> Unit,
    onRecordMeeting: () -> Unit,
    onViewTranscript: () -> Unit,
    onDeleteMeeting: () -> Unit,
    onVoteForTime: () -> Unit,
    onVoteForFormat: () -> Unit
) {
  Column(
      modifier =
          Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)

        // Status-specific action buttons
        when (meeting.status) {
          MeetingStatus.SCHEDULED,
          MeetingStatus.IN_PROGRESS -> {
            if (meeting.format == MeetingFormat.VIRTUAL && meeting.link != null) {
              Button(
                  onClick = { onJoinMeeting(meeting.link) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON)) {
                    Icon(imageVector = Icons.Default.VideoCall, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Meeting")
                  }
            }
            OutlinedButton(
                onClick = onRecordMeeting,
                modifier =
                    Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.RECORD_BUTTON)) {
                  Text("Start Recording")
                }
          }
          MeetingStatus.COMPLETED -> {
            Button(
                onClick = onViewTranscript,
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON)) {
                  Icon(imageVector = Icons.Default.Description, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("View Transcript")
                }
          }
          MeetingStatus.OPEN_TO_VOTES -> {
            Button(
                onClick = onVoteForTime,
                modifier =
                    Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.VOTE_TIME_BUTTON)) {
                  Icon(imageVector = Icons.Default.HowToVote, contentDescription = "Vote for time")
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Vote for time")
                }

            Button(
                onClick = onVoteForFormat,
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(MeetingDetailScreenTestTags.VOTE_FORMAT_BUTTON)) {
                  Icon(
                      imageVector = Icons.Default.HowToVote, contentDescription = "Vote for format")
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Vote for format")
                }
          }
        }

        OutlinedButton(
            onClick = onDeleteMeeting,
            modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.DELETE_BUTTON),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Delete Meeting")
            }
      }
}

/**
 * Delete confirmation dialog.
 *
 * @param onConfirm Callback invoked when user confirms deletion.
 * @param onDismiss Callback invoked when user dismisses the dialog.
 */
@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Delete Meeting") },
      text = {
        Text("Are you sure you want to delete this meeting? This action cannot be undone.")
      },
      confirmButton = {
        TextButton(
            onClick = onConfirm,
            modifier = Modifier.testTag(MeetingDetailScreenTestTags.CONFIRM_DELETE_BUTTON)) {
              Text("Delete", color = MaterialTheme.colorScheme.error)
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(MeetingDetailScreenTestTags.CANCEL_DELETE_BUTTON)) {
              Text("Cancel")
            }
      },
      modifier = Modifier.testTag(MeetingDetailScreenTestTags.DELETE_CONFIRMATION_DIALOG))
}

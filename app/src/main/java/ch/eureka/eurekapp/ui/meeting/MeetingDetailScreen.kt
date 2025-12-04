// Portions of this code were generated with the help of Grok, ChatGPT, and Claude.
package ch.eureka.eurekapp.ui.meeting

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.LightingBlue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.utils.Formatters
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Helper function to get alpha value based on connection status. Returns 1f if connected, 0.6f if
 * offline.
 */
private fun getAlpha(isConnected: Boolean): Float = if (isConnected) 1f else 0.6f

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
  const val ERROR_MSG = "ErrorMsg"
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
  const val VOTE_FOR_MEETING_PROPOSAL_BUTTON = "VoteForMeetingProposal"
  const val EDIT_BUTTON = "EditButton"
  const val SAVE_BUTTON = "SaveButton"
  const val CANCEL_EDIT_BUTTON = "CancelEditButton"
  const val OFFLINE_MESSAGE = "offlineMessage"
  const val CONTENT_COLUMN = "ContentColumn"
  const val DOWNLOADING_FILES_PROGRESS_INDICATOR = "DownloadingFilesProgressIndicator"
  const val DOWNLOADING_FILES_BUTTON = "DownloadingFilesButton"
}

/**
 * Data class representing all the actions than can be executed by buttons on the meeting detail
 * screen.
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onJoinMeeting Callback when user clicks join meeting button.
 * @param onVoteForMeetingProposalClick Callback when the "Vote for meeting proposals" button is
 *   clicked.
 * @param onRecordMeeting Callback when user clicks record button, receives projectId and meetingId.
 * @param onViewTranscript Callback when user clicks view transcript button, receives projectId and
 *   meetingId.
 * @param onNavigateToMeeting Callback when user clicks navigate to meeting button.
 */
data class MeetingDetailActionsConfig(
    val onNavigateBack: () -> Unit = {},
    val onJoinMeeting: (String, Boolean) -> Unit = { _, _ -> },
    val onRecordMeeting: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onVoteForMeetingProposalClick: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onNavigateToMeeting: (Boolean) -> Unit = {},
)

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
 * @param actionsConfig The actions that can be executed with buttons on the detail meeting screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    projectId: String,
    meetingId: String,
    viewModel: MeetingDetailViewModel = viewModel { MeetingDetailViewModel(projectId, meetingId) },
    attachmentsViewModel: MeetingAttachmentsViewModel = viewModel(),
    actionsConfig: MeetingDetailActionsConfig = MeetingDetailActionsConfig()
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
      actionsConfig.onNavigateBack()
    }
  }

  LaunchedEffect(uiState.updateSuccess) {
    if (uiState.updateSuccess) {
      Toast.makeText(context, "Meeting updated successfully", Toast.LENGTH_SHORT).show()
      viewModel.clearUpdateSuccess()
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
              IconButton(onClick = actionsConfig.onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
              }
            })
      },
      content = { padding ->
        if (uiState.isLoading) {
          LoadingScreen()
        } else if (uiState.meeting == null) {
          Text(
              modifier = Modifier.testTag(MeetingDetailScreenTestTags.ERROR_MESSAGE),
              text =
                  "There was an error while loading meetings : ${uiState.errorMsg ?: throw IllegalStateException("Error message should not be null if meeting is null.")}")
        } else {
          uiState.meeting?.let { meeting ->
            MeetingDetailContent(
                modifier = Modifier.padding(padding),
                meeting = meeting,
                participants = uiState.participants,
                attachmentsViewModel = attachmentsViewModel,
                editConfig =
                    EditConfig(
                        isEditMode = uiState.isEditMode,
                        isSaving = uiState.isSaving,
                        editTitle = uiState.editTitle,
                        editDateTime = uiState.editDateTime,
                        editDuration = uiState.editDuration,
                        hasTouchedTitle = uiState.hasTouchedTitle,
                        hasTouchedDateTime = uiState.hasTouchedDateTime,
                        hasTouchedDuration = uiState.hasTouchedDuration),
                actionsConfig =
                    MeetingDetailContentActionsConfig(
                        onJoinMeeting = actionsConfig.onJoinMeeting,
                        onRecordMeeting = actionsConfig.onRecordMeeting,
                        onViewTranscript = actionsConfig.onViewTranscript,
                        onDeleteMeeting = { showDeleteDialog = true },
                        onVoteForMeetingProposals = { isConnected ->
                          actionsConfig.onVoteForMeetingProposalClick(
                              projectId, meetingId, isConnected)
                        },
                        onEditMeeting = { isConnected ->
                          viewModel.toggleEditMode(meeting, isConnected)
                        },
                        onSaveMeeting = { isConnected ->
                          viewModel.saveMeetingChanges(meeting, isConnected)
                        },
                        onCancelEdit = { viewModel.toggleEditMode(null) },
                        onUpdateTitle = viewModel::updateEditTitle,
                        onUpdateDateTime = viewModel::updateEditDateTime,
                        onUpdateDuration = viewModel::updateEditDuration,
                        onTouchTitle = viewModel::touchTitle,
                        onTouchDateTime = viewModel::touchDateTime,
                        onTouchDuration = viewModel::touchDuration,
                        onNavigateToMeeting = actionsConfig.onNavigateToMeeting),
                isConnected = uiState.isConnected)
          } ?: ErrorScreen(message = uiState.errorMsg ?: "Meeting not found")
        }
      })

  if (showDeleteDialog) {
    DeleteConfirmationDialog(
        onConfirm = {
          showDeleteDialog = false
          viewModel.deleteMeeting(projectId, meetingId, uiState.isConnected)
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
 * Data class representing all the actions that can be executed by the buttons in the detail
 * content.
 *
 * @param onJoinMeeting Callback invoked when user clicks join meeting button, receives meeting
 *   link.
 * @param onRecordMeeting Callback invoked when user clicks record meeting button, receives
 *   projectId and meetingId.
 * @param onViewTranscript Callback invoked when user clicks view transcript button, receives
 *   projectId and meetingId.
 * @param onDeleteMeeting Callback invoked when user clicks delete meeting button.
 * @param onVoteForMeetingProposals Callback invoked when user votes for meeting proposals.
 * @param onEditMeeting Callback invoked when user clicks edit meeting button.
 * @param onSaveMeeting Callback invoked when user saves meeting changes, receives connectivity
 *   status.
 * @param onCancelEdit Callback invoked when user cancels edit mode.
 * @param onUpdateTitle Callback invoked when edit title changes.
 * @param onUpdateDateTime Callback invoked when edit date/time changes.
 * @param onUpdateDuration Callback invoked when edit duration changes.
 * @param onNavigateToMeeting Callback invoked when user clicks navigate to meeting button.
 */
data class MeetingDetailContentActionsConfig(
    val onJoinMeeting: (String, Boolean) -> Unit,
    val onRecordMeeting: (String, String, Boolean) -> Unit,
    val onViewTranscript: (String, String, Boolean) -> Unit,
    val onDeleteMeeting: () -> Unit,
    val onVoteForMeetingProposals: (Boolean) -> Unit,
    val onEditMeeting: (Boolean) -> Unit,
    val onSaveMeeting: (Boolean) -> Unit,
    val onCancelEdit: () -> Unit,
    val onUpdateTitle: (String) -> Unit,
    val onUpdateDateTime: (Timestamp) -> Unit,
    val onUpdateDuration: (Int) -> Unit,
    val onTouchTitle: () -> Unit,
    val onTouchDateTime: () -> Unit,
    val onTouchDuration: () -> Unit,
    val onNavigateToMeeting: (Boolean) -> Unit,
)

/**
 * Configuration for edit mode state.
 *
 * @param isEditMode Whether the screen is in edit mode.
 * @param isSaving Whether a save operation is in progress.
 * @param editTitle The title being edited.
 * @param editDateTime The date/time being edited.
 * @param editDuration The duration being edited.
 */
data class EditConfig(
    val isEditMode: Boolean,
    val isSaving: Boolean,
    val editTitle: String,
    val editDateTime: Timestamp?,
    val editDuration: Int,
    val hasTouchedTitle: Boolean,
    val hasTouchedDateTime: Boolean,
    val hasTouchedDuration: Boolean,
)

/**
 * Configuration for action button callbacks.
 *
 * @param onJoinMeeting Callback invoked when user clicks join meeting button, receives meeting
 *   link.
 * @param onRecordMeeting Callback invoked when user clicks record meeting button, receives
 *   projectId and meetingId.
 * @param onViewTranscript Callback invoked when user clicks view transcript button, receives
 *   projectId and meetingId.
 * @param onDeleteMeeting Callback invoked when user clicks delete meeting button.
 * @param onVoteForMeetingProposals Callback invoked when user votes for meeting proposals.
 * @param onEditMeeting Callback invoked when user clicks edit meeting button.
 * @param onNavigateToMeeting Callback invoked when user clicks navigate to meeting button.
 */
data class ActionButtonsConfig(
    val onJoinMeeting: (String, Boolean) -> Unit,
    val onRecordMeeting: (String, String, Boolean) -> Unit,
    val onViewTranscript: (String, String, Boolean) -> Unit,
    val onDeleteMeeting: () -> Unit,
    val onVoteForMeetingProposals: (Boolean) -> Unit,
    val onEditMeeting: (Boolean) -> Unit,
    val onNavigateToMeeting: (Boolean) -> Unit,
)

/**
 * Main content displaying meeting details.
 *
 * @param meeting The meeting to display.
 * @param participants List of participants in the meeting.
 * @param editConfig Configuration for edit mode state.
 * @param actionsConfig Actions that can be executed by buttons in the detail content.
 * @param modifier Modifier to be applied to the root composable.
 * @param isConnected Whether the device is connected to the internet.
 */
@Composable
private fun MeetingDetailContent(
    meeting: Meeting,
    participants: List<Participant>,
    editConfig: EditConfig,
    actionsConfig: MeetingDetailContentActionsConfig,
    modifier: Modifier = Modifier,
    attachmentsViewModel: MeetingAttachmentsViewModel,
    isConnected: Boolean = true,
) {
  LazyColumn(
      modifier =
          Modifier.fillMaxSize().then(modifier).testTag(MeetingDetailScreenTestTags.CONTENT_COLUMN),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { MeetingHeader(meeting = meeting) }

        item {
          if (editConfig.isEditMode) {
            // ppbbbb
            EditableMeetingInfoCard(
                config =
                    EditableMeetingInfoCardConfig(
                        editTitle = editConfig.editTitle,
                        editDateTime = editConfig.editDateTime,
                        editDuration = editConfig.editDuration,
                        meetingStatus = meeting.status,
                        hasTouchedTitle = editConfig.hasTouchedTitle,
                        hasTouchedDateTime = editConfig.hasTouchedDateTime,
                        hasTouchedDuration = editConfig.hasTouchedDuration,
                        onTitleChange = actionsConfig.onUpdateTitle,
                        onDateTimeChange = actionsConfig.onUpdateDateTime,
                        onDurationChange = actionsConfig.onUpdateDuration,
                        onTouchTitle = actionsConfig.onTouchTitle,
                        onTouchDateTime = actionsConfig.onTouchDateTime,
                        onTouchDuration = actionsConfig.onTouchDuration))
          } else {
            MeetingInformationCard(meeting = meeting)
          }
        }

        item { ParticipantsSection(participants = participants) }

        item { AttachmentsSection(meeting = meeting, attachmentsViewModel = attachmentsViewModel) }

        item {
          if (editConfig.isEditMode) {
            EditModeButtons(
                onSave = actionsConfig.onSaveMeeting,
                onCancel = actionsConfig.onCancelEdit,
                isSaving = editConfig.isSaving,
                isConnected = isConnected)
          } else {
            ActionButtonsSection(
                meeting = meeting,
                actionsConfig =
                    ActionButtonsConfig(
                        onJoinMeeting = actionsConfig.onJoinMeeting,
                        onRecordMeeting = actionsConfig.onRecordMeeting,
                        onViewTranscript = actionsConfig.onViewTranscript,
                        onDeleteMeeting = actionsConfig.onDeleteMeeting,
                        onVoteForMeetingProposals = actionsConfig.onVoteForMeetingProposals,
                        onEditMeeting = actionsConfig.onEditMeeting,
                        onNavigateToMeeting = actionsConfig.onNavigateToMeeting,
                    ),
                isConnected = isConnected,
            )
          }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (!isConnected) {
          item {
            Text(
                text =
                    "You are offline. Editing meetings is unavailable to prevent sync conflicts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier.padding(16.dp).testTag(MeetingDetailScreenTestTags.OFFLINE_MESSAGE))
          }
        }
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

              MeetingDateTimeInfo(meeting)
              MeetingFormatInfo(meeting)
              MeetingLocationInfo(meeting)
              MeetingLinkInfo(meeting)
            }
      }
}

@Composable
private fun MeetingDateTimeInfo(meeting: Meeting) {
  if (meeting.datetime != null && meeting.status != MeetingStatus.IN_PROGRESS) {
    InfoRow(
        icon = Icons.Default.Schedule,
        label = "Date & Time",
        value = Formatters.formatDateTime(meeting.datetime.toDate()),
        testTag = MeetingDetailScreenTestTags.MEETING_DATETIME)
  }
}

@Composable
private fun MeetingFormatInfo(meeting: Meeting) {
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
}

@Composable
private fun MeetingLocationInfo(meeting: Meeting) {
  if (meeting.format == MeetingFormat.IN_PERSON && meeting.location != null) {
    InfoRow(
        icon = Icons.Default.Place,
        label = "Location",
        value = meeting.location.name,
        testTag = MeetingDetailScreenTestTags.MEETING_LOCATION)
  }
}

@Composable
private fun MeetingLinkInfo(meeting: Meeting) {
  if (meeting.format == MeetingFormat.VIRTUAL && meeting.link != null) {
    InfoRow(
        icon = Icons.Default.VideoCall,
        label = "Meeting Link",
        value = meeting.link,
        testTag = MeetingDetailScreenTestTags.MEETING_LINK)
  }
}

/**
 * Holds all editable meeting information fields and their interaction callbacks.
 *
 * This configuration object groups the editable title, date/time, and duration fields along with
 * their respective change and touch event handlers. It helps reduce the number of parameters passed
 * to [EditableMeetingInfoCard].
 *
 * @property editTitle The current editable title of the meeting.
 * @property editDateTime The currently selected editable meeting date and time.
 * @property editDuration The editable meeting duration, in minutes.
 * @property meetingStatus The current status of the meeting (e.g., scheduled, completed,
 *   cancelled).
 * @property hasTouchedTitle Whether the title field has been interacted with.
 * @property hasTouchedDateTime Whether the date/time field has been interacted with.
 * @property hasTouchedDuration Whether the duration field has been interacted with.
 * @property onTitleChange Callback triggered when the meeting title changes.
 * @property onDateTimeChange Callback triggered when the meeting date or time changes.
 * @property onDurationChange Callback triggered when the meeting duration changes.
 * @property onTouchTitle Callback triggered when the title field is first touched.
 * @property onTouchDateTime Callback triggered when the date/time field is first touched.
 * @property onTouchDuration Callback triggered when the duration field is first touched.
 */
data class EditableMeetingInfoCardConfig(
    val editTitle: String,
    val editDateTime: Timestamp?,
    val editDuration: Int,
    val meetingStatus: MeetingStatus,
    val hasTouchedTitle: Boolean,
    val hasTouchedDateTime: Boolean,
    val hasTouchedDuration: Boolean,
    val onTitleChange: (String) -> Unit,
    val onDateTimeChange: (Timestamp) -> Unit,
    val onDurationChange: (Int) -> Unit,
    val onTouchTitle: () -> Unit,
    val onTouchDateTime: () -> Unit,
    val onTouchDuration: () -> Unit
)

/**
 * Editable card displaying meeting information in edit mode.
 *
 * Provides editable input fields for meeting title, date/time, and duration, using configuration
 * data supplied through [EditableMeetingInfoCardConfig].
 *
 * @param config The configuration containing editable field values, state flags, and callbacks for
 *   change and touch events.
 */
/**
 * Editable card displaying meeting information in edit mode.
 *
 * Provides editable input fields for meeting title, date/time, and duration, using configuration
 * data supplied through [EditableMeetingInfoCardConfig].
 *
 * @param config The configuration containing editable field values, state flags, and callbacks for
 *   change and touch events.
 */
@Composable
private fun EditableMeetingInfoCard(config: EditableMeetingInfoCardConfig) {
  // Convert Timestamp to LocalDate and LocalTime
  val localDateTime =
      config.editDateTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
  val editDate = localDateTime?.toLocalDate() ?: LocalDate.now()
  val editTime = localDateTime?.toLocalTime() ?: LocalTime.now()

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = "Edit Meeting Information",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold)

              HorizontalDivider()

              EditableTitleField(config = config)
              EditableDateTimeField(config = config, editDate = editDate, editTime = editTime)
              EditableDurationField(config = config)
            }
      }
}

/** Helper composable for the Title field to reduce complexity. */
@Composable
private fun EditableTitleField(config: EditableMeetingInfoCardConfig) {
  // Title field
  OutlinedTextField(
      value = config.editTitle,
      onValueChange = config.onTitleChange,
      label = { Text("Title") },
      placeholder = { Text("Meeting title") },
      modifier =
          Modifier.fillMaxWidth().onFocusChanged { focusState ->
            if (focusState.isFocused) {
              config.onTouchTitle()
            }
          })
  if (config.editTitle.isBlank() && config.hasTouchedTitle) {
    Text(
        text = "Title cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(MeetingDetailScreenTestTags.ERROR_MSG))
  }
}

/** Helper composable for the Date/Time fields to reduce complexity. */
@Composable
private fun EditableDateTimeField(
    config: EditableMeetingInfoCardConfig,
    editDate: LocalDate,
    editTime: LocalTime
) {
  // Date and Time fields - only editable when meeting is SCHEDULED
  if (config.meetingStatus == MeetingStatus.SCHEDULED) {
    // Date field
    DateInputField(
        selectedDate = editDate,
        label = "Date",
        placeHolder = "Select date",
        tag = "EditMeetingDate",
        onDateSelected = { newDate ->
          val newDateTime =
              java.time.LocalDateTime.of(newDate, editTime)
                  .atZone(ZoneId.systemDefault())
                  .toInstant()
          config.onDateTimeChange(Timestamp(newDateTime.epochSecond, newDateTime.nano))
        },
        onDateTouched = { config.onTouchDateTime() })

    // Time field
    TimeInputField(
        selectedTime = editTime,
        label = "Time",
        placeHolder = "Select time",
        tag = "EditMeetingTime",
        onTimeSelected = { newTime ->
          val newDateTime =
              java.time.LocalDateTime.of(editDate, newTime)
                  .atZone(ZoneId.systemDefault())
                  .toInstant()
          config.onDateTimeChange(Timestamp(newDateTime.epochSecond, newDateTime.nano))
        },
        onTimeTouched = { config.onTouchDateTime() })

    if (config.editDateTime == null && config.hasTouchedDateTime) {
      Text(
          text = "Date and time must be set",
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(MeetingDetailScreenTestTags.ERROR_MSG))
    }

    if (config.editDateTime != null &&
        config.hasTouchedDateTime &&
        java.time.LocalDateTime.of(editDate, editTime).isBefore(java.time.LocalDateTime.now())) {
      Text(
          text = "Meeting should be scheduled in the future.",
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(MeetingDetailScreenTestTags.ERROR_MSG))
    }
  }
}

/** Helper composable for the Duration field to reduce complexity. */
@Composable
private fun EditableDurationField(config: EditableMeetingInfoCardConfig) {
  // Duration field

  SingleChoiceInputField(
      config =
          SingleChoiceInputFieldConfig(
              currentValue = config.editDuration,
              displayValue = { d -> "$d minutes" },
              label = "Duration",
              placeholder = "Select duration",
              icon = Icons.Default.HourglassTop,
              iconDescription = "Select duration",
              alertDialogTitle = "Select a duration",
              options = listOf(5, 10, 15, 20, 30, 45, 60),
              tag = "EditMeetingDuration",
              onOptionSelected = { duration ->
                config.onTouchDuration()
                config.onDurationChange(duration)
              }))
  if (config.editDuration <= 0 && config.hasTouchedDuration) {
    Text(
        text = "Duration must be greater than 0",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(MeetingDetailScreenTestTags.ERROR_MSG))
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
    Spacer(modifier = Modifier.width(8.dp))
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
 * @param meeting the meeting whose attachments to display
 * @param attachmentsViewModel meeting attachments view model
 */
@Composable
fun AttachmentsSection(
    meeting: Meeting,
    attachmentsViewModel: MeetingAttachmentsViewModel = viewModel()
) {
  val attachments = meeting.attachmentUrls
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

              Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    MeetingAttachmentFilePicker(
                        meeting.projectId, meeting.meetingID, attachmentsViewModel)
                    if (attachments.isEmpty()) {
                      Text(
                          text = "No attachments",
                          style = MaterialTheme.typography.bodyMedium,
                          color = Color.Gray,
                          modifier =
                              Modifier.testTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE))
                    } else {
                      attachments.forEach { attachment ->
                        AttachmentItem(
                            attachmentUrl = attachment,
                            projectId = meeting.projectId,
                            meetingId = meeting.meetingID,
                            attachmentsViewModel = attachmentsViewModel)
                      }
                    }
                  }
            }
      }
}

object AttachmentItemTestTags {
  fun deleteButtonAttachmentTestTag(downloadUrl: String): String {
    return "delete_button_$downloadUrl"
  }

  fun downloadButtonAttachmentTestTag(downloadUrl: String): String {
    return "download_button_$downloadUrl"
  }

  fun downloadButtonCircularProgressIndicatorTestTag(downloadUrl: String): String {
    return "download_button_circular_progress_indicator_$downloadUrl"
  }
}

/**
 * Individual attachment item.
 *
 * @param projectId the project id
 * @param meetingId the meeting id
 * @param attachmentUrl The URL of the attachment to display.
 * @param attachmentsViewModel view model that handles attachments
 */
@Composable
fun AttachmentItem(
    projectId: String,
    meetingId: String,
    attachmentUrl: String,
    attachmentsViewModel: MeetingAttachmentsViewModel
) {
  val context = LocalContext.current
  val downloadingFilesSet = remember { attachmentsViewModel.isDownloadingFile }.collectAsState()
  Row(
      modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.ATTACHMENT_ITEM),
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = "Attachment",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                  text = attachmentsViewModel.getFilenameFromDownloadURL(attachmentUrl) ?: "",
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 1)
            }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                  modifier =
                      Modifier.testTag(
                          AttachmentItemTestTags.deleteButtonAttachmentTestTag(attachmentUrl)),
                  onClick = {
                    attachmentsViewModel.deleteFileFromMeetingAttachments(
                        projectId,
                        meetingId,
                        attachmentUrl,
                        onFailure = { message ->
                          Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        })
                  },
                  colors =
                      IconButtonDefaults.iconButtonColors(
                          containerColor = LightColorScheme.primary)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        tint = Color.White,
                        contentDescription = null)
                  }
              Spacer(modifier = Modifier.width(12.dp))
              if (downloadingFilesSet.value.contains(attachmentUrl)) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.testTag(
                            AttachmentItemTestTags.downloadButtonCircularProgressIndicatorTestTag(
                                attachmentUrl)),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp)
              } else {
                IconButton(
                    modifier =
                        Modifier.testTag(
                            AttachmentItemTestTags.downloadButtonAttachmentTestTag(attachmentUrl)),
                    onClick = {
                      attachmentsViewModel.downloadFileToPhone(
                          context,
                          attachmentUrl,
                          {},
                          { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                          })
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = LightingBlue)) {
                      Icon(
                          imageVector = Icons.Default.Download,
                          tint = Color.White,
                          contentDescription = null)
                    }
              }
            }
      }
}

/**
 * Action buttons section (join, record, transcript, delete, edit).
 *
 * @param meeting The meeting for which to display action buttons.
 * @param actionsConfig Configuration for action button callbacks.
 * @param isConnected Whether the device is connected to the internet.
 */
@Composable
private fun ActionButtonsSection(
    meeting: Meeting,
    actionsConfig: ActionButtonsConfig,
    isConnected: Boolean = true,
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
                  onClick = { actionsConfig.onJoinMeeting(meeting.link, isConnected) },
                  enabled = isConnected,
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON)
                          .alpha(getAlpha(isConnected))) {
                    Icon(imageVector = Icons.Default.VideoCall, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Meeting")
                  }
            }
            if (meeting.format == MeetingFormat.IN_PERSON && meeting.location != null) {
              Button(
                  onClick = { actionsConfig.onNavigateToMeeting(isConnected) },
                  enabled = isConnected,
                  modifier = Modifier.fillMaxWidth().alpha(getAlpha(isConnected))) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = stringResource(R.string.location_icon))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Location")
                  }
            }
            OutlinedButton(
                onClick = {
                  actionsConfig.onRecordMeeting(meeting.projectId, meeting.meetingID, isConnected)
                },
                enabled = isConnected,
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(MeetingDetailScreenTestTags.RECORD_BUTTON)
                        .alpha(getAlpha(isConnected))) {
                  Text("Start Recording")
                }
          }
          MeetingStatus.COMPLETED -> {
            Button(
                onClick = {
                  actionsConfig.onViewTranscript(meeting.projectId, meeting.meetingID, isConnected)
                },
                enabled = isConnected,
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON)
                        .alpha(getAlpha(isConnected))) {
                  Icon(imageVector = Icons.Default.Description, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("View Transcript")
                }
          }
          MeetingStatus.OPEN_TO_VOTES -> {

            Button(
                onClick = { actionsConfig.onVoteForMeetingProposals(isConnected) },
                enabled = isConnected,
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(MeetingDetailScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
                        .alpha(getAlpha(isConnected))) {
                  Icon(
                      imageVector = Icons.Default.HowToVote,
                      contentDescription = "Vote for meeting proposal")
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Vote for meeting proposals")
                }
          }
        }

        // Edit button
        OutlinedButton(
            onClick = { actionsConfig.onEditMeeting(isConnected) },
            enabled = isConnected,
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
                    .alpha(getAlpha(isConnected))) {
              Text("Edit Meeting")
            }

        OutlinedButton(
            onClick = { actionsConfig.onDeleteMeeting() },
            enabled = isConnected,
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(MeetingDetailScreenTestTags.DELETE_BUTTON)
                    .alpha(getAlpha(isConnected)),
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
 * Edit mode buttons (Save and Cancel).
 *
 * @param onSave Callback invoked when user clicks save button, receives connectivity status.
 * @param onCancel Callback invoked when user clicks cancel button.
 * @param isSaving Whether a save operation is in progress.
 * @param isConnected Whether the device is connected.
 */
@Composable
private fun EditModeButtons(
    onSave: (Boolean) -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean,
    isConnected: Boolean
) {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
        text = "Edit Mode",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold)

    Button(
        onClick = { onSave(isConnected) },
        modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.SAVE_BUTTON),
        enabled = !isSaving && isConnected) {
          if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
          } else {
            Text("Save Changes")
          }
        }

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().testTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON),
        enabled = !isSaving) {
          Text("Cancel")
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

@Composable
private fun MeetingAttachmentFilePicker(
    projectId: String,
    meetingId: String,
    meetingAttachmentsViewModel: MeetingAttachmentsViewModel
) {
  val uploadingFile = remember { meetingAttachmentsViewModel.isUploadingFile }.collectAsState()
  val context = LocalContext.current
  val contentResolver = context.contentResolver
  val filePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        if (uri != null) {
          meetingAttachmentsViewModel.uploadMeetingFileToFirestore(
              contentResolver,
              uri,
              projectId,
              meetingId,
              {},
              { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() })
        }
      }

  Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        if (!uploadingFile.value) {
          Button(
              modifier = Modifier.testTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_BUTTON),
              onClick = { filePickerLauncher.launch("*/*") }) {
                Text("Pick a File")
              }
        } else {
          CircularProgressIndicator(
              modifier =
                  Modifier.testTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_PROGRESS_INDICATOR)
                      .size(48.dp),
              color = MaterialTheme.colorScheme.primary,
              strokeWidth = 4.dp)
        }
      }
}

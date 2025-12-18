/* Portions of this code were written with the help of Claude Code and Grok.*/

package ch.eureka.eurekapp.ui.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.calendar.MeetingCalendarViewModel
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.InteractiveHelpEntryPoint
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters

private const val PRESSED_SCALE = 0.98f
private const val NORMAL_SCALE = 1f
private const val ANIMATION_DURATION_MS = 150
private val CardGradientColors = listOf(Color.White, EColors.GradientLightColor)

object MeetingScreenTestTags {
  const val MEETING_SCREEN = "MeetingScreen"
  const val MEETING_SCREEN_TITLE = "MeetingScreenTitle"
  const val MEETING_SCREEN_DESCRIPTION = "MeetingScreenDescription"
  const val MEETING_TABS = "MeetingTabs"
  const val MEETING_TAB_UPCOMING = "MeetingTabUpcoming"
  const val MEETING_TAB_PAST = "MeetingTabPast"
  const val MEETING_CARD = "MeetingCard"
  const val MEETING_TITLE = "MeetingTitle"
  const val MEETING_STATUS_TEXT = "MeetingStatusText"
  const val MEETING_DATETIME = "MeetingDateTime"
  const val MEETING_DURATION = "MeetingDuration"
  const val MEETING_VOTE_FOR_DATETIME_MESSAGE = "MeetingVoteForDateTimeMessage"
  const val MEETING_VOTE_FOR_FORMAT_MESSAGE = "MeetingVoteForFormatMessage"
  const val MEETING_LINK = "MeetingLink"
  const val MEETING_LOCATION = "MeetingLocation"
  const val JOIN_MEETING_BUTTON = "JoinMeetingButton"
  const val DIRECTIONS_BUTTON = "DirectionsButton"
  const val RECORD_BUTTON = "RecordButton"
  const val VOTE_FOR_MEETING_PROPOSAL_BUTTON = "VoteForMeetingProposalButton"
  const val VIEW_TRANSCRIPT_BUTTON = "SeeTranscriptButton"
  const val NO_UPCOMING_MEETINGS_MESSAGE = "NoUpcomingMeetingsMessageTest"
  const val NO_PAST_MEETINGS_MESSAGE = "NoPastMeetingsMessage"
  const val CREATE_MEETING_BUTTON = "CreateMeetingButton"
  const val CLOSE_VOTES_BUTTON = "CloseVotesButton"
  const val OFFLINE_MESSAGE = "offlineMessage"

  fun getCalendarButtonTestTagForScheduledMeeting(meetingId: String): String {
    return "calendar_test_tag_button_$meetingId"
  }
}

/**
 * Config for the main composable to draw the meetings screen.
 *
 * @property onCreateMeeting Callback executed when the users creates a new meeting.
 * @property onMeetingClick Callback when a meeting card is clicked, receives projectId and
 *   meetingId.
 * @property onVoteForMeetingProposalClick Callback when the "Vote for meeting proposals" button is
 *   clicked.
 * @property onNavigateToMeeting Callback called when user wants to navigate to meeting location.
 * @property onViewTranscript Callback called when user wants to view the transcript of a meting.
 * @property onRecord Callback executed when the user clicks on the record button.
 */
data class MeetingScreenConfig(
    val onCreateMeeting: (Boolean) -> Unit,
    val onMeetingClick: (String, String) -> Unit = { _, _ -> },
    val onVoteForMeetingProposalClick: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onNavigateToMeeting: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onRecord: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onFileManagementScreenClick: () -> Unit = {}
)

/**
 * Main composable to draw the meetings screen.
 *
 * @param meetingViewModel The view model associated to the meetings screen.
 * @param config Config for the composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingScreen(
    config: MeetingScreenConfig,
    meetingViewModel: MeetingViewModel = viewModel(),
    calendarViewModel: MeetingCalendarViewModel = viewModel(),
) {
  val context = LocalContext.current

  var hasCalendarReadPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED)
  }
  var hasCalendarWritePermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED)
  }

  val launcherCalendarReadPermission =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasCalendarReadPermission = isGranted
      }

  val launcherCalendarWritePermission =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasCalendarWritePermission = isGranted
      }

  LaunchedEffect(Unit) {
    if (!hasCalendarReadPermission) {
      launcherCalendarReadPermission.launch(Manifest.permission.READ_CALENDAR)
    }
    if (!hasCalendarWritePermission) {
      launcherCalendarWritePermission.launch(Manifest.permission.WRITE_CALENDAR)
    }
  }

  val uiState by meetingViewModel.uiState.collectAsState()

  // Show error message if there is any
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      meetingViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(Unit) { meetingViewModel.loadMeetings() }

  LaunchedEffect(meetingViewModel.userId) {
    if (meetingViewModel.userId == null) {
      meetingViewModel.setErrorMsg("Not logged in")
    }
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Meetings",
            modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN_TITLE),
            actions = {
              InteractiveHelpEntryPoint(
                  helpContext = HelpContext.MEETINGS,
                  modifier = Modifier.testTag("meetingsHelpButton"))
              IconButton(
                  onClick = config.onFileManagementScreenClick,
                  modifier = Modifier.testTag(TasksScreenTestTags.FILES_MANAGEMENT_BUTTON)) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = "Manage Files",
                        tint = EColors.WhiteTextColor)
                  }
            })
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { config.onCreateMeeting(uiState.isConnected) },
            modifier =
                Modifier
                    .size(60.dp)
                    .offset(y = (-48).dp)
                    .testTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON),
            containerColor =
                if (uiState.isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Create meeting")
            }
      },
      content = { padding ->
        MeetingScreenContent(
            padding = padding,
            uiState = uiState,
            config = config,
            meetingViewModel = meetingViewModel,
            calendarViewModel = calendarViewModel)
      })
}

@Composable
private fun MeetingScreenContent(
    padding: PaddingValues,
    uiState: MeetingUIState,
    config: MeetingScreenConfig,
    meetingViewModel: MeetingViewModel,
    calendarViewModel: MeetingCalendarViewModel
) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(padding)
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .background(Color.White)
              .testTag(MeetingScreenTestTags.MEETING_SCREEN)) {
        Text(
            modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN_DESCRIPTION),
            text = "Schedule and manage your team meetings",
            style = MaterialTheme.typography.bodyLarge,
            color = EColors.GrayTextColor2,
            fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(4.dp))

        if (!uiState.isConnected) {
          Text(
              text = "You are offline. Meeting creation is unavailable to prevent sync conflicts.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
              modifier =
                  Modifier.padding(Spacing.md).testTag(MeetingScreenTestTags.OFFLINE_MESSAGE))
        }

        RoundedTabRow(
            tabs = MeetingTab.entries.toTypedArray(),
            selectedTab = uiState.selectedTab,
            onTabSelected = { meetingViewModel.selectTab(it) })

        Spacer(Modifier.height(4.dp))

        when (uiState.selectedTab) {
          MeetingTab.UPCOMING ->
              MeetingsList(
                  MeetingsListConfig(
                      modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                      meetings = uiState.upcomingMeetings,
                      tabName = MeetingTab.UPCOMING.name.lowercase(),
                      isCurrentUserId = { uid -> meetingViewModel.userId == uid },
                      onMeetingClick = config.onMeetingClick,
                      onVoteForMeetingProposalClick = config.onVoteForMeetingProposalClick,
                      onNavigateToMeeting = config.onNavigateToMeeting,
                      onCloseVotes = { meeting, isConnected ->
                        meetingViewModel.closeVotesForMeeting(meeting, isConnected)
                      },
                      onViewTranscript = config.onViewTranscript,
                      onRecord = config.onRecord,
                      isConnected = uiState.isConnected),
                  calendarViewModel = calendarViewModel)
          MeetingTab.PAST ->
              MeetingsList(
                  MeetingsListConfig(
                      modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                      meetings = uiState.pastMeetings,
                      tabName = MeetingTab.PAST.name.lowercase(),
                      isCurrentUserId = { uid -> meetingViewModel.userId == uid },
                      onMeetingClick = config.onMeetingClick,
                      isConnected = uiState.isConnected),
                  calendarViewModel = calendarViewModel)
        }
      }
}

/**
 * Config for component that displays the meetings.
 *
 * @property modifier Modifier used in the component.
 * @property meetings Meetings list to display.
 * @property tabName Name of the tab in which to display these meetings.
 * @property isCurrentUserId Function taking as argument a user ID and return true if this is the Id
 *   of the user that is currently logged in and false otherwise.
 * @property onMeetingClick Callback when a meeting card is clicked.
 * @property onVoteForMeetingProposalClick Callback when the "Vote for meeting proposals" button is
 *   clicked.
 * @property onNavigateToMeeting Function executed when user navigates to location of meeting.
 * @property onCloseVotes Function executed when the user clicks on "close votes" button.
 * @property onViewTranscript Function executed when user clicks on "meeting transcripts" button.
 * @property onRecord Function executed when user clicks on "record" button.
 * @property isConnected Boolean indicating if the device is connected to the internet.
 */
data class MeetingsListConfig(
    val modifier: Modifier,
    val meetings: List<Meeting>,
    val tabName: String,
    val isCurrentUserId: (String) -> Boolean,
    val onMeetingClick: (String, String) -> Unit = { _, _ -> },
    val onVoteForMeetingProposalClick: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onNavigateToMeeting: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onCloseVotes: (Meeting, Boolean) -> Unit = { _, _ -> },
    val onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onRecord: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val isConnected: Boolean,
)

/**
 * Handles joining a meeting by opening the meeting link in a browser.
 *
 * @param context Android context for starting activities and showing toasts
 * @param link The meeting link to open, or null if no link is available
 */
private fun handleJoinMeeting(context: Context, link: String?) {
  if (!link.isNullOrBlank()) {
    val browserIntent =
        Intent(Intent.ACTION_VIEW, link.toUri()).apply {
          addCategory(Intent.CATEGORY_BROWSABLE)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    context.startActivity(browserIntent)
  } else {
    Toast.makeText(context, "No meeting link available", Toast.LENGTH_SHORT).show()
  }
}

/**
 * Component that displays the meetings.
 *
 * @param config Config of that composable.
 * @param calendarViewModel calendarViewModel for keeping state of added meetings to calendar
 * @param context the local context to use
 */
@Composable
fun MeetingsList(
    config: MeetingsListConfig,
    calendarViewModel: MeetingCalendarViewModel = viewModel(),
    context: Context = LocalContext.current
) {
  val registeredMeetings = remember { calendarViewModel.registeredMeetings }.collectAsState()

  if (config.meetings.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = config.modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
          items(config.meetings.size) { index ->
            val meeting = config.meetings[index]
            MeetingCard(
                meeting = meeting,
                config =
                    MeetingCardConfig(
                        isCurrentUserId = config.isCurrentUserId,
                        onClick = { config.onMeetingClick(meeting.projectId, meeting.meetingID) },
                        onJoinMeeting = { _ -> handleJoinMeeting(context, meeting.link) },
                        onVoteForMeetingProposals = { isConnected ->
                          config.onVoteForMeetingProposalClick(
                              meeting.projectId, meeting.meetingID, isConnected)
                        },
                        onDirections = { isConnected ->
                          config.onNavigateToMeeting(
                              meeting.projectId, meeting.meetingID, isConnected)
                        },
                        onCloseVotes = { meeting, isConnected ->
                          config.onCloseVotes(meeting, isConnected)
                        },
                        onViewTranscript = { projectId, meetingId, isConnected ->
                          config.onViewTranscript(projectId, meetingId, isConnected)
                        },
                        onRecord = { projectId, meetingId, isConnected ->
                          config.onRecord(projectId, meetingId, isConnected)
                        },
                        onAddMeetingToCalendar = { meeting ->
                          calendarViewModel.addMeetingToCalendar(
                              context.contentResolver,
                              meeting,
                              onSuccess = {
                                Toast.makeText(
                                        context,
                                        "Successfully saved the event to the calendar!",
                                        Toast.LENGTH_SHORT)
                                    .show()
                              },
                              onFailure = {
                                Toast.makeText(
                                        context,
                                        "There was a problem saving the event to the calendar!",
                                        Toast.LENGTH_SHORT)
                                    .show()
                              })
                        },
                        isMeetingAddedToCalendar =
                            registeredMeetings.value.getOrElse(meeting.meetingID) {
                              if (meeting.status == MeetingStatus.SCHEDULED) {
                                calendarViewModel.checkIsMeetingRegisteredInCalendar(
                                    context.contentResolver, meeting)
                              } else {
                                true
                              }
                              false
                            },
                        isConnected = config.isConnected,
                    ))
          }
        }
  } else {
    Text(
        modifier =
            config.modifier.testTag(
                if (config.tabName == MeetingTab.UPCOMING.name.lowercase())
                    MeetingScreenTestTags.NO_UPCOMING_MEETINGS_MESSAGE
                else MeetingScreenTestTags.NO_PAST_MEETINGS_MESSAGE),
        text = "You have no ${config.tabName} meetings yet.")
  }
}

/**
 * Data class representing all the actions that can be executed by buttons on a meeting card.
 *
 * @property isCurrentUserId Function taking as argument a user ID and return true if this is the Id
 *   of the user that is currently logged in and false otherwise.
 * @property isMeetingAddedToCalendar the state that keeps track if the meeting is added to calendar
 *   or not
 * @property onClick Function to execute when the card is clicked (for navigation to detail screen).
 * @property onJoinMeeting Function to execute when user clicks on button to join meeting.
 * @property onVoteForMeetingProposals Function to execute when user clicks on button to vote for
 *   meeting proposals.
 * @property onVoteForFormat Function to execute when user clicks on button to vote for meeting
 *   format
 * @property onDirections Function to execute when user clicks on button to navigate to a meeting.
 * @property onRecord Function to execute when user clicks on record button.
 * @property onViewSummary Function executed when user clicks on view summary button.
 * @property onViewTranscript Function executed when user clicks on view transcript button.
 * @property onCloseVotes Function executed when the user clicks on "close votes" button.
 * @property isConnected Boolean indicating if the device is connected to the internet.
 * @property onAddMeetingToCalendar Function executed when the user clicks "add to calendar" button
 */
data class MeetingCardConfig(
    val isCurrentUserId: (String) -> Boolean,
    val isMeetingAddedToCalendar: Boolean = false,
    val onClick: () -> Unit = {},
    val onJoinMeeting: (Boolean) -> Unit = {},
    val onVoteForMeetingProposals: (Boolean) -> Unit = {},
    val onVoteForFormat: () -> Unit = {},
    val onDirections: (Boolean) -> Unit = {},
    val onRecord: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onViewSummary: () -> Unit = {},
    val onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val onCloseVotes: (Meeting, Boolean) -> Unit = { _, _ -> },
    val isConnected: Boolean,
    val onAddMeetingToCalendar: (Meeting) -> Unit = { _ -> }
)

/**
 * Component that displays the information of a meeting.
 *
 * @param meeting The meeting to display.
 * @param config The meeting card config.
 */
@Composable
fun MeetingCard(
    meeting: Meeting,
    config: MeetingCardConfig,
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by
      animateFloatAsState(
          targetValue = if (isPressed) PRESSED_SCALE else NORMAL_SCALE,
          animationSpec = tween(ANIMATION_DURATION_MS))

  val statusColor =
      when (meeting.status) {
        MeetingStatus.OPEN_TO_VOTES -> EColors.StatusBlue
        MeetingStatus.SCHEDULED -> EColors.GrayTextColor2
        MeetingStatus.COMPLETED -> EColors.SecondaryTextColor
        MeetingStatus.IN_PROGRESS -> EColors.StatusGreen
      }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .scale(scale)
              .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp))
              .clickable(role = Role.Button, onClick = config.onClick)
              .testTag(MeetingScreenTestTags.MEETING_CARD),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = EColors.CardBorderColor,
                        shape = RoundedCornerShape(12.dp))
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors = CardGradientColors,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)))) {
              Column(modifier = Modifier.padding(20.dp)) {
                // Header: Title + Status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meeting.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = EColors.TitleTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_TITLE))
                      }
                      // Status badge
                      Surface(
                          color = statusColor.copy(alpha = 0.1f),
                          shape = RoundedCornerShape(12.dp)) {
                            Text(
                                modifier =
                                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag(MeetingScreenTestTags.MEETING_STATUS_TEXT),
                                text = meeting.status.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold)
                          }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata rows with icons
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  // Datetime/Duration row
                  when (meeting.status) {
                    MeetingStatus.OPEN_TO_VOTES -> {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier =
                                    Modifier.size(32.dp)
                                        .background(
                                            color = EColors.IconBackgroundColor,
                                            shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center) {
                                  Icon(
                                      imageVector = Icons.Default.HourglassTop,
                                      contentDescription = "Duration",
                                      modifier = Modifier.size(16.dp),
                                      tint = EColors.GrayTextColor2)
                                }
                            Column {
                              Text(
                                  modifier =
                                      Modifier.testTag(MeetingScreenTestTags.MEETING_DURATION),
                                  text = "${meeting.duration} minutes",
                                  style = MaterialTheme.typography.bodyMedium,
                                  color = EColors.TitleTextColor,
                                  fontWeight = FontWeight.SemiBold)
                              Spacer(modifier = Modifier.height(4.dp))
                              Row(
                                  verticalAlignment = Alignment.CenterVertically,
                                  horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.HowToVote,
                                        contentDescription = "Vote",
                                        modifier = Modifier.size(14.dp),
                                        tint = EColors.GrayTextColor2)
                                    Text(
                                        modifier =
                                            Modifier.testTag(
                                                MeetingScreenTestTags
                                                    .MEETING_VOTE_FOR_DATETIME_MESSAGE),
                                        text = "Vote for your preferred datetime(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EColors.GrayTextColor2)
                                  }
                            }
                          }
                    }
                    MeetingStatus.SCHEDULED,
                    MeetingStatus.IN_PROGRESS,
                    MeetingStatus.COMPLETED -> {
                      checkNotNull(meeting.datetime) {
                        "Datetime should be set if meeting is not open to votes."
                      }
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier =
                                    Modifier.size(32.dp)
                                        .background(
                                            color = EColors.IconBackgroundColor,
                                            shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center) {
                                  Icon(
                                      imageVector = Icons.Default.Schedule,
                                      contentDescription = "Schedule",
                                      modifier = Modifier.size(16.dp),
                                      tint = EColors.GrayTextColor2)
                                }
                            Text(
                                modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_DATETIME),
                                text = Formatters.formatDateTime(meeting.datetime.toDate()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = EColors.TitleTextColor,
                                fontWeight = FontWeight.SemiBold)
                          }
                    }
                  }

                  // Format/Location row
                  when (meeting.status) {
                    MeetingStatus.OPEN_TO_VOTES -> {
                      when (meeting.format) {
                        MeetingFormat.IN_PERSON,
                        MeetingFormat.VIRTUAL ->
                            throw IllegalStateException(
                                "Format must not be set if meeting is open to votes.")
                        null -> {
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier =
                                        Modifier.size(32.dp)
                                            .background(
                                                color = EColors.IconBackgroundColor,
                                                shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center) {
                                      Icon(
                                          imageVector = Icons.Default.HowToVote,
                                          contentDescription = "Vote",
                                          modifier = Modifier.size(16.dp),
                                          tint = EColors.GrayTextColor2)
                                    }
                                Text(
                                    modifier =
                                        Modifier.testTag(
                                            MeetingScreenTestTags.MEETING_VOTE_FOR_FORMAT_MESSAGE),
                                    text = "Vote for your preferred meeting format",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EColors.SecondaryTextColor,
                                    fontWeight = FontWeight.Medium)
                              }
                        }
                      }
                    }
                    MeetingStatus.SCHEDULED,
                    MeetingStatus.IN_PROGRESS,
                    MeetingStatus.COMPLETED -> {
                      when (meeting.format) {
                        MeetingFormat.IN_PERSON -> {
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier =
                                        Modifier.size(32.dp)
                                            .background(
                                                color = EColors.IconBackgroundColor,
                                                shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center) {
                                      Icon(
                                          imageVector = Icons.Default.Place,
                                          contentDescription = "Location",
                                          modifier = Modifier.size(16.dp),
                                          tint = EColors.GrayTextColor2)
                                    }
                                Text(
                                    modifier =
                                        Modifier.testTag(MeetingScreenTestTags.MEETING_LOCATION),
                                    text =
                                        meeting.location?.name
                                            ?: throw IllegalStateException(
                                                "Location of in-person meeting closed to votes should exist."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EColors.SecondaryTextColor,
                                    fontWeight = FontWeight.Medium)
                              }
                        }
                        MeetingFormat.VIRTUAL -> {
                          if (meeting.status != MeetingStatus.COMPLETED) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                  Box(
                                      modifier =
                                          Modifier.size(32.dp)
                                              .background(
                                                  color = EColors.IconBackgroundColor,
                                                  shape = RoundedCornerShape(8.dp)),
                                      contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VideoCall,
                                            contentDescription = "Video call",
                                            modifier = Modifier.size(16.dp),
                                            tint = EColors.GrayTextColor2)
                                      }
                                  Text(
                                      modifier =
                                          Modifier.testTag(MeetingScreenTestTags.MEETING_LINK),
                                      text =
                                          meeting.link
                                              ?: throw IllegalStateException(
                                                  "Link to scheduled/in progress virtual meeting should exist."),
                                      style = MaterialTheme.typography.bodyMedium,
                                      color = EColors.SecondaryTextColor,
                                      fontWeight = FontWeight.Medium)
                                }
                          }
                        }
                        null ->
                            throw IllegalStateException(
                                "Format of meeting closed to votes should be set.")
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically) {
                      when (meeting.status) {
                        MeetingStatus.OPEN_TO_VOTES -> {
                          Button(
                              onClick = { config.onVoteForMeetingProposals(config.isConnected) },
                              modifier =
                                  Modifier.weight(1f)
                                      .testTag(
                                          MeetingScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON),
                              enabled = config.isConnected,
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = MaterialTheme.colorScheme.primary,
                                      contentColor = Color.White)) {
                                Text(
                                    "Vote",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold)
                              }
                          if (config.isCurrentUserId(meeting.createdBy)) {
                            OutlinedButton(
                                onClick = { config.onCloseVotes(meeting, config.isConnected) },
                                modifier =
                                    Modifier.testTag(MeetingScreenTestTags.CLOSE_VOTES_BUTTON),
                                enabled = config.isConnected,
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary)) {
                                  Text(
                                      "Close votes",
                                      style = MaterialTheme.typography.labelLarge,
                                      fontWeight = FontWeight.SemiBold)
                                }
                          }
                        }
                        MeetingStatus.SCHEDULED,
                        MeetingStatus.IN_PROGRESS -> {
                          when (meeting.format) {
                            MeetingFormat.IN_PERSON -> {
                              Button(
                                  onClick = { config.onDirections(config.isConnected) },
                                  modifier =
                                      Modifier.weight(1f)
                                          .testTag(MeetingScreenTestTags.DIRECTIONS_BUTTON),
                                  enabled = config.isConnected,
                                  colors =
                                      ButtonDefaults.buttonColors(
                                          containerColor = MaterialTheme.colorScheme.primary,
                                          contentColor = Color.White)) {
                                    Text(
                                        "Directions",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold)
                                  }
                              OutlinedButton(
                                  onClick = {
                                    config.onRecord(
                                        meeting.projectId, meeting.meetingID, config.isConnected)
                                  },
                                  modifier = Modifier.testTag(MeetingScreenTestTags.RECORD_BUTTON),
                                  enabled = config.isConnected,
                                  colors =
                                      ButtonDefaults.outlinedButtonColors(
                                          contentColor = MaterialTheme.colorScheme.primary)) {
                                    Text(
                                        "Record",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold)
                                  }
                              if (!config.isMeetingAddedToCalendar) {
                                IconButton(
                                    modifier =
                                        Modifier.testTag(
                                            MeetingScreenTestTags
                                                .getCalendarButtonTestTagForScheduledMeeting(
                                                    meeting.meetingID)),
                                    onClick = { config.onAddMeetingToCalendar(meeting) }) {
                                      Icon(
                                          imageVector = Icons.Default.CalendarToday,
                                          contentDescription = "Add to calendar",
                                          tint = MaterialTheme.colorScheme.primary,
                                          modifier = Modifier.size(24.dp))
                                    }
                              }
                            }
                            MeetingFormat.VIRTUAL -> {
                              Button(
                                  onClick = { config.onJoinMeeting(config.isConnected) },
                                  modifier =
                                      Modifier.fillMaxWidth()
                                          .testTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON),
                                  enabled = config.isConnected,
                                  colors =
                                      ButtonDefaults.buttonColors(
                                          containerColor = MaterialTheme.colorScheme.primary,
                                          contentColor = Color.White)) {
                                    Text(
                                        "Join meeting",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold)
                                  }
                            }
                            null ->
                                throw IllegalStateException(
                                    "Format of scheduled/in progress should exist.")
                          }
                        }
                        MeetingStatus.COMPLETED -> {
                          Button(
                              onClick = {
                                config.onViewTranscript(
                                    meeting.projectId, meeting.meetingID, config.isConnected)
                              },
                              modifier =
                                  Modifier.fillMaxWidth()
                                      .testTag(MeetingScreenTestTags.VIEW_TRANSCRIPT_BUTTON),
                              colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = MaterialTheme.colorScheme.primary,
                                      contentColor = Color.White)) {
                                Text(
                                    "View Transcript",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold)
                              }
                        }
                      }
                    }
              }
            }
      }
}

/**
 * Component to display tabs meeting at the top of the page.
 *
 * @param tabs All the tabs to display.
 * @param selectedTab The tab meeting currently selected by the user.
 * @param onTabSelected Function to execute when tab is selected.
 */
@Composable
fun RoundedTabRow(
    tabs: Array<MeetingTab>,
    selectedTab: MeetingTab,
    onTabSelected: (MeetingTab) -> Unit
) {
  Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      tonalElevation = 2.dp,
      modifier =
          Modifier.padding(horizontal = 8.dp)
              .fillMaxWidth()
              .wrapContentHeight()
              .testTag(MeetingScreenTestTags.MEETING_TABS)) {
        Row(
            modifier = Modifier.padding(6.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier =
                        Modifier.weight(1f)
                            .height(40.dp)
                            .padding(horizontal = 4.dp)
                            .testTag(
                                if (tab.ordinal == 0) MeetingScreenTestTags.MEETING_TAB_UPCOMING
                                else MeetingScreenTestTags.MEETING_TAB_PAST)
                            .clickable { onTabSelected(tab) },
                ) {
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = tab.name,
                        color =
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                  }
                }
              }
            }
      }
}

/* Portions of this code were written with the help of Claude Code and Grok.*/

package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters

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
}

/**
 * Config for the main composable to draw the meetings screen.
 *
 * @property projectId The ID of the project to display the meetings from.
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
    val projectId: String,
    val onCreateMeeting: () -> Unit,
    val onMeetingClick: (String, String) -> Unit = { _, _ -> },
    val onVoteForMeetingProposalClick: (String, String) -> Unit = { _, _ -> },
    val onNavigateToMeeting: (String, String) -> Unit = { _, _ -> },
    val onViewTranscript: (String, String) -> Unit = { _, _ -> },
    val onRecord: (String, String) -> Unit = { _, _ -> }
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
) {

  val context = LocalContext.current
  val uiState by meetingViewModel.uiState.collectAsState()

  // Show error message if there is any
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      meetingViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(Unit) { meetingViewModel.loadMeetings(config.projectId) }

  LaunchedEffect(meetingViewModel.userId) {
    if (meetingViewModel.userId == null) {
      meetingViewModel.setErrorMsg("Not logged in")
    }
  }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              if (uiState.isConnected) {
                config.onCreateMeeting()
              } else {
                Toast.makeText(
                        context,
                        "Meeting creation is unavailable offline to prevent sync conflicts.",
                        Toast.LENGTH_SHORT)
                    .show()
              }
            },
            modifier = Modifier.testTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON),
            containerColor =
                if (uiState.isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Create meeting")
            }
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(10.dp)
                    .testTag(MeetingScreenTestTags.MEETING_SCREEN)) {
              Text(
                  modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN_TITLE),
                  text = "Meetings",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                  modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN_DESCRIPTION),
                  text = "Schedule and manage your team meetings",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.Gray)

              Spacer(Modifier.height(16.dp))

              if (!uiState.isConnected) {
                Text(
                    text =
                        "You are offline. Meeting creation is unavailable to prevent sync conflicts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier.padding(Spacing.md).testTag(MeetingScreenTestTags.OFFLINE_MESSAGE))
              }

              RoundedTabRow(
                  tabs = MeetingTab.entries.toTypedArray(),
                  selectedTab = uiState.selectedTab,
                  onTabSelected = { meetingViewModel.selectTab(it) })

              Spacer(Modifier.height(16.dp))

              when (uiState.selectedTab) {
                MeetingTab.UPCOMING ->
                    MeetingsList(
                        MeetingsListConfig(
                            modifier = Modifier.padding(padding),
                            meetings = uiState.upcomingMeetings,
                            tabName = MeetingTab.UPCOMING.name.lowercase(),
                            projectId = config.projectId,
                            isCurrentUserId = { uid -> meetingViewModel.userId == uid },
                            onMeetingClick = config.onMeetingClick,
                            onVoteForMeetingProposalClick = config.onVoteForMeetingProposalClick,
                            onNavigateToMeeting = config.onNavigateToMeeting,
                            onCloseVotes = { meeting ->
                              meetingViewModel.closeVotesForMeeting(meeting)
                            },
                            onViewTranscript = config.onViewTranscript,
                            onRecord = config.onRecord,
                            isConnected = uiState.isConnected))
                MeetingTab.PAST ->
                    MeetingsList(
                        MeetingsListConfig(
                            modifier = Modifier.padding(padding),
                            meetings = uiState.pastMeetings,
                            tabName = MeetingTab.PAST.name.lowercase(),
                            projectId = config.projectId,
                            isCurrentUserId = { uid -> meetingViewModel.userId == uid },
                            onMeetingClick = config.onMeetingClick,
                            isConnected = uiState.isConnected))
              }
            }
      })
}

/**
 * Config for component that displays the meetings.
 *
 * @property modifier Modifier used in the component.
 * @property meetings Meetings list to display.
 * @property tabName Name of the tab in which to display these meetings.
 * @property projectId The ID of the project containing the meetings.
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
    val projectId: String = "",
    val isCurrentUserId: (String) -> Boolean,
    val onMeetingClick: (String, String) -> Unit = { _, _ -> },
    val onVoteForMeetingProposalClick: (String, String) -> Unit = { _, _ -> },
    val onNavigateToMeeting: (String, String) -> Unit = { _, _ -> },
    val onCloseVotes: (Meeting) -> Unit = { _ -> },
    val onViewTranscript: (String, String) -> Unit = { _, _ -> },
    val onRecord: (String, String) -> Unit = { _, _ -> },
    val isConnected: Boolean,
)

/**
 * Component that displays the meetings.
 *
 * @param config Config of that composable.
 */
@Composable
fun MeetingsList(config: MeetingsListConfig) {
  if (config.meetings.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = config.modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          items(config.meetings.size) { index ->
            MeetingCard(
                meeting = config.meetings[index],
                config =
                    MeetingCardConfig(
                        isCurrentUserId = config.isCurrentUserId,
                        onClick = {
                          config.onMeetingClick(config.projectId, config.meetings[index].meetingID)
                        },
                        onVoteForMeetingProposals = {
                          config.onVoteForMeetingProposalClick(
                              config.projectId, config.meetings[index].meetingID)
                        },
                        onDirections = {
                          config.onNavigateToMeeting(
                              config.projectId, config.meetings[index].meetingID)
                        },
                        onCloseVotes = config.onCloseVotes,
                        onViewTranscript = config.onViewTranscript,
                        onRecord = config.onRecord,
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
 */
data class MeetingCardConfig(
    val isCurrentUserId: (String) -> Boolean,
    val onClick: () -> Unit = {},
    val onJoinMeeting: () -> Unit = {},
    val onVoteForMeetingProposals: () -> Unit = {},
    val onVoteForFormat: () -> Unit = {},
    val onDirections: () -> Unit = {},
    val onRecord: (String, String) -> Unit = { _, _ -> },
    val onViewSummary: () -> Unit = {},
    val onViewTranscript: (String, String) -> Unit = { _, _ -> },
    val onCloseVotes: (Meeting) -> Unit = { _ -> },
    val isConnected: Boolean,
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
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(5.dp)
              .wrapContentHeight()
              .clickable(onClick = config.onClick)
              .testTag(MeetingScreenTestTags.MEETING_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // title and status row
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).testTag(MeetingScreenTestTags.MEETING_TITLE))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_STATUS_TEXT),
                text = meeting.status.description,
                style = MaterialTheme.typography.bodySmall,
                color =
                    when (meeting.status) {
                      MeetingStatus.OPEN_TO_VOTES -> Color.Blue
                      MeetingStatus.SCHEDULED -> Color.Red
                      MeetingStatus.COMPLETED -> Color.DarkGray
                      MeetingStatus.IN_PROGRESS -> Color.Green
                    })
          }

          Spacer(modifier = Modifier.height(8.dp))

          // datetime row(s)
          when (meeting.status) {
            MeetingStatus.OPEN_TO_VOTES -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.HourglassTop,
                      contentDescription = "Schedule icon.",
                      modifier = Modifier.size(16.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_DURATION),
                      text = "${meeting.duration} minutes",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.HowToVote,
                      contentDescription = "Vote icon.",
                      modifier = Modifier.size(16.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      modifier =
                          Modifier.testTag(MeetingScreenTestTags.MEETING_VOTE_FOR_DATETIME_MESSAGE),
                      text = "Vote for your preferred datetime(s)",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
            MeetingStatus.SCHEDULED,
            MeetingStatus.IN_PROGRESS,
            MeetingStatus.COMPLETED -> {
              checkNotNull(meeting.datetime) {
                "Datetime should be set if meeting is not open to votes."
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule icon.",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_DATETIME),
                    text = Formatters.formatDateTime(meeting.datetime.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // format row
          Row(verticalAlignment = Alignment.CenterVertically) {
            when (meeting.status) {
              MeetingStatus.OPEN_TO_VOTES -> {
                when (meeting.format) {
                  MeetingFormat.IN_PERSON,
                  MeetingFormat.VIRTUAL ->
                      throw IllegalStateException(
                          "Format must not be set if meeting is open to votes.")
                  null -> {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = "Vote icon.",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        modifier =
                            Modifier.testTag(MeetingScreenTestTags.MEETING_VOTE_FOR_FORMAT_MESSAGE),
                        text = "Vote for your preferred meeting format",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
              MeetingStatus.SCHEDULED,
              MeetingStatus.IN_PROGRESS,
              MeetingStatus.COMPLETED -> {
                when (meeting.format) {
                  MeetingFormat.IN_PERSON -> {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Place icon.",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_LOCATION),
                        text =
                            meeting.location?.name
                                ?: throw IllegalStateException(
                                    "Location of in-person meeting closed to votes should exist."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  MeetingFormat.VIRTUAL -> {
                    if (meeting.status != MeetingStatus.COMPLETED) {
                      Icon(
                          imageVector = Icons.Default.VideoCall,
                          contentDescription = "Video call icon.",
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                          modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_LINK),
                          text =
                              meeting.link
                                  ?: throw IllegalStateException(
                                      "Link to scheduled/in progress virtual meeting should exist."),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                  null ->
                      throw IllegalStateException(
                          "Format of meeting closed to votes should be set.")
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // button row
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically) {
                when (meeting.status) {
                  MeetingStatus.OPEN_TO_VOTES -> {
                    Button(
                        onClick = config.onVoteForMeetingProposals,
                        modifier =
                            Modifier.testTag(
                                MeetingScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON),
                        enabled = config.isConnected,
                    ) {
                      Text("Vote for meeting proposals")
                    }
                    if (config.isCurrentUserId(meeting.createdBy)) {
                      Spacer(modifier = Modifier.width(10.dp))
                      Button(
                          onClick = { config.onCloseVotes(meeting) },
                          modifier = Modifier.testTag(MeetingScreenTestTags.CLOSE_VOTES_BUTTON),
                          enabled = config.isConnected) {
                            Text("Close votes")
                          }
                    }
                  }
                  MeetingStatus.SCHEDULED,
                  MeetingStatus.IN_PROGRESS -> {
                    when (meeting.format) {
                      MeetingFormat.IN_PERSON -> {
                        Button(
                            onClick = config.onDirections,
                            modifier = Modifier.testTag(MeetingScreenTestTags.DIRECTIONS_BUTTON),
                            enabled = config.isConnected,
                        ) {
                          Text("Directions")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { config.onRecord(meeting.projectId, meeting.meetingID) },
                            modifier = Modifier.testTag(MeetingScreenTestTags.RECORD_BUTTON),
                            enabled = config.isConnected,
                        ) {
                          Text("Record")
                        }
                      }
                      MeetingFormat.VIRTUAL -> {
                        Button(
                            onClick = config.onJoinMeeting,
                            modifier = Modifier.testTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON),
                            enabled = config.isConnected,
                        ) {
                          Text("Join meeting")
                        }
                      }
                      null ->
                          throw IllegalStateException(
                              "Format of scheduled/in progress should exist.")
                    }
                  }
                  MeetingStatus.COMPLETED -> {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { config.onViewTranscript(meeting.projectId, meeting.meetingID) },
                        modifier = Modifier.testTag(MeetingScreenTestTags.VIEW_TRANSCRIPT_BUTTON),
                    ) {
                      Text("Transcript")
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

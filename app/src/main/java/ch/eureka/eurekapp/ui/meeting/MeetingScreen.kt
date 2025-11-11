/* Portions of this code were written with the help of chatGPT.*/

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
  const val VOTE_FOR_DATETIME_BUTTON = "VoteForDateTimeButton"
  const val VOTE_FOR_FORMAT_BUTTON = "VoteForFormatButton"

  const val VIEW_SUMMARY_BUTTON = "SeeSummaryButton"
  const val VIEW_TRANSCRIPT_BUTTON = "SeeTranscriptButton"
  const val NO_UPCOMING_MEETINGS_MESSAGE = "NoUpcomingMeetingsMessageTest"
  const val NO_PAST_MEETINGS_MESSAGE = "NoPastMeetingsMessage"
  const val CREATE_MEETING_BUTTON = "CreateMeetingButton"
}

/**
 * Main composable to draw the meetings screen.
 *
 * @param meetingViewModel The view model associated to the meetings screen.
 * @param projectId The ID of the project to display the meetings from.
 * @param onMeetingClick Callback when a meeting card is clicked, receives projectId and meetingId.
 * @param onVoteForMeetingProposalClick Callback when the "Vote for meeting proposals" button is
 *   clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingScreen(
    projectId: String,
    onCreateMeeting: () -> Unit,
    meetingViewModel: MeetingViewModel = viewModel(),
    onMeetingClick: (String, String) -> Unit = { _, _ -> },
    onVoteForMeetingProposalClick: (String, String) -> Unit = { _, _ -> }
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

  LaunchedEffect(Unit) { meetingViewModel.loadMeetings(projectId) }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { onCreateMeeting() },
            modifier = Modifier.testTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON)) {
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

              RoundedTabRow(
                  tabs = MeetingTab.entries.toTypedArray(),
                  selectedTab = uiState.selectedTab,
                  onTabSelected = { meetingViewModel.selectTab(it) })

              Spacer(Modifier.height(16.dp))

              when (uiState.selectedTab) {
                MeetingTab.UPCOMING ->
                    MeetingsList(
                        modifier = Modifier.padding(padding),
                        meetings = uiState.upcomingMeetings,
                        tabName = MeetingTab.UPCOMING.name.lowercase(),
                        projectId = projectId,
                        onMeetingClick = onMeetingClick,
                        onVoteForMeetingProposalClick = onVoteForMeetingProposalClick)
                MeetingTab.PAST ->
                    MeetingsList(
                        modifier = Modifier.padding(padding),
                        meetings = uiState.pastMeetings,
                        tabName = MeetingTab.PAST.name.lowercase(),
                        projectId = projectId,
                        onMeetingClick = onMeetingClick,
                        onVoteForMeetingProposalClick = onVoteForMeetingProposalClick)
              }
            }
      })
}

/**
 * Component that displays the meetings.
 *
 * @param modifier Modifier used in the component.
 * @param meetings Meetings list to display.
 * @param tabName Name of the tab in which to display these meetings.
 * @param projectId The ID of the project containing the meetings.
 * @param onMeetingClick Callback when a meeting card is clicked.
 * @param onVoteForMeetingProposalClick Callback when the "Vote for meeting proposals" button is
 *   clicked.
 */
@Composable
fun MeetingsList(
    modifier: Modifier,
    meetings: List<Meeting>,
    tabName: String,
    projectId: String = "",
    onMeetingClick: (String, String) -> Unit = { _, _ -> },
    onVoteForMeetingProposalClick: (String, String) -> Unit = { _, _ -> }
) {
  if (meetings.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          items(meetings.size) { index ->
            MeetingCard(
                meeting = meetings[index],
                config =
                    MeetingCardConfig(
                        onClick = { onMeetingClick(projectId, meetings[index].meetingID) },
                        onVoteForMeetingProposals = {
                          onVoteForMeetingProposalClick(projectId, meetings[index].meetingID)
                        }),
            )
          }
        }
  } else {
    Text(
        modifier =
            modifier.testTag(
                if (tabName == MeetingTab.UPCOMING.name.lowercase())
                    MeetingScreenTestTags.NO_UPCOMING_MEETINGS_MESSAGE
                else MeetingScreenTestTags.NO_PAST_MEETINGS_MESSAGE),
        text = "You have no $tabName meetings yet.")
  }
}

/**
 * Data class representing all the actions that can be executed by buttons on a meeting card.
 *
 * @param onClick Function to execute when the card is clicked (for navigation to detail screen).
 * @param onJoinMeeting Function to execute when user clicks on button to join meeting.
 * @param onVoteForMeetingProposals Function to execute when user clicks on button to vote for
 *   meeting proposals.
 * @param onVoteForFormat Function to execute when user clicks on button to vote for meeting format
 * @param onDirections Function to execute when user clicks on button to navigate to a meeting.
 * @param onRecord Function to execute when user clicks on record button.
 * @param onViewSummary Function to execute whe user clicks on view summary button.
 * @param onViewTranscript Function to execute whe user clicks on view transcript button.
 */
data class MeetingCardConfig(
    val onClick: () -> Unit = {},
    val onJoinMeeting: () -> Unit = {},
    val onVoteForMeetingProposals: () -> Unit = {},
    val onVoteForFormat: () -> Unit = {},
    val onDirections: () -> Unit = {},
    val onRecord: () -> Unit = {},
    val onViewSummary: () -> Unit = {},
    val onViewTranscript: () -> Unit = {},
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
              if (meeting.datetime == null) {
                throw IllegalStateException(
                    "Datetime should be set if meeting is not open to votes.")
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
                    Button( // TODO : fix this
                        onClick = config.onVoteForMeetingProposals,
                        modifier = Modifier.testTag(MeetingScreenTestTags.VOTE_FOR_DATETIME_BUTTON),
                    ) {
                      Text("Vote for datetime")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = config.onVoteForFormat,
                        modifier = Modifier.testTag(MeetingScreenTestTags.VOTE_FOR_FORMAT_BUTTON),
                    ) {
                      Text("Vote for format")
                    }
                  }
                  MeetingStatus.SCHEDULED,
                  MeetingStatus.IN_PROGRESS -> {
                    when (meeting.format) {
                      MeetingFormat.IN_PERSON -> {
                        Button(
                            onClick = config.onDirections,
                            modifier = Modifier.testTag(MeetingScreenTestTags.DIRECTIONS_BUTTON),
                        ) {
                          Text("Directions")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = config.onRecord,
                            modifier = Modifier.testTag(MeetingScreenTestTags.RECORD_BUTTON),
                        ) {
                          Text("Record")
                        }
                      }
                      MeetingFormat.VIRTUAL -> {
                        Button(
                            onClick = config.onJoinMeeting,
                            modifier = Modifier.testTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON),
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
                    Button(
                        onClick = config.onViewSummary,
                        modifier = Modifier.testTag(MeetingScreenTestTags.VIEW_SUMMARY_BUTTON),
                    ) {
                      Text("View summary")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = config.onViewTranscript,
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

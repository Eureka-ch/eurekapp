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
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ch.eureka.eurekapp.model.data.meeting.formatTimeSlot
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
  const val MEETING_TIMESLOT = "MeetingTimeSlot"
  const val MEETING_VOTE_FOR_DATETIME_MESSAGE = "MeetingVoteForDateTimeMessage"
  const val MEETING_VOTE_FOR_FORMAT_MESSAGE = "MeetingVoteForFormatMessage"
  const val MEETING_LINK = "MeetingLink"
  const val MEETING_LOCATION = "MeetingLocation"
  const val JOIN_MEETING_BUTTON = "JoinMeetingButton"
  const val DIRECTIONS_BUTTON = "DirectionsButton"
  const val RECORD_BUTTON = "RecordButton"
  const val VOTE_FOR_DATETIME_BUTTON = "VoteForDateTimeButton"
  const val VOTE_FOR_FORMAT_BUTTON = "VoteForFormatButton"
  const val NO_UPCOMING_MEETINGS_MESSAGE = "NoUpcomingMeetingsMessageTest"
  const val NO_PAST_MEETINGS_MESSAGE = "NoPastMeetingsMessage"
}

/**
 * Main composable to draw the meetings screen.
 *
 * @param meetingViewModel The view model associated to the meetings screen.
 * @param projectId The ID of the project to display the meetings from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingScreen(
    projectId: String,
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

  LaunchedEffect(Unit) { meetingViewModel.loadMeetings(projectId) }

  Scaffold(
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
                  tabs = MeetingTab.values(),
                  selectedTab = uiState.selectedTab,
                  onTabSelected = { meetingViewModel.selectTab(it) })

              Spacer(Modifier.height(16.dp))

              when (uiState.selectedTab) {
                MeetingTab.UPCOMING ->
                    MeetingsList(
                        modifier = Modifier.padding(padding),
                        meetings = uiState.upcomingMeetings,
                        tabName = MeetingTab.UPCOMING.name.lowercase())
                MeetingTab.PAST ->
                    MeetingsList(
                        modifier = Modifier.padding(padding),
                        meetings = uiState.pastMeetings,
                        tabName = MeetingTab.PAST.name.lowercase())
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
 */
@Composable
fun MeetingsList(
    modifier: Modifier,
    meetings: List<Meeting>,
    tabName: String,
) {
  if (meetings.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          items(meetings.size) { index -> MeetingCard(meeting = meetings[index]) }
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
 * Component that displays the information of a meeting.
 *
 * @param meeting The meeting to display.
 * @param onJoinMeeting Function to execute when user clicks on button to join meeting.
 * @param onVoteForDateTime Function to execute when user clicks on button to vote for datetime.
 * @param onVoteForFormat Function to execute when user clicks on button to vote for meeting format
 * @param onDirections Function to execute when user clicks on button to navigate to a meeting.
 * @param onRecord Function to execute when user clicks on record button.
 */
@Composable
fun MeetingCard(
    meeting: Meeting,
    onJoinMeeting: () -> Unit = {},
    onVoteForDateTime: () -> Unit = {},
    onVoteForFormat: () -> Unit = {},
    onDirections: () -> Unit = {},
    onRecord: () -> Unit = {},
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(5.dp)
              .wrapContentHeight()
              .testTag(MeetingScreenTestTags.MEETING_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).testTag(MeetingScreenTestTags.MEETING_TITLE))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_STATUS_TEXT),
                text = if (meeting.canVote) "Voting in progress" else "Scheduled",
                style = MaterialTheme.typography.bodySmall,
                color = if (meeting.canVote) Color.Blue else Color.Red)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            if (meeting.datetime == null) {
              meeting.timeSlot?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.Schedule,
                      contentDescription = "Schedule icon.",
                      modifier = Modifier.size(16.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_TIMESLOT),
                      text = meeting.timeSlot.formatTimeSlot(),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
            } else {
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            when (meeting.format) {
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
                                "Location of meeting does not exist."), // TODO : change this later
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              MeetingFormat.VIRTUAL -> {
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
                            ?: throw IllegalStateException("Link to meeting does not exist."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          if (meeting.canVote) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Button(
                  onClick = onVoteForDateTime,
                  modifier = Modifier.testTag(MeetingScreenTestTags.VOTE_FOR_DATETIME_BUTTON),
              ) {
                Text("Vote for datetime")
              }
              Spacer(modifier = Modifier.width(10.dp))
              Button(
                  onClick = onVoteForFormat,
                  modifier = Modifier.testTag(MeetingScreenTestTags.VOTE_FOR_FORMAT_BUTTON),
              ) {
                Text("Vote for format")
              }
            }
          } else {
            when (meeting.format) {
              MeetingFormat.IN_PERSON -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Button(
                      onClick = onDirections,
                      modifier = Modifier.testTag(MeetingScreenTestTags.DIRECTIONS_BUTTON),
                  ) {
                    Text("Directions")
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Button(
                      onClick = onRecord,
                      modifier = Modifier.testTag(MeetingScreenTestTags.RECORD_BUTTON),
                  ) {
                    Text("Record")
                  }
                }
              }
              MeetingFormat.VIRTUAL -> {
                Button(
                    onClick = onJoinMeeting,
                    modifier = Modifier.testTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON),
                ) {
                  Text("Join meeting")
                }
              }
              null -> {}
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

/* Portions of this code were generated with the help of Grok, Gemini and Claude. */
package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for MeetingScreen behavior when offline. */
@RunWith(AndroidJUnit4::class)
class MeetingScreenOfflineTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)

  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private lateinit var viewModel: MeetingViewModel
  private lateinit var mockRepository: MeetingRepositoryMock
  private val testProjectId = "testProject123"
  private val testUserId = "testUser123"

  @Before
  fun setUp() {
    mockConnectivityObserver =
        MockConnectivityObserver(InstrumentationRegistry.getInstrumentation().targetContext)
    mockRepository = MeetingRepositoryMock()
    viewModel =
        MeetingViewModel(
            repository = mockRepository, // Fixed parameter name
            getCurrentUserId = { testUserId },
            connectivityObserver = mockConnectivityObserver)
  }

  // No tearDown needed as we aren't using the Emulator

  @Test
  fun meetingScreenOffline_displaysMessage() {
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }, onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingScreenOffline_disablesCreateButton() {
    mockConnectivityObserver.setConnected(false)

    var createMeetingClicked = false

    composeTestRule.setContent {
      MeetingScreen(
          config =
              MeetingScreenConfig(
                  onCreateMeeting = { isConnected ->
                    if (isConnected) {
                      createMeetingClicked = true
                    }
                  },
                  onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Try to click create meeting button
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Verify callback was not triggered
    assert(!createMeetingClicked) { "Create meeting should not be triggered when offline" }
  }

  @Test
  fun meetingScreenOffline_viewsExistingMeetings() {
    val meeting =
        Meeting(
            meetingID = "meeting1",
            projectId = testProjectId,
            title = "Offline Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    mockRepository.setMeetings(listOf(meeting))
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }, onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify meeting is displayed
    composeTestRule.onNodeWithText("Offline Meeting").assertIsDisplayed()
  }

  @Test
  fun meetingScreenOffline_goesOfflineUpdatesUI() {
    mockConnectivityObserver.setConnected(true)

    composeTestRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }, onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify no offline message initially
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()

    // Simulate going offline
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Verify offline message appears
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingScreenOffline_comesBackOnlineUpdatesUI() {
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }, onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify offline message is displayed
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()

    // Simulate coming back online
    mockConnectivityObserver.setConnected(true)

    composeTestRule.waitForIdle()

    // Verify offline message disappears
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()
  }

  @Test
  fun meetingScreenOffline_disablesVoteButtons() {
    val meeting =
        Meeting(
            meetingID = "meeting1",
            projectId = testProjectId,
            title = "Open Vote Meeting",
            status = MeetingStatus.OPEN_TO_VOTES,
            duration = 60,
            createdBy = testUserId)

    mockRepository.setMeetings(listOf(meeting))
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }, onMeetingClick = { _, _ -> }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify meeting is displayed
    composeTestRule.onNodeWithText("Open Vote Meeting").assertIsDisplayed()

    // Verify vote button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingScreenOffline_allowsNavigationToDetail() {
    val meeting =
        Meeting(
            meetingID = "meeting1",
            projectId = testProjectId,
            title = "Navigable Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.IN_PERSON,
            location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0),
            datetime = Timestamp.now(),
            createdBy = "user1")

    mockRepository.setMeetings(listOf(meeting))
    mockConnectivityObserver.setConnected(false)

    var navigatedMeetingId: String? = null
    var navigatedProjectId: String? = null

    composeTestRule.setContent {
      MeetingScreen(
          config =
              MeetingScreenConfig(
                  onCreateMeeting = { _ -> },
                  onMeetingClick = { projectId, meetingId ->
                    navigatedProjectId = projectId
                    navigatedMeetingId = meetingId
                  }),
          meetingViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Click on the meeting card
    composeTestRule.onNodeWithText("Navigable Meeting").performClick()

    composeTestRule.waitForIdle()

    // Assert that navigation callback was triggered with correct IDs
    assert(navigatedMeetingId == "meeting1") { "Meeting ID should be captured on click" }
    assert(navigatedProjectId == testProjectId) { "Project ID should be captured on click" }
  }
}

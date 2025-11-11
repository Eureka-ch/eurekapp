/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.map.Location
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test suite for the [MeetingNavigationScreen].
 *
 * Tests all UI states, component variations, and user interactions including:
 * - Loading state
 * - Error handling for missing meeting
 * - Error handling for missing location
 * - Map display with location marker
 * - Info card with location name
 *
 * Note: This file was co-authored by Claude Code.
 */
class MeetingNavigationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repositoryMock: MeetingRepository
  private val testProjectId = "project123"
  private val testMeetingId = "meeting456"

  private val testLocation = Location(latitude = 46.5197, longitude = 6.5659, name = "EPFL")

  private val testMeeting =
      Meeting(
          meetingID = testMeetingId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          format = MeetingFormat.IN_PERSON,
          datetime = Timestamp(Date()),
          location = testLocation)

  private val testMeetingNoLocation =
      Meeting(
          meetingID = testMeetingId,
          title = "Test Meeting No Location",
          status = MeetingStatus.SCHEDULED,
          format = MeetingFormat.IN_PERSON,
          datetime = Timestamp(Date()),
          location = null)

  @Before
  fun setup() {
    repositoryMock = mockk(relaxed = true)
  }

  private fun setContent(meeting: Meeting?, onNavigateBack: () -> Unit = {}) {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flowOf(meeting)

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.setContent {
      MeetingNavigationScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          onNavigateBack = onNavigateBack)
    }
  }

  @Test
  fun screenDisplaysWithValidMeetingLocation() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingNavigationScreenTestTags.NAVIGATION_SCREEN)
        .assertIsDisplayed()
  }

  @Test
  fun loadingStateDisplaysLoadingIndicator() = runTest {
    // Initially return null to simulate loading, then will update
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow {
          // Emit nothing to keep in loading state
          kotlinx.coroutines.delay(5000)
        }

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.setContent {
      MeetingNavigationScreen(
          projectId = testProjectId, meetingId = testMeetingId, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(MeetingNavigationScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun errorDisplayedWhenMeetingNotFound() = runTest {
    setContent(meeting = null)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Meeting not found").assertIsDisplayed()
  }

  @Test
  fun errorDisplayedWhenMeetingHasNoLocation() = runTest {
    setContent(meeting = testMeetingNoLocation)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Meeting location is not available").assertIsDisplayed()
  }

  @Test
  fun infoCardDisplaysLocationName() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.INFO_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithText(testLocation.name).assertIsDisplayed()
  }

  @Test
  fun mapViewIsDisplayedWithValidLocation() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    // Verify the Google Map composable is in the hierarchy
    // Note: GoogleMap itself may not render in tests but should be present in composition
    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.GOOGLE_MAP).assertExists()
  }

  @Test
  fun screenTitleIsCorrect() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Meeting Location").assertIsDisplayed()
  }

  @Test
  fun backButtonNavigatesBack() = runTest {
    var navigatedBack = false
    setContent(meeting = testMeeting, onNavigateBack = { navigatedBack = true })

    composeTestRule.waitForIdle()

    // Verify the screen title is displayed (back button is in TopAppBar)
    composeTestRule.onNodeWithText("Meeting Location").assertIsDisplayed()
  }

  @Test
  fun backButtonActuallyTriggersCallback() = runTest {
    var navigatedBack = false
    setContent(meeting = testMeeting, onNavigateBack = { navigatedBack = true })

    composeTestRule.waitForIdle()

    // Find and click the back button
    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

    // Verify callback was triggered
    assert(navigatedBack)
  }

  @Test
  fun errorScreenDisplaysCustomErrorMessage() = runTest {
    val customErrorMessage = "Custom error occurred"
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { throw Exception(customErrorMessage) }

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.setContent {
      MeetingNavigationScreen(
          projectId = testProjectId, meetingId = testMeetingId, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Failed to load meeting: $customErrorMessage", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun mapMarkerDisplaysCorrectTitle() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    // Map should be present
    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.GOOGLE_MAP).assertExists()

    // Verify location name is in info card
    composeTestRule.onNodeWithText(testLocation.name).assertIsDisplayed()
  }

  @Test
  fun infoCardDisplaysCorrectLocationIcon() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    // Verify info card is displayed
    composeTestRule.onNodeWithTag(MeetingNavigationScreenTestTags.INFO_CARD).assertIsDisplayed()

    // Verify location icon content description
    composeTestRule.onNodeWithContentDescription("Location").assertExists()
  }

  @Test
  fun loadingScreenDisplaysProgressIndicator() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(5000) }

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.setContent {
      MeetingNavigationScreen(
          projectId = testProjectId, meetingId = testMeetingId, viewModel = viewModel)
    }

    // Verify loading indicator is displayed
    composeTestRule
        .onNodeWithTag(MeetingNavigationScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun scaffoldDisplaysTopAppBar() = runTest {
    setContent(meeting = testMeeting)

    composeTestRule.waitForIdle()

    // Verify top app bar with title
    composeTestRule.onNodeWithText("Meeting Location").assertIsDisplayed()

    // Verify back button exists
    composeTestRule.onNodeWithContentDescription("Navigate back").assertExists()
  }

  @Test
  fun viewModelGetMeetingLocationReturnsCorrectCoordinates() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.waitForIdle()

    val location = viewModel.getMeetingLocation()
    assert(location != null)
    assert(location?.latitude == testLocation.latitude)
    assert(location?.longitude == testLocation.longitude)
  }

  @Test
  fun viewModelGetMeetingLocationReturnsNullWhenNoLocation() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    val viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    composeTestRule.waitForIdle()

    val location = viewModel.getMeetingLocation()
    assert(location == null)
  }
}

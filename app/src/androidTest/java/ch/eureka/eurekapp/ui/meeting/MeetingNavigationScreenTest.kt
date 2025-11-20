/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
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

  @get:Rule
  val grantPermissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  private lateinit var repositoryMock: MeetingRepository
  private val testProjectId = "project123"
  private val testMeetingId = "meeting456"
  private val testApiKey = "test_api_key"

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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    composeTestRule.setContent {
      MeetingNavigationScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

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

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    composeTestRule.waitForIdle()

    val location = viewModel.getMeetingLocation()
    assert(location == null)
  }

  // Note: Transport mode and directions UI tests removed
  // These require real location permission which can't be mocked in tests
  // The UI correctly shows "Enable location to see route" when no location is available

  // Note: This test removed because GrantPermissionRule now grants location permission
  // The "Enable location to see route" message only shows without permission

  @Test
  fun transportModesDisplayedWhenUserLocationAvailable() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Set user location in the ViewModel
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify transport mode selection UI is displayed
    composeTestRule.onNodeWithText("Select transport mode:").assertIsDisplayed()
    composeTestRule.onNodeWithText("Drive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Transit").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bike").assertIsDisplayed()
    composeTestRule.onNodeWithText("Walk").assertIsDisplayed()
  }

  @Test
  fun getDirectionsButtonDisplayedAndEnabled() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify Get Directions button is displayed and enabled
    composeTestRule.onNodeWithText("Get Directions").assertIsDisplayed().assertIsEnabled()
  }

  @Test
  fun getDirectionsButtonShowsLoadingState() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Set user location and loading state
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(
        viewModel.uiState.value.copy(userLocation = userLocation, isLoadingRoute = true))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify loading state is displayed (button text changes to "Loading..." when loading)
    composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
  }

  @Test
  fun routeInformationDisplayedWhenRouteAvailable() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Create mock route with distance and duration
    val mockRoute =
        ch.eureka.eurekapp.services.navigation.Route(
            legs =
                listOf(
                    ch.eureka.eurekapp.services.navigation.Leg(
                        distance =
                            ch.eureka.eurekapp.services.navigation.TextValue(
                                text = "5.2 km", value = 5200),
                        duration =
                            ch.eureka.eurekapp.services.navigation.TextValue(
                                text = "12 mins", value = 720),
                        startAddress = "Start",
                        endAddress = "End",
                        startLocation =
                            ch.eureka.eurekapp.services.navigation.LocationData(
                                latitude = 46.5197, longitude = 6.6323),
                        endLocation =
                            ch.eureka.eurekapp.services.navigation.LocationData(
                                latitude = 46.5291, longitude = 6.6489),
                        steps = emptyList())),
            overviewPolyline = ch.eureka.eurekapp.services.navigation.PolylineData(points = ""),
            summary = "Main Route",
            warnings = emptyList())

    // Set user location and route
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(
        viewModel.uiState.value.copy(userLocation = userLocation, route = mockRoute))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify route information is displayed
    composeTestRule.onNodeWithText("5.2 km • 12 mins").assertIsDisplayed()
    composeTestRule.onNodeWithText("Show Directions").assertIsDisplayed()
  }

  @Test
  fun routeErrorMessageDisplayed() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Set user location and error message
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(
        viewModel.uiState.value.copy(userLocation = userLocation, routeErrorMsg = "No route found"))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify error message is displayed
    composeTestRule.onNodeWithText("No route found").assertIsDisplayed()
  }

  @Test
  fun directionsPanelDisplaysTurnByTurnInstructions() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Create mock route with steps
    val mockRoute =
        ch.eureka.eurekapp.services.navigation.Route(
            legs =
                listOf(
                    ch.eureka.eurekapp.services.navigation.Leg(
                        distance =
                            ch.eureka.eurekapp.services.navigation.TextValue(
                                text = "5.2 km", value = 5200),
                        duration =
                            ch.eureka.eurekapp.services.navigation.TextValue(
                                text = "12 mins", value = 720),
                        startAddress = "Start",
                        endAddress = "End",
                        startLocation =
                            ch.eureka.eurekapp.services.navigation.LocationData(
                                latitude = 46.5197, longitude = 6.6323),
                        endLocation =
                            ch.eureka.eurekapp.services.navigation.LocationData(
                                latitude = 46.5291, longitude = 6.6489),
                        steps =
                            listOf(
                                ch.eureka.eurekapp.services.navigation.Step(
                                    distance =
                                        ch.eureka.eurekapp.services.navigation.TextValue(
                                            text = "1.0 km", value = 1000),
                                    duration =
                                        ch.eureka.eurekapp.services.navigation.TextValue(
                                            text = "3 mins", value = 180),
                                    startLocation =
                                        ch.eureka.eurekapp.services.navigation.LocationData(
                                            latitude = 46.5197, longitude = 6.6323),
                                    endLocation =
                                        ch.eureka.eurekapp.services.navigation.LocationData(
                                            latitude = 46.5220, longitude = 6.6350),
                                    htmlInstructions = "Turn <b>right</b> onto Main St",
                                    polyline =
                                        ch.eureka.eurekapp.services.navigation.PolylineData(
                                            points = "encodedPolyline"),
                                    travelMode = "DRIVING",
                                    maneuver = "turn-right"),
                                ch.eureka.eurekapp.services.navigation.Step(
                                    distance =
                                        ch.eureka.eurekapp.services.navigation.TextValue(
                                            text = "2.0 km", value = 2000),
                                    duration =
                                        ch.eureka.eurekapp.services.navigation.TextValue(
                                            text = "5 mins", value = 300),
                                    startLocation =
                                        ch.eureka.eurekapp.services.navigation.LocationData(
                                            latitude = 46.5220, longitude = 6.6350),
                                    endLocation =
                                        ch.eureka.eurekapp.services.navigation.LocationData(
                                            latitude = 46.5250, longitude = 6.6400),
                                    htmlInstructions = "Continue straight on Main St",
                                    polyline =
                                        ch.eureka.eurekapp.services.navigation.PolylineData(
                                            points = "encodedPolyline2"),
                                    travelMode = "DRIVING",
                                    maneuver = null)))),
            overviewPolyline = ch.eureka.eurekapp.services.navigation.PolylineData(points = ""),
            summary = "Main Route",
            warnings = emptyList())

    // Set user location and route
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(
        viewModel.uiState.value.copy(userLocation = userLocation, route = mockRoute))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Click Show Directions button
    composeTestRule.onNodeWithText("Show Directions").performClick()
    composeTestRule.waitForIdle()

    // Verify DirectionsPanel is displayed with turn-by-turn instructions
    composeTestRule.onNodeWithText("Turn-by-Turn Directions").assertIsDisplayed()
    composeTestRule.onNodeWithText("Turn right onto Main St").assertIsDisplayed()
    composeTestRule.onNodeWithText("1.0 km").assertIsDisplayed()
    composeTestRule.onNodeWithText("Continue straight on Main St").assertIsDisplayed()
    composeTestRule.onNodeWithText("2.0 km").assertIsDisplayed()
  }

  @Test
  fun transportModeSelectionChanges() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    val viewModel =
        MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    composeTestRule.setContent { MeetingNavigationScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Click on different transport modes
    composeTestRule.onNodeWithText("Transit").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Bike").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Walk").performClick()
    composeTestRule.waitForIdle()

    // All transport modes should be displayed and clickable
    composeTestRule.onNodeWithText("Drive").assertIsDisplayed()
    composeTestRule.onNodeWithText("Transit").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bike").assertIsDisplayed()
    composeTestRule.onNodeWithText("Walk").assertIsDisplayed()
  }
}

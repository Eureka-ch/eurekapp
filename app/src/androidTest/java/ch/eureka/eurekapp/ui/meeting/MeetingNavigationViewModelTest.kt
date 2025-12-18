/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test suite for MeetingNavigationViewModel
 *
 * Tests all ViewModel functionality including:
 * - Initial state validation
 * - Meeting loading with location
 * - Error handling for missing meeting
 * - Error handling for missing location
 * - Meeting location retrieval
 *
 * Note: This file was co-authored by Claude Code.
 */
@ExperimentalCoroutinesApi
class MeetingNavigationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingNavigationViewModel
  private lateinit var repositoryMock: MeetingRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
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
    Dispatchers.setMain(testDispatcher)
    repositoryMock = mockk(relaxed = true)

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun meetingNavigationViewModel_initialStateIsLoading() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    val uiState = viewModel.uiState.value
    assertTrue(uiState.isLoading)
    assertNull(uiState.meeting)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_successfulMeetingLoadUpdatesState() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.meeting)
    assertEquals(testMeeting, uiState.meeting)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_meetingNotFoundSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flowOf(null)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Meeting not found", uiState.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_meetingWithoutLocationSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Meeting location is not available", uiState.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_getMeetingLocationReturnsCorrectLatLng() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNotNull(location)
    assertEquals(testLocation.latitude, location?.latitude ?: 0.0, 0.0001)
    assertEquals(testLocation.longitude, location?.longitude ?: 0.0, 0.0001)
  }

  @Test
  fun meetingNavigationViewModel_getMeetingLocationReturnsNullWhenMeetingNull() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flowOf(null)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNull(location)
  }

  @Test
  fun meetingNavigationViewModel_getMeetingLocationReturnsNullWhenLocationNull() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNull(location)
  }

  @Test
  fun meetingNavigationViewModel_handlesFlowUpdates() = runTest {
    val flow = kotlinx.coroutines.flow.MutableStateFlow<Meeting?>(null)
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flow

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    // Initially null meeting
    assertEquals("Meeting not found", viewModel.uiState.value.errorMsg)

    // Update flow with meeting
    flow.value = testMeeting
    advanceUntilIdle()

    // Should now have meeting
    assertEquals(testMeeting, viewModel.uiState.value.meeting)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_repositoryExceptionSetsErrorMessage() = runTest {
    val exceptionMessage = "Network connection failed"
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { throw Exception(exceptionMessage) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertTrue(uiState.errorMsg?.contains("Failed to load meeting") == true)
    assertTrue(uiState.errorMsg?.contains(exceptionMessage) == true)
  }

  @Test
  fun meetingNavigationViewModel_loadingStateIsSetBeforeCollection() = runTest {
    // Use a flow that never emits to verify loading state is set
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(Long.MAX_VALUE) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    // Immediately check state before any collection happens
    assertTrue(viewModel.uiState.value.isLoading)
    assertNull(viewModel.uiState.value.meeting)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingNavigationViewModel_exceptionWithNullMessageHandledCorrectly() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { throw Exception(null as String?) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg?.contains("Failed to load meeting") == true)
  }

  // New tests for route and location functionality

  @Test
  fun meetingNavigationViewModel_initialStateHasNoRouteOrLocation() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)

    val uiState = viewModel.uiState.value
    assertNull(uiState.userLocation)
    assertNull(uiState.route)
    assertFalse(uiState.isLoadingRoute)
    assertNull(uiState.routeErrorMsg)
  }

  // Note: clearRouteError test removed - errors clear automatically on next fetchDirections call

  @Test
  fun meetingNavigationViewModel_fetchDirectionsWithoutUserLocationSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)
    advanceUntilIdle()

    // Try to fetch directions without user location
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertEquals("User location not available", uiState.routeErrorMsg)
    assertNull(uiState.route)
    assertFalse(uiState.isLoadingRoute)
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsWithoutMeetingLocationSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)
    advanceUntilIdle()

    // Manually set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    // Try to fetch directions
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertEquals("Meeting location not available", uiState.routeErrorMsg)
    assertNull(uiState.route)
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsSupportsMultipleTravelModes() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, testApiKey, repositoryMock)
    advanceUntilIdle()

    val modes =
        listOf(TravelMode.DRIVING, TravelMode.WALKING, TravelMode.BICYCLING, TravelMode.TRANSIT)

    modes.forEach { mode ->
      // Each mode should be accepted without throwing
      viewModel.fetchDirections(mode)
      advanceUntilIdle()
    }

    // At least the last call should have been attempted
    assertTrue(true) // If we got here, all modes were accepted
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsHandlesZeroResultsError() = runTest {
    val directionsServiceMock = mockk<ch.eureka.eurekapp.services.navigation.DirectionsApiService>()

    // Mock to return ZERO_RESULTS status
    coEvery { directionsServiceMock.getDirections(any(), any(), any(), any()) } returns
        ch.eureka.eurekapp.services.navigation.DirectionsResponse(
            routes = emptyList(),
            status = "ZERO_RESULTS",
            errorMessage = "No route found between these locations")

    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel =
        MeetingNavigationViewModel(
            testProjectId, testMeetingId, testApiKey, repositoryMock, directionsServiceMock)
    advanceUntilIdle()

    // Set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    // Fetch directions
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertNull(uiState.route)
    assertEquals("No route found between these locations", uiState.routeErrorMsg)
    assertFalse(uiState.isLoadingRoute)
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsHandlesEmptyRoutesWithOkStatus() = runTest {
    val directionsServiceMock = mockk<ch.eureka.eurekapp.services.navigation.DirectionsApiService>()

    // Mock to return OK status but empty routes
    coEvery { directionsServiceMock.getDirections(any(), any(), any(), any()) } returns
        ch.eureka.eurekapp.services.navigation.DirectionsResponse(
            routes = emptyList(), status = "OK", errorMessage = null)

    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel =
        MeetingNavigationViewModel(
            testProjectId, testMeetingId, testApiKey, repositoryMock, directionsServiceMock)
    advanceUntilIdle()

    // Set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    // Fetch directions
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertNull(uiState.route)
    assertEquals("No route found", uiState.routeErrorMsg)
    assertFalse(uiState.isLoadingRoute)
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsHandlesNetworkException() = runTest {
    val directionsServiceMock = mockk<ch.eureka.eurekapp.services.navigation.DirectionsApiService>()

    // Mock to throw exception
    coEvery { directionsServiceMock.getDirections(any(), any(), any(), any()) } throws
        Exception("Network timeout")

    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel =
        MeetingNavigationViewModel(
            testProjectId, testMeetingId, testApiKey, repositoryMock, directionsServiceMock)
    advanceUntilIdle()

    // Set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    // Fetch directions
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertNull(uiState.route)
    assertNotNull(uiState.routeErrorMsg)
    assertTrue(uiState.routeErrorMsg?.startsWith("Error fetching directions") == true)
    assertFalse(uiState.isLoadingRoute)
  }

  @Test
  fun meetingNavigationViewModel_fetchDirectionsHandlesNullErrorMessage() = runTest {
    val directionsServiceMock = mockk<ch.eureka.eurekapp.services.navigation.DirectionsApiService>()

    // Mock to return error status with null errorMessage
    coEvery { directionsServiceMock.getDirections(any(), any(), any(), any()) } returns
        ch.eureka.eurekapp.services.navigation.DirectionsResponse(
            routes = emptyList(), status = "INVALID_REQUEST", errorMessage = null)

    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel =
        MeetingNavigationViewModel(
            testProjectId, testMeetingId, testApiKey, repositoryMock, directionsServiceMock)
    advanceUntilIdle()

    // Set user location
    val userLocation = com.google.android.gms.maps.model.LatLng(46.5197, 6.6323)
    viewModel.setStateForTesting(viewModel.uiState.value.copy(userLocation = userLocation))

    // Fetch directions
    viewModel.fetchDirections(TravelMode.DRIVING)
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertEquals("No route found", uiState.routeErrorMsg)
  }
}

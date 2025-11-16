/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.map.Location
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.Timestamp
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
    Dispatchers.setMain(testDispatcher)
    repositoryMock = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsLoading() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    val uiState = viewModel.uiState.value
    assertTrue(uiState.isLoading)
    assertNull(uiState.meeting)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun successfulMeetingLoadUpdatesState() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.meeting)
    assertEquals(testMeeting, uiState.meeting)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingNotFoundSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flowOf(null)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Meeting not found", uiState.errorMsg)
  }

  @Test
  fun meetingWithoutLocationSetsError() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Meeting location is not available", uiState.errorMsg)
  }

  @Test
  fun getMeetingLocationReturnsCorrectLatLng() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeeting)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNotNull(location)
    assertEquals(testLocation.latitude, location?.latitude ?: 0.0, 0.0001)
    assertEquals(testLocation.longitude, location?.longitude ?: 0.0, 0.0001)
  }

  @Test
  fun getMeetingLocationReturnsNullWhenMeetingNull() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flowOf(null)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNull(location)
  }

  @Test
  fun getMeetingLocationReturnsNullWhenLocationNull() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        flowOf(testMeetingNoLocation)

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val location = viewModel.getMeetingLocation()
    assertNull(location)
  }

  @Test
  fun viewModelHandlesFlowUpdates() = runTest {
    val flow = kotlinx.coroutines.flow.MutableStateFlow<Meeting?>(null)
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns flow

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

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
  fun repositoryExceptionSetsErrorMessage() = runTest {
    val exceptionMessage = "Network connection failed"
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { throw Exception(exceptionMessage) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertTrue(uiState.errorMsg?.contains("Failed to load meeting") == true)
    assertTrue(uiState.errorMsg?.contains(exceptionMessage) == true)
  }

  @Test
  fun loadingStateIsSetBeforeCollection() = runTest {
    // Use a flow that never emits to verify loading state is set
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(Long.MAX_VALUE) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    // Immediately check state before any collection happens
    assertTrue(viewModel.uiState.value.isLoading)
    assertNull(viewModel.uiState.value.meeting)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun exceptionWithNullMessageHandledCorrectly() = runTest {
    every { repositoryMock.getMeetingById(testProjectId, testMeetingId) } returns
        kotlinx.coroutines.flow.flow { throw Exception(null as String?) }

    viewModel = MeetingNavigationViewModel(testProjectId, testMeetingId, repositoryMock)

    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg?.contains("Failed to load meeting") == true)
  }
}

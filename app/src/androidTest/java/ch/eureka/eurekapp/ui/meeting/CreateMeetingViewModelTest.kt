/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.model.map.LocationRepository
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import io.mockk.every
import io.mockk.mockkStatic
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CreateMeetingViewModel. This suite aims for 100% test coverage of the ViewModel
 * and its UIState.
 *
 * Note : some tests were generated with Gemini
 */
@ExperimentalCoroutinesApi
class CreateMeetingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: CreateMeetingViewModel
  private lateinit var repositoryMock: MockCreateMeetingRepository
  private lateinit var locationRepositoryMock: MockLocationRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  private var currentUserId: String? = "test-user-id"

  private val futureDateTime: LocalDateTime = LocalDateTime.now().plusDays(1)
  private val pastDateTime: LocalDateTime = LocalDateTime.now().minusDays(1)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MockCreateMeetingRepository()
    locationRepositoryMock = MockLocationRepository()

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0

    viewModel =
        CreateMeetingViewModel(
            repository = repositoryMock,
            locationRepository = locationRepositoryMock,
            getCurrentUserId = { currentUserId })

    testDispatcher.scheduler.advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun uiStateIsValidLogicIsCorrect() {
    var state =
        CreateMeetingUIState(
            title = "Valid Title",
            duration = 10,
            date = futureDateTime.toLocalDate(),
            time = futureDateTime.toLocalTime())

    state = state.copy(format = MeetingFormat.VIRTUAL)

    assertTrue(state.isValid)

    state = state.copy(title = "")
    assertFalse(state.isValid)
    state = state.copy(title = " ")
    assertFalse(state.isValid)

    state = state.copy(title = "Valid Title", duration = 0)
    assertFalse(state.isValid)
    state = state.copy(duration = 4)
    assertFalse(state.isValid)

    state =
        state.copy(
            duration = 10, date = pastDateTime.toLocalDate(), time = pastDateTime.toLocalTime())
    assertFalse(state.isValid)

    state =
        state.copy(
            title = "",
            duration = 0,
            date = pastDateTime.toLocalDate(),
            time = pastDateTime.toLocalTime())
    assertFalse(state.isValid)
  }

  @Test
  fun initialStateIsCorrect() {
    val uiState = viewModel.uiState.value

    assertEquals("", uiState.title)
    assertEquals(LocalDate.now().plusDays(7), uiState.date)
    assertNotNull(uiState.time)
    assertEquals(0, uiState.duration)
    assertEquals(MeetingFormat.IN_PERSON, uiState.format)
    assertFalse(uiState.meetingSaved)
    assertFalse(uiState.hasTouchedTitle)
    assertFalse(uiState.hasTouchedDate)
    assertFalse(uiState.hasTouchedTime)
    assertNull(uiState.errorMsg)
    assertFalse(uiState.isValid)
    assertEquals("", uiState.locationQuery)
    assertTrue(uiState.locationSuggestions.isEmpty())
    assertNull(uiState.selectedLocation)
  }

  @Test
  fun clearErrorMsgSetsErrorMsgToNull() {
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setErrorMsgUpdatesErrorMsg() {
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setTitleUpdatesTitle() {
    val newTitle = "My Meeting Title"
    viewModel.setTitle(newTitle)
    assertEquals(newTitle, viewModel.uiState.value.title)
  }

  @Test
  fun setDateUpdatesDate() {
    val newDate = LocalDate.of(2025, 10, 28)
    viewModel.setDate(newDate)
    assertEquals(newDate, viewModel.uiState.value.date)
  }

  @Test
  fun setTimeUpdatesTime() {
    val newTime = LocalTime.of(14, 30)
    viewModel.setTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.time)
  }

  @Test
  fun setDurationUpdatesDuration() {
    val newDuration = 45
    viewModel.setDuration(newDuration)
    assertEquals(newDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun setFormatUpdatesFormat() {
    val newFormat = MeetingFormat.VIRTUAL
    viewModel.setFormat(newFormat)
    assertEquals(newFormat, viewModel.uiState.value.format)
  }

  @Test
  fun setMeetingSavedUpdatesMeetingSaved() {
    viewModel.setMeetingSaved()
    assertTrue(viewModel.uiState.value.meetingSaved)
  }

  @Test
  fun touchTitleUpdatesHasTouchedTitle() {
    viewModel.touchTitle()
    assertTrue(viewModel.uiState.value.hasTouchedTitle)
  }

  @Test
  fun touchDateUpdatesHasTouchedDate() {
    viewModel.touchDate()
    assertTrue(viewModel.uiState.value.hasTouchedDate)
  }

  @Test
  fun touchTimeUpdatesHasTouchedTime() {
    viewModel.touchTime()
    assertTrue(viewModel.uiState.value.hasTouchedTime)
  }

  @Test
  fun createMeetingWhenStateIsInvalidSetsErrorAndReturns() {
    viewModel.setTitle("")
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenTimeIsInPastSetsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(pastDateTime.toLocalDate())
    viewModel.setTime(pastDateTime.toLocalTime())

    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenUserNotLoggedInSetsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null

    viewModel.createMeeting("project-123")

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenValidRepositorySuccessSetsMeetingSaved() = runTest {
    val title = "Successful Meeting"
    val date = futureDateTime.toLocalDate()
    val time = futureDateTime.toLocalTime()
    val duration = 60
    val projectId = "project-success"
    val userId = "test-user-id"
    val meetingFormat = MeetingFormat.IN_PERSON
    val location = Location(1.0, 1.0, "Loc")

    val expectedInstant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()

    viewModel.setTitle(title)
    viewModel.setDate(date)
    viewModel.setTime(time)
    viewModel.setDuration(duration)
    viewModel.setFormat(meetingFormat)
    viewModel.setLocation(location)

    assertTrue(viewModel.uiState.value.isValid)

    assertEquals(userId, currentUserId)

    repositoryMock.shouldSucceed = true

    viewModel.createMeeting(projectId)

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)
    assertNull(viewModel.uiState.value.errorMsg)

    assertEquals(userId, repositoryMock.lastCreatorId)
    assertEquals(MeetingRole.HOST, repositoryMock.lastCreatorRole)

    val createdMeeting = repositoryMock.lastMeetingCreated
    assertNotNull(createdMeeting)
    assertEquals(projectId, createdMeeting!!.projectId)
    assertEquals(title, createdMeeting.title)
    assertEquals(duration, createdMeeting.duration)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, createdMeeting.status)
    assertEquals(userId, createdMeeting.createdBy)
    assertNotNull(createdMeeting.meetingID)
    assertFalse(createdMeeting.meetingID.isBlank())
    assertEquals(location, createdMeeting.location)

    assertEquals(1, createdMeeting.meetingProposals.size)
    val proposal = createdMeeting.meetingProposals[0]

    assertEquals(expectedInstant.epochSecond, proposal.dateTime.seconds)

    assertEquals(1, proposal.votes.size)
    val proposalVote = proposal.votes[0]

    assertEquals(userId, proposalVote.userId)
    assertEquals(listOf(meetingFormat), proposalVote.formatPreferences)
  }

  @Test
  fun createMeetingWhenValidRepositoryFailureSetsErrorMsg() = runTest {
    viewModel.setTitle("Failed Meeting")
    viewModel.setDuration(15)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)

    assertTrue(viewModel.uiState.value.isValid)

    assertEquals("test-user-id", currentUserId)

    repositoryMock.shouldSucceed = false
    repositoryMock.failureException = Exception("Database is down")

    viewModel.createMeeting("project-fail")

    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.meetingSaved)
    assertEquals("Meeting could not be created.", viewModel.uiState.value.errorMsg)

    assertNotNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingUiStateCopyWorksAsExpected() {
    val originalState =
        CreateMeetingUIState(title = "Original", duration = 10, hasTouchedDate = false)
    val copiedState = originalState.copy(title = "Copied", hasTouchedDate = true)

    assertEquals("Original", originalState.title)
    assertEquals(10, originalState.duration)
    assertFalse(originalState.hasTouchedDate)
    assertEquals("Copied", copiedState.title)
    assertEquals(10, copiedState.duration)
    assertTrue(copiedState.hasTouchedDate)
  }

  @Test
  fun setLocationUpdatesSelectedLocation() {
    val location = Location(46.5197, 6.6323, "Lausanne")
    viewModel.setLocation(location)
    assertEquals(location, viewModel.uiState.value.selectedLocation)
  }

  @Test
  fun uiStateIsValidWithInPersonFormatRequiresLocation() {
    viewModel.setTitle("Meeting")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.IN_PERSON)

    assertNull(viewModel.uiState.value.selectedLocation)
    assertFalse(
        "In-Person meeting should be invalid without a location", viewModel.uiState.value.isValid)

    viewModel.setLocation(Location(0.0, 0.0, "Test"))
    assertTrue("In-Person meeting should be valid with a location", viewModel.uiState.value.isValid)
  }

  @Test
  fun uiStateIsValidWithVirtualFormatDoesNotRequireLocation() {
    viewModel.setTitle("Meeting")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)

    viewModel.setLocationQuery("")

    assertTrue(
        "Virtual meeting should be valid without a location", viewModel.uiState.value.isValid)
  }

  @Test
  fun setLocationQueryUpdatesQueryAndFetchesSuggestions() = runTest {
    val query = "Geneva"
    val expectedSuggestions = listOf(Location(46.2, 6.1, "Geneva"))
    locationRepositoryMock.searchResults = expectedSuggestions

    viewModel.setLocationQuery(query)

    assertEquals(query, viewModel.uiState.value.locationQuery)

    testDispatcher.scheduler.advanceTimeBy(400L)
    testDispatcher.scheduler.runCurrent()

    assertEquals(expectedSuggestions, viewModel.uiState.value.locationSuggestions)
  }

  @Test
  fun setLocationQueryWithEmptyStringClearsSuggestions() = runTest {
    locationRepositoryMock.searchResults = listOf(Location(0.0, 0.0, "Old"))

    viewModel.setLocationQuery("Old")
    testDispatcher.scheduler.advanceTimeBy(400L)
    testDispatcher.scheduler.runCurrent()

    assertFalse(viewModel.uiState.value.locationSuggestions.isEmpty())

    viewModel.setLocationQuery("")

    testDispatcher.scheduler.runCurrent()

    assertEquals("", viewModel.uiState.value.locationQuery)
    assertTrue(
        "Suggestions should be empty when query is empty",
        viewModel.uiState.value.locationSuggestions.isEmpty())
  }

  @Test
  fun setLocationQueryHandlesRepositoryException() = runTest {
    locationRepositoryMock.shouldThrow = true

    viewModel.setLocationQuery("Error City")

    testDispatcher.scheduler.advanceTimeBy(400L)
    testDispatcher.scheduler.runCurrent()

    assertTrue(viewModel.uiState.value.locationSuggestions.isEmpty())
  }

  @Test
  fun createMeetingPassesLocationToRepository() = runTest {
    val location = Location(40.7, -74.0, "New York")

    viewModel.setTitle("Location Meeting")
    viewModel.setDuration(60)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.IN_PERSON)
    viewModel.setLocation(location)
    repositoryMock.shouldSucceed = true

    viewModel.createMeeting("proj-loc")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)

    val createdMeeting = repositoryMock.lastMeetingCreated
    assertNotNull(createdMeeting)
    assertEquals(location, createdMeeting?.location)
  }
}

/** Mock implementation of LocationRepository for testing. */
class MockLocationRepository : LocationRepository {
  var searchResults: List<Location> = emptyList()
  var shouldThrow: Boolean = false

  override suspend fun search(query: String): List<Location> {
    if (shouldThrow) {
      throw Exception("Mock location error")
    }
    return searchResults
  }
}

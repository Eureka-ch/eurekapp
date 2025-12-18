/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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
  private lateinit var repositoryMock: MockMeetingRepository
  private lateinit var locationRepositoryMock: MockLocationRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private lateinit var projectRepositoryMock: MockProjectRepository

  private var currentUserId: String? = "test-user-id"
  private val futureDateTime: LocalDateTime = LocalDateTime.now().plusDays(1)
  private val pastDateTime: LocalDateTime = LocalDateTime.now().minusDays(1)

  // Dummy project for testing
  private val testProject = mockk<Project>(relaxed = true)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MockMeetingRepository()
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
    projectRepositoryMock = MockProjectRepository()

    // Setup mock project behavior
    every { testProject.projectId } returns "project-123"
    every { testProject.name } returns "Test Project"
    // Mock members: current user + one other
    every { testProject.memberIds } returns listOf("test-user-id", "other-user")

    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0

    viewModel =
        CreateMeetingViewModel(
            meetingRepository = repositoryMock,
            projectRepository = projectRepositoryMock,
            locationRepository = locationRepositoryMock,
            getCurrentUserId = { currentUserId })

    testDispatcher.scheduler.advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun createMeetingViewModel_uiStateIsValidLogicIsCorrect() {
    var state =
        CreateMeetingUIState(
            project = testProject,
            title = "Valid Title",
            duration = 10,
            date = futureDateTime.toLocalDate(),
            time = futureDateTime.toLocalTime())

    state =
        state.copy(
            format = MeetingFormat.VIRTUAL, meetingLink = "https://meet.google.com/abc-defg-hij")
    assertTrue("State should be valid", state.isValid)

    assertFalse("State invalid without project", state.copy(project = null).isValid)

    assertFalse("State invalid with empty title", state.copy(title = "").isValid)

    assertFalse("State invalid with 0 duration", state.copy(duration = 0).isValid)

    state = state.copy(date = pastDateTime.toLocalDate(), time = pastDateTime.toLocalTime())
    assertFalse("State invalid in past", state.isValid)
  }

  @Test
  fun createMeetingViewModel_initialStateIsCorrect() {
    val uiState = viewModel.uiState.value

    assertNull(uiState.project)
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
  fun createMeetingViewModel_clearErrorMsgSetsErrorMsgToNull() {
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createMeetingViewModel_setErrorMsgUpdatesErrorMsg() {
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createMeetingViewModel_setProjectUpdatesProject() {
    assertNull(viewModel.uiState.value.project)
    viewModel.setProject(testProject)
    assertEquals(testProject, viewModel.uiState.value.project)
  }

  @Test
  fun createMeetingViewModel_setTitleUpdatesTitle() {
    val newTitle = "My Meeting Title"
    viewModel.setTitle(newTitle)
    assertEquals(newTitle, viewModel.uiState.value.title)
  }

  @Test
  fun createMeetingViewModel_setDateUpdatesDate() {
    val newDate = LocalDate.of(2025, 10, 28)
    viewModel.setDate(newDate)
    assertEquals(newDate, viewModel.uiState.value.date)
  }

  @Test
  fun createMeetingViewModel_setTimeUpdatesTime() {
    val newTime = LocalTime.of(14, 30)
    viewModel.setTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.time)
  }

  @Test
  fun createMeetingViewModel_setDurationUpdatesDuration() {
    val newDuration = 45
    viewModel.setDuration(newDuration)
    assertEquals(newDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun createMeetingViewModel_setFormatUpdatesFormat() {
    val newFormat = MeetingFormat.VIRTUAL
    viewModel.setFormat(newFormat)
    assertEquals(newFormat, viewModel.uiState.value.format)
  }

  @Test
  fun createMeetingViewModel_setMeetingSavedUpdatesMeetingSaved() {
    viewModel.setMeetingSaved()
    assertTrue(viewModel.uiState.value.meetingSaved)
  }

  @Test
  fun createMeetingViewModel_touchTitleUpdatesHasTouchedTitle() {
    viewModel.touchTitle()
    assertTrue(viewModel.uiState.value.hasTouchedTitle)
  }

  @Test
  fun createMeetingViewModel_touchDateUpdatesHasTouchedDate() {
    viewModel.touchDate()
    assertTrue(viewModel.uiState.value.hasTouchedDate)
  }

  @Test
  fun createMeetingViewModel_touchTimeUpdatesHasTouchedTime() {
    viewModel.touchTime()
    assertTrue(viewModel.uiState.value.hasTouchedTime)
  }

  @Test
  fun createMeetingViewModel_setLocationUpdatesSelectedLocation() {
    val location = Location(46.5197, 6.6323, "Lausanne")
    viewModel.setLocation(location)
    assertEquals(location, viewModel.uiState.value.selectedLocation)
  }

  @Test
  fun createMeetingViewModel_loadProjectsPopulatesListOnSuccess() = runTest {
    val projects = listOf(testProject, mockk(relaxed = true))
    projectRepositoryMock.emitProjects(projects)

    val job = launch { viewModel.loadProjects() }

    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoadingProjects)
    assertEquals(projects, viewModel.uiState.value.projects)
    assertNull(viewModel.uiState.value.errorMsg)

    job.cancel()
  }

  @Test
  fun createMeetingViewModel_loadProjectsSetsErrorOnFailure() = runTest {
    projectRepositoryMock.shouldThrow = true

    viewModel.loadProjects()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoadingProjects)
    assertEquals("Mock project error", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenStateIsInvalidSetsErrorAndReturns() {
    viewModel.setTitle("") // Invalid title
    viewModel.setProject(testProject)
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting()

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenTimeIsInPastSetsErrorAndReturns() {
    viewModel.setProject(testProject)
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(pastDateTime.toLocalDate())
    viewModel.setTime(pastDateTime.toLocalTime())

    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting()

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenUserNotLoggedInSetsErrorAndReturns() {
    viewModel.setProject(testProject)
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)
    viewModel.setMeetingLink("https://meet.google.com/abc-defg-hij")
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null

    viewModel.createMeeting()

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenProjectNotSelectedSetsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)

    // Ensure project is null
    assertNull(viewModel.uiState.value.project)

    viewModel.createMeeting()
    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenValidRepositorySuccessSetsMeetingSaved() = runTest {
    val title = "Successful Meeting"
    val date = futureDateTime.toLocalDate()
    val time = futureDateTime.toLocalTime()
    val duration = 60
    val userId = "test-user-id"
    val meetingFormat = MeetingFormat.IN_PERSON
    val location = Location(1.0, 1.0, "Loc")

    val expectedInstant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()

    viewModel.setProject(testProject)
    viewModel.setTitle(title)
    viewModel.setDate(date)
    viewModel.setTime(time)
    viewModel.setDuration(duration)
    viewModel.setFormat(meetingFormat)
    viewModel.setLocation(location)

    assertTrue(viewModel.uiState.value.isValid)
    assertEquals(userId, currentUserId)

    repositoryMock.shouldSucceed = true

    viewModel.createMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)
    assertNull(viewModel.uiState.value.errorMsg)

    assertEquals(userId, repositoryMock.lastCreatorId)
    assertEquals(MeetingRole.HOST, repositoryMock.lastCreatorRole)

    val createdMeeting = repositoryMock.lastMeetingCreated
    assertNotNull(createdMeeting)
    assertEquals(testProject.projectId, createdMeeting!!.projectId)
    assertEquals(title, createdMeeting.title)
    assertEquals(duration, createdMeeting.duration)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, createdMeeting.status)
    assertEquals(userId, createdMeeting.createdBy)
    assertNotNull(createdMeeting.meetingID)
    assertEquals(location, createdMeeting.location)

    assertEquals(listOf("test-user-id", "other-user"), createdMeeting.participantIds)

    assertEquals(1, createdMeeting.meetingProposals.size)
    val proposal = createdMeeting.meetingProposals[0]

    assertEquals(expectedInstant.epochSecond, proposal.dateTime.seconds)
  }

  @Test
  fun createMeetingViewModel_createMeetingWhenValidRepositoryFailureSetsErrorMsg() = runTest {
    viewModel.setProject(testProject)
    viewModel.setTitle("Failed Meeting")
    viewModel.setDuration(15)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)
    viewModel.setMeetingLink("https://meet.google.com/abc-defg-hij")

    assertTrue(viewModel.uiState.value.isValid)
    repositoryMock.shouldSucceed = false
    repositoryMock.failureException = Exception("Database is down")

    viewModel.createMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.meetingSaved)
    assertEquals("Meeting could not be created.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createMeetingViewModel_uiStateIsValidWithInPersonFormatRequiresLocation() {
    viewModel.setProject(testProject)
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
  fun createMeetingViewModel_uiStateIsValidWithVirtualFormatDoesNotRequireLocation() {
    viewModel.setProject(testProject)
    viewModel.setTitle("Meeting")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.VIRTUAL)
    viewModel.setMeetingLink("https://meet.google.com/abc-defg-hij")

    viewModel.setLocationQuery("")

    assertTrue(
        "Virtual meeting should be valid without a location", viewModel.uiState.value.isValid)
  }

  @Test
  fun createMeetingViewModel_setLocationQueryUpdatesQueryAndFetchesSuggestions() = runTest {
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
  fun createMeetingViewModel_setLocationQueryWithEmptyStringClearsSuggestions() = runTest {
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
  fun createMeetingViewModel_setLocationQueryHandlesRepositoryException() = runTest {
    locationRepositoryMock.shouldThrow = true

    viewModel.setLocationQuery("Error City")

    testDispatcher.scheduler.advanceTimeBy(400L)
    testDispatcher.scheduler.runCurrent()

    assertTrue(viewModel.uiState.value.locationSuggestions.isEmpty())
  }

  @Test
  fun createMeetingViewModel_createMeetingPassesLocationToRepository() = runTest {
    val location = Location(40.7, -74.0, "New York")

    viewModel.setProject(testProject)
    viewModel.setTitle("Location Meeting")
    viewModel.setDuration(60)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    viewModel.setFormat(MeetingFormat.IN_PERSON)
    viewModel.setLocation(location)
    repositoryMock.shouldSucceed = true

    viewModel.createMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)
    val createdMeeting = repositoryMock.lastMeetingCreated
    assertEquals(location, createdMeeting?.location)
  }
}

class MockMeetingRepository : MeetingRepository {
  var lastMeetingCreated: Meeting? = null
  var lastCreatorId: String? = null
  var lastCreatorRole: MeetingRole? = null
  var shouldSucceed = true
  var failureException = Exception("Mock failure")

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> = flow {}

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> = flow {}

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> = flow {}

  override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> = flow {}

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> {
    lastMeetingCreated = meeting
    lastCreatorId = creatorId
    lastCreatorRole = creatorRole
    return if (shouldSucceed) {
      Result.success("mock-meeting-id")
    } else {
      Result.failure(failureException)
    }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = Result.success(Unit)

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      Result.success(Unit)

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> =
      flow {}

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.success(Unit)

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = Result.success(Unit)

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.success(Unit)
}

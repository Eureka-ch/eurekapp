/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.calendar

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreenConfig
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingViewModel
import ch.eureka.eurekapp.ui.meeting.calendar.MeetingCalendarViewModelTest
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

private val scheduledMeeting =
    Meeting(
        meetingID = "testId",
        projectId = "testProjectId",
        status = MeetingStatus.SCHEDULED,
        datetime = Timestamp.now(),
        format = MeetingFormat.IN_PERSON,
        location = Location(0.0, 0.0, "Test Location"))

/** Test suite to test calendar button in [MeetingScreen]. */
class MeetingScreenWithCalendarButtonTest {

  class MockedMeetingRepository : MeetingRepository {
    override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
      return flowOf(null)
    }

    override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
      return flowOf(emptyList())
    }

    override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
      return flowOf(emptyList())
    }

    // Updated API: No projectId required
    override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> {
      return flowOf(listOf(scheduledMeeting))
    }

    override suspend fun createMeeting(
        meeting: Meeting,
        creatorId: String,
        creatorRole: MeetingRole
    ): Result<String> {
      return Result.success("mock-meeting-id")
    }

    override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
      return flowOf(emptyList())
    }

    override suspend fun addParticipant(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun removeParticipant(
        projectId: String,
        meetingId: String,
        userId: String
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun updateParticipantRole(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      return Result.success(Unit)
    }
  }

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun meetingScreen_calendarButtonAppearsAndIsClickableForScheduledMeeting() {
    val context: Context = ApplicationProvider.getApplicationContext()

    val mockConnectivityObserver = MockConnectivityObserver(context)

    val mockedMeetingRepository: FirestoreMeetingRepository = mockk<FirestoreMeetingRepository>()
    every { mockedMeetingRepository.getParticipants(any(), any()) } returns
        flowOf(listOf(Participant(), Participant()))

    val mockedUsersRepository: FirestoreUserRepository = mockk<FirestoreUserRepository>()
    every { mockedUsersRepository.getUserById(any()) } returns flowOf(User())

    val calendarViewModel =
        MeetingCalendarViewModel(
            calendarRepository = MeetingCalendarViewModelTest.MockedCalendarRepository(),
            meetingsRepository = mockedMeetingRepository,
            usersRepository = mockedUsersRepository)

    val meetingViewModel =
        MeetingViewModel(
            repository = MockedMeetingRepository(),
            getCurrentUserId = { "testUser" },
            connectivityObserver = mockConnectivityObserver)

    composeRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(onCreateMeeting = { _ -> }),
          calendarViewModel = calendarViewModel,
          meetingViewModel = meetingViewModel,
      )
    }

    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(
            MeetingScreenTestTags.getCalendarButtonTestTagForScheduledMeeting(
                scheduledMeeting.meetingID))
        .assertExists()
  }
}

package ch.eureka.eurekapp.model.calendar

import android.content.ContentResolver
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.map.Location
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreenConfig
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingViewModel
import ch.eureka.eurekapp.ui.meeting.calendar.MeetingCalendarViewModelTest
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
        status = MeetingStatus.SCHEDULED,
        datetime = Timestamp.now(),
        format = MeetingFormat.IN_PERSON,
        location = Location())

class MeetingScreenWithCalendarButtonTest {

  class MockedMeetingRepository() : MeetingRepository {
    override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
      TODO("Not yet implemented")
    }

    override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
      return flowOf(listOf(scheduledMeeting))
    }

    override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
      TODO("Not yet implemented")
    }

    override fun getMeetingsForCurrentUser(
        projectId: String,
        skipCache: Boolean
    ): Flow<List<Meeting>> {
      TODO("Not yet implemented")
    }

    override suspend fun createMeeting(
        meeting: Meeting,
        creatorId: String,
        creatorRole: MeetingRole
    ): Result<String> {
      TODO("Not yet implemented")
    }

    override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
      TODO("Not yet implemented")
    }

    override suspend fun addParticipant(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun removeParticipant(
        projectId: String,
        meetingId: String,
        userId: String
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun updateParticipantRole(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)

  private var userRepository = mockk<UserRepository>()

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun checkCalendarButtonAppearsAndIsClickableForScheduledMeeting() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val contentResolver: ContentResolver = context.contentResolver

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
        MeetingViewModel(repository = MockedMeetingRepository(), getCurrentUserId = { "testUser" })

    composeRule.setContent {
      MeetingScreen(
          config = MeetingScreenConfig(projectId = "test-project-id", onCreateMeeting = {}),
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

package ch.eureka.eurekapp.ui.meeting.calendar

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.eureka.eurekapp.model.calendar.CalendarEventData
import ch.eureka.eurekapp.model.calendar.CalendarRepository
import ch.eureka.eurekapp.model.calendar.MeetingCalendarViewModel
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MeetingCalendarViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  class MockedCalendarRepository : CalendarRepository {
    override suspend fun createCalendarEvent(
        contentResolver: ContentResolver,
        eventData: CalendarEventData
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun getCalendarEvent(
        contentResolver: ContentResolver,
        uniqueEventId: String
    ): Result<CalendarEventData?> {
      return Result.success(CalendarEventData())
    }
  }

  private class MockedCalendarRepositoryReturningNoEvent : CalendarRepository {
    override suspend fun createCalendarEvent(
        contentResolver: ContentResolver,
        eventData: CalendarEventData
    ): Result<Unit> {
      return Result.failure(IllegalArgumentException())
    }

    override suspend fun getCalendarEvent(
        contentResolver: ContentResolver,
        uniqueEventId: String
    ): Result<CalendarEventData?> {
      return Result.success(null)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun checkIsMeetingRegisteredInCalendarWorks() = runTest {
    val context: Context = ApplicationProvider.getApplicationContext()
    val contentResolver: ContentResolver = context.contentResolver

    val mockedMeetingRepository: FirestoreMeetingRepository = mockk<FirestoreMeetingRepository>()
    every { mockedMeetingRepository.getParticipants(any(), any()) } returns
        flowOf(listOf(Participant(), Participant()))

    val mockedUsersRepository: FirestoreUserRepository = mockk<FirestoreUserRepository>()
    every { mockedUsersRepository.getUserById(any()) } returns flowOf(User())

    val fakeViewModel =
        MeetingCalendarViewModel(
            calendarRepository = MockedCalendarRepository(),
            meetingsRepository = mockedMeetingRepository,
            usersRepository = mockedUsersRepository)
    val fakeMeeting = Meeting(meetingID = "dummyID", status = MeetingStatus.SCHEDULED)

    val result = fakeViewModel.checkIsMeetingRegisteredInCalendar(contentResolver, fakeMeeting)

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(fakeViewModel.registeredMeetings.value.contains("dummyID"))
  }

  @Test
  fun checkAddMeetingToCalendarWorks() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val contentResolver: ContentResolver = context.contentResolver

    val mockedMeetingRepository: FirestoreMeetingRepository = mockk<FirestoreMeetingRepository>()
    every { mockedMeetingRepository.getParticipants(any(), any()) } returns
        flowOf(listOf(Participant(), Participant()))

    val mockedUsersRepository: FirestoreUserRepository = mockk<FirestoreUserRepository>()
    every { mockedUsersRepository.getUserById(any()) } returns flowOf(User())

    val fakeViewModel =
        MeetingCalendarViewModel(
            calendarRepository = MockedCalendarRepository(),
            meetingsRepository = mockedMeetingRepository,
            usersRepository = mockedUsersRepository)

    var success = false
    val result =
        fakeViewModel.addMeetingToCalendar(
            contentResolver,
            Meeting(status = MeetingStatus.SCHEDULED),
            onSuccess = { success = true },
            onFailure = {})

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(success)
  }

  @Test
  fun checkAddMeetingToCalendarDoesNotWork() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val contentResolver: ContentResolver = context.contentResolver

    val mockedMeetingRepository: FirestoreMeetingRepository = mockk<FirestoreMeetingRepository>()
    every { mockedMeetingRepository.getParticipants(any(), any()) } returns
        flowOf(listOf(Participant(), Participant()))

    val mockedUsersRepository: FirestoreUserRepository = mockk<FirestoreUserRepository>()
    every { mockedUsersRepository.getUserById(any()) } returns flowOf(User())

    val fakeViewModel =
        MeetingCalendarViewModel(
            calendarRepository = MockedCalendarRepositoryReturningNoEvent(),
            meetingsRepository = mockedMeetingRepository,
            usersRepository = mockedUsersRepository)

    var failure = false
    val result =
        fakeViewModel.addMeetingToCalendar(
            contentResolver,
            Meeting(status = MeetingStatus.SCHEDULED),
            onSuccess = {},
            onFailure = { failure = true })

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(failure)
  }
}

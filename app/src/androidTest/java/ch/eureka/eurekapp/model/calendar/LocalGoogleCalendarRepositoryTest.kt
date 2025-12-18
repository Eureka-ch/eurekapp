/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.model.calendar

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.description

class LocalGoogleCalendarRepositoryTest {
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)

  private var userRepository = mockk<UserRepository>()

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun localGoogleCalendarRepository_testCreateCalendarEventWorks() = runBlocking {
    val contentResolver = mockk<ContentResolver>()

    every { contentResolver.insert(any<Uri>(), any()) } returns Uri.parse("content://fake/1")
    val repo = LocalGoogleCalendarRepository(usersRepository = userRepository)
    val fakeUser = User(email = "example@gmail.com")

    every { userRepository.getCurrentUser() } returns flowOf(fakeUser)

    val fakeCursor = mockk<Cursor>()

    every { fakeCursor.moveToFirst() } returns true
    every { fakeCursor.getInt(any()) } returns 1
    every { fakeCursor.close() } returns Unit

    every { contentResolver.applyBatch(CalendarContract.AUTHORITY, any()) } returns arrayOf()

    every { contentResolver.query(any(), any(), any(), any(), any()) } returns fakeCursor

    val fakeCalendarEventData =
        CalendarEventData(
            title = "Test project",
            description = "description",
            location = "location",
            startTimeMillis = 0L,
            endTimeMillis = 100L,
            attendees = listOf(CalendarAttendee(email = "example@gmail.com", name = "Ilias")))

    val t = repo.createCalendarEvent(contentResolver, fakeCalendarEventData).isSuccess

    assertTrue(t)
  }

  @Test
  fun localGoogleCalendarRepository_testGetCalendarEventWorks() = runBlocking {
    var counter = 0
    val fakeCursor = mockk<Cursor>()

    val stringToReturn = "dummy"

    val repo = LocalGoogleCalendarRepository()

    every { fakeCursor.moveToFirst() } returns true
    every { fakeCursor.getInt(any()) } returns 0
    every { fakeCursor.close() } returns Unit
    every { fakeCursor.getString(any()) } returns stringToReturn
    every { fakeCursor.getLong(any()) } returns 0
    every { fakeCursor.moveToNext() } answers
        {
          counter++
          counter <= 2
        }

    val contentResolver = mockk<ContentResolver>()
    every { contentResolver.query(any(), any(), any(), any(), any()) } answers
        {
          counter = 0
          fakeCursor
        }

    val expectedCalendarData =
        CalendarEventData(
            title = stringToReturn,
            description = stringToReturn,
            location = stringToReturn,
            startTimeMillis = 0L,
            endTimeMillis = 0L,
            attendees =
                listOf(
                    CalendarAttendee(
                        email = stringToReturn,
                        name = stringToReturn,
                        type = 0,
                        relationship = 0,
                        status = 0),
                    CalendarAttendee(
                        email = stringToReturn,
                        name = stringToReturn,
                        type = 0,
                        relationship = 0,
                        status = 0)),
            reminders =
                listOf(
                    CalendarReminder(minutesBefore = 0, method = 0),
                    CalendarReminder(minutesBefore = 0, method = 0)),
            availability = 0,
            eventUid = stringToReturn)

    val trueValue = repo.getCalendarEvent(contentResolver, "")

    assertTrue(trueValue.isSuccess)

    Log.d("TEST-CALENDAR", trueValue.getOrNull().toString())
    Log.d("TEST-CALENDAR", expectedCalendarData.toString())
    assertTrue(expectedCalendarData == trueValue.getOrNull())
  }
}

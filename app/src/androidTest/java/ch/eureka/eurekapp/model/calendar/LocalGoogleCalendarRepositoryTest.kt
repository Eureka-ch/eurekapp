package ch.eureka.eurekapp.model.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.TimeZone

class LocalGoogleCalendarRepositoryTest {
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR
    )

    private lateinit var repo: LocalGoogleCalendarRepository
    private var userRepository = mockk<UserRepository>()

    @Before
    fun setup(){
        repo = LocalGoogleCalendarRepository(userRepository)
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testCreateCalendarEventWorks(){
        val contentResolver = mockk<ContentResolver>()

        every {contentResolver.insert(any<Uri>(), any())} returns Uri.parse("content://fake/1")
        val repo = LocalGoogleCalendarRepository()
        val fakeUser = User(email = "example@gmail.com")

        every {userRepository.getCurrentUser()} returns flowOf(fakeUser)

        val fakeCursor = mock<Cursor>()

        every { fakeCursor.moveToFirst() } returns true
        every {fakeCursor.getInt(any())  } returns 1
    }
}
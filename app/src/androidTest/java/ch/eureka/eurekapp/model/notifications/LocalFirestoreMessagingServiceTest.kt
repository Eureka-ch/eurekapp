/* Portions of this file were written with the help of Gemini Pro 3 and Claude. */
package ch.eureka.eurekapp.model.notifications

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import ch.eureka.eurekapp.services.notifications.LocalFirestoreMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.junit.After
import org.junit.Before
import org.junit.Test

class LocalFirestoreMessagingServiceTest {

  private lateinit var service: LocalFirestoreMessagingService
  private lateinit var mockNotificationManager: NotificationManager
  private lateinit var mockNotificationManagerCompat: NotificationManagerCompat

  @Before
  fun setup() {
    MockKAnnotations.init(this)

    // Mock Static Android Utils only (safer than System classes)
    mockkStatic(ContextCompat::class)
    mockkStatic(NotificationManagerCompat::class)

    // We do NOT mock Intent or Builder constructors.
    // We let them run naturally using the real Context we inject below.

    mockNotificationManager = mockk(relaxed = true)
    mockNotificationManagerCompat = mockk(relaxed = true)

    // Initialize Service as a Spy
    service = spyk(LocalFirestoreMessagingService())

    // 1. Inject a REAL Context so Intent() and Builder() work without crashing
    val realContext = ApplicationProvider.getApplicationContext<Context>()
    attachContext(service, realContext)

    // 2. Mock specific system calls to prevent side effects (like real notifications showing up)
    // Delegate system service calls to our mock
    every { service.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
    // Ensure Compat manager returns our mock
    every { NotificationManagerCompat.from(any<Context>()) } returns mockNotificationManagerCompat

    // 3. Permission Mocking
    every { ContextCompat.checkSelfPermission(any(), any()) } returns
        PackageManager.PERMISSION_GRANTED
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // Helper to attach real Context to the Service (mimics Android lifecycle)
  private fun attachContext(service: Service, context: Context) {
    val attachMethod =
        ContextWrapper::class.java.getDeclaredMethod("attachBaseContext", Context::class.java)
    attachMethod.isAccessible = true
    attachMethod.invoke(service, context)
  }

  @Test
  fun localFirestoreMessagingService_testOnNewToken() {
    service.onNewToken("token")
  }

  @Test
  fun localFirestoreMessagingService_testMessagePathMeeting() {
    // Given
    val dataMap =
        mapOf(
            "type" to NotificationType.MEETING_NOTIFICATION.backendTypeString,
            "title" to "Meeting",
            "body" to "Body")
    val message = RemoteMessage.Builder("sender").setData(dataMap).build()

    // When
    service.onMessageReceived(message)

    // Then: Verify notify was called on our mock manager
    verify { mockNotificationManagerCompat.notify(any(), any()) }
  }

  @Test
  fun localFirestoreMessagingService_testMessagePathMessage() {
    val dataMap =
        mapOf("type" to NotificationType.MESSAGE_NOTIFICATION.backendTypeString, "id" to "123")
    val message = RemoteMessage.Builder("sender").setData(dataMap).build()

    service.onMessageReceived(message)

    verify { mockNotificationManagerCompat.notify(any(), any()) }
  }

  @Test
  fun localFirestoreMessagingService_testMessagePathGeneralAndMissingData() {
    val dataMap = emptyMap<String, String>()
    val message = RemoteMessage.Builder("sender").setData(dataMap).build()

    service.onMessageReceived(message)

    verify { mockNotificationManagerCompat.notify(any(), any()) }
  }

  @Test
  fun localFirestoreMessagingService_testPermissionDenied() {
    every { ContextCompat.checkSelfPermission(any(), any()) } returns
        PackageManager.PERMISSION_DENIED
    val dataMap = mapOf("title" to "Test")
    val message = RemoteMessage.Builder("sender").setData(dataMap).build()

    service.onMessageReceived(message)

    // Verify notify was NOT called
    verify(exactly = 0) { mockNotificationManagerCompat.notify(any(), any()) }
  }

  @Test
  fun localFirestoreMessagingService_testChannelCreationVersionHigh() {
    setSdkInt(Build.VERSION_CODES.O)
    val dataMap = mapOf("type" to "general")
    val message = RemoteMessage.Builder("sender").setData(dataMap).build()

    service.onMessageReceived(message)

    // Verify channel creation on the mocked NotificationManager
    verify { mockNotificationManager.createNotificationChannel(any()) }
  }

  private fun setSdkInt(version: Int) {
    try {
      val field = Build.VERSION::class.java.getField("SDK_INT")
      field.isAccessible = true
      val modifiersField = Field::class.java.getDeclaredField("modifiers")
      modifiersField.isAccessible = true
      modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
      field.set(null, version)
    } catch (e: Exception) {}
  }
}

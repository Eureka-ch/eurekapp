// Portions of this code were generated with the help of Claude Sonnet 4.5
package ch.eureka.eurekapp.model.notifications

import org.junit.Assert.*
import org.junit.Test

class NotificationTypeTest {

  @Test
  fun notificationType_allNotificationTypesExist() {
    val types = NotificationType.entries
    assertTrue(types.isNotEmpty())
    assertTrue(types.size >= 3)
  }

  @Test
  fun notificationType_generalNotificationProperties() {
    val general = NotificationType.GENERAL_NOTIFICATION
    assertEquals("General", general.displayString)
    assertEquals("general", general.backendTypeString)
  }

  @Test
  fun notificationType_meetingNotificationProperties() {
    val meeting = NotificationType.MEETING_NOTIFICATION
    assertEquals("Meetings", meeting.displayString)
    assertEquals("meeting", meeting.backendTypeString)
  }

  @Test
  fun notificationType_messageNotificationProperties() {
    val message = NotificationType.MESSAGE_NOTIFICATION
    assertEquals("Message", message.displayString)
    assertEquals("message", message.backendTypeString)
  }

  @Test
  fun notificationType_enumValuesMethod() {
    val values = NotificationType.entries.toTypedArray()
    assertTrue(values.contains(NotificationType.GENERAL_NOTIFICATION))
    assertTrue(values.contains(NotificationType.MEETING_NOTIFICATION))
    assertTrue(values.contains(NotificationType.MESSAGE_NOTIFICATION))
  }

  @Test
  fun notificationType_enumValueOfMethod() {
    assertEquals(
        NotificationType.GENERAL_NOTIFICATION, NotificationType.valueOf("GENERAL_NOTIFICATION"))
    assertEquals(
        NotificationType.MEETING_NOTIFICATION, NotificationType.valueOf("MEETING_NOTIFICATION"))
    assertEquals(
        NotificationType.MESSAGE_NOTIFICATION, NotificationType.valueOf("MESSAGE_NOTIFICATION"))
  }

  @Test
  fun notificationType_allDisplayStringsAreUnique() {
    val displayStrings = NotificationType.entries.map { it.displayString }
    assertEquals(displayStrings.size, displayStrings.toSet().size)
  }

  @Test
  fun notificationType_allBackendTypeStringsAreUnique() {
    val backendStrings = NotificationType.entries.map { it.backendTypeString }
    assertEquals(backendStrings.size, backendStrings.toSet().size)
  }
}

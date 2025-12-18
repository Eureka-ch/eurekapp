// Portions of this code were generated with the help of Gemini Pro 3
package ch.eureka.eurekapp.model.data.user

import ch.eureka.eurekapp.model.notifications.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserNotificationSettingsKeysTest {

  @Test
  fun userNotificationSettingsKeys_testEnumProperties() {
    // Verify Meeting Notification properties
    val meetingScheduled = UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY
    assertEquals("Notify when meeting scheduled: ", meetingScheduled.displayName)
    assertEquals(NotificationType.MEETING_NOTIFICATION, meetingScheduled.notificationType)

    val meetingVotes = UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY
    assertEquals("Notify when meeting is open to votes: ", meetingVotes.displayName)
    assertEquals(NotificationType.MEETING_NOTIFICATION, meetingVotes.notificationType)

    // Verify Message Notification properties
    val newMessage = UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY
    assertEquals("Notify when a new message is sent: ", newMessage.displayName)
    assertEquals(NotificationType.MESSAGE_NOTIFICATION, newMessage.notificationType)
  }

  @Test
  fun userNotificationSettingsKeys_testDefaultValuesMapSize() {
    // Ensure the map has the same number of entries as the Enum
    assertEquals(
        UserNotificationSettingsKeys.entries.size, defaultValuesNotificationSettingsKeys.size)
  }

  @Test
  fun userNotificationSettingsKeys_testDefaultValuesMapContent() {
    // Verify every Enum key is present in the map and set to TRUE by default
    UserNotificationSettingsKeys.entries.forEach { key ->
      assertTrue(
          "Map should contain key ${key.name}",
          defaultValuesNotificationSettingsKeys.containsKey(key.name))
      assertTrue(
          "Default value for ${key.name} should be true",
          defaultValuesNotificationSettingsKeys[key.name] == true)
    }
  }
}

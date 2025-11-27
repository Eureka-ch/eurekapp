package ch.eureka.eurekapp.model.data.user

import ch.eureka.eurekapp.model.notifications.NotificationType

enum class UserNotificationSettingsKeys(
    val displayName: String,
    val notificationType: NotificationType
) {
  // Meeting notifications
  ON_MEETING_SCHEDULED_NOTIFY(
      "Notify when meeting scheduled: ", NotificationType.MEETING_NOTIFICATION),
  ON_MEETING_OPEN_TO_VOTES_NOTIFY(
      "Notify when meeting is open to votes: ", NotificationType.MEETING_NOTIFICATION),
  ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE(
      "Notify when meeting is in 10 minutes: ", NotificationType.MEETING_NOTIFICATION),

  // Message notifications
  ON_NEW_MESSAGE_NOTIFY(
      "Notify when a new message is sent: ", NotificationType.MESSAGE_NOTIFICATION)
}

val defaultValuesNotificationSettingsKeys: Map<String, Boolean> =
    UserNotificationSettingsKeys.entries.associate { key -> key.name to true }

package ch.eureka.eurekapp.model.data.user

enum class UserNotificationSettingsKeys {
    //Meeting notifications
    ON_MEETING_SCHEDULED_NOTIFY,
    ON_MEETING_OPEN_TO_VOTES_NOTIFY,
    ON_MEETING_CREATED_NOTIFY,
    ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE,

    //Message notifications
    ON_NEW_MESSAGE_NOTIFY
}

val defaultValuesNotificationSettingsKeys: Map<String, Boolean> =
    UserNotificationSettingsKeys.entries.associate { key -> key.name to true }
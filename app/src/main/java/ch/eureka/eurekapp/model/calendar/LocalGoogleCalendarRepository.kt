// This documentation was generated with the help of ChatGPT-5 by OpenAI.
package ch.eureka.eurekapp.model.calendar

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.user.UserRepository
import java.util.TimeZone
import kotlinx.coroutines.flow.first

/**
 * Repository for interacting with the local Google Calendar on the device. Provides functions to
 * create, read events, and retrieve attendees and reminders.
 *
 * @property usersRepository Repository used to retrieve user data.
 */
class LocalGoogleCalendarRepository(
    private val usersRepository: UserRepository = RepositoriesProvider.userRepository
) : CalendarRepository {

  private object CompanionLocalGoogleCalendarRepository {
    /** Projection for querying calendars. */
    val CALENDAR_PROJECTION: Array<String> =
        arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT)

    /** Index of the calendar ID in the projection array. */
    val PROJECTION_ID_INDEX: Int = 0

    /** URI for calendars content provider. */
    val calendarUri: Uri = CalendarContract.Calendars.CONTENT_URI

    /** URI for events content provider. */
    val eventsUri = CalendarContract.Events.CONTENT_URI
    /** Projection for querying events. */
    val eventsProjection =
        arrayOf(
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events._ID)

    val IDX_DTSTART = 0
    val IDX_DTEND = 1
    val IDX_TITLE = 2
    val IDX_DESCRIPTION = 3
    val IDX_EVENT_LOCATION = 4
    val IDX_EVENT_ID = 5

    /** Selection string to query events by description. */
    val eventSelection = "${CalendarContract.Events.DESCRIPTION} = ?"

    /** Projection for querying event attendees. */
    val eventAttendeesProjection =
        arrayOf(
            CalendarContract.Attendees.ATTENDEE_NAME,
            CalendarContract.Attendees.ATTENDEE_EMAIL,
            CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
            CalendarContract.Attendees.ATTENDEE_TYPE,
            CalendarContract.Attendees.ATTENDEE_STATUS)

    val IDX_ATTENDEE_NAME = 0
    val IDX_ATTENDEE_EMAIL = 1
    val IDX_ATTENDEE_RELATIONSHIP = 2
    val IDX_ATTENDEE_TYPE = 3
    val IDX_ATTENDEE_STATUS = 4

    /** Selection string to query attendees by event ID. */
    val attendeeSelection = "${CalendarContract.Attendees.EVENT_ID} = ?"

    /** Projection for querying reminders of an event. */
    val eventRemindersProjection =
        arrayOf(CalendarContract.Reminders.MINUTES, CalendarContract.Reminders.METHOD)

    val IDX_REMINDER_MINUTES = 0
    val IDX_REMINDER_METHOD = 1

    /** Selection string to query reminders by event ID. */
    val remindersSelection = "${CalendarContract.Reminders.EVENT_ID} = ?"
  }

  override suspend fun createCalendarEvent(
      contentResolver: ContentResolver,
      eventData: CalendarEventData
  ): Result<Unit> = runCatching {
    val operations = ArrayList<ContentProviderOperation>()

    val googleCalendarID = getGoogleCalendarID(contentResolver)

    val eventToContentValues = eventToContentValues(googleCalendarID, eventData)

    operations.add(
        ContentProviderOperation.newInsert(CalendarContract.Events.CONTENT_URI)
            .withValues(eventToContentValues)
            .build())

    // Add the attendees of the event
    for (attendee: CalendarAttendee in eventData.attendees) {
      operations.add(
          ContentProviderOperation.newInsert(CalendarContract.Attendees.CONTENT_URI)
              .withValueBackReference(CalendarContract.Attendees.EVENT_ID, 0)
              .withValues(attendeeToContentValues(0L, attendee))
              .build())
    }

    // Add the reminders of the event
    for (reminder: CalendarReminder in eventData.reminders) {
      operations.add(
          ContentProviderOperation.newInsert(CalendarContract.Reminders.CONTENT_URI)
              .withValueBackReference(CalendarContract.Attendees.EVENT_ID, 0)
              .withValues(reminderToContentValue(0L, reminder))
              .build())
    }
    contentResolver.applyBatch(CalendarContract.AUTHORITY, operations)
  }

  override suspend fun getCalendarEvent(
      contentResolver: ContentResolver,
      uniqueEventId: String
  ): Result<CalendarEventData?> = runCatching {
    val eventPair = getEventFromGoogleCalendar(contentResolver, uniqueEventId)
    val calendarEventData = eventPair.second
    val eventCalendarId = eventPair.first
    val attendees = getAttendeesFromEventId(contentResolver, eventCalendarId)
    val reminders = getRemindersFromEventId(contentResolver, eventCalendarId)

    return Result.success(calendarEventData.copy(reminders = reminders, attendees = attendees))
  }

  /**
   * Queries the Google Calendar for a specific event by its unique ID.
   *
   * @param contentResolver The content resolver used to access the calendar provider.
   * @param uniqueEventId The unique identifier of the event.
   * @return Pair of the calendar event ID and the corresponding CalendarEventData.
   */
  private fun getEventFromGoogleCalendar(
      contentResolver: ContentResolver,
      uniqueEventId: String
  ): Pair<Long, CalendarEventData> {
    val eventSelectionArgs = arrayOf(uniqueEventId)
    val eventCursor =
        contentResolver.query(
            CompanionLocalGoogleCalendarRepository.eventsUri,
            CompanionLocalGoogleCalendarRepository.eventsProjection,
            CompanionLocalGoogleCalendarRepository.eventSelection,
            eventSelectionArgs,
            null)

    checkNotNull(eventCursor)

    check(eventCursor.moveToFirst())

    val dateStart = eventCursor.getLong(CompanionLocalGoogleCalendarRepository.IDX_DTSTART)
    val dateEnd = eventCursor.getLong(CompanionLocalGoogleCalendarRepository.IDX_DTEND)
    val title = eventCursor.getString(CompanionLocalGoogleCalendarRepository.IDX_TITLE)
    val description = eventCursor.getString(CompanionLocalGoogleCalendarRepository.IDX_DESCRIPTION)
    val eventLocation =
        eventCursor.getString(CompanionLocalGoogleCalendarRepository.IDX_EVENT_LOCATION)
    val eventCalendarId = eventCursor.getLong(CompanionLocalGoogleCalendarRepository.IDX_EVENT_ID)

    var calendarEventData =
        CalendarEventData(
            title = title,
            description = description,
            location = eventLocation,
            startTimeMillis = dateStart,
            endTimeMillis = dateEnd,
            eventUid = description)
    eventCursor.close()

    return eventCalendarId to calendarEventData
  }

  /**
   * Retrieves all attendees of a given calendar event.
   *
   * @param contentResolver The content resolver used to access the calendar provider.
   * @param eventCalendarId The ID of the event to fetch attendees for.
   * @return List of CalendarAttendee objects associated with the event.
   */
  private fun getAttendeesFromEventId(
      contentResolver: ContentResolver,
      eventCalendarId: Long
  ): List<CalendarAttendee> {
    // Now we need to get the attendees
    val attendeeSelectionArgs = arrayOf(eventCalendarId.toString())

    val attendeesCursor =
        contentResolver.query(
            CalendarContract.Attendees.CONTENT_URI,
            CompanionLocalGoogleCalendarRepository.eventAttendeesProjection,
            CompanionLocalGoogleCalendarRepository.attendeeSelection,
            attendeeSelectionArgs,
            null)

    checkNotNull(attendeesCursor)
    val attendees: MutableList<CalendarAttendee> = mutableListOf()
    while (attendeesCursor.moveToNext()) {
      val attendeeName =
          attendeesCursor.getString(CompanionLocalGoogleCalendarRepository.IDX_ATTENDEE_NAME)
      val attendeeEmail =
          attendeesCursor.getString(CompanionLocalGoogleCalendarRepository.IDX_ATTENDEE_EMAIL)
      val attendeeRelationship =
          attendeesCursor.getInt(CompanionLocalGoogleCalendarRepository.IDX_ATTENDEE_RELATIONSHIP)
      val attendeeType =
          attendeesCursor.getInt(CompanionLocalGoogleCalendarRepository.IDX_ATTENDEE_TYPE)
      val attendeeStatus =
          attendeesCursor.getInt(CompanionLocalGoogleCalendarRepository.IDX_ATTENDEE_STATUS)

      val attendee =
          CalendarAttendee(
              email = attendeeEmail,
              name = attendeeName,
              type = attendeeType,
              relationship = attendeeRelationship,
              status = attendeeStatus)
      attendees += attendee
    }
    attendeesCursor.close()
    return attendees.toList()
  }

  /**
   * Retrieves all reminders associated with a calendar event.
   *
   * @param contentResolver The content resolver used to access the calendar provider.
   * @param eventCalendarId The ID of the event to fetch reminders for.
   * @return List of CalendarReminder objects associated with the event.
   */
  private fun getRemindersFromEventId(
      contentResolver: ContentResolver,
      eventCalendarId: Long
  ): List<CalendarReminder> {
    // search reminders
    val remindersSelectionArgs = arrayOf(eventCalendarId.toString())

    val remindersCursor =
        contentResolver.query(
            CalendarContract.Reminders.CONTENT_URI,
            CompanionLocalGoogleCalendarRepository.eventRemindersProjection,
            CompanionLocalGoogleCalendarRepository.remindersSelection,
            remindersSelectionArgs,
            null)
    checkNotNull(remindersCursor)
    val reminders: MutableList<CalendarReminder> = mutableListOf()
    while (remindersCursor.moveToNext()) {
      val minutesBefore =
          remindersCursor.getInt(CompanionLocalGoogleCalendarRepository.IDX_REMINDER_MINUTES)
      val method =
          remindersCursor.getInt(CompanionLocalGoogleCalendarRepository.IDX_REMINDER_METHOD)

      val reminder = CalendarReminder(minutesBefore = minutesBefore, method = method)

      reminders += reminder
    }
    remindersCursor.close()
    return reminders.toList()
  }

  /**
   * Retrieves the Google Calendar ID for the current user.
   *
   * @param contentResolver The content resolver used to access the calendar provider.
   * @return The integer ID of the Google Calendar.
   */
  private suspend fun getGoogleCalendarID(contentResolver: ContentResolver): Int {
    val user = usersRepository.getCurrentUser().first()
    checkNotNull(user)
    val selectionQuery =
        "((${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (${
            CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
    val selectionArgs: Array<String> = arrayOf("com.google", user.email)

    val cur: Cursor? =
        contentResolver.query(
            CompanionLocalGoogleCalendarRepository.calendarUri,
            CompanionLocalGoogleCalendarRepository.CALENDAR_PROJECTION,
            selectionQuery,
            selectionArgs,
            null)
    checkNotNull(cur)
    var googleCalendarID: Int?
    check(cur.moveToFirst())
    val calID = cur.getInt(CompanionLocalGoogleCalendarRepository.PROJECTION_ID_INDEX)
    googleCalendarID = calID
    cur.close()
    return googleCalendarID
  }

  /**
   * Converts a CalendarEventData object to ContentValues for insertion into the calendar.
   *
   * @param googleCalendarID The ID of the calendar to insert the event into.
   * @param eventData The data of the event to convert.
   * @return ContentValues representing the calendar event.
   */
  private fun eventToContentValues(
      googleCalendarID: Int,
      eventData: CalendarEventData
  ): ContentValues {
    val systemTimeZone = TimeZone.getDefault().id
    return ContentValues().apply {
      put(CalendarContract.Events.DTSTART, eventData.startTimeMillis)
      put(CalendarContract.Events.DTEND, eventData.endTimeMillis)
      put(CalendarContract.Events.EVENT_LOCATION, eventData.location)
      put(CalendarContract.Events.TITLE, eventData.title)
      put(CalendarContract.Events.DESCRIPTION, eventData.eventUid)
      put(CalendarContract.Events.CALENDAR_ID, googleCalendarID)
      put(CalendarContract.Events.EVENT_TIMEZONE, systemTimeZone)
    }
  }

  /**
   * Converts a CalendarAttendee to ContentValues for insertion into the attendees table.
   *
   * @param eventID The ID of the event the attendee belongs to.
   * @param attendee The attendee data to convert.
   * @return ContentValues representing the attendee.
   */
  private fun attendeeToContentValues(eventID: Long, attendee: CalendarAttendee): ContentValues {
    return ContentValues().apply {
      put(CalendarContract.Attendees.ATTENDEE_NAME, attendee.name)
      put(CalendarContract.Attendees.ATTENDEE_EMAIL, attendee.email)
      put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, attendee.relationship)
      put(CalendarContract.Attendees.ATTENDEE_TYPE, attendee.type)
      put(CalendarContract.Attendees.ATTENDEE_STATUS, attendee.status)
      put(CalendarContract.Attendees.EVENT_ID, eventID)
    }
  }

  /**
   * Converts a CalendarReminder to ContentValues for insertion into the reminders table.
   *
   * @param eventID The ID of the event the reminder belongs to.
   * @param reminder The reminder data to convert.
   * @return ContentValues representing the reminder.
   */
  private fun reminderToContentValue(eventID: Long, reminder: CalendarReminder): ContentValues {
    return ContentValues().apply {
      put(CalendarContract.Reminders.MINUTES, reminder.minutesBefore)
      put(CalendarContract.Reminders.EVENT_ID, eventID)
      put(CalendarContract.Reminders.METHOD, reminder.method)
    }
  }
}

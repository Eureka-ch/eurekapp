//This documentation was generated with the help of ChatGPT-5 by OpenAI.
package ch.eureka.eurekapp.model.calendar

import android.content.ContentResolver

/**
 * Defines the contract for interacting with calendar data on the device.
 *
 * Implementations of this interface provide access to create and retrieve
 * calendar events using the Android Calendar Provider, including handling
 * additional metadata such as attendees and reminders.
 *
 * This abstraction allows the app to interact with calendar functionality
 * without depending directly on a specific storage or provider implementation.
 */
interface CalendarRepository {
    /**
     * Creates a calendar event with the given data, including attendees and reminders.
     *
     * @param contentResolver The content resolver used to access the calendar provider.
     * @param eventData The data of the event to create.
     * @return Result indicating success or failure.
     */
    suspend fun createCalendarEvent(contentResolver: ContentResolver,
                                    eventData: CalendarEventData): Result<Unit>
    /**
     * Retrieves a calendar event by its unique ID.
     *
     * @param contentResolver The content resolver used to access the calendar provider.
     * @param uniqueEventId The unique identifier of the event.
     * @return Result containing the event data or null if not found.
     */
    suspend fun getCalendarEvent(contentResolver: ContentResolver,
                                 uniqueEventId: String): Result<CalendarEventData?>
}
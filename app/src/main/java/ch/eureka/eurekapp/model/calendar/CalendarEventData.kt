
//Portions of this code were generated with the help of ChatGPT-5 by OpenAI.

package ch.eureka.eurekapp.model.calendar
import android.provider.CalendarContract

data class CalendarEventData(
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 1L,
    val allDay: Boolean = false,
    val attendees: List<CalendarAttendee> = emptyList(),
    val reminders: List<CalendarReminder> = listOf(
        CalendarReminder(minutesBefore = 1440),
        CalendarReminder(minutesBefore = 10)
    ),
    val availability: Int = CalendarContract.Events.AVAILABILITY_BUSY,
    val eventUid: String = ""
)

data class CalendarAttendee(
    val email: String,
    val name: String? = null,
    val type: Int = CalendarContract.Attendees.TYPE_REQUIRED,
    val relationship: Int = CalendarContract.Attendees.RELATIONSHIP_ATTENDEE,
    val status: Int = CalendarContract.Attendees.ATTENDEE_STATUS_INVITED,
)

data class CalendarReminder(
    val minutesBefore: Int,
    val method: Int = CalendarContract.Reminders.METHOD_ALERT
)
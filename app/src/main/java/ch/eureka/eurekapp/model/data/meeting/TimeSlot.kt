package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Data class representing a time slot for a meeting.
 *
 * @property startTime The beginning of the time slot.
 * @property endTime The end of the time slot.
 */
data class TimeSlot(
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now()
)

/**
 * Extension function to display the timeslot as a formatted string.
 *
 * @return the formatted string representation of the [TimeSlot].
 */
fun TimeSlot.formatTimeSlot(): String {
  val startTime = this.startTime.toDate()
  val endTime = this.endTime.toDate()

  val dayFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
  val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

  val dayPart = dayFormatter.format(startTime)
  val startPart = timeFormatter.format(startTime)
  val endPart = timeFormatter.format(endTime)

  return "$dayPart · $startPart–$endPart"
}

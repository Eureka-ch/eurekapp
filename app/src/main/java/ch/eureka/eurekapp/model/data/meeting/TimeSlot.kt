package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp

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

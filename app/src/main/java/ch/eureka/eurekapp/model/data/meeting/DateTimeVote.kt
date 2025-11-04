package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp

/**
 * Data class representing a vote for a certain datetime for a meeting.
 *
 * @param dateTime The datetime voted for.
 * @param voters List of user IDs of the users that vote for that datetime.
 */
data class DateTimeVote(
    val dateTime: Timestamp = Timestamp.now(),
    val voters: List<String> = emptyList()
)

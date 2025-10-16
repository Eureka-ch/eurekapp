package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp

/**
 * Data class representing a votes from a user for it's preferred datetime for a meeting.
 *
 * @property userId The ID of the user who voted these [votes].
 * @property votes The datetime votes of the user whose user ID is [userId].
 */
data class MeetingDateTimeVotes(val userId: String = "", val votes: List<Timestamp> = emptyList())

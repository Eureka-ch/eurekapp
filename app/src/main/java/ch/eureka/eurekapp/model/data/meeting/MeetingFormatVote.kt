package ch.eureka.eurekapp.model.data.meeting

/**
 * Data class representing a vote from a user for it's preferred meeting format.
 *
 * @property userId The ID of the user who voted [vote].
 * @property vote The meeting format vote of the user who's user ID is [userId].
 */
data class MeetingFormatVote(
    val userId: String = "",
    val vote: MeetingFormat = MeetingFormat.IN_PERSON
)

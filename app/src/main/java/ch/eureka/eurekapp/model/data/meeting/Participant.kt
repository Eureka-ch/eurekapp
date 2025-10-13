package ch.eureka.eurekapp.model.data.meeting

data class Participant(
    val userId: String = "",
    val role: MeetingRole = MeetingRole.PARTICIPANT // host, participant
)

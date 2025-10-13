package ch.eureka.eurekapp.model.data.meeting

data class Meeting(
    val meetingID: String = "",
    val projectId: String = "",
    val taskId: String? = null,
    val title: String = "",
    val status: MeetingStatus = MeetingStatus.SCHEDULED,
    val attachmentUrls: List<String> = emptyList(),
    val createdBy: String = ""
)

package ch.eureka.eurekapp.model.data.transcription

import com.google.firebase.Timestamp

/**
 * Data class representing an audio transcription for a meeting.
 * 
 */
data class AudioTranscription(
    val transcriptionId: String = "",
    val meetingId: String = "",
    val projectId: String = "",
    val audioDownloadUrl: String = "",
    val transcriptionText: String = "",
    val status: String = "",
    val errorMessage: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)

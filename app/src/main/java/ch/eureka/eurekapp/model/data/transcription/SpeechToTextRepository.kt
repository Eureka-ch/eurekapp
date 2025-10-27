package ch.eureka.eurekapp.model.data.transcription

import kotlinx.coroutines.flow.Flow

interface SpeechToTextRepository {
  /** Transcribe audio from download URL and save to Firestore */
  suspend fun transcribeAudio(
      audioDownloadUrl: String,
      meetingId: String,
      projectId: String
  ): Result<String>

  /** Get all transcriptions for a meeting with real time updates */
  fun getTranscriptionsForMeeting(
      projectId: String,
      meetingId: String
  ): Flow<List<AudioTranscription>>

  /** Get single transcription with real time updates */
  fun getTranscriptionById(
      projectId: String,
      meetingId: String,
      transcriptionId: String
  ): Flow<AudioTranscription?>

  /** Delete transcription */
  suspend fun deleteTranscription(
      projectId: String,
      meetingId: String,
      transcriptionId: String
  ): Result<Unit>
}

package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.chatbot.ChatbotRepository
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.transcription.AudioTranscription
import ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository
import ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for transcript view screen with audio and transcription data.
 *
 * Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5
 */
data class TranscriptUIState(
    val meeting: Meeting? = null,
    val transcript: AudioTranscription? = null,
    val audioUrl: String? = null,
    val transcriptionText: String? = null,
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.PENDING,
    val summary: String? = null,
    val isSummarizing: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
)

/**
 * ViewModel for displaying audio recordings with their transcriptions. Provides real-time updates
 * as transcription status changes.
 */
class TranscriptViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val transcriptId: String,
    private val meetingRepository: MeetingRepository =
        FirestoreRepositoriesProvider.meetingRepository,
    private val speechToTextRepository: SpeechToTextRepository =
        FirestoreRepositoriesProvider.speechToTextRepository,
    private val chatbotRepository: ChatbotRepository = ChatbotRepository(),
) : ViewModel() {

  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _summary = MutableStateFlow<String?>(null)
  private val _isSummarizing = MutableStateFlow(false)

  private fun validateData(meeting: Meeting?, transcript: AudioTranscription?): String? {
    return when {
      meeting == null -> "Meeting not found"
      transcript == null -> "Transcript not found"
      meeting.attachmentUrls.isEmpty() -> "No audio recording found for this meeting"
      transcript.audioDownloadUrl.isBlank() -> "Transcript has invalid audio URL"
      else -> null
    }
  }

  /** UI state with real-time updates for meeting and transcription data. */
  val uiState: StateFlow<TranscriptUIState> =
      combine(
              meetingRepository.getMeetingById(projectId, meetingId),
              speechToTextRepository.getTranscriptionById(projectId, meetingId, transcriptId),
              _summary,
              _isSummarizing,
              _errorMsg) { meeting, transcript, summary, isSummarizing, errorMsg ->
                val validationError = validateData(meeting, transcript)

                TranscriptUIState(
                    meeting = if (validationError == null) meeting else null,
                    transcript = if (validationError == null) transcript else null,
                    audioUrl =
                        if (validationError == null) meeting?.attachmentUrls?.firstOrNull()
                        else null,
                    transcriptionText =
                        if (validationError == null &&
                            transcript?.status == TranscriptionStatus.COMPLETED)
                            transcript.transcriptionText
                        else null,
                    transcriptionStatus = transcript?.status ?: TranscriptionStatus.PENDING,
                    summary = summary,
                    isSummarizing = isSummarizing,
                    errorMsg =
                        errorMsg
                            ?: validationError
                            ?: if (transcript?.status == TranscriptionStatus.FAILED)
                                transcript.errorMessage ?: "Transcription failed"
                            else null,
                    isLoading = false,
                )
              }
          .onStart { emit(TranscriptUIState(isLoading = true)) }
          .catch { e ->
            emit(
                TranscriptUIState(
                    isLoading = false, errorMsg = e.message ?: "Failed to load transcript data"))
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = TranscriptUIState(isLoading = true))

  fun clearErrorMsg() {
    _errorMsg.value = null
  }

  /** Generates an AI summary of the transcript using the chatbot. */
  fun generateSummary() {
    viewModelScope.launch {
      val transcriptText = uiState.value.transcriptionText
      if (transcriptText.isNullOrBlank()) {
        _errorMsg.value = "No transcript available to summarize"
        return@launch
      }

      _isSummarizing.value = true
      _errorMsg.value = null

      try {
        val systemPrompt = "Summarize this meeting transcript in 3 concise bullet points." // Temporary, will later add a better system prompt
        val response = chatbotRepository.sendMessage(systemPrompt, transcriptText)
        _summary.value = response
      } catch (e: Exception) {
        _errorMsg.value = e.message ?: "Failed to generate summary"
        _summary.value = null
      } finally {
        _isSummarizing.value = false
      }
    }
  }
}

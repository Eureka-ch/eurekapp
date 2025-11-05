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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val audioUrl: String? = null,
    val transcriptionText: String? = null,
    val transcriptionStatus: TranscriptionStatus? = null,
    val summary: String? = null,
    val isSummarizing: Boolean = false,
    val isGeneratingTranscript: Boolean = false,
    val hasTranscript: Boolean = false,
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
    private val meetingRepository: MeetingRepository =
        FirestoreRepositoriesProvider.meetingRepository,
    private val speechToTextRepository: SpeechToTextRepository =
        FirestoreRepositoriesProvider.speechToTextRepository,
    private val chatbotRepository: ChatbotRepository = ChatbotRepository(),
) : ViewModel() {

  private val _errorMsg = MutableStateFlow<String?>(null)
  private val _summary = MutableStateFlow<String?>(null)
  private val _isSummarizing = MutableStateFlow(false)
  private val _isGeneratingTranscript = MutableStateFlow(false)

  private fun validateData(meeting: Meeting?): String? {
    return when {
      meeting == null -> "Meeting not found"
      meeting.attachmentUrls.isEmpty() -> "No audio recording found for this meeting"
      else -> null
    }
  }

  /** UI state with real-time updates for meeting and transcription data. */
  val uiState: StateFlow<TranscriptUIState> =
      meetingRepository
          .getMeetingById(projectId, meetingId)
          .flatMapLatest { meeting ->
            // If meeting has transcriptId, observe the transcript, otherwise emit null
            val transcriptFlow =
                if (meeting?.transcriptId != null) {
                  speechToTextRepository.getTranscriptionById(
                      projectId, meetingId, meeting.transcriptId)
                } else {
                  flowOf(null)
                }

            combine(
                flowOf(meeting),
                transcriptFlow,
                _summary,
                _isSummarizing,
                _isGeneratingTranscript,
                _errorMsg) { flows ->
                  val m = flows[0] as? Meeting
                  val transcript = flows[1] as? AudioTranscription
                  val summary = flows[2] as? String
                  val isSummarizing = flows[3] as Boolean
                  val isGeneratingTranscript = flows[4] as Boolean
                  val errorMsg = flows[5] as? String

                  val validationError = validateData(m)

                  TranscriptUIState(
                      meeting = if (validationError == null) m else null,
                      audioUrl =
                          if (validationError == null) m?.attachmentUrls?.firstOrNull() else null,
                      transcriptionText =
                          if (validationError == null &&
                              transcript?.status == TranscriptionStatus.COMPLETED)
                              transcript.transcriptionText
                          else null,
                      transcriptionStatus = transcript?.status,
                      summary = summary,
                      isSummarizing = isSummarizing,
                      isGeneratingTranscript = isGeneratingTranscript,
                      hasTranscript = m?.transcriptId != null,
                      errorMsg =
                          errorMsg
                              ?: validationError
                              ?: if (transcript?.status == TranscriptionStatus.FAILED)
                                  transcript.errorMessage ?: "Transcription failed"
                              else null,
                      isLoading = false,
                  )
                }
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

  /** Generates transcript from the audio URL */
  fun generateTranscript() {
    viewModelScope.launch {
      val meeting = uiState.value.meeting
      val audioUrl = uiState.value.audioUrl

      if (audioUrl.isNullOrBlank()) {
        _errorMsg.value = "No audio recording available"
        return@launch
      }

      _isGeneratingTranscript.value = true
      _errorMsg.value = null

      try {
        val result =
            speechToTextRepository.transcribeAudio(
                audioDownloadUrl = audioUrl,
                meetingId = meetingId,
                projectId = projectId,
                languageCode = "en-US")

        result
            .onSuccess { transcriptId ->
              // Update meeting with transcriptId
              meeting?.let { m ->
                val updatedMeeting = m.copy(transcriptId = transcriptId)
                meetingRepository.updateMeeting(updatedMeeting)
              }
            }
            .onFailure { e -> _errorMsg.value = e.message ?: "Failed to generate transcript" }
      } catch (e: Exception) {
        _errorMsg.value = e.message ?: "Failed to generate transcript"
      } finally {
        _isGeneratingTranscript.value = false
      }
    }
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
        val systemPrompt = "Summarize this meeting transcript in 3 concise bullet points."
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

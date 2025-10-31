/*
 * Note: This file was co-authored by Claude Code.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting detail screen.
 *
 * @property meeting The detailed meeting information, or null if loading or not found.
 * @property participants List of participants in the meeting.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether a data loading operation is in progress.
 * @property deleteSuccess Whether the meeting was successfully deleted.
 */
data class MeetingDetailUIState(
    val meeting: Meeting? = null,
    val participants: List<Participant> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val deleteSuccess: Boolean = false,
)

/**
 * ViewModel for the meeting detail screen.
 *
 * Manages the state and business logic for displaying detailed meeting information, including
 * real-time updates of meeting data and participants list.
 *
 * @property projectId The ID of the project containing the meeting.
 * @property meetingId The ID of the meeting to display.
 * @property repository The repository for meeting data operations.
 */
class MeetingDetailViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = FirestoreRepositoriesProvider.meetingRepository,
) : ViewModel() {

  private val _deleteSuccess = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)

  /**
   * Validates that all required fields of a meeting are valid.
   *
   * @param meeting The meeting to validate, or null if not found.
   * @return Error message if validation fails, null if all fields are valid.
   */
  private fun validateMeeting(meeting: Meeting?): String? {
    if (meeting == null) return "Meeting not found"

    return when {
      meeting.title.isBlank() -> "Meeting has invalid title"
      meeting.meetingID.isBlank() -> "Meeting has invalid ID"
      meeting.format == MeetingFormat.IN_PERSON && meeting.location == null ->
          "In-person meeting must have a location"
      meeting.format == MeetingFormat.VIRTUAL && meeting.link.isNullOrBlank() ->
          "Virtual meeting must have a link"
      else -> null
    }
  }

  /**
   * UI state combining meeting data, participants, and operation states.
   *
   * Follows the project's Flow pattern: declarative state initialization in init block using
   * stateIn() with WhileSubscribed strategy for automatic lifecycle management.
   *
   * Validates all meeting fields before displaying - if any required field is invalid, shows an
   * error message instead of displaying potentially corrupted data.
   */
  val uiState: StateFlow<MeetingDetailUIState> =
      combine(
              repository.getMeetingById(projectId, meetingId),
              repository.getParticipants(projectId, meetingId),
              _deleteSuccess,
              _errorMsg) { meeting, participants, deleteSuccess, errorMsg ->
                val validationError = validateMeeting(meeting)
                MeetingDetailUIState(
                    meeting = if (validationError == null) meeting else null,
                    participants = participants,
                    isLoading = false,
                    errorMsg = errorMsg ?: validationError,
                    deleteSuccess = deleteSuccess)
              }
          .onStart { emit(MeetingDetailUIState(isLoading = true)) }
          .catch { e ->
            emit(
                MeetingDetailUIState(
                    isLoading = false, errorMsg = e.message ?: "Failed to load meeting details"))
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = MeetingDetailUIState(isLoading = true))

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _errorMsg.value = null
  }

  /**
   * Delete the meeting from the repository.
   *
   * @param projectId The ID of the project containing the meeting.
   * @param meetingId The ID of the meeting to delete.
   */
  fun deleteMeeting(projectId: String, meetingId: String) {
    viewModelScope.launch {
      repository
          .deleteMeeting(projectId, meetingId)
          .onSuccess { _deleteSuccess.value = true }
          .onFailure { e -> _errorMsg.value = e.message ?: "Failed to delete meeting" }
    }
  }
}

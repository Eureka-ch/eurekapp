/*
 * Note: This file was co-authored by Claude Code.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
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
 * @property repository The repository for meeting data operations.
 */
class MeetingDetailViewModel(
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingDetailUIState())
  val uiState: StateFlow<MeetingDetailUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Load the meeting details and participants with real-time updates.
   *
   * This method combines two Flows (meeting data and participants) to provide real-time updates
   * when either the meeting or its participants change.
   *
   * @param projectId The ID of the project containing the meeting.
   * @param meetingId The ID of the meeting to load.
   */
  fun loadMeetingDetails(projectId: String, meetingId: String) {
    viewModelScope.launch {
      combine(
              repository.getMeetingById(projectId, meetingId),
              repository.getParticipants(projectId, meetingId)) { meeting, participants ->
                MeetingDetailUIState(
                    meeting = meeting,
                    participants = participants,
                    isLoading = false,
                    errorMsg = if (meeting == null) "Meeting not found" else null)
              }
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e ->
            _uiState.update {
              it.copy(isLoading = false, errorMsg = e.message ?: "Failed to load meeting details")
            }
          }
          .collect { newState ->
            _uiState.update { currentState ->
              newState.copy(deleteSuccess = currentState.deleteSuccess)
            }
          }
    }
  }

  /**
   * Delete the meeting from the repository.
   *
   * @param projectId The ID of the project containing the meeting.
   * @param meetingId The ID of the meeting to delete.
   */
  fun deleteMeeting(projectId: String, meetingId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      repository
          .deleteMeeting(projectId, meetingId)
          .onSuccess { _uiState.update { it.copy(isLoading = false, deleteSuccess = true) } }
          .onFailure { e ->
            _uiState.update {
              it.copy(isLoading = false, errorMsg = e.message ?: "Failed to delete meeting")
            }
          }
    }
  }
}

/*
 * Note: This file was co-authored by Claude Code.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
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
   * UI state combining meeting data, participants, and operation states.
   *
   * Follows the project's Flow pattern: declarative state initialization in init block using
   * stateIn() with WhileSubscribed strategy for automatic lifecycle management.
   */
  val uiState: StateFlow<MeetingDetailUIState> =
      combine(
              repository.getMeetingById(projectId, meetingId),
              repository.getParticipants(projectId, meetingId),
              _deleteSuccess,
              _errorMsg) { meeting, participants, deleteSuccess, errorMsg ->
                MeetingDetailUIState(
                    meeting = meeting,
                    participants = participants,
                    isLoading = false,
                    errorMsg = errorMsg ?: if (meeting == null) "Meeting not found" else null,
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

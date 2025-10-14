package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting screen.
 *
 * @property currentMeetings All the meetings that are planned or in progress.
 * @property pastMeetings All meetings that are finished.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether an authentication operation is in progress.
 */
data class MeetingUIState(
    val currentMeetings: List<Meeting> = emptyList(),
    val pastMeetings: List<Meeting> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
)

class MeetingViewModel(
    private val projectId: String,
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingUIState())
  val uiState: StateFlow<MeetingUIState> = _uiState

  init {
    loadMeetings()
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /** Load all the meetings from the database for the project ID [projectId] into the UI state. */
  fun loadMeetings() {
    viewModelScope.launch {
      repository
          .getMeetingsInProject(projectId)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { meetings ->
            _uiState.update {
              it.copy(
                  isLoading = false,
                  currentMeetings = meetings.filterNot { meeting -> meeting.ended },
                  pastMeetings = meetings.filter { meeting -> meeting.ended })
            }
          }
    }
  }
}

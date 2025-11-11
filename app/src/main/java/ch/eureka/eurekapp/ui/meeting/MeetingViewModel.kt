/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting screen.
 *
 * @property upcomingMeetings All the meetings that are planned or in progress.
 * @property pastMeetings All meetings that are finished.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether an authentication operation is in progress.
 */
data class MeetingUIState(
    val upcomingMeetings: List<Meeting> = emptyList(),
    val pastMeetings: List<Meeting> = emptyList(),
    val selectedTab: MeetingTab = MeetingTab.UPCOMING,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
)

/**
 * Enum that represents a meeting tab in the UI.
 *
 * @property name The name of that tag.
 */
enum class MeetingTab {
  UPCOMING,
  PAST
}

class MeetingViewModel(
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingUIState())
  val uiState: StateFlow<MeetingUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Load all the meetings from the database for the project ID [projectId] into the UI state.
   *
   * @param projectId The ID of the project on which to requests the meetings.
   */
  fun loadMeetings(projectId: String) {
    viewModelScope.launch {
      repository
          .getMeetingsInProject(projectId)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { meetings ->
            _uiState.update {
              it.copy(
                  isLoading = false,
                  upcomingMeetings =
                      meetings
                          .filterNot { meeting -> meeting.status == MeetingStatus.COMPLETED }
                          .sortedBy { m ->
                            m.datetime
                                ?: m.meetingProposals
                                    .filter { mpv -> mpv.votes.isNotEmpty() }
                                    .minOfOrNull { e ->
                                      e.dateTime
                                    } // will never give null because there is always at least a
                            // meeting that has a non-zero votes
                          }
                          .reversed(),
                  pastMeetings =
                      meetings
                          .filter { meeting -> meeting.status == MeetingStatus.COMPLETED }
                          .sortedBy { m ->
                            m.datetime
                                ?: m.meetingProposals
                                    .filter { mpv -> mpv.votes.isNotEmpty() }
                                    .minOfOrNull { e ->
                                      e.dateTime
                                    } // will never give null because there is always at least a
                            // meeting proposal that has a non-zero votes
                          }
                          .reversed())
            }
          }
    }
  }

  /**
   * Sets the current selected tab of the UI state.
   *
   * @param tab The new tab selected.
   */
  fun selectTab(tab: MeetingTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }
}

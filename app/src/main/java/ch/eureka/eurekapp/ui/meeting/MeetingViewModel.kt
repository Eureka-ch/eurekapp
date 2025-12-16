/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/

// Ports of this code were generated with the help of Grok.
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting screen.
 *
 * @property upcomingMeetings All the meetings that are planned or in progress.
 * @property pastMeetings All meetings that are finished.
 * @property selectedTab The currently selected tab.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether an authentication operation is in progress.
 * @property isConnected Whether the device is connected to the internet.
 */
data class MeetingUIState(
    val upcomingMeetings: List<Meeting> = emptyList(),
    val pastMeetings: List<Meeting> = emptyList(),
    val selectedTab: MeetingTab = MeetingTab.UPCOMING,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val isConnected: Boolean = true
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

/**
 * View model for the meeting screen.
 *
 * @property repository The repository from which to pull the meetings from.
 * @property getCurrentUserId Function to get current user ID.
 * @property connectivityObserver The connectivity observer.
 */
class MeetingViewModel(
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val connectivityObserver: ConnectivityObserver =
        ConnectivityObserverProvider.connectivityObserver,
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingUIState())
  val uiState: StateFlow<MeetingUIState> = _uiState
  val userId = getCurrentUserId()

  // Add connectivity observer
  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Clears the error message in the UI state.
   *
   * @param msg The message to set
   */
  fun setErrorMsg(msg: String) {
    _uiState.update { it.copy(errorMsg = msg) }
  }

  /** Load all the meetings from the database where the current user is in. */
  fun loadMeetings() {
    viewModelScope.launch {
      repository
          .getMeetingsForCurrentUser(skipCache = true)
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
   * Closes the votes for a given meeting.
   *
   * @param meeting The meeting to close the votes for.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun closeVotesForMeeting(meeting: Meeting, isConnected: Boolean = true) {
    if (isConnected) {
      if (!canCloseVotes(meeting)) {
        return
      }

      val maxVoteCount = meeting.meetingProposals.maxOfOrNull { it.votes.size } ?: 0

      if (maxVoteCount == 0) {
        setErrorMsg("No votes have been cast for any proposal.")
        return
      }

      val mostPopularProposals = meeting.meetingProposals.filter { it.votes.size == maxVoteCount }

      val winningProposal =
          mostPopularProposals.maxByOrNull { proposal ->
            val formatCounts =
                proposal.votes.flatMap { it.formatPreferences }.groupingBy { it }.eachCount()

            formatCounts.values.maxOrNull() ?: 0
          }

      if (winningProposal == null) {
        setErrorMsg("Could not determine a winning proposal")
        return
      }

      val finalFormatCounts =
          winningProposal.votes.flatMap { it.formatPreferences }.groupingBy { it }.eachCount()

      val winningFormat = finalFormatCounts.maxByOrNull { it.value }?.key

      if (winningFormat == null) {
        setErrorMsg("Could not determine a winning format from the votes")
        return
      }

      if (winningFormat == MeetingFormat.IN_PERSON && meeting.location == null) {
        setErrorMsg("Cannot close votes, in-person meeting has no location.")
        return
      }

      val updatedMeeting =
          meeting.copy(
              status = MeetingStatus.SCHEDULED,
              datetime = winningProposal.dateTime,
              format = winningFormat,
              link =
                  if (winningFormat == MeetingFormat.VIRTUAL) "https://meet.google.com/1234"
                  else null, // change later
              meetingProposals = emptyList())

      viewModelScope.launch {
        repository
            .updateMeeting(meeting = updatedMeeting)
            .onFailure { setErrorMsg("Meeting votes could not be closed.") }
            .onSuccess { loadMeetings() }
      }
    }
  }

  private fun canCloseVotes(meeting: Meeting): Boolean {
    if (userId == null) {
      return false
    }

    if (meeting.createdBy != userId) {
      setErrorMsg("Cannot close votes since you are not the creator of the meeting.")
      return false
    }

    return true
  }

  /**
   * Sets the current selected tab of the UI state.
   *
   * @param tab The new tab selected.
   */
  fun selectTab(tab: MeetingTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }

  // Add method to update connectivity status
  init {
    viewModelScope.launch {
      _isConnected.collect { isConnected -> _uiState.update { it.copy(isConnected = isConnected) } }
    }
  }
}

/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.DateTimeVote
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to vote for datetime of meetings
 *
 * @property dateTimeVotes The datetime votes for the meeting.
 * @property meeting The meeting to fore datetime for.
 * @property votesSaved True if the votes where correctly saved to the database, false otherwise.
 * @property errorMsg Error message to display.
 * @property isLoading Whether an authentication operation is in progress.
 */
data class DateTimeVoteUIState(
    val dateTimeVotes: List<DateTimeVote> = emptyList(),
    val meeting: Meeting = Meeting(),
    val votesSaved: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
)

/**
 * View model for the screen to vote for datetime.
 *
 * @property projectId The ID of the project in which the meeting to vote on is.
 * @property meetingId The ID of the meeting to vote on.
 * @property repository The repository to in which the meeting to vote on is.
 */
class DateTimeVoteViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val _uiState = MutableStateFlow(DateTimeVoteUIState())
  val uiState: StateFlow<DateTimeVoteUIState> = _uiState
  val userId = getCurrentUserId()

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

  /** Mark the datetime votes as saved in the database. */
  fun setVotesSaved() {
    _uiState.update { it.copy(votesSaved = true) }
  }

  /**
   * Vote for a given datetime
   *
   * @param dateTimeVote The datetime to vote for.
   */
  fun voteForDateTime(dateTimeVote: DateTimeVote) {

    val index = uiState.value.dateTimeVotes.indexOf(dateTimeVote)

    if (index < 0) {
      setErrorMsg("Datetime to vote for does not exists.")
      return
    }

    if (userId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val newVoters = _uiState.value.dateTimeVotes[index].voters.toMutableList()
    newVoters.add(userId)

    val newDateTimeVotes =
        _uiState.value.dateTimeVotes
            .toMutableList()
            .apply { this[index] = this[index].copy(voters = newVoters.toList()) }
            .toList()
    _uiState.update { it.copy(dateTimeVotes = newDateTimeVotes) }
  }

  /**
   * Retract vote for a given datetime.
   *
   * @param dateTimeVote The datetime to retract the vote for.
   */
  fun retractVoteForDateTime(dateTimeVote: DateTimeVote) {
    val index = uiState.value.dateTimeVotes.indexOf(dateTimeVote)

    if (index < 0) {
      setErrorMsg("Datetime to vote for does not exists.")
      return
    }

    if (userId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val newVoters = _uiState.value.dateTimeVotes[index].voters.toMutableList()

    if (!newVoters.contains(userId)) {
      setErrorMsg("Cannot retract vote since you did not vote in the first place")
      return
    }

    newVoters.remove(userId)

    val newDateTimeVotes =
        _uiState.value.dateTimeVotes
            .toMutableList()
            .apply { this[index] = this[index].copy(voters = newVoters.toList()) }
            .toList()
    _uiState.update { it.copy(dateTimeVotes = newDateTimeVotes) }
  }

  /** Load datetime votes of meeting with ID [meetingId] from the database. */
  fun loadDateTimeVotes() {
    viewModelScope.launch {
      repository
          .getMeetingById(projectId, meetingId)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { meeting ->
            meeting?.let { m ->
              if (m.dateTimeVotes.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Meeting is null.") }
              } else {
                _uiState.update {
                  it.copy(
                      isLoading = false,
                      dateTimeVotes = m.dateTimeVotes,
                      meeting = meeting,
                      votesSaved = false)
                }
              }
            } ?: _uiState.update { it.copy(isLoading = false, errorMsg = "Meeting is null.") }
          }
    }
  }

  /** Update the meeting with the new votes on the database. */
  fun confirmDateTimeVotes() {
    viewModelScope.launch {
      repository
          .updateMeeting(
              meeting = uiState.value.meeting.copy(dateTimeVotes = uiState.value.dateTimeVotes))
          .onFailure { setErrorMsg("Meeting could not be updated.") }
          .onSuccess { setVotesSaved() }
    }
  }
}

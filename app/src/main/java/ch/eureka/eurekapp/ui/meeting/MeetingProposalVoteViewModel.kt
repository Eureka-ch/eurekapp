/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to vote for meeting proposals of a meeting.
 *
 * @property meetingProposals The meeting proposals for the meeting.
 * @property meeting The meeting to which the meeting proposal is died to.
 * @property votesSaved True if the votes where correctly saved to the database, false otherwise.
 * @property errorMsg Error message to display.
 * @property isLoading Whether an authentication operation is in progress.
 */
data class MeetingProposalVoteUIState(
    val meetingProposals: List<MeetingProposal> = emptyList(),
    val meeting: Meeting = Meeting(),
    val votesSaved: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
)

/**
 * View model for the screen to vote for meeting proposal.
 *
 * @property projectId The ID of the project in which the meeting to vote on is.
 * @property meetingId The ID of the meeting to vote on.
 * @property repository The repository to in which the meeting to vote on is.
 */
class MeetingProposalVoteViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingProposalVoteUIState())
  val uiState: StateFlow<MeetingProposalVoteUIState> = _uiState
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
   * Vote for a given meeting proposal.
   *
   * @param meetingProposal The meeting proposal to vote for.
   * @param meetingProposalVote The vote for that meeting proposal to add.
   */
  fun voteForMeetingProposal(
      meetingProposal: MeetingProposal,
      meetingProposalVote: MeetingProposalVote
  ) {

    val index = uiState.value.meetingProposals.indexOf(meetingProposal)

    if (index < 0) {
      setErrorMsg("Meeting proposal to vote for does not exists.")
      return
    }

    if (userId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val newVotes = _uiState.value.meetingProposals[index].votes.toMutableList()
    newVotes.add(meetingProposalVote)

    val newMeetingProposals =
        _uiState.value.meetingProposals
            .toMutableList()
            .apply { this[index] = this[index].copy(votes = newVotes.toList()) }
            .toList()
    _uiState.update { it.copy(meetingProposals = newMeetingProposals) }
  }

  /**
   * Retract vote for a given meeting proposal.
   *
   * @param meetingProposal The meeting proposal to retract the vote for.
   */
  fun retractVoteForMeetingProposal(
      meetingProposal: MeetingProposal,
  ) {
    val index = uiState.value.meetingProposals.indexOf(meetingProposal)

    if (index < 0) {
      setErrorMsg("Meeting proposal to retract vote for does not exists.")
      return
    }

    if (userId == null) {
      setErrorMsg("Not logged in")
      return
    }

    if (!_uiState.value.meetingProposals[index].votes.map { it.userId }.contains(userId)) {
      setErrorMsg("Cannot retract vote since you did not vote in the first place")
      return
    }

    val newVotes = _uiState.value.meetingProposals[index].votes.filter { it.userId != userId }

    val newMeetingProposals =
        _uiState.value.meetingProposals
            .toMutableList()
            .apply { this[index] = this[index].copy(votes = newVotes) }
            .toList()
    _uiState.update { it.copy(meetingProposals = newMeetingProposals) }
  }

  /** Load meeting proposals of meeting with ID [meetingId] from the database. */
  fun loadMeetingProposals() {
    viewModelScope.launch {
      repository
          .getMeetingById(projectId, meetingId)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { meeting ->
            meeting?.let { m ->
              if (m.meetingProposals.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Meeting is null.") }
              } else {
                _uiState.update {
                  it.copy(
                      isLoading = false,
                      meetingProposals = m.meetingProposals,
                      meeting = meeting,
                      votesSaved = false)
                }
              }
            } ?: _uiState.update { it.copy(isLoading = false, errorMsg = "Meeting is null.") }
          }
    }
  }

  /** Update the meeting with the new votes on the database. */
  fun confirmMeetingProposalsVotes() {
    viewModelScope.launch {
      repository
          .updateMeeting(
              meeting =
                  uiState.value.meeting.copy(meetingProposals = uiState.value.meetingProposals))
          .onFailure { setErrorMsg("Meeting could not be updated.") }
          .onSuccess { setVotesSaved() }
    }
  }
}

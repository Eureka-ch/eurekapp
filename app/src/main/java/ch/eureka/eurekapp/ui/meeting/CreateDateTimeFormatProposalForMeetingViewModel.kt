/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
Portions of the code in this file were written with the help of chatGPT.
 */
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to create meeting proposals.
 *
 * @property meeting The meeting to which the meeting datetime/format proposal is died to.
 * @property date The date of the meeting proposal to be created.
 * @property time The start time of the meeting proposal to be created.
 * @property format The format of the meeting proposal.
 * @property saved Marker set to true if the meeting datetime/format proposal waa successfully
 *   saved, false otherwise.
 * @property hasTouchedDate Marker set to true if the user has already clicked on the date field,
 *   false otherwise.
 * @property hasTouchedTime Marker set to true if the user has already clicked on the time field,
 *   false otherwise.
 * @property errorMsg Error message to display.
 * @property isLoading Whether an authentication operation is in progress.
 */
data class CreateDateTimeFormatProposalForMeetingUIState(
    val meeting: Meeting = Meeting(),
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val format: MeetingFormat = MeetingFormat.IN_PERSON,
    val saved: Boolean = false,
    val hasTouchedDate: Boolean = false,
    val hasTouchedTime: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
) {
  /** States whether the UI is in a state where the meeting proposal can be saved. */
  val isValid: Boolean
    get() = LocalDateTime.of(date, time).isAfter(LocalDateTime.now())
}

/**
 * ViewModel for the screen that enables users to create a new meeting proposal that proposes a new
 * datetime and format for a meeting.
 *
 * @property projectId The project ID in which the meeting to add a proposal resides.
 * @property meetingId The meeting ID of the meeting to add the datetime/format proposal to.
 * @property repository The repository in which the meeting to add a proposal resides.
 * @property getCurrentUserId Function to get current user ID.
 */
class CreateDateTimeFormatProposalForMeetingViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateDateTimeFormatProposalForMeetingUIState())
  val uiState: StateFlow<CreateDateTimeFormatProposalForMeetingUIState> = _uiState

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

  /**
   * Set a proposed initial date for the meeting to be created.
   *
   * @param date The date of the meeting proposal to be created.
   */
  fun setDate(date: LocalDate) {
    _uiState.update { it.copy(date = date) }
  }

  /**
   * Set a proposed initial time for the meeting to be created
   *
   * @param time The time of the meeting proposal to be created.
   */
  fun setTime(time: LocalTime) {
    _uiState.update { it.copy(time = time) }
  }

  /**
   * Set the format of the meeting to be created.
   *
   * @param format The format of the meeting to be created.
   */
  fun setFormat(format: MeetingFormat) {
    _uiState.update { it.copy(format = format) }
  }

  /** Mark the the meeting datetime/format proposal as saved in the database. */
  fun setSaved() {
    _uiState.update { it.copy(saved = true) }
  }

  /** Mark the date field as touched. */
  fun touchDate() {
    _uiState.update { it.copy(hasTouchedDate = true) }
  }

  /** Mark the time field as touched. */
  fun touchTime() {
    _uiState.update { it.copy(hasTouchedTime = true) }
  }

  /** Load meeting with ID [meetingId] from the database. */
  fun loadMeeting() {
    viewModelScope.launch {
      repository
          .getMeetingById(projectId, meetingId)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { meeting ->
            meeting?.let { m ->
              if (m.meetingProposals.isEmpty()) {
                _uiState.update {
                  it.copy(
                      isLoading = false,
                      errorMsg =
                          "Datetime format proposal must be created on already creating meeting proposal")
                }
              } else {
                _uiState.update { it.copy(isLoading = false, meeting = meeting, saved = false) }
              }
            } ?: _uiState.update { it.copy(isLoading = false, errorMsg = "Meeting is null.") }
          }
    }
  }

  /** Update the meeting with the new meeting proposal. */
  fun createDateTimeFormatProposalForMeeting() {

    if (!uiState.value.isValid) {
      setErrorMsg("Meeting should be scheduled in the future.")
      return
    }

    val creatorId = getCurrentUserId()

    if (creatorId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val timeInstant =
        LocalDateTime.of(uiState.value.date, uiState.value.time)
            .atZone(ZoneId.systemDefault())
            .toInstant()

    val newMeetingProposals = uiState.value.meeting.meetingProposals.toMutableList()
    newMeetingProposals.add(
        MeetingProposal(
            Timestamp(timeInstant),
            listOf(MeetingProposalVote(creatorId, listOf(uiState.value.format)))))

    viewModelScope.launch {
      repository
          .updateMeeting(
              meeting = uiState.value.meeting.copy(meetingProposals = newMeetingProposals))
          .onFailure { setErrorMsg("Datetime/format meeting proposal could not be created.") }
          .onSuccess { setSaved() }
    }
  }
}

/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
Portions of the code in this file were written with the help of chatGPT.
 */
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to create meetings.
 *
 * @param title The title of the meeting to be created.
 * @param date The date of the time slot for the meeting to be created.
 * @param time The start time of the time slot for the meeting to be created.
 * @param duration The duration of the meeting.
 * @param format The format of the meeting.
 * @param meetingSaved Marker set to true if the meeting waa successfully saved, false otherwise.
 * @param hasTouchedTitle Marker set to true if the user has already clicked on the title field,
 *   false otherwise.
 * @param errorMsg Error message to display.
 */
data class CreateMeetingUIState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val duration: Int = 0,
    val format: MeetingFormat = MeetingFormat.IN_PERSON,
    val meetingSaved: Boolean = false,
    val hasTouchedTitle: Boolean = false,
    val hasTouchedDate: Boolean = false,
    val hasTouchedTime: Boolean = false,
    val errorMsg: String? = null
) {
  /** States whether the UI is in a state where the meeting can be saved. */
  val isValid: Boolean
    get() =
        title.isNotBlank() &&
            duration >= 5 &&
            LocalDateTime.of(date, time).isAfter(LocalDateTime.now())
}

/**
 * View model for the screen to create meetings.
 *
 * @param repository The repository to upload the meeting to.
 * @param getCurrentUserId Function to get current user ID.
 */
class CreateMeetingViewModel(
    private val repository: MeetingRepository = FirestoreMeetingRepository(),
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateMeetingUIState())
  val uiState: StateFlow<CreateMeetingUIState> = _uiState

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
   * Set the title of the meeting proposal to be created.
   *
   * @param title The title of the meeting proposal to be created.
   */
  fun setTitle(title: String) {
    _uiState.update { it.copy(title = title) }
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
   * Set duration fo the meeting to be created.
   *
   * @param duration The duration of the meeting to be created.
   */
  fun setDuration(duration: Int) {
    _uiState.update { it.copy(duration = duration) }
  }

  /**
   * Set the format of the meeting to be created.
   *
   * @param format The format of the meeting to be created.
   */
  fun setFormat(format: MeetingFormat) {
    _uiState.update { it.copy(format = format) }
  }

  /** Mark the the meeting proposal as saved in the database. */
  fun setMeetingSaved() {
    _uiState.update { it.copy(meetingSaved = true) }
  }

  /** Mark the title field as touched. */
  fun touchTitle() {
    _uiState.update { it.copy(hasTouchedTitle = true) }
  }

  /** Mark the date field as touched. */
  fun touchDate() {
    _uiState.update { it.copy(hasTouchedDate = true) }
  }

  /** Mark the time field as touched. */
  fun touchTime() {
    _uiState.update { it.copy(hasTouchedTime = true) }
  }

  /**
   * Create and upload a meeting proposal to the database for the project ID [projectId].
   *
   * @param projectId The ID of the project to which the meeting is created for.
   */
  fun createMeeting(projectId: String) {
    if (!uiState.value.isValid) {
      setErrorMsg("At least one field is not set")
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

    val meeting =
        Meeting(
            meetingID = IdGenerator.generateMeetingId(),
            projectId = projectId,
            title = uiState.value.title,
            status = MeetingStatus.OPEN_TO_VOTES,
            duration = uiState.value.duration,
            meetingProposals =
                listOf(
                    MeetingProposal(
                        Timestamp(timeInstant),
                        listOf(MeetingProposalVote(creatorId, listOf(uiState.value.format))))),
            createdBy = creatorId)

    viewModelScope.launch {
      repository
          .createMeeting(meeting = meeting, creatorId = creatorId, creatorRole = MeetingRole.HOST)
          .onFailure { setErrorMsg("Meeting could not be created.") }
          .onSuccess { setMeetingSaved() }
    }
  }
}

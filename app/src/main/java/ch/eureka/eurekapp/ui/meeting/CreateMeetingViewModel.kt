/*
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
 */
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.TimeSlot
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
 * @param startTime The start time of the time slot for the meeting to be created.
 * @param endTime The start time of the time slot for the meeting to be created.
 * @param meetingSaved Marker set to true if the meeting waa successfully saved, false otherwise.
 * @param hasTouchedTitle Marker set to true if the user has already clicked on the title field,
 *   false otherwise.
 * @param hasTouchedStartTime Marker set to true if the user has already clicked on the start time
 *   field, false otherwise.
 * @param hasTouchedEndTime Marker set to true if the user has already clicked on the start time
 *   field, false otherwise.
 * @param errorMsg Error message to display.
 */
data class CreateMeetingUIState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now(),
    val endTime: LocalTime = LocalTime.now(),
    val meetingSaved: Boolean = false,
    val hasTouchedTitle: Boolean = false,
    val hasTouchedStartTime: Boolean = false,
    val hasTouchedEndTime: Boolean = false,
    val errorMsg: String? = null
) {
  /** States whether the UI is in a state where the meeting can be saved. */
  val isValid: Boolean
    get() = title.isNotBlank() && startTime.isBefore(endTime)
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
   * Set the date of the meeting proposal to be created.
   *
   * @param date The date of the meeting proposal to be created.
   */
  fun setDate(date: LocalDate) {
    _uiState.update { it.copy(date = date) }
  }

  /**
   * Set the start time for the time slot of the meeting proposal to be created.
   *
   * @param startTime The start time of the timeslot of the meeting proposal to be created.
   */
  fun setStartTime(startTime: LocalTime) {
    _uiState.update { it.copy(startTime = startTime) }
  }

  /**
   * Set the end time for the time slot of the meeting proposal to be created.
   *
   * @param endTime The end time of the timeslot of the meeting proposal to be created.
   */
  fun setEndTime(endTime: LocalTime) {
    _uiState.update { it.copy(endTime = endTime) }
  }

  /** Mark the the meeting proposal as saved in the database. */
  fun setMeetingSaved() {
    _uiState.update { it.copy(meetingSaved = true) }
  }

  /** Mark the title field as touched. */
  fun touchTitle() {
    _uiState.update { it.copy(hasTouchedTitle = true) }
  }

  /** Mark the start time field as touched. */
  fun touchStartTime() {
    _uiState.update { it.copy(hasTouchedStartTime = true) }
  }

  /** Mark the end time field as touched. */
  fun touchEndTime() {
    _uiState.update { it.copy(hasTouchedEndTime = true) }
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

    // These two lines where written with the help of chatGPT
    val startTimeInstant =
        LocalDateTime.of(uiState.value.date, uiState.value.startTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    val endTimeInstant =
        LocalDateTime.of(uiState.value.date, uiState.value.endTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()

    val meeting =
        Meeting(
            meetingID = IdGenerator.generateMeetingId(),
            projectId = projectId,
            title = uiState.value.title,
            status = MeetingStatus.OPEN_TO_VOTES,
            timeSlot =
                TimeSlot(
                    startTime = Timestamp(startTimeInstant.epochSecond, startTimeInstant.nano),
                    endTime = Timestamp(endTimeInstant.epochSecond, endTimeInstant.nano)),
            createdBy = creatorId)

    viewModelScope.launch {
      repository
          .createMeeting(meeting = meeting, creatorId = creatorId, creatorRole = MeetingRole.HOST)
          .onFailure { setErrorMsg("Meeting could not be created.") }
          .onSuccess { setMeetingSaved() }
    }
  }
}

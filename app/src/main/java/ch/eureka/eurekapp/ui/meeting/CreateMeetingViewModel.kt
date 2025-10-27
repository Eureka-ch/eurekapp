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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to create meetings.
 *
 * @param title The title of the meeting to be created.
 * @param startTime The start time of the time slot for the meeting to be created.
 * @param endTime The start time of the time slot for the meeting to be created.
 * @param invalidTitleMsg The message to display when [title] is invalid.
 * @param invalidStartTimeMsg The message to display when [startTime] is invalid.
 * @param invalidEndTimeMsg The message to display when [endTime] is invalid.
 * @param meetingSaved Marker set to true if the meeting waa successfully saved, false otherwise.
 * @param errorMsg Error message to display.
 */
data class CreateMeetingUIState(
    val title: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val invalidTitleMsg: String? = null,
    val invalidStartTimeMsg: String? = null,
    val invalidEndTimeMsg: String? = null,
    val meetingSaved: Boolean = false,
    val errorMsg: String? = null
) {
  /** Sates whether the UI is in a state where the meeting can be saved. */
  val isValid: Boolean
    get() = invalidTitleMsg == null && invalidStartTimeMsg == null && invalidEndTimeMsg == null
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
   * Set the start time for the time slot of the meeting proposal to be created.
   *
   * @param startTime The start time of the timeslot of the meeting proposal to be created.
   */
  fun setStartTime(startTime: Timestamp) {
    _uiState.update { it.copy(startTime = startTime) }
  }

  /**
   * Set the end time for the time slot of the meeting proposal to be created.
   *
   * @param endTime The end time of the timeslot of the meeting proposal to be created.
   */
  fun setEndTime(endTime: Timestamp) {
    _uiState.update { it.copy(endTime = endTime) }
  }

  /** Mark the the meeting proposal as saved in the database. */
  fun setMeetingSaved() {
    _uiState.update { it.copy(meetingSaved = true) }
  }

  /**
   * Create and upload a meeting proposal to the database for the project ID [projectId].
   *
   * @param projectId The ID of the project to which the meeting is created for.
   */
  fun createMeeting(projectId: String) {
    val state = uiState.value
    if (state.isValid) {
      setErrorMsg("At least one field is not set")
      return
    }

    val creatorId = getCurrentUserId()

    if (creatorId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val meeting =
        Meeting(
            meetingID = IdGenerator.generateMeetingId(),
            projectId = projectId,
            title = uiState.value.title,
            status = MeetingStatus.OPEN_TO_VOTES,
            timeSlot =
                TimeSlot(startTime = uiState.value.startTime, endTime = uiState.value.endTime),
            createdBy = creatorId)

    viewModelScope.launch {
      repository
          .createMeeting(meeting = meeting, creatorId = creatorId, creatorRole = MeetingRole.HOST)
          .onFailure { setErrorMsg("Meeting could not be created.") }
          .onSuccess { setMeetingSaved() }
    }
  }
}

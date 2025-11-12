/*
Note: This file was co-authored by Claude Code.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Participant
import com.google.firebase.Timestamp
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
 * @property isEditMode Whether the screen is in edit mode.
 * @property editTitle The title being edited.
 * @property editDateTime The date/time being edited.
 * @property editDuration The duration being edited.
 * @property updateSuccess Whether the meeting was successfully updated.
 * @property isSaving Whether a save operation is in progress.
 */
data class MeetingDetailUIState(
    val meeting: Meeting? = null,
    val participants: List<Participant> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val deleteSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val editTitle: String = "",
    val editDateTime: Timestamp? = null,
    val editDuration: Int = 0,
    val updateSuccess: Boolean = false,
    val isSaving: Boolean = false,
    val hasTouchedTitle: Boolean = false,
    val hasTouchedDateTime: Boolean = false,
    val hasTouchedDuration: Boolean = false,
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
  private val _isEditMode = MutableStateFlow(false)
  private val _editTitle = MutableStateFlow("")
  private val _editDateTime = MutableStateFlow<Timestamp?>(null)
  private val _editDuration = MutableStateFlow(30)
  private val _updateSuccess = MutableStateFlow(false)
  private val _isSaving = MutableStateFlow(false)
  private val _hasTouchedTitle = MutableStateFlow(false)
  private val _hasTouchedDateTime = MutableStateFlow(false)
  private val _hasTouchedDuration = MutableStateFlow(false)

  /**
   * Validates that all required fields of a meeting are valid.
   *
   * @param meeting The meeting to validate, or null if not found.
   * @return Error message if validation fails, null if all fields are valid.
   */
  private fun validateMeeting(meeting: Meeting?): String? {
    if (meeting == null) return "Meeting not found"

    return when {
      meeting.title.isBlank() -> "Meeting has invalid title"
      meeting.meetingID.isBlank() -> "Meeting has invalid ID"
      meeting.format == MeetingFormat.IN_PERSON && meeting.location == null ->
          "In-person meeting must have a location"
      meeting.format == MeetingFormat.VIRTUAL && meeting.link.isNullOrBlank() ->
          "Virtual meeting must have a link"
      else -> null
    }
  }

  /**
   * UI state combining meeting data, participants, and operation states.
   *
   * Follows the project's Flow pattern: declarative state initialization in init block using
   * stateIn() with WhileSubscribed strategy for automatic lifecycle management.
   *
   * Validates all meeting fields before displaying - if any required field is invalid, shows an
   * error message instead of displaying potentially corrupted data.
   */
  val uiState: StateFlow<MeetingDetailUIState> =
      combine(
              repository.getMeetingById(projectId, meetingId),
              repository.getParticipants(projectId, meetingId),
              combine(_deleteSuccess, _errorMsg, _isEditMode, _editTitle, _editDateTime) {
                  deleteSuccess,
                  errorMsg,
                  isEditMode,
                  editTitle,
                  editDateTime ->
                EditState(deleteSuccess, errorMsg, isEditMode, editTitle, editDateTime)
              },
              combine(_editDuration, _updateSuccess, _isSaving) { duration, success, saving ->
                SaveState(duration, success, saving)
              },
              combine(_hasTouchedTitle, _hasTouchedDateTime, _hasTouchedDuration) {
                  title,
                  dateTime,
                  duration ->
                TouchState(title, dateTime, duration)
              }) { meeting, participants, editState, saveState, touchState ->
                val validationError = validateMeeting(meeting)
                MeetingDetailUIState(
                    meeting = if (validationError == null) meeting else null,
                    participants = participants,
                    isLoading = false,
                    errorMsg = editState.errorMsg ?: validationError,
                    deleteSuccess = editState.deleteSuccess,
                    isEditMode = editState.isEditMode,
                    editTitle = editState.editTitle,
                    editDateTime = editState.editDateTime,
                    editDuration = saveState.editDuration,
                    updateSuccess = saveState.updateSuccess,
                    isSaving = saveState.isSaving,
                    hasTouchedTitle = touchState.hasTouchedTitle,
                    hasTouchedDateTime = touchState.hasTouchedDateTime,
                    hasTouchedDuration = touchState.hasTouchedDuration)
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

  private data class EditState(
      val deleteSuccess: Boolean,
      val errorMsg: String?,
      val isEditMode: Boolean,
      val editTitle: String,
      val editDateTime: Timestamp?
  )

  private data class SaveState(
      val editDuration: Int,
      val updateSuccess: Boolean,
      val isSaving: Boolean
  )

  private data class TouchState(
      val hasTouchedTitle: Boolean,
      val hasTouchedDateTime: Boolean,
      val hasTouchedDuration: Boolean
  )

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

  /**
   * Toggle edit mode and initialize edit fields with current meeting data.
   *
   * @param meeting The current meeting to edit, or null to exit edit mode.
   */
  fun toggleEditMode(meeting: Meeting?) {
    if (_isEditMode.value) {
      // Exiting edit mode - reset fields
      _isEditMode.value = false
      _editTitle.value = ""
      _editDateTime.value = null
      _editDuration.value = 30
      _errorMsg.value = null
      _hasTouchedTitle.value = false
      _hasTouchedDateTime.value = false
      _hasTouchedDuration.value = false
    } else {
      // Entering edit mode - populate fields with current values
      meeting?.let {
        _editTitle.value = it.title
        _editDateTime.value = it.datetime
        _editDuration.value = it.duration
        _isEditMode.value = true
        _errorMsg.value = null
      }
    }
  }

  /**
   * Update the edit title field.
   *
   * @param title The new title value.
   */
  fun updateEditTitle(title: String) {
    _editTitle.value = title
  }

  /**
   * Update the edit date/time field.
   *
   * @param dateTime The new date/time value.
   */
  fun updateEditDateTime(dateTime: Timestamp?) {
    _editDateTime.value = dateTime
  }

  /**
   * Update the edit duration field.
   *
   * @param duration The new duration value.
   */
  fun updateEditDuration(duration: Int) {
    _editDuration.value = duration
  }

  /** Mark the title field as touched. */
  fun touchTitle() {
    _hasTouchedTitle.value = true
  }

  /** Mark the date/time field as touched. */
  fun touchDateTime() {
    _hasTouchedDateTime.value = true
  }

  /** Mark the duration field as touched. */
  fun touchDuration() {
    _hasTouchedDuration.value = true
  }

  /**
   * Validate edit fields before saving.
   *
   * @return Error message if validation fails, null if valid.
   */
  private fun validateEditFields(): String? {
    val editDateTime = _editDateTime.value
    val isDateTimeInPast =
        editDateTime?.let {
          val dateTime =
              it.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
          dateTime.isBefore(java.time.LocalDateTime.now())
        } ?: false

    return when {
      _editTitle.value.isBlank() -> "Title cannot be empty"
      editDateTime == null -> "Date and time must be set"
      isDateTimeInPast -> "Meeting should be scheduled in the future"
      _editDuration.value <= 0 -> "Duration must be greater than 0"
      else -> null
    }
  }

  /**
   * Save the edited meeting changes to the repository.
   *
   * @param currentMeeting The current meeting object to update.
   */
  fun saveMeetingChanges(currentMeeting: Meeting) {
    val validationError = validateEditFields()
    if (validationError != null) {
      _errorMsg.value = validationError
      return
    }

    _isSaving.value = true
    viewModelScope.launch {
      val updatedMeeting =
          currentMeeting.copy(
              title = _editTitle.value,
              datetime = _editDateTime.value,
              duration = _editDuration.value)

      repository
          .updateMeeting(updatedMeeting)
          .onSuccess {
            _updateSuccess.value = true
            _isEditMode.value = false
            _isSaving.value = false
            _errorMsg.value = null
          }
          .onFailure { e ->
            _errorMsg.value = e.message ?: "Failed to update meeting"
            _isSaving.value = false
          }
    }
  }

  /** Clear the update success flag. */
  fun clearUpdateSuccess() {
    _updateSuccess.value = false
  }
}

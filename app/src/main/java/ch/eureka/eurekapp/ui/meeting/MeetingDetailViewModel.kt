/*
Note: This file was co-authored by Claude Code, Claude 4.5 Sonnet, Gemini and Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting detail screen.
 *
 * @property meeting The detailed meeting information, or null if loading or not found.
 * @property meetingProjectName The name of the project in which the meeting was created, or null if
 *   loading or not found.
 * @property creatorUser User information of the meeting creator, or null if not found.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether a data loading operation is in progress.
 * @property deleteSuccess Whether the meeting was successfully deleted.
 * @property isEditMode Whether the screen is in edit mode.
 * @property editTitle The title being edited.
 * @property editDateTime The date/time being edited.
 * @property editDuration The duration being edited.
 * @property updateSuccess Whether the meeting was successfully updated.
 * @property isSaving Whether a save operation is in progress.
 * @property isConnected Whether the device is connected to the internet.
 * @property isCreator Whether the current user is the creator of the meeting.
 */
data class MeetingDetailUIState(
    val meeting: Meeting? = null,
    val meetingProjectName: String? = null,
    val creatorUser: User? = null,
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
    val isConnected: Boolean = true,
    val isCreator: Boolean = false
)

/**
 * ViewModel for the meeting detail screen.
 *
 * Manages the state and business logic for displaying detailed meeting information, including
 * real-time updates of meeting data and creator user information.
 *
 * @property projectId The ID of the project containing the meeting.
 * @property meetingId The ID of the meeting to display.
 * @property repository The repository for meeting data operations.
 * @property projectRepository The repository for project data operations.
 * @property userRepository The repository for user data operations.
 * @property connectivityObserver The connectivity observer.
 */
class MeetingDetailViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = RepositoriesProvider.meetingRepository,
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val connectivityObserver: ConnectivityObserver =
        ConnectivityObserverProvider.connectivityObserver,
    getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val userId = getCurrentUserId()

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

  // Add connectivity observer
  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

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
   * Internal data class representing the edit state of the meeting.
   *
   * @property deleteSuccess Whether the meeting was successfully deleted.
   * @property errorMsg An error message to display, or null if there is no error.
   * @property isEditMode Whether the screen is in edit mode.
   * @property editTitle The title being edited.
   * @property editDateTime The date/time being edited.
   */
  private data class EditState(
      val deleteSuccess: Boolean,
      val errorMsg: String?,
      val isEditMode: Boolean,
      val editTitle: String,
      val editDateTime: Timestamp?
  )

  /**
   * Internal data class representing the save state of the meeting.
   *
   * @property editDuration The duration being edited.
   * @property updateSuccess Whether the meeting was successfully updated.
   * @property isSaving Whether a save operation is in progress.
   */
  private data class SaveState(
      val editDuration: Int,
      val updateSuccess: Boolean,
      val isSaving: Boolean
  )

  /**
   * Internal data class representing the touch state of edit fields.
   *
   * @property hasTouchedTitle Whether the title field has been touched.
   * @property hasTouchedDateTime Whether the date/time field has been touched.
   * @property hasTouchedDuration Whether the duration field has been touched.
   */
  private data class TouchState(
      val hasTouchedTitle: Boolean,
      val hasTouchedDateTime: Boolean,
      val hasTouchedDuration: Boolean
  )

  /**
   * Internal data class representing the combined state for UI flow.
   *
   * @property meeting The detailed meeting information.
   * @property creatorUser User information of the meeting creator.
   * @property meetingProjectName The name of the project.
   * @property editState The edit state.
   * @property saveState The save state.
   * @property touchState The touch state.
   */
  private data class CombinedState(
      val meeting: Meeting?,
      val creatorUser: User?,
      val meetingProjectName: String?,
      val editState: EditState,
      val saveState: SaveState,
      val touchState: TouchState
  )

  /**
   * UI state combining meeting data, creator user, and operation states.
   *
   * Follows the project's Flow pattern: declarative state initialization in init block using
   * stateIn() with WhileSubscribed strategy for automatic lifecycle management.
   *
   * Validates all meeting fields before displaying - if any required field is invalid, shows an
   * error message instead of displaying potentially corrupted data.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<MeetingDetailUIState> =
      combine(
              combine(
                  repository.getMeetingById(projectId, meetingId).flatMapLatest { meeting ->
                    if (meeting?.createdBy?.isNotEmpty() == true) {
                      userRepository.getUserById(meeting.createdBy).map { user -> meeting to user }
                    } else {
                      flowOf(meeting to null)
                    }
                  },
                  projectRepository.getProjectById(projectId),
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
                  }) { meetingWithCreator, project, editState, saveState, touchState ->
                    CombinedState(
                        meeting = meetingWithCreator.first,
                        creatorUser = meetingWithCreator.second,
                        meetingProjectName = project?.name,
                        editState = editState,
                        saveState = saveState,
                        touchState = touchState)
                  },
              _isConnected) { combined, isConnected ->
                val validationError = validateMeeting(combined.meeting)
                MeetingDetailUIState(
                    meeting = if (validationError == null) combined.meeting else null,
                    meetingProjectName =
                        if (validationError == null) combined.meetingProjectName else null,
                    creatorUser = combined.creatorUser,
                    isLoading = false,
                    errorMsg = combined.editState.errorMsg ?: validationError,
                    deleteSuccess = combined.editState.deleteSuccess,
                    isEditMode = combined.editState.isEditMode,
                    editTitle = combined.editState.editTitle,
                    editDateTime = combined.editState.editDateTime,
                    editDuration = combined.saveState.editDuration,
                    updateSuccess = combined.saveState.updateSuccess,
                    isSaving = combined.saveState.isSaving,
                    hasTouchedTitle = combined.touchState.hasTouchedTitle,
                    hasTouchedDateTime = combined.touchState.hasTouchedDateTime,
                    hasTouchedDuration = combined.touchState.hasTouchedDuration,
                    isConnected = isConnected,
                    isCreator = combined.meeting?.createdBy == userId)
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

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _errorMsg.value = null
  }

  /**
   * Delete the meeting from the repository.
   *
   * @param projectId The ID of the project containing the meeting.
   * @param meetingId The ID of the meeting to delete.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun deleteMeeting(projectId: String, meetingId: String, isConnected: Boolean = true) {
    if (isConnected) {
      viewModelScope.launch {
        repository
            .deleteMeeting(projectId, meetingId)
            .onSuccess { _deleteSuccess.value = true }
            .onFailure { e -> _errorMsg.value = e.message ?: "Failed to delete meeting" }
      }
    }
  }

  /**
   * Toggle edit mode and initialize edit fields with current meeting data.
   *
   * @param meeting The current meeting to edit, or null to exit edit mode.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun toggleEditMode(meeting: Meeting?, isConnected: Boolean = true) {
    if (isConnected) {
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
      isDateTimeInPast -> "Meeting should be scheduled in the future."
      _editDuration.value <= 0 -> "Duration must be greater than 0"
      else -> null
    }
  }

  /**
   * Save the edited meeting changes to the repository.
   *
   * @param currentMeeting The current meeting object to update.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun saveMeetingChanges(currentMeeting: Meeting, isConnected: Boolean) {
    if (isConnected) {
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
  }

  /** Clear the update success flag. */
  fun clearUpdateSuccess() {
    _updateSuccess.value = false
  }

  /**
   * Starts the meeting by updating its status to IN_PROGRESS.
   *
   * @param meeting The meeting to start.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun startMeeting(meeting: Meeting, isConnected: Boolean) {
    when {
      !isConnected -> _errorMsg.value = "Cannot start meeting while offline"
      userId != meeting.createdBy ->
          _errorMsg.value = "Only the meeting creator can start the meeting"
      meeting.status != MeetingStatus.SCHEDULED ->
          _errorMsg.value = "Meeting can only be started if it is scheduled"
      else -> {
        val updatedMeeting = meeting.copy(status = MeetingStatus.IN_PROGRESS)
        viewModelScope.launch {
          repository.updateMeeting(updatedMeeting).onFailure { e ->
            _errorMsg.value = e.message ?: "Failed to start meeting"
          }
        }
      }
    }
  }

  /**
   * Ends the meeting by updating its status to COMPLETED.
   *
   * @param meeting The meeting to end.
   * @param isConnected Whether the device is connected to the internet.
   */
  fun endMeeting(meeting: Meeting, isConnected: Boolean) {
    when {
      !isConnected -> _errorMsg.value = "Cannot end meeting while offline"
      userId != meeting.createdBy ->
          _errorMsg.value = "Only the meeting creator can end the meeting"
      meeting.status != MeetingStatus.IN_PROGRESS ->
          _errorMsg.value = "Meeting can only be ended if it is in progress"
      else -> {
        val updatedMeeting = meeting.copy(status = MeetingStatus.COMPLETED)
        viewModelScope.launch {
          repository.updateMeeting(updatedMeeting).onFailure { e ->
            _errorMsg.value = e.message ?: "Failed to end meeting"
          }
        }
      }
    }
  }

  /**
   * Checks if the meeting should have already started.
   *
   * @param meeting The meeting to check.
   * @return True if the meeting's start time has passed and it's still SCHEDULED, false otherwise.
   */
  fun shouldMeetingBeStarted(meeting: Meeting): Boolean {
    if (meeting.status != MeetingStatus.SCHEDULED) return false
    if (meeting.datetime == null) return false

    val meetingDateTime =
        meeting.datetime
            .toDate()
            .toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
    return meetingDateTime.isBefore(java.time.LocalDateTime.now())
  }

  /**
   * Checks if the meeting should have already ended.
   *
   * @param meeting The meeting to check.
   * @return True if the meeting's end time has passed and it's still IN_PROGRESS, false otherwise.
   */
  fun shouldMeetingBeEnded(meeting: Meeting): Boolean {
    if (meeting.status != MeetingStatus.IN_PROGRESS) return false
    if (meeting.datetime == null) return false

    val meetingDateTime =
        meeting.datetime
            .toDate()
            .toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
    val endDateTime = meetingDateTime.plusMinutes(meeting.duration.toLong())
    return endDateTime.isBefore(java.time.LocalDateTime.now())
  }
}

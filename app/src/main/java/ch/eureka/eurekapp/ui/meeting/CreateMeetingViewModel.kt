/*
Portions of the code in this file are copy-pasted from the Bootcamp solution B3 provided by the SwEnt staff.
Portions of the code in this file were written with the help of chatGPT.
 */
package ch.eureka.eurekapp.ui.meeting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.HttpClientProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.model.map.LocationRepository
import ch.eureka.eurekapp.model.map.NominatimLocationRepository
import ch.eureka.eurekapp.utils.MeetingLinkValidator
import ch.eureka.eurekapp.utils.MeetingPlatform
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the screen to create meetings.
 *
 * @property project The project selected by the user for the meeting to be created.
 * @property projects The projects the from which the user can choose a project to link the meeting
 *   tob created to.
 * @property isLoadingProjects True if projects are being loaded, false otherwise.
 * @property title The title of the meeting to be created.
 * @property date The date of the the meeting to be created.
 * @property time The start time of the the meeting to be created.
 * @property duration The duration of the meeting.
 * @property format The format of the meeting.
 * @property selectedLocation The potential selected location for the meeting.
 * @property locationQuery The location name currently queried.
 * @property locationSuggestions The location suggestions for [locationQuery].
 * @property meetingLink The video meeting link (for VIRTUAL meetings).
 * @property linkValidationError The error message for link validation, null if valid.
 * @property linkValidationWarning The warning message for link validation, null if no warning.
 * @property hasTouchedLink Marker set to true if the user has already clicked on the link field,
 *   false otherwise.
 * @property detectedPlatform The detected video meeting platform from the link.
 * @property meetingSaved Marker set to true if the meeting waa successfully saved, false otherwise.
 * @property hasTouchedTitle Marker set to true if the user has already clicked on the title field,
 *   false otherwise.
 * @property hasTouchedDate Marker set to true if the user has already clicked on the date field,
 *   false otherwise.
 * @property hasTouchedTime Marker set to true if the user has already clicked on the time field,
 *   false otherwise.
 * @property errorMsg Error message to display.
 */
data class CreateMeetingUIState(
    val project: Project? = null,
    val projects: List<Project> = emptyList(),
    val isLoadingProjects: Boolean = false,
    val title: String = "",
    val date: LocalDate = LocalDate.now().plusDays(7),
    val time: LocalTime = LocalTime.now(),
    val duration: Int = 0,
    val format: MeetingFormat = MeetingFormat.IN_PERSON,
    val selectedLocation: Location? = null,
    val locationQuery: String = "",
    val locationSuggestions: List<Location> = emptyList(),
    val meetingLink: String? = null,
    val linkValidationError: String? = null,
    val linkValidationWarning: String? = null,
    val hasTouchedLink: Boolean = false,
    val detectedPlatform: MeetingPlatform = MeetingPlatform.UNKNOWN,
    val meetingSaved: Boolean = false,
    val hasTouchedTitle: Boolean = false,
    val hasTouchedDate: Boolean = false,
    val hasTouchedTime: Boolean = false,
    val errorMsg: String? = null
) {
  /** States whether the UI is in a state where the meeting can be saved. */
  val isValid: Boolean
    get() =
        project != null &&
            title.isNotBlank() &&
            duration >= 5 &&
            LocalDateTime.of(date, time).isAfter(LocalDateTime.now()) &&
            (format == MeetingFormat.IN_PERSON && selectedLocation != null ||
                format == MeetingFormat.VIRTUAL &&
                    !meetingLink.isNullOrBlank() &&
                    linkValidationError == null)
}

/**
 * View model for the screen to create meetings.
 *
 * @property meetingRepository The repository to upload the meeting to.
 * @property projectRepository The projects repository.
 * @property locationRepository The repository for the location reverse geo-coding.
 * @property getCurrentUserId Function to get current user ID.
 */
@OptIn(FlowPreview::class)
class CreateMeetingViewModel(
    private val meetingRepository: MeetingRepository = FirestoreMeetingRepository(),
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client),
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateMeetingUIState())
  val uiState: StateFlow<CreateMeetingUIState> = _uiState

  private val searchIntent =
      MutableSharedFlow<String>(
          replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  init {
    viewModelScope.launch {
      searchIntent
          .debounce(300L)
          .filter { it.length >= 2 }
          .distinctUntilChanged()
          .collectLatest { query ->
            try {
              val results = locationRepository.search(query)
              _uiState.update { it.copy(locationSuggestions = results) }
            } catch (e: Exception) {
              Log.e("CreateMeetingViewModel", "Error fetching suggestions", e)
              _uiState.update { it.copy(locationSuggestions = emptyList()) }
            }
          }
    }
    loadProjects()
  }

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
   * Set hte project for the meeting to be created.
   *
   * @param project The project of the meeting proposal to be created.
   */
  fun setProject(project: Project) {
    _uiState.update { it.copy(project = project) }
  }

  /**
   * Load all the available projects for the current user, And populates the UI state with those
   * projects.
   */
  fun loadProjects() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingProjects = true) }

      try {
        projectRepository.getProjectsForCurrentUser(skipCache = false).collect { projects ->
          _uiState.update { it.copy(isLoadingProjects = false, projects = projects) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoadingProjects = false, errorMsg = e.message) }
      }
    }
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

  /**
   * Set the location of the meeting to be created.
   *
   * @param location The location of the meeting to be created.
   */
  fun setLocation(location: Location) {
    _uiState.update { it.copy(selectedLocation = location) }
  }

  /**
   * Search for suggestions for location suggestions for the given [query] and populates the
   * suggestions and sets the location query.
   *
   * @param query The location name query.
   */
  fun setLocationQuery(query: String) {
    _uiState.update { it.copy(locationQuery = query) }

    if (query.isEmpty()) {
      _uiState.update { it.copy(locationSuggestions = emptyList()) }
    } else {
      searchIntent.tryEmit(query)
    }
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
   * Set the meeting link and validate it.
   *
   * @param link The meeting link URL.
   */
  fun setMeetingLink(link: String) {
    val (isValid, message) = MeetingLinkValidator.validateMeetingLink(link)
    val platform = MeetingLinkValidator.detectPlatform(link)

    _uiState.update {
      it.copy(
          meetingLink = link,
          linkValidationError = if (!isValid) message else null,
          linkValidationWarning = if (isValid && message != null) message else null,
          detectedPlatform = platform)
    }
  }

  /** Mark the link field as touched. */
  fun touchLink() {
    _uiState.update { it.copy(hasTouchedLink = true) }
  }

  /** Create and upload a meeting proposal to the database. */
  fun createMeeting() {
    if (!uiState.value.isValid) {
      setErrorMsg("At least one field is not set")
      return
    }

    val creatorId = getCurrentUserId()

    if (creatorId == null) {
      setErrorMsg("Not logged in")
      return
    }

    val selectedProject = uiState.value.project

    if (selectedProject == null) {
      setErrorMsg("Project not selected")
      return
    }

    val timeInstant =
        LocalDateTime.of(uiState.value.date, uiState.value.time)
            .atZone(ZoneId.systemDefault())
            .toInstant()

    val meeting =
        Meeting(
            meetingID = IdGenerator.generateMeetingId(),
            projectId = selectedProject.projectId,
            title = uiState.value.title,
            status = MeetingStatus.OPEN_TO_VOTES,
            duration = uiState.value.duration,
            location = uiState.value.selectedLocation,
            link = uiState.value.meetingLink,
            meetingProposals =
                listOf(
                    MeetingProposal(
                        Timestamp(timeInstant),
                        listOf(MeetingProposalVote(creatorId, listOf(uiState.value.format))))),
            createdBy = creatorId,
            participantIds = selectedProject.memberIds)

    viewModelScope.launch {
      meetingRepository
          .createMeeting(meeting = meeting, creatorId = creatorId, creatorRole = MeetingRole.HOST)
          .onFailure { setErrorMsg("Meeting could not be created.") }
          .onSuccess { setMeetingSaved() }
    }
  }
}

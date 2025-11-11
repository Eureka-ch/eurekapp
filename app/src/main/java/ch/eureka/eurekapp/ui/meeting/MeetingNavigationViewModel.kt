/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Data class to represent the UI state of the meeting location screen.
 *
 * @property meeting The meeting whose location to display, or null if loading or not found.
 * @property isLoading Whether a data loading operation is in progress.
 * @property errorMsg An error message to display, or null if there is no error.
 */
data class MeetingNavigationUIState(
    val meeting: Meeting? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

/**
 * ViewModel for the meeting location screen.
 *
 * Manages the state and business logic for displaying a meeting location on a map.
 *
 * @property projectId The ID of the project containing the meeting.
 * @property meetingId The ID of the meeting to display.
 * @property repository The repository for meeting data operations.
 */
class MeetingNavigationViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val repository: MeetingRepository = FirestoreRepositoriesProvider.meetingRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingNavigationUIState(isLoading = true))
  val uiState: StateFlow<MeetingNavigationUIState> = _uiState.asStateFlow()

  init {
    loadMeeting()
  }

  /** Loads meeting data from the repository. */
  private fun loadMeeting() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      repository
          .getMeetingById(projectId, meetingId)
          .catch { exception ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false, errorMsg = "Failed to load meeting: ${exception.message}")
          }
          .collect { meeting ->
            if (meeting == null) {
              _uiState.value =
                  _uiState.value.copy(isLoading = false, errorMsg = "Meeting not found")
            } else if (meeting.location == null) {
              _uiState.value =
                  _uiState.value.copy(
                      isLoading = false, errorMsg = "Meeting location is not available")
            } else {
              _uiState.value =
                  _uiState.value.copy(meeting = meeting, isLoading = false, errorMsg = null)
            }
          }
    }
  }

  /**
   * Gets the meeting location as a LatLng.
   *
   * @return Meeting location, or null if not available.
   */
  fun getMeetingLocation(): LatLng? {
    return _uiState.value.meeting?.location?.let { LatLng(it.latitude, it.longitude) }
  }
}

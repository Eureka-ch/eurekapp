/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Data class to represent the UI state of the meeting location screen.
 *
 * @property meeting The meeting whose location to display. Only present when valid.
 * @property isLoading Whether a data loading operation is in progress.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property userLocation The user's current location, or null if not available.
 * @property route The route from user location to meeting, or null if not loaded.
 * @property isLoadingRoute Whether a route fetch operation is in progress.
 * @property routeErrorMsg An error message for route fetching, or null if no error.
 */
data class MeetingNavigationUIState(
    val meeting: Meeting? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val userLocation: LatLng? = null,
    val route: ch.eureka.eurekapp.services.navigation.Route? = null,
    val isLoadingRoute: Boolean = false,
    val routeErrorMsg: String? = null
)

/**
 * ViewModel for the meeting location screen.
 *
 * Manages the state and business logic for displaying a meeting location on a map.
 *
 * @property projectId The ID of the project containing the meeting.
 * @property meetingId The ID of the meeting to display.
 * @property apiKey Google Maps API key for directions.
 * @property repository The repository for meeting data operations.
 */
class MeetingNavigationViewModel(
    private val projectId: String,
    private val meetingId: String,
    private val apiKey: String,
    private val repository: MeetingRepository = FirestoreRepositoriesProvider.meetingRepository
) : ViewModel() {

  internal val _uiState = MutableStateFlow(MeetingNavigationUIState(isLoading = true))
  val uiState: StateFlow<MeetingNavigationUIState> = _uiState.asStateFlow()

  private val directionsService = ch.eureka.eurekapp.services.navigation.DirectionsApiServiceFactory.create()

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

  /**
   * Fetches the user's current location.
   *
   * @param context Android context for location services.
   */
  fun fetchUserLocation(context: Context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return
    }

    viewModelScope.launch {
      try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()

        if (location != null) {
          _uiState.value = _uiState.value.copy(
              userLocation = LatLng(location.latitude, location.longitude)
          )
        }
      } catch (e: Exception) {
        // Silently fail - user location is optional
      }
    }
  }

  /**
   * Fetches directions from user's location to the meeting.
   *
   * @param travelMode The travel mode (driving, walking, bicycling, transit).
   */
  fun fetchDirections(travelMode: String = "driving") {
    val userLoc = _uiState.value.userLocation
    val meetingLoc = getMeetingLocation()

    if (userLoc == null) {
      _uiState.value = _uiState.value.copy(
          routeErrorMsg = "User location not available"
      )
      return
    }

    if (meetingLoc == null) {
      _uiState.value = _uiState.value.copy(
          routeErrorMsg = "Meeting location not available"
      )
      return
    }

    viewModelScope.launch {
      try {
        _uiState.value = _uiState.value.copy(
            isLoadingRoute = true,
            routeErrorMsg = null
        )

        val origin = ch.eureka.eurekapp.services.navigation.DirectionsUtils.formatLocation(userLoc.latitude, userLoc.longitude)
        val destination = ch.eureka.eurekapp.services.navigation.DirectionsUtils.formatLocation(meetingLoc.latitude, meetingLoc.longitude)
        val mode = ch.eureka.eurekapp.services.navigation.DirectionsUtils.formatTravelMode(travelMode)

        val response = directionsService.getDirections(
            origin = origin,
            destination = destination,
            mode = mode,
            apiKey = apiKey
        )

        if (response.status == "OK" && response.routes.isNotEmpty()) {
          _uiState.value = _uiState.value.copy(
              route = response.routes[0],
              isLoadingRoute = false,
              routeErrorMsg = null
          )
        } else {
          _uiState.value = _uiState.value.copy(
              isLoadingRoute = false,
              routeErrorMsg = response.errorMessage ?: "No route found"
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
            isLoadingRoute = false,
            routeErrorMsg = "Error fetching directions: ${e.message}"
        )
      }
    }
  }

  /**
   * Calculate straight-line distance between two points in kilometers.
   */
  fun calculateDistance(from: LatLng, to: LatLng): Double {
    val earthRadius = 6371.0 // km
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
  }
}

/* Portions of the code in this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.map.Location
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * UI state for the screen to pick a location for a meeting.
 *
 * @property selectedLocation The selected location.
 * @property initialCameraPosition The initial camera position.
 */
data class MeetingLocationSelectionUIState(
    val selectedLocation: Location? = null,
    val initialCameraPosition: LatLng = LatLng(46.5197, 6.6323)
)

/** View model for the screen to select the location for a meeting. */
class MeetingLocationSelectionViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(MeetingLocationSelectionUIState())
  val uiState: StateFlow<MeetingLocationSelectionUIState> = _uiState.asStateFlow()

  private val connectivityObserver = ConnectivityObserverProvider.connectivityObserver
  val isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  /**
   * Updates the selected location.
   *
   * @param latLng The coordinates clicked.
   * @param name The name of the place (if a POI was clicked), otherwise null.
   */
  fun selectLocation(latLng: LatLng, name: String? = null) {
    val locationName =
        name
            ?: "${String.format(Locale.US, "%.4f", latLng.latitude)}, ${String.format(Locale.US, "%.4f", latLng.longitude)}"

    val newLocation =
        Location(latitude = latLng.latitude, longitude = latLng.longitude, name = locationName)

    _uiState.update { it.copy(selectedLocation = newLocation) }
  }
}

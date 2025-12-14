/* Portions of the code in this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.map.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/** Test tags for the meeting location selection screen. */
object MeetingLocationSelectionTestTags {
  const val SCREEN_TITLE = "SelectLocationTitle"
  const val GOOGLE_MAP = "GoogleMap"
  const val SAVE_BUTTON = "SaveLocationButton"
  const val MARKER = "SelectedLocationMarker"
}

/**
 * Main composable for the screen to pick the location of a meeting on the map.
 *
 * @param onLocationSelected Callback executed when the location is selected (saved).
 * @param onBack Callback called when the suer clicks on the back button.
 * @param viewModel The view model associated with that screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingLocationSelectionScreen(
    onLocationSelected: (Location) -> Unit,
    onBack: () -> Unit,
    viewModel: MeetingLocationSelectionViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(uiState.initialCameraPosition, 12f)
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Pick a Location",
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            })
      },
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      GoogleMap(
          modifier = Modifier.fillMaxSize().testTag(MeetingLocationSelectionTestTags.GOOGLE_MAP),
          cameraPositionState = cameraPositionState,
          uiSettings = MapUiSettings(zoomControlsEnabled = true),
          onPOIClick = { poi -> viewModel.selectLocation(poi.latLng, poi.name) },
          onMapClick = { latLng -> viewModel.selectLocation(latLng, null) }) {
            uiState.selectedLocation?.let { location ->
              Marker(
                  state =
                      MarkerState(
                          position =
                              com.google.android.gms.maps.model.LatLng(
                                  location.latitude, location.longitude)),
                  title = location.name,
                  snippet = if (location.name.contains(",")) "Coordinates" else "Point of Interest",
                  tag = MeetingLocationSelectionTestTags.MARKER)
            }
          }

      Button(
          onClick = { uiState.selectedLocation?.let { onLocationSelected(it) } },
          enabled = uiState.selectedLocation != null,
          modifier =
              Modifier.align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .padding(16.dp)
                  .testTag(MeetingLocationSelectionTestTags.SAVE_BUTTON)) {
            Text("Save Location")
          }
    }
  }
}

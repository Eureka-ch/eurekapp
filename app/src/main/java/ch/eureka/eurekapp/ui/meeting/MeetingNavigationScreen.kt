/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Test tags for MeetingNavigationScreen composable.
 *
 * Provides semantic identifiers for UI testing with Compose UI Test framework.
 */
object MeetingNavigationScreenTestTags {
  const val NAVIGATION_SCREEN = "MeetingNavigationScreen"
  const val LOADING_INDICATOR = "NavigationLoadingIndicator"
  const val ERROR_MESSAGE = "NavigationErrorMessage"
  const val GOOGLE_MAP = "GoogleMapView"
  const val INFO_CARD = "NavigationInfoCard"
}

/** Default zoom level for the map when displaying meeting location. */
private const val DEFAULT_MAP_ZOOM = 15f

/**
 * Main composable for the meeting location screen.
 *
 * Displays a Google Map with the meeting location marker and basic location information.
 *
 * @param projectId The ID of the project containing the meeting.
 * @param meetingId The ID of the meeting whose location to display.
 * @param viewModel The ViewModel managing the screen state.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingNavigationScreen(
    projectId: String,
    meetingId: String,
    viewModel: MeetingNavigationViewModel = viewModel {
      MeetingNavigationViewModel(projectId, meetingId)
    },
    onNavigateBack: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
      modifier = Modifier.testTag(MeetingNavigationScreenTestTags.NAVIGATION_SCREEN),
      topBar = {
        TopAppBar(
            title = { Text(text = stringResource(R.string.meeting_location_title)) },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back))
              }
            })
      },
      content = { padding ->
        if (uiState.isLoading) {
          LoadingScreen()
        } else if (uiState.meeting == null || uiState.errorMsg != null) {
          ErrorScreen(message = uiState.errorMsg ?: stringResource(R.string.meeting_not_found))
        } else {
          MapContent(modifier = Modifier.padding(padding), uiState = uiState)
        }
      })
}

/** Loading indicator screen. */
@Composable
private fun LoadingScreen() {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(16.dp)
              .testTag(MeetingNavigationScreenTestTags.LOADING_INDICATOR),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.loading_meeting_location),
            style = MaterialTheme.typography.bodyMedium)
      }
}

/**
 * Error message screen.
 *
 * @param message The error message to display to the user.
 */
@Composable
private fun ErrorScreen(message: String) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(16.dp)
              .testTag(MeetingNavigationScreenTestTags.ERROR_MESSAGE),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error)
      }
}

/**
 * Main map content displaying the meeting location.
 *
 * @param modifier Modifier to be applied to the root composable.
 * @param uiState The current UI state.
 */
@Composable
private fun MapContent(modifier: Modifier = Modifier, uiState: MeetingNavigationUIState) {
  val meeting = uiState.meeting ?: return
  val meetingLocation = meeting.location?.let { LatLng(it.latitude, it.longitude) } ?: return

  Column(modifier = Modifier.fillMaxSize().then(modifier)) {
    // Google Map
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      MapView(meetingLocation = meetingLocation, meetingName = meeting.location.name)
    }

    // Information Card
    InfoCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp), locationName = meeting.location.name)
  }
}

/**
 * Google Map view showing meeting location.
 *
 * @param meetingLocation The meeting location coordinates.
 * @param meetingName The name of the meeting location.
 */
@Composable
private fun MapView(meetingLocation: LatLng, meetingName: String) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(meetingLocation, DEFAULT_MAP_ZOOM)
  }

  GoogleMap(
      modifier = Modifier.fillMaxSize().testTag(MeetingNavigationScreenTestTags.GOOGLE_MAP),
      cameraPositionState = cameraPositionState) {
        // Meeting location marker
        Marker(
            state = MarkerState(position = meetingLocation),
            title = meetingName,
            snippet = "Meeting Location")
      }
}

/**
 * Information card displaying the meeting location name.
 *
 * @param modifier Modifier to be applied to the card.
 * @param locationName The name of the meeting location.
 */
@Composable
private fun InfoCard(modifier: Modifier = Modifier, locationName: String) {
  Card(
      modifier = modifier.testTag(MeetingNavigationScreenTestTags.INFO_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Default.Place,
              contentDescription = "Location",
              modifier = Modifier.size(24.dp),
              tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
              text = locationName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold)
        }
      }
}

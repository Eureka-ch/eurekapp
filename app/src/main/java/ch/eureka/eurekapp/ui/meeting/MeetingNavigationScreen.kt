/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.ui.meeting

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

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
 * Available travel modes for route calculation.
 *
 * @property displayName Human-readable name shown in the UI
 * @property apiValue Value sent to the Google Directions API
 */
enum class TravelMode(val displayName: String, val apiValue: String) {
  DRIVING("Drive", "driving"),
  TRANSIT("Transit", "transit"),
  BICYCLING("Bike", "bicycling"),
  WALKING("Walk", "walking")
}

/**
 * Main composable for the meeting location screen.
 *
 * We use composable overloading here because having API key generated here keeps the things
 * compartmentalized. Also this pattern enables reusability and testability as recommended by the
 * Android Jetpack Compose documentation.
 *
 * Displays a Google Map with the meeting location marker and navigation features.
 *
 * @param projectId The ID of the project containing the meeting.
 * @param meetingId The ID of the meeting whose location to display.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingNavigationScreen(projectId: String, meetingId: String, onNavigateBack: () -> Unit = {}) {
  val context = LocalContext.current

  // Get API key from manifest
  val apiKey = remember {
    context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData
        .getString("com.google.android.geo.API_KEY") ?: ""
  }

  val viewModel: MeetingNavigationViewModel = viewModel {
    MeetingNavigationViewModel(projectId, meetingId, apiKey)
  }

  MeetingNavigationScreen(
      projectId = projectId,
      meetingId = meetingId,
      viewModel = viewModel,
      onNavigateBack = onNavigateBack)
}

/**
 * Overload for testing - accepts a ViewModel parameter.
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
    viewModel: MeetingNavigationViewModel,
    onNavigateBack: () -> Unit = {}
) {

  val uiState by viewModel.uiState.collectAsState()

  // Track selected travel mode
  var selectedTravelMode by remember { mutableStateOf(TravelMode.DRIVING) }

  // Track whether to show directions panel
  var showDirections by remember { mutableStateOf(false) }

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
          MapContent(
              modifier = Modifier.padding(padding),
              uiState = uiState,
              viewModel = viewModel,
              selectedTravelMode = selectedTravelMode,
              onTravelModeChange = { selectedTravelMode = it },
              showDirections = showDirections,
              onShowDirectionsChange = { showDirections = it })
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
 * Main map content displaying the meeting location with navigation features.
 *
 * @param modifier Modifier to be applied to the root composable.
 * @param uiState The current UI state.
 * @param viewModel The ViewModel for location operations.
 * @param selectedTravelMode The currently selected travel mode.
 * @param onTravelModeChange Callback when travel mode changes.
 * @param showDirections Whether to show the directions panel.
 * @param onShowDirectionsChange Callback to toggle directions panel.
 */
@Composable
private fun MapContent(
    modifier: Modifier = Modifier,
    uiState: MeetingNavigationUIState,
    viewModel: MeetingNavigationViewModel,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
    showDirections: Boolean,
    onShowDirectionsChange: (Boolean) -> Unit
) {
  val context = LocalContext.current
  val meeting = uiState.meeting ?: return
  val meetingLocation = meeting.location?.let { LatLng(it.latitude, it.longitude) } ?: return

  // Try to get user location once
  LaunchedEffect(Unit) { viewModel.fetchUserLocation(context) }

  Column(modifier = Modifier.fillMaxSize().then(modifier)) {
    // Google Map
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      MapView(
          meetingLocation = meetingLocation,
          meetingName = meeting.location.name,
          userLocation = uiState.userLocation,
          route = uiState.route)

      // Directions panel overlay
      if (showDirections && uiState.route != null) {
        DirectionsPanel(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            route = uiState.route,
            onClose = { onShowDirectionsChange(false) })
      }
    }

    // Information Card
    InfoCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        locationName = meeting.location.name,
        meetingLocation = meetingLocation,
        userLocation = uiState.userLocation,
        route = uiState.route,
        isLoadingRoute = uiState.isLoadingRoute,
        routeErrorMsg = uiState.routeErrorMsg,
        selectedTravelMode = selectedTravelMode,
        onTravelModeChange = onTravelModeChange,
        onFetchDirections = { viewModel.fetchDirections(selectedTravelMode) },
        onShowDirections = { onShowDirectionsChange(true) },
        viewModel = viewModel)
  }
}

/**
 * Google Map view showing meeting location and route polyline.
 *
 * @param meetingLocation The meeting location coordinates.
 * @param meetingName The name of the meeting location.
 * @param userLocation The user's current location, or null if not available.
 * @param route The route data from Google Directions API, or null if not loaded.
 */
@Composable
private fun MapView(
    meetingLocation: LatLng,
    meetingName: String,
    userLocation: LatLng? = null,
    route: ch.eureka.eurekapp.services.navigation.Route? = null
) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(meetingLocation, DEFAULT_MAP_ZOOM)
  }

  GoogleMap(
      modifier = Modifier.fillMaxSize().testTag(MeetingNavigationScreenTestTags.GOOGLE_MAP),
      cameraPositionState = cameraPositionState) {
        // Meeting location marker
        Marker(
            state = rememberMarkerState(position = meetingLocation),
            title = meetingName,
            snippet = "Meeting Location")

        // User location marker
        userLocation?.let {
          Marker(state = rememberMarkerState(position = it), title = "Your Location")
        }

        // Draw route polyline if available
        if (route != null && userLocation != null) {
          val polylinePoints =
              ch.eureka.eurekapp.services.navigation.DirectionsUtils.decodePolyline(
                  route.overviewPolyline.points)
          val latLngPoints = polylinePoints.map { LatLng(it.first, it.second) }

          Polyline(
              points = latLngPoints,
              color = Color(0xFF4285F4),
              width = 10f,
              jointType = JointType.ROUND,
              startCap = ButtCap(),
              endCap = ButtCap())
        }
      }
}

/**
 * Information card displaying location, transport modes, and route information.
 *
 * @param modifier Modifier to be applied to the card.
 * @param locationName The name of the meeting location.
 * @param meetingLocation The coordinates of the meeting location.
 * @param userLocation The user's current location, or null if not available.
 * @param route The route data from Google Directions API, or null if not loaded.
 * @param isLoadingRoute Whether a route is currently being loaded.
 * @param routeErrorMsg Error message from route fetching, or null if no error.
 * @param selectedTravelMode The currently selected travel mode.
 * @param onTravelModeChange Callback when travel mode changes.
 * @param onFetchDirections Callback to fetch directions for selected mode.
 * @param onShowDirections Callback to show the directions panel.
 * @param viewModel The ViewModel for distance calculations.
 */
@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    locationName: String,
    meetingLocation: LatLng,
    userLocation: LatLng? = null,
    route: ch.eureka.eurekapp.services.navigation.Route? = null,
    isLoadingRoute: Boolean = false,
    routeErrorMsg: String? = null,
    selectedTravelMode: TravelMode = TravelMode.DRIVING,
    onTravelModeChange: (TravelMode) -> Unit = {},
    onFetchDirections: () -> Unit = {},
    onShowDirections: () -> Unit = {},
    viewModel: MeetingNavigationViewModel
) {
  val context = LocalContext.current
  var hasLocationPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
  }

  // Permission launcher
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
          viewModel.fetchUserLocation(context)
        }
      }

  Card(
      modifier = modifier.testTag(MeetingNavigationScreenTestTags.INFO_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Location name
          Row(verticalAlignment = Alignment.CenterVertically) {
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

          if (!hasLocationPermission || userLocation == null) {
            // Show message and button to enable location
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enable location to see route",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(12.dp))

            // Show button based on permission status
            if (!hasLocationPermission) {
              // Need permission - show Enable Location button
              Button(
                  onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                  modifier = Modifier.fillMaxWidth()) {
                    Text("Enable Location")
                  }
            } else {
              // Have permission but no location yet - show Get Location button
              Button(
                  onClick = { viewModel.fetchUserLocation(context) },
                  modifier = Modifier.fillMaxWidth()) {
                    Text("Get My Location")
                  }
            }
          } else {
            // Transport mode selection
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select transport mode:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  TravelMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedTravelMode == mode,
                        onClick = { onTravelModeChange(mode) },
                        label = { Text(mode.displayName) },
                        leadingIcon = {
                          Icon(
                              imageVector =
                                  when (mode) {
                                    TravelMode.DRIVING -> Icons.Default.DirectionsCar
                                    TravelMode.TRANSIT -> Icons.Default.DirectionsTransit
                                    TravelMode.BICYCLING -> Icons.Default.DirectionsBike
                                    TravelMode.WALKING -> Icons.Default.DirectionsWalk
                                  },
                              contentDescription = mode.displayName)
                        })
                  }
                }

            // Get Directions button
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onFetchDirections,
                enabled = !isLoadingRoute,
                modifier = Modifier.fillMaxWidth()) {
                  if (isLoadingRoute) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...")
                  } else {
                    Text("Get Directions")
                  }
                }

            // Show route information if available
            if (route != null && route.legs.isNotEmpty()) {
              val leg = route.legs[0]
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                  text = "${leg.distance.text} • ${leg.duration.text}",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium)

              // Show Directions button
              Spacer(modifier = Modifier.height(8.dp))
              Button(onClick = onShowDirections, modifier = Modifier.fillMaxWidth()) {
                Text("Show Directions")
              }
            }

            // Show error if any
            routeErrorMsg?.let { error ->
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                  text = error,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
}

/**
 * Directions panel showing turn-by-turn instructions.
 *
 * @param modifier Modifier to be applied to the panel.
 * @param route The route data containing steps.
 * @param onClose Callback to close the directions panel.
 */
@Composable
private fun DirectionsPanel(
    modifier: Modifier = Modifier,
    route: ch.eureka.eurekapp.services.navigation.Route,
    onClose: () -> Unit
) {
  Card(
      modifier = modifier.fillMaxWidth(0.9f).height(400.dp),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Header
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Turn-by-Turn Directions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Close directions")
                }
              }

          // Steps list
          if (route.legs.isNotEmpty()) {
            val steps = route.legs[0].steps
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)) {
                  steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top) {
                          // Step number
                          Text(
                              text = "${index + 1}.",
                              style = MaterialTheme.typography.bodyMedium,
                              fontWeight = FontWeight.Bold,
                              modifier = Modifier.width(30.dp))

                          Spacer(modifier = Modifier.width(8.dp))

                          // Step instruction and distance
                          Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text =
                                    ch.eureka.eurekapp.services.navigation.DirectionsUtils
                                        .stripHtmlTags(step.htmlInstructions),
                                style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = step.distance.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary)
                          }
                        }

                    if (index < steps.size - 1) {
                      Spacer(modifier = Modifier.height(4.dp))
                    }
                  }

                  // Add bottom padding for last item
                  Spacer(modifier = Modifier.height(16.dp))
                }
          }
        }
      }
}

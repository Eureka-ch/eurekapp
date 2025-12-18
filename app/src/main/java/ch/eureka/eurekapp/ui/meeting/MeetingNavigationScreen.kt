/*
Note: This file was co-authored by Claude Code and Grok.
*/
package ch.eureka.eurekapp.ui.meeting

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
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
 * Route state data for navigation.
 *
 * @property userLocation User's current location.
 * @property route The calculated route from user to meeting location.
 * @property isLoadingRoute Whether route is currently being fetched.
 * @property routeErrorMsg Error message if route fetch failed.
 * @property selectedTravelMode Currently selected travel mode.
 */
data class RouteState(
    val userLocation: LatLng? = null,
    val route: ch.eureka.eurekapp.services.navigation.Route? = null,
    val isLoadingRoute: Boolean = false,
    val routeErrorMsg: String? = null,
    val selectedTravelMode: TravelMode = TravelMode.DRIVING
)

/**
 * Action callbacks for route navigation.
 *
 * @property onTravelModeChange Callback when travel mode changes.
 * @property onFetchDirections Callback to fetch directions.
 * @property onShowDirections Callback to show turn-by-turn directions.
 */
data class RouteActions(
    val onTravelModeChange: (TravelMode) -> Unit = {},
    val onFetchDirections: () -> Unit = {},
    val onShowDirections: () -> Unit = {}
)

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

  MeetingNavigationScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
}

/**
 * Overload for testing - accepts a ViewModel parameter.
 *
 * @param viewModel The ViewModel managing the screen state.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingNavigationScreen(
    viewModel: MeetingNavigationViewModel,
    onNavigateBack: () -> Unit = {}
) {

  val uiState by viewModel.uiState.collectAsState()

  val isConnected by viewModel.isConnected.collectAsState()
  val context = LocalContext.current

  // Navigate back if connection is lost
  LaunchedEffect(isConnected) {
    if (!isConnected) {
      Toast.makeText(context, "Connection lost. Returning to previous screen.", Toast.LENGTH_SHORT)
          .show()
      onNavigateBack()
    }
  }

  // Track selected travel mode
  var selectedTravelMode by remember { mutableStateOf(TravelMode.DRIVING) }

  // Track whether to show directions panel
  var showDirections by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(MeetingNavigationScreenTestTags.NAVIGATION_SCREEN),
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.meeting_location_title),
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
        routeState =
            RouteState(
                userLocation = uiState.userLocation,
                route = uiState.route,
                isLoadingRoute = uiState.isLoadingRoute,
                routeErrorMsg = uiState.routeErrorMsg,
                selectedTravelMode = selectedTravelMode),
        routeActions =
            RouteActions(
                onTravelModeChange = onTravelModeChange,
                onFetchDirections = { viewModel.fetchDirections(selectedTravelMode) },
                onShowDirections = { onShowDirectionsChange(true) }),
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
            snippet = stringResource(R.string.meeting_location_title))

        // User location marker
        userLocation?.let {
          Marker(
              state = rememberMarkerState(position = it),
              title = stringResource(R.string.meeting_your_location))
        }

        // Draw route polyline if available
        if (route != null && userLocation != null) {
          val polylinePoints =
              ch.eureka.eurekapp.services.navigation.DirectionsUtils.decodePolyline(
                  route.overviewPolyline.points)
          val latLngPoints = polylinePoints.map { LatLng(it.first, it.second) }

          Polyline(
              points = latLngPoints,
              color = EColors.GoogleBlue,
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
 * @param routeState Route-related state (user location, route, loading status, errors, travel
 *   mode).
 * @param routeActions Callbacks for route-related actions.
 * @param viewModel The ViewModel for distance calculations.
 */
@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    locationName: String,
    routeState: RouteState,
    routeActions: RouteActions,
    viewModel: MeetingNavigationViewModel
) {
  val context = LocalContext.current
  var hasLocationPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
  }

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
          LocationHeader(locationName = locationName)

          if (!hasLocationPermission || routeState.userLocation == null) {
            LocationPermissionSection(
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = {
                  permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onGetLocation = { viewModel.fetchUserLocation(context) })
          } else {
            NavigationControlsSection(routeState = routeState, routeActions = routeActions)
          }
        }
      }
}

/**
 * Displays the location name with icon.
 *
 * @param locationName The name of the location.
 */
@Composable
private fun LocationHeader(locationName: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Filled.Place,
        contentDescription = stringResource(R.string.location_icon),
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = locationName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold)
  }
}

/**
 * Displays location permission request UI.
 *
 * @param hasLocationPermission Whether location permission is granted.
 * @param onRequestPermission Callback to request permission.
 * @param onGetLocation Callback to fetch user location.
 */
@Composable
private fun LocationPermissionSection(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onGetLocation: () -> Unit
) {
  Spacer(modifier = Modifier.height(12.dp))
  Text(
      text = stringResource(R.string.meeting_enable_location_to_see_route),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.secondary)

  Spacer(modifier = Modifier.height(12.dp))

  if (!hasLocationPermission) {
    Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.meeting_enable_location_button))
    }
  } else {
    Button(onClick = onGetLocation, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.meeting_get_my_location))
    }
  }
}

/**
 * Displays navigation controls including travel mode selector, directions button, and route info.
 *
 * @param routeState Route-related state.
 * @param routeActions Callbacks for route-related actions.
 */
@Composable
private fun NavigationControlsSection(routeState: RouteState, routeActions: RouteActions) {
  TravelModeSelector(
      selectedMode = routeState.selectedTravelMode, onModeChange = routeActions.onTravelModeChange)

  DirectionsButton(
      isLoading = routeState.isLoadingRoute, onFetchDirections = routeActions.onFetchDirections)

  RouteInfoDisplay(route = routeState.route, onShowDirections = routeActions.onShowDirections)

  RouteErrorDisplay(errorMessage = routeState.routeErrorMsg)
}

/**
 * Displays travel mode selection chips.
 *
 * @param selectedMode Currently selected travel mode.
 * @param onModeChange Callback when mode changes.
 */
@Composable
private fun TravelModeSelector(selectedMode: TravelMode, onModeChange: (TravelMode) -> Unit) {
  Spacer(modifier = Modifier.height(16.dp))
  Text(
      text = stringResource(R.string.meeting_select_transport_mode),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Medium)

  Spacer(modifier = Modifier.height(8.dp))
  Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TravelMode.values().forEach { mode ->
          FilterChip(
              selected = selectedMode == mode,
              onClick = { onModeChange(mode) },
              label = { Text(mode.displayName) },
              leadingIcon = {
                Icon(imageVector = getTravelModeIcon(mode), contentDescription = mode.displayName)
              })
        }
      }
}

/**
 * Returns the icon for a travel mode.
 *
 * @param mode The travel mode.
 * @return The icon vector.
 */
private fun getTravelModeIcon(mode: TravelMode) =
    when (mode) {
      TravelMode.DRIVING -> Icons.Filled.DirectionsCar
      TravelMode.TRANSIT -> Icons.Filled.DirectionsTransit
      TravelMode.BICYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
      TravelMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    }

/**
 * Displays the Get Directions button with loading state.
 *
 * @param isLoading Whether directions are being fetched.
 * @param onFetchDirections Callback to fetch directions.
 */
@Composable
private fun DirectionsButton(isLoading: Boolean, onFetchDirections: () -> Unit) {
  Spacer(modifier = Modifier.height(12.dp))
  Button(onClick = onFetchDirections, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
      Spacer(modifier = Modifier.width(8.dp))
      Text(stringResource(R.string.meeting_loading_text))
    } else {
      Text(stringResource(R.string.meeting_get_directions))
    }
  }
}

/**
 * Displays route information and Show Directions button.
 *
 * @param route The calculated route.
 * @param onShowDirections Callback to show turn-by-turn directions.
 */
@Composable
private fun RouteInfoDisplay(
    route: ch.eureka.eurekapp.services.navigation.Route?,
    onShowDirections: () -> Unit
) {
  if (route != null && route.legs.isNotEmpty()) {
    val leg = route.legs[0]
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "${leg.distance.text} • ${leg.duration.text}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium)

    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onShowDirections, modifier = Modifier.fillMaxWidth()) {
      Text("Show Directions")
    }
  }
}

/**
 * Displays route error message if present.
 *
 * @param errorMessage The error message to display.
 */
@Composable
private fun RouteErrorDisplay(errorMessage: String?) {
  errorMessage?.let { error ->
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error)
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

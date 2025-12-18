/* Portions of the code in this file were written with the help of Gemini and Claude. */
package ch.eureka.eurekapp.ui.map

import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [MeetingLocationSelectionViewModel].
 *
 * Note : some tests where generated with Gemini.
 */
class MeetingLocationSelectionViewModelTest {

  private lateinit var viewModel: MeetingLocationSelectionViewModel
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  @Before
  fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    viewModel = MeetingLocationSelectionViewModel()
  }

  @Test
  fun meetingLocationSelectionViewModel_initialStateIsCorrect() {
    val state = viewModel.uiState.value

    assertNull(state.selectedLocation)
    assertNotNull(state.initialCameraPosition)
  }

  @Test
  fun meetingLocationSelectionViewModel_selectLocationWithNameUpdatesStateWithPoiName() {
    val latLng = LatLng(40.7128, -74.0060)
    val poiName = "Empire State Building"

    viewModel.selectLocation(latLng, poiName)

    val selected = viewModel.uiState.value.selectedLocation
    assertNotNull(selected)
    assertEquals(40.7128, selected!!.latitude, 0.0)
    assertEquals(-74.0060, selected.longitude, 0.0)
    assertEquals(poiName, selected.name)
  }

  @Test
  fun meetingLocationSelectionViewModel_selectLocationWithoutNameFormatsCoordinatesCorrectly() {
    val latLng = LatLng(46.519655, 6.632273)

    viewModel.selectLocation(latLng, null)

    val selected = viewModel.uiState.value.selectedLocation
    assertNotNull(selected)
    assertEquals(46.519655, selected!!.latitude, 0.0)
    assertEquals(6.632273, selected.longitude, 0.0)

    val expectedName = "46.5197, 6.6323"
    assertEquals(expectedName, selected.name)
  }

  @Test
  fun meetingLocationSelectionViewModel_uiStateCopyWorksAsExpected() {
    val latLng = LatLng(0.0, 0.0)
    val state =
        MeetingLocationSelectionUIState(selectedLocation = null, initialCameraPosition = latLng)

    val newLocation = Location(1.0, 1.0, "Test")
    val newState = state.copy(selectedLocation = newLocation)

    assertNotNull(newState.selectedLocation)
    assertEquals("Test", newState.selectedLocation?.name)
    assertEquals(latLng, newState.initialCameraPosition)
  }
}

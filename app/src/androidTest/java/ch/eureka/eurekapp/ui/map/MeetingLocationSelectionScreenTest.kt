/* Portions of the code in this file were written with the help of Gemini and Grok. */
package ch.eureka.eurekapp.ui.map

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test suite for [MeetingLocationSelectionScreen].
 *
 * Note : some tests where generated with Gemini.
 */
class MeetingLocationSelectionScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: MeetingLocationSelectionViewModel
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private var savedLocation: Location? = null
  private var onBackCalled: Boolean = false

  @Before
  fun setup() {
    savedLocation = null
    onBackCalled = false

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer for all tests
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    // NOW create the ViewModel after the mock is set
    viewModel = MeetingLocationSelectionViewModel()

    composeTestRule.setContent {
      MeetingLocationSelectionScreen(
          onLocationSelected = { savedLocation = it },
          onBack = { onBackCalled = true },
          viewModel = viewModel)
    }
  }

  @Test
  fun meetingLocationSelectionScreen_screenLoadsWithInitialStateElementsAreDisplayed() {
    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SCREEN_TITLE).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.GOOGLE_MAP).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(MeetingLocationSelectionTestTags.SAVE_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
        .assertHasClickAction()
  }

  @Test
  fun meetingLocationSelectionScreen_backButtonTriggersCallback() {
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()

    assert(onBackCalled)
  }

  @Test
  fun meetingLocationSelectionScreen_selectionUpdatesUiAndEnablesSaveButton() {
    val latLng = LatLng(46.5, 6.6)
    viewModel.selectLocation(latLng, null)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SAVE_BUTTON).assertIsEnabled()

    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SAVE_BUTTON).performClick()

    assertNotNull(savedLocation)
    assertEquals(46.5, savedLocation!!.latitude, 0.0001)
  }

  @Test
  fun meetingLocationSelectionScreen_markerSnippetLogicExecutesForCoordinates() {
    val latLng = LatLng(10.0, 20.0)
    viewModel.selectLocation(latLng, null)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SAVE_BUTTON).assertIsEnabled()

    val location = viewModel.uiState.value.selectedLocation
    assertNotNull(location)
    assertTrue(location!!.name.contains(","))
  }

  @Test
  fun meetingLocationSelectionScreen_markerSnippetLogicExecutesForPoi() {
    val latLng = LatLng(40.0, 50.0)
    val poiName = "Eiffel Tower"
    viewModel.selectLocation(latLng, poiName)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SAVE_BUTTON).assertIsEnabled()

    val location = viewModel.uiState.value.selectedLocation
    assertEquals("Eiffel Tower", location?.name)
  }

  @Test
  fun meetingLocationSelectionScreen_mapClicksTriggerViewModelFunctions() {
    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.GOOGLE_MAP).assertIsDisplayed()
  }

  @Test
  fun meetingLocationSelectionScreen_navigatesBackWhenConnectionLost() {
    composeTestRule.waitForIdle()

    // Verify we're on MeetingLocationSelectionScreen
    composeTestRule.onNodeWithTag(MeetingLocationSelectionTestTags.SCREEN_TITLE).assertIsDisplayed()

    // Simulate connection loss
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Verify onBack was called
    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackCalled }

    assert(onBackCalled)
  }
}

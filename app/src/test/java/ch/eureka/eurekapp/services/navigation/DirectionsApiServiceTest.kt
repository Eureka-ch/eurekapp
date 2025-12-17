/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.services.navigation

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/** Tests for DirectionsApiService and related utility functions. */
class DirectionsApiServiceTest {

  private lateinit var mockWebServer: MockWebServer
  private lateinit var service: DirectionsApiService

  @Before
  fun setup() {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    val json = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }

    val retrofit =
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    service = retrofit.create(DirectionsApiService::class.java)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun directionsApiService_getDirectionsReturnsSuccessfulResponseWithValidData() = runTest {
    val mockResponse =
        """
            {
                "routes": [{
                    "legs": [{
                        "distance": {"text": "5.2 km", "value": 5200},
                        "duration": {"text": "12 mins", "value": 720},
                        "start_address": "Start Location",
                        "end_address": "End Location",
                        "start_location": {"lat": 46.5197, "lng": 6.6323},
                        "end_location": {"lat": 46.5291, "lng": 6.6489},
                        "steps": [{
                            "distance": {"text": "1.0 km", "value": 1000},
                            "duration": {"text": "3 mins", "value": 180},
                            "start_location": {"lat": 46.5197, "lng": 6.6323},
                            "end_location": {"lat": 46.5220, "lng": 6.6350},
                            "html_instructions": "Turn <b>right</b> onto Main St",
                            "polyline": {"points": "encodedPolyline"},
                            "travel_mode": "DRIVING"
                        }]
                    }],
                    "overview_polyline": {"points": "encodedOverviewPolyline"},
                    "summary": "Main Route via Highway",
                    "warnings": []
                }],
                "status": "OK"
            }
        """
            .trimIndent()

    mockWebServer.enqueue(MockResponse().setBody(mockResponse))

    val response =
        service.getDirections(
            origin = "46.5197,6.6323",
            destination = "46.5291,6.6489",
            mode = "driving",
            apiKey = "test_key")

    assertEquals("OK", response.status)
    assertEquals(1, response.routes.size)

    val route = response.routes[0]
    assertEquals("Main Route via Highway", route.summary)
    assertEquals("encodedOverviewPolyline", route.overviewPolyline.points)

    val leg = route.legs[0]
    assertEquals("5.2 km", leg.distance.text)
    assertEquals(5200, leg.distance.value)
    assertEquals("12 mins", leg.duration.text)
    assertEquals(720, leg.duration.value)

    val step = leg.steps[0]
    assertEquals("Turn <b>right</b> onto Main St", step.htmlInstructions)
    assertEquals("DRIVING", step.travelMode)
  }

  @Test
  fun directionsApiService_getDirectionsHandlesErrorResponse() = runTest {
    val mockResponse =
        """
            {
                "routes": [],
                "status": "ZERO_RESULTS",
                "error_message": "No route found"
            }
        """
            .trimIndent()

    mockWebServer.enqueue(MockResponse().setBody(mockResponse))

    val response =
        service.getDirections(
            origin = "0,0", destination = "0,0", mode = "driving", apiKey = "test_key")

    assertEquals("ZERO_RESULTS", response.status)
    assertEquals("No route found", response.errorMessage)
    assertTrue(response.routes.isEmpty())
  }

  @Test
  fun directionsApiService_decodePolylineCorrectlyDecodesGooglePolylineFormat() {
    val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
    val decoded = DirectionsUtils.decodePolyline(encoded)

    assertTrue(decoded.isNotEmpty())
    assertEquals(3, decoded.size)

    // Verify approximate coordinates (polyline encoding has some precision loss)
    val (lat1, lng1) = decoded[0]
    assertEquals(38.5, lat1, 0.1)
    assertEquals(-120.2, lng1, 0.1)
  }

  @Test
  fun directionsApiService_decodePolylineHandlesEmptyString() {
    val decoded = DirectionsUtils.decodePolyline("")
    assertTrue(decoded.isEmpty())
  }

  @Test
  fun directionsApiService_formatLocationCreatesCorrectParameterString() {
    val result = DirectionsUtils.formatLocation(46.5197, 6.6323)
    assertEquals("46.5197,6.6323", result)
  }

  @Test
  fun directionsApiService_formatLocationHandlesNegativeCoordinates() {
    val result = DirectionsUtils.formatLocation(-34.6037, -58.3816)
    assertEquals("-34.6037,-58.3816", result)
  }

  @Test
  fun directionsApiService_formatTravelModeConvertsToLowercase() {
    assertEquals("driving", DirectionsUtils.formatTravelMode("DRIVING"))
    assertEquals("walking", DirectionsUtils.formatTravelMode("WALKING"))
    assertEquals("bicycling", DirectionsUtils.formatTravelMode("BICYCLING"))
    assertEquals("transit", DirectionsUtils.formatTravelMode("TRANSIT"))
  }

  @Test
  fun directionsApiService_stripHtmlTagsRemovesHtmlFromInstructions() {
    val html = "Turn <b>right</b> onto <div>Main St</div>"
    val result = DirectionsUtils.stripHtmlTags(html)
    assertEquals("Turn right onto Main St", result)
  }

  @Test
  fun directionsApiService_stripHtmlTagsHandlesPlainText() {
    val plain = "Continue straight"
    val result = DirectionsUtils.stripHtmlTags(plain)
    assertEquals("Continue straight", result)
  }

  @Test
  fun directionsApiService_stripHtmlTagsHandlesComplexHtml() {
    val html = "Turn <b>left</b> <div style='color:red'>at the <span>traffic light</span></div>"
    val result = DirectionsUtils.stripHtmlTags(html)
    assertEquals("Turn left at the traffic light", result)
  }

  @Test
  fun directionsApiService_directionsApiServiceFactoryCreatesServiceSuccessfully() {
    val service = DirectionsApiServiceFactory.create()
    assertNotNull(service)
  }

  @Test
  fun directionsApiService_serviceHandlesMultipleWaypointsInLegs() = runTest {
    val mockResponse =
        """
            {
                "routes": [{
                    "legs": [
                        {
                            "distance": {"text": "3 km", "value": 3000},
                            "duration": {"text": "8 mins", "value": 480},
                            "start_address": "Start",
                            "end_address": "Waypoint 1",
                            "start_location": {"lat": 46.5197, "lng": 6.6323},
                            "end_location": {"lat": 46.5250, "lng": 6.6400},
                            "steps": []
                        },
                        {
                            "distance": {"text": "2 km", "value": 2000},
                            "duration": {"text": "5 mins", "value": 300},
                            "start_address": "Waypoint 1",
                            "end_address": "End",
                            "start_location": {"lat": 46.5250, "lng": 6.6400},
                            "end_location": {"lat": 46.5291, "lng": 6.6489},
                            "steps": []
                        }
                    ],
                    "overview_polyline": {"points": "test"},
                    "summary": "Test Route"
                }],
                "status": "OK"
            }
        """
            .trimIndent()

    mockWebServer.enqueue(MockResponse().setBody(mockResponse))

    val response =
        service.getDirections(
            origin = "46.5197,6.6323",
            destination = "46.5291,6.6489",
            mode = "driving",
            apiKey = "test_key")

    assertEquals(2, response.routes[0].legs.size)
    assertEquals("Start", response.routes[0].legs[0].startAddress)
    assertEquals("End", response.routes[0].legs[1].endAddress)
  }

  @Test
  fun directionsApiService_directionsUtilsFormatLocationFormatsCorrectly() {
    val result = DirectionsUtils.formatLocation(46.5197, 6.5659)
    assertEquals("46.5197,6.5659", result)
  }

  @Test
  fun directionsApiService_directionsUtilsFormatTravelModeConvertsToLowercase() {
    assertEquals("driving", DirectionsUtils.formatTravelMode("DRIVING"))
    assertEquals("walking", DirectionsUtils.formatTravelMode("Walking"))
    assertEquals("bicycling", DirectionsUtils.formatTravelMode("BICYCLING"))
  }

  @Test
  fun directionsApiService_directionsUtilsStripHtmlTagsRemovesAllHtml() {
    val html = "<b>Turn left</b> onto <div style='color:red'>Main Street</div>"
    val result = DirectionsUtils.stripHtmlTags(html)
    assertEquals("Turn left onto Main Street", result)
  }

  @Test
  fun directionsApiService_directionsUtilsStripHtmlTagsHandlesPlainText() {
    val plainText = "Continue straight"
    val result = DirectionsUtils.stripHtmlTags(plainText)
    assertEquals("Continue straight", result)
  }

  @Test
  fun directionsApiService_directionsUtilsDecodePolylineDecodesSimplePolyline() {
    // Encoded polyline for a simple 2-point line
    val encoded = "_p~iF~ps|U_ulLnnqC"
    val decoded = DirectionsUtils.decodePolyline(encoded)

    assertTrue(decoded.isNotEmpty())
    assertEquals(2, decoded.size)
  }

  @Test
  fun directionsApiService_directionsUtilsDecodePolylineHandlesEmptyString() {
    val decoded = DirectionsUtils.decodePolyline("")
    assertTrue(decoded.isEmpty())
  }

  // Note: DNS fallback logic (lines 115-141 in DirectionsApiService.kt) is difficult to unit test
  // because the googleDns resolver is private and MockWebServer bypasses DNS lookup entirely.
  // This fallback is tested through:
  // 1. Integration tests on real devices/emulators
  // 2. Manual testing with network conditions
  // The DNS fallback provides redundancy for maps.googleapis.com when system DNS fails,
  // using hardcoded IP addresses (142.250.185.3, 142.250.185.35) as fallback.

  @Test
  fun directionsApiService_directionsResponseSerializationHandlesAllFields() {
    val json =
        """
      {
        "routes": [],
        "status": "ZERO_RESULTS",
        "error_message": "No routes found"
      }
    """
            .trimIndent()

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val response = jsonParser.decodeFromString<DirectionsResponse>(json)

    assertEquals("ZERO_RESULTS", response.status)
    assertEquals("No routes found", response.errorMessage)
    assertTrue(response.routes.isEmpty())
  }

  @Test
  fun directionsApiService_routeSerializationHandlesAllFields() {
    val json =
        """
      {
        "legs": [],
        "overview_polyline": {"points": "encoded"},
        "summary": "Test Route",
        "warnings": ["Warning 1", "Warning 2"]
      }
    """
            .trimIndent()

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val route = jsonParser.decodeFromString<Route>(json)

    assertEquals("Test Route", route.summary)
    assertEquals("encoded", route.overviewPolyline.points)
    assertEquals(2, route.warnings.size)
    assertTrue(route.legs.isEmpty())
  }

  @Test
  fun directionsApiService_legSerializationHandlesAllFields() {
    val json =
        """
      {
        "distance": {"text": "5 km", "value": 5000},
        "duration": {"text": "10 mins", "value": 600},
        "start_address": "Start",
        "end_address": "End",
        "start_location": {"lat": 46.5, "lng": 6.6},
        "end_location": {"lat": 46.6, "lng": 6.7},
        "steps": []
      }
    """
            .trimIndent()

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val leg = jsonParser.decodeFromString<Leg>(json)

    assertEquals("5 km", leg.distance.text)
    assertEquals(5000, leg.distance.value)
    assertEquals("10 mins", leg.duration.text)
    assertEquals(600, leg.duration.value)
    assertEquals("Start", leg.startAddress)
    assertEquals("End", leg.endAddress)
    assertEquals(46.5, leg.startLocation.latitude, 0.001)
    assertEquals(6.6, leg.startLocation.longitude, 0.001)
  }

  @Test
  fun directionsApiService_stepSerializationHandlesAllFieldsIncludingOptionalManeuver() {
    val jsonWithManeuver =
        """
      {
        "distance": {"text": "100 m", "value": 100},
        "duration": {"text": "1 min", "value": 60},
        "start_location": {"lat": 46.5, "lng": 6.6},
        "end_location": {"lat": 46.51, "lng": 6.61},
        "html_instructions": "Turn left",
        "polyline": {"points": "abc"},
        "travel_mode": "DRIVING",
        "maneuver": "turn-left"
      }
    """
            .trimIndent()

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val step = jsonParser.decodeFromString<Step>(jsonWithManeuver)

    assertEquals("Turn left", step.htmlInstructions)
    assertEquals("DRIVING", step.travelMode)
    assertEquals("turn-left", step.maneuver)
    assertEquals(100, step.distance.value)
  }

  @Test
  fun directionsApiService_stepSerializationHandlesNullManeuver() {
    val jsonWithoutManeuver =
        """
      {
        "distance": {"text": "100 m", "value": 100},
        "duration": {"text": "1 min", "value": 60},
        "start_location": {"lat": 46.5, "lng": 6.6},
        "end_location": {"lat": 46.51, "lng": 6.61},
        "html_instructions": "Continue",
        "polyline": {"points": "abc"},
        "travel_mode": "WALKING"
      }
    """
            .trimIndent()

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val step = jsonParser.decodeFromString<Step>(jsonWithoutManeuver)

    assertEquals("Continue", step.htmlInstructions)
    assertEquals("WALKING", step.travelMode)
    assertNull(step.maneuver)
  }

  @Test
  fun directionsApiService_textValueSerializationHandlesBothTextAndValue() {
    val json = """{"text": "5.2 km", "value": 5200}"""

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val textValue = jsonParser.decodeFromString<TextValue>(json)

    assertEquals("5.2 km", textValue.text)
    assertEquals(5200, textValue.value)
  }

  @Test
  fun directionsApiService_locationDataSerializationHandlesLatLngFields() {
    val json = """{"lat": 46.5197, "lng": 6.6323}"""

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val location = jsonParser.decodeFromString<LocationData>(json)

    assertEquals(46.5197, location.latitude, 0.0001)
    assertEquals(6.6323, location.longitude, 0.0001)
  }

  @Test
  fun directionsApiService_polylineDataSerializationHandlesPointsField() {
    val json = """{"points": "_p~iF~ps|U_ulLnnqC"}"""

    val jsonParser = Json {
      ignoreUnknownKeys = true
      coerceInputValues = true
    }
    val polyline = jsonParser.decodeFromString<PolylineData>(json)

    assertEquals("_p~iF~ps|U_ulLnnqC", polyline.points)
  }
}

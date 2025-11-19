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
  fun `getDirections returns successful response with valid data`() = runTest {
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
  fun `getDirections handles error response`() = runTest {
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
  fun `decodePolyline correctly decodes Google polyline format`() {
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
  fun `decodePolyline handles empty string`() {
    val decoded = DirectionsUtils.decodePolyline("")
    assertTrue(decoded.isEmpty())
  }

  @Test
  fun `formatLocation creates correct parameter string`() {
    val result = DirectionsUtils.formatLocation(46.5197, 6.6323)
    assertEquals("46.5197,6.6323", result)
  }

  @Test
  fun `formatLocation handles negative coordinates`() {
    val result = DirectionsUtils.formatLocation(-34.6037, -58.3816)
    assertEquals("-34.6037,-58.3816", result)
  }

  @Test
  fun `formatTravelMode converts to lowercase`() {
    assertEquals("driving", DirectionsUtils.formatTravelMode("DRIVING"))
    assertEquals("walking", DirectionsUtils.formatTravelMode("WALKING"))
    assertEquals("bicycling", DirectionsUtils.formatTravelMode("BICYCLING"))
    assertEquals("transit", DirectionsUtils.formatTravelMode("TRANSIT"))
  }

  @Test
  fun `stripHtmlTags removes HTML from instructions`() {
    val html = "Turn <b>right</b> onto <div>Main St</div>"
    val result = DirectionsUtils.stripHtmlTags(html)
    assertEquals("Turn right onto Main St", result)
  }

  @Test
  fun `stripHtmlTags handles plain text`() {
    val plain = "Continue straight"
    val result = DirectionsUtils.stripHtmlTags(plain)
    assertEquals("Continue straight", result)
  }

  @Test
  fun `stripHtmlTags handles complex HTML`() {
    val html = "Turn <b>left</b> <div style='color:red'>at the <span>traffic light</span></div>"
    val result = DirectionsUtils.stripHtmlTags(html)
    assertEquals("Turn left at the traffic light", result)
  }

  @Test
  fun `DirectionsApiServiceFactory creates service successfully`() {
    val service = DirectionsApiServiceFactory.create("test_api_key")
    assertNotNull(service)
  }

  @Test
  fun `DirectionsApiServiceFactory creates service without API key`() {
    val service = DirectionsApiServiceFactory.create()
    assertNotNull(service)
  }

  @Test
  fun `service handles multiple waypoints in legs`() = runTest {
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
}

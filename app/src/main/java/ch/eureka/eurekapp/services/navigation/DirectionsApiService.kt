/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.services.navigation

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.InetAddress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/** Data models for Google Directions API responses. */

/**
 * Top-level response from Directions API.
 *
 * @property routes List of possible routes from origin to destination
 * @property status Response status code (OK, ZERO_RESULTS, etc.)
 * @property errorMessage Human-readable error message if status is not OK
 */
@Serializable
data class DirectionsResponse(
    val routes: List<Route> = emptyList(),
    val status: String,
    @SerialName("error_message") val errorMessage: String? = null
)

/**
 * A single route from origin to destination.
 *
 * @property legs Route segments, typically one per waypoint
 * @property overviewPolyline Encoded polyline for the entire route
 * @property summary Short description of the route
 * @property warnings Advisory notices about the route
 */
@Serializable
data class Route(
    val legs: List<Leg> = emptyList(),
    @SerialName("overview_polyline") val overviewPolyline: PolylineData,
    val summary: String = "",
    val warnings: List<String> = emptyList()
)

/**
 * A leg of the route (typically one per waypoint).
 *
 * @property distance Total distance for this leg
 * @property duration Estimated travel time for this leg
 * @property startAddress Human-readable starting address
 * @property endAddress Human-readable ending address
 * @property startLocation Geographic coordinates of start point
 * @property endLocation Geographic coordinates of end point
 * @property steps Turn-by-turn navigation steps
 */
@Serializable
data class Leg(
    val distance: TextValue,
    val duration: TextValue,
    @SerialName("start_address") val startAddress: String,
    @SerialName("end_address") val endAddress: String,
    @SerialName("start_location") val startLocation: LocationData,
    @SerialName("end_location") val endLocation: LocationData,
    val steps: List<Step> = emptyList()
)

/**
 * A single step in the navigation with instructions.
 *
 * @property distance Distance covered in this step
 * @property duration Time required for this step
 * @property startLocation Geographic coordinates where step begins
 * @property endLocation Geographic coordinates where step ends
 * @property htmlInstructions Navigation instruction with HTML formatting
 * @property polyline Encoded polyline for this step's path
 * @property travelMode Mode of travel (DRIVING, WALKING, etc.)
 * @property maneuver Optional maneuver type (turn-left, turn-right, etc.)
 */
@Serializable
data class Step(
    val distance: TextValue,
    val duration: TextValue,
    @SerialName("start_location") val startLocation: LocationData,
    @SerialName("end_location") val endLocation: LocationData,
    @SerialName("html_instructions") val htmlInstructions: String,
    val polyline: PolylineData,
    @SerialName("travel_mode") val travelMode: String,
    val maneuver: String? = null
)

/**
 * Distance or duration with text and numeric value.
 *
 * @property text Human-readable formatted value (e.g., "5.2 km", "12 mins")
 * @property value Numeric value in base units (meters for distance, seconds for duration)
 */
@Serializable data class TextValue(val text: String, val value: Int)

/**
 * Geographic location as latitude/longitude.
 *
 * @property latitude Latitude in degrees (-90 to 90)
 * @property longitude Longitude in degrees (-180 to 180)
 */
@Serializable
data class LocationData(
    @SerialName("lat") val latitude: Double,
    @SerialName("lng") val longitude: Double
)

/**
 * Encoded polyline data for route visualization.
 *
 * @property points Google-encoded polyline string representing the path
 */
@Serializable data class PolylineData(val points: String)

/** Retrofit service interface for Google Directions API. */
interface DirectionsApiService {
  /**
   * Get directions from origin to destination.
   *
   * @param origin Starting location as "lat,lng"
   * @param destination Ending location as "lat,lng"
   * @param mode Travel mode: driving, walking, bicycling, transit
   * @param apiKey Google Maps API key
   * @return DirectionsResponse with route data
   */
  @GET("maps/api/directions/json")
  suspend fun getDirections(
      @Query("origin") origin: String,
      @Query("destination") destination: String,
      @Query("mode") mode: String = "driving",
      @Query("key") apiKey: String
  ): DirectionsResponse
}

/** Factory for creating DirectionsApiService instances. */
object DirectionsApiServiceFactory {
  /** Base URL for Google Maps Directions API. */
  private const val BASE_URL = "https://maps.googleapis.com/"

  /** Hostname for Google Maps API used in DNS fallback. */
  private const val MAPS_HOSTNAME = "maps.googleapis.com"

  /** JSON configuration for parsing API responses with lenient handling. */
  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  /**
   * Custom DNS implementation with hardcoded fallback IPs.
   *
   * **EMULATOR-ONLY WORKAROUND**: This DNS resolver exists solely to work around broken DNS
   * resolution in Android emulators. It attempts system DNS first, then falls back to hardcoded
   * Google Maps API IP addresses when DNS lookup fails.
   *
   * On real devices, system DNS works correctly and the fallback is never used.
   */
  private val googleDns =
      object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
          return try {
            // Try system DNS first
            Dns.SYSTEM.lookup(hostname)
          } catch (_: Exception) {
            // Fallback: Use known IP addresses for maps.googleapis.com
            when (hostname) {
              MAPS_HOSTNAME -> {
                // Google Maps API IP addresses (multiple for redundancy)
                listOf(
                    InetAddress.getByAddress(
                        MAPS_HOSTNAME,
                        byteArrayOf(142.toByte(), 250.toByte(), 185.toByte(), 3.toByte())),
                    InetAddress.getByAddress(
                        MAPS_HOSTNAME,
                        byteArrayOf(142.toByte(), 250.toByte(), 185.toByte(), 35.toByte())))
              }
              else -> {
                // For other hostnames, try to use Google's public DNS
                InetAddress.getAllByName(hostname).toList()
              }
            }
          }
        }
      }

  /**
   * Create a DirectionsApiService instance.
   *
   * @return Configured DirectionsApiService with custom DNS fallback
   */
  fun create(): DirectionsApiService {
    val client = OkHttpClient.Builder().dns(googleDns).build()

    val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    return retrofit.create(DirectionsApiService::class.java)
  }
}

/** Utility functions for working with directions data. */
object DirectionsUtils {
  /**
   * Decode a polyline string into a list of LatLng points. Implements Google's polyline encoding
   * algorithm.
   *
   * @param encoded The encoded polyline string
   * @return List of latitude/longitude pairs
   */
  fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
    val poly = mutableListOf<Pair<Double, Double>>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
      var result = 1
      var shift = 0
      var b: Int
      do {
        b = encoded[index++].code - 63 - 1
        result += b shl shift
        shift += 5
      } while (b >= 0x1f)
      lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

      result = 1
      shift = 0
      do {
        b = encoded[index++].code - 63 - 1
        result += b shl shift
        shift += 5
      } while (b >= 0x1f)
      lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

      poly.add(Pair(lat / 1E5, lng / 1E5))
    }

    return poly
  }

  /**
   * Format origin/destination parameter from latitude and longitude.
   *
   * @param latitude The latitude
   * @param longitude The longitude
   * @return Formatted string "lat,lng"
   */
  fun formatLocation(latitude: Double, longitude: Double): String {
    return "$latitude,$longitude"
  }

  /**
   * Convert travel mode enum to API parameter.
   *
   * @param mode The travel mode
   * @return API parameter string
   */
  fun formatTravelMode(mode: String): String {
    return mode.lowercase()
  }

  /**
   * Strip HTML tags from instruction text.
   *
   * @param html HTML instruction string
   * @return Plain text instruction
   */
  fun stripHtmlTags(html: String): String {
    return html
        .replace("<b>", "")
        .replace("</b>", "")
        .replace("<div[^>]*>", " ")
        .replace("</div>", "")
        .replace(Regex("<[^>]*>"), "")
        .trim()
  }
}

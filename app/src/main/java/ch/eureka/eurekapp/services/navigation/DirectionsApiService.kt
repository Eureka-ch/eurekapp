/*
Note: This file was co-authored by Claude Code.
*/
package ch.eureka.eurekapp.services.navigation

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Data models for Google Directions API responses.
 */

/** Top-level response from Directions API. */
@Serializable
data class DirectionsResponse(
    val routes: List<Route> = emptyList(),
    val status: String,
    @SerialName("error_message") val errorMessage: String? = null
)

/** A single route from origin to destination. */
@Serializable
data class Route(
    val legs: List<Leg> = emptyList(),
    @SerialName("overview_polyline") val overviewPolyline: PolylineData,
    val summary: String = "",
    val warnings: List<String> = emptyList()
)

/** A leg of the route (typically one per waypoint). */
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

/** A single step in the navigation with instructions. */
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

/** Distance or duration with text and numeric value. */
@Serializable
data class TextValue(
    val text: String,
    val value: Int // meters for distance, seconds for duration
)

/** Geographic location as latitude/longitude. */
@Serializable
data class LocationData(
    @SerialName("lat") val latitude: Double,
    @SerialName("lng") val longitude: Double
)

/** Encoded polyline data for route visualization. */
@Serializable
data class PolylineData(
    val points: String // Encoded polyline string
)

/**
 * Retrofit service interface for Google Directions API.
 */
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

/**
 * Factory for creating DirectionsApiService instances.
 */
object DirectionsApiServiceFactory {
    private const val BASE_URL = "https://maps.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true // Ignore fields we don't need
        coerceInputValues = true // Handle nulls gracefully
    }

    /**
     * Custom DNS implementation using Google's public DNS servers.
     * Fallback for emulators with broken DNS resolution.
     */
    private val googleDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                // Try system DNS first
                Dns.SYSTEM.lookup(hostname)
            } catch (e: Exception) {
                // Fallback: Use known IP addresses for maps.googleapis.com
                when (hostname) {
                    "maps.googleapis.com" -> {
                        // Google Maps API IP addresses (multiple for redundancy)
                        listOf(
                            InetAddress.getByAddress("maps.googleapis.com", byteArrayOf(142.toByte(), 250.toByte(), 185.toByte(), 3.toByte())),
                            InetAddress.getByAddress("maps.googleapis.com", byteArrayOf(142.toByte(), 250.toByte(), 185.toByte(), 35.toByte()))
                        )
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
     * @param apiKey Google Maps API key (optional, can be passed per request)
     * @return Configured DirectionsApiService
     */
    fun create(apiKey: String? = null): DirectionsApiService {
        val client = OkHttpClient.Builder()
            .dns(googleDns)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(DirectionsApiService::class.java)
    }
}

/**
 * Utility functions for working with directions data.
 */
object DirectionsUtils {
    /**
     * Decode a polyline string into a list of LatLng points.
     * Implements Google's polyline encoding algorithm.
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

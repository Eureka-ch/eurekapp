/*
Portions of this file where written with the help of chatGPT and Gemini.
Portions of this file are copy-pasted from the bootcamp provided by the SwEnt staff.
 */
package ch.eureka.eurekapp.model.map

import android.util.Log
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException // <--- Added Import

class NominatimLocationRepository(val client: OkHttpClient) : LocationRepository {
  override suspend fun search(query: String): List<Location> {

    val url =
        HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("format", "json")
            .build()

    val request =
        Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "Eurekapp Android/1.0 (eurekapp.ch@gmail.com)")
            .build()

    val response = client.newCall(request).awaitResponse()

    response.use {
      if (!response.isSuccessful) {
        Log.e("NominatimLocationRepository", "HTTP error ${response.code}")
        return emptyList()
      }

      val bodyString = response.body?.string() ?: throw IOException("Empty body")

      return parseJsonResponse(bodyString)
    }
  }

  private suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(
        object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) {
              continuation.resumeWithException(e)
            }
          }

          override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) {
              continuation.resume(response)
            } else {
              response.close()
            }
          }
        })

    continuation.invokeOnCancellation {
      try {
        cancel()
      } catch (_: Throwable) {}
    }
  }

  private fun parseJsonResponse(jsonStr: String): List<Location> {
    try {
      val location = mutableListOf<Location>()
      val jsonArray = JSONArray(jsonStr)

      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)

        val name = obj.optString("name")
        val lat = obj.optString("lat").toDoubleOrNull()
        val lon = obj.optString("lon").toDoubleOrNull()

        if (lat != null && lon != null && name.isNotEmpty()) {
          location.add(Location(lat, lon, name))
        }
      }

      return location.toList()
    } catch (e: JSONException) {
      Log.e("NominatimLocationRepository", "Error parsing JSON", e)
      return emptyList()
    }
  }
}

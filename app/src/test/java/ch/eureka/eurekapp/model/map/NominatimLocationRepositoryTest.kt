/*
Portions of the code in this file were written with the help of Gemini.
*/
package ch.eureka.eurekapp.model.map

import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [NominatimLocationRepository].
 *
 * Note : some tests where generated with the help of Gemini.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NominatimLocationRepositoryTest {

  @MockK private lateinit var mockClient: OkHttpClient

  @MockK private lateinit var mockCall: Call

  @MockK private lateinit var mockResponse: Response

  @MockK private lateinit var mockResponseBody: ResponseBody

  private lateinit var repository: NominatimLocationRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = NominatimLocationRepository(mockClient)

    every { mockClient.newCall(any()) } returns mockCall

    mockkStatic(Log::class)
    every { Log.e(any(), any()) } returns 0
    every { Log.e(any(), any(), any()) } returns 0 // For the exception logging
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `search returns list of locations on successful response`() = runTest {
    val jsonResponse =
        """
            [
                {
                    "name": "EPFL",
                    "lat": "46.5191",
                    "lon": "6.5668"
                },
                {
                    "name": "Lausanne",
                    "lat": "46.5197",
                    "lon": "6.6323"
                }
            ]
        """
            .trimIndent()

    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns jsonResponse
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Switzerland")

    assertEquals(2, result.size)
    assertEquals("EPFL", result[0].name)
    assertEquals(46.5191, result[0].latitude, 0.0001)
    assertEquals(6.5668, result[0].longitude, 0.0001)
  }

  @Test
  fun `search returns empty list on HTTP error`() = runTest {
    every { mockResponse.isSuccessful } returns false
    every { mockResponse.code } returns 404
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Unknown")

    assertTrue(result.isEmpty())
    verify { Log.e("NominatimLocationRepository", "HTTP error 404") }
  }

  @Test
  fun `search throws IOException on network failure`() = runTest {
    val exception = IOException("No Internet")
    val slot = slot<Callback>()

    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onFailure(mockCall, exception)
        }

    try {
      repository.search("Query")
      org.junit.Assert.fail("Should have thrown IOException")
    } catch (e: IOException) {
      assertEquals("No Internet", e.message)
    }
  }

  @Test
  fun `search throws IOException on empty body`() = runTest {
    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns null
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    try {
      repository.search("Query")
      org.junit.Assert.fail("Should have thrown IOException")
    } catch (e: IOException) {
      assertEquals("Empty body", e.message)
    }
  }

  @Test
  fun `search filters out invalid locations in JSON`() = runTest {
    val jsonResponse =
        """
            [
                {
                    "name": "Valid Place",
                    "lat": "10.0",
                    "lon": "20.0"
                },
                {
                    "name": "Invalid Place",
                    "lat": "not_a_number",
                    "lon": "20.0"
                }
            ]
        """
            .trimIndent()

    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns jsonResponse
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Mix")

    assertEquals(1, result.size)
    assertEquals("Valid Place", result[0].name)
  }

  @Test
  fun `awaitResponse cancels call when coroutine is cancelled`() = runTest {
    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } returns Unit
    every { mockCall.cancel() } returns Unit

    val job = launch(UnconfinedTestDispatcher()) { repository.search("Cancel Me") }

    job.cancel()

    verify { mockCall.cancel() }
  }

  @Test
  fun `search returns empty list on malformed JSON`() = runTest {
    val badJson = "<html><body>Error 500</body></html>"

    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns badJson
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Bad JSON")

    assertTrue(result.isEmpty())
    verify { Log.e("NominatimLocationRepository", "Error parsing JSON", any()) }
  }

  @Test
  fun `search returns empty list when JSON is not an array`() = runTest {
    val errorJson = """ { "error": "Rate limit exceeded" } """

    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns errorJson
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Error Object")

    assertTrue(result.isEmpty())
    verify { Log.e("NominatimLocationRepository", "Error parsing JSON", any()) }
  }

  @Test
  fun `search skips items with missing fields`() = runTest {
    val jsonResponse =
        """
            [
                {
                    "name": "Valid Place",
                    "lat": "10.0",
                    "lon": "10.0"
                },
                {
                    "lat": "20.0",
                    "lon": "20.0"
                },
                {
                    "name": "Missing Lat",
                    "lon": "30.0"
                }
            ]
        """
            .trimIndent()

    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns jsonResponse
    every { mockResponse.close() } returns Unit

    val slot = slot<Callback>()
    every { mockCall.enqueue(capture(slot)) } answers
        {
          slot.captured.onResponse(mockCall, mockResponse)
        }

    val result = repository.search("Partial")

    assertEquals(1, result.size)
    assertEquals("Valid Place", result[0].name)
  }
}

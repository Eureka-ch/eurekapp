// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseMcpTokenRepositoryTest {

  private lateinit var mockWebServer: MockWebServer
  private lateinit var repository: FirebaseMcpTokenRepository
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    mockAuth = mockk()
    mockUser = mockk()

    val mockTokenResult = mockk<GetTokenResult>()
    every { mockTokenResult.token } returns "test-id-token"

    val mockTask: Task<GetTokenResult> = Tasks.forResult(mockTokenResult)
    every { mockUser.getIdToken(any()) } returns mockTask
    every { mockAuth.currentUser } returns mockUser

    repository =
        FirebaseMcpTokenRepository(
            auth = mockAuth,
            functionsBaseUrl = mockWebServer.url("/").toString().dropLast(1),
            ioDispatcher = testDispatcher)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun createToken_returnsTokenOnSuccess() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": true,
          "data": {
            "token": "mcp-token-123",
            "name": "My Token",
            "expiresAt": "2025-01-01T00:00:00Z"
          }
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.createToken("My Token", 30)

        assertTrue(result.isSuccess)
        val token = result.getOrNull()
        assertNotNull(token)
        assertEquals("mcp-token-123", token?.tokenId)
        assertEquals("My Token", token?.name)
      }

  @Test
  fun createToken_returnsFailureOnHttpError() =
      runTest(testDispatcher) {
        mockWebServer.enqueue(MockResponse().setBody("Server error").setResponseCode(500))

        val result = repository.createToken("My Token", 30)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to create token") == true)
      }

  @Test
  fun createToken_returnsFailureOnApiError() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": false,
          "error": "Token limit exceeded"
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.createToken("My Token", 30)

        assertTrue(result.isFailure)
        assertEquals("Token limit exceeded", result.exceptionOrNull()?.message)
      }

  @Test
  fun revokeToken_returnsSuccess() =
      runTest(testDispatcher) {
        val responseBody = """{"success": true}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.revokeToken("token-123")

        assertTrue(result.isSuccess)
      }

  @Test
  fun revokeToken_returnsFailureOnHttpError() =
      runTest(testDispatcher) {
        mockWebServer.enqueue(MockResponse().setBody("Not found").setResponseCode(404))

        val result = repository.revokeToken("token-123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to revoke token") == true)
      }

  @Test
  fun revokeToken_returnsFailureOnApiError() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": false,
          "error": "Token not found"
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.revokeToken("token-123")

        assertTrue(result.isFailure)
        assertEquals("Token not found", result.exceptionOrNull()?.message)
      }

  @Test
  fun listTokens_returnsTokensOnSuccess() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": true,
          "data": [
            {
              "tokenId": "token-1",
              "name": "Token One",
              "createdAt": "2025-01-01T00:00:00Z",
              "expiresAt": "2025-02-01T00:00:00Z"
            },
            {
              "tokenId": "token-2",
              "name": "Token Two",
              "createdAt": "2025-01-02T00:00:00Z",
              "expiresAt": "2025-02-02T00:00:00Z"
            }
          ]
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.listTokens()

        assertTrue(result.isSuccess)
        val tokens = result.getOrNull()
        assertNotNull(tokens)
        assertEquals(2, tokens?.size)
        assertEquals("token-1", tokens?.get(0)?.tokenId)
        assertEquals("Token One", tokens?.get(0)?.name)
        assertEquals("token-2", tokens?.get(1)?.tokenId)
      }

  @Test
  fun listTokens_returnsEmptyListWhenNoTokens() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": true,
          "data": []
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.listTokens()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
      }

  @Test
  fun listTokens_returnsFailureOnHttpError() =
      runTest(testDispatcher) {
        mockWebServer.enqueue(MockResponse().setBody("Unauthorized").setResponseCode(401))

        val result = repository.listTokens()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to list tokens") == true)
      }

  @Test
  fun listTokens_returnsFailureOnApiError() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": false,
          "error": "Permission denied"
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.listTokens()

        assertTrue(result.isFailure)
        assertEquals("Permission denied", result.exceptionOrNull()?.message)
      }

  @Test
  fun listTokens_handlesMissingOptionalFields() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": true,
          "data": [
            {
              "tokenId": "token-1",
              "name": ""
            }
          ]
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.listTokens()

        assertTrue(result.isSuccess)
        val token = result.getOrNull()?.firstOrNull()
        assertNotNull(token)
        assertEquals("token-1", token?.tokenId)
        assertEquals("", token?.name)
        assertEquals(null, token?.createdAt)
        assertEquals(null, token?.expiresAt)
      }

  @Test
  fun repository_throwsWhenUserNotAuthenticated() =
      runTest(testDispatcher) {
        every { mockAuth.currentUser } returns null

        val result = repository.createToken("Test", 30)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
      }

  @Test
  fun repository_throwsWhenIdTokenIsNull() =
      runTest(testDispatcher) {
        val mockTokenResult = mockk<GetTokenResult>()
        every { mockTokenResult.token } returns null
        val mockTask: Task<GetTokenResult> = Tasks.forResult(mockTokenResult)
        every { mockUser.getIdToken(any()) } returns mockTask

        val result = repository.createToken("Test", 30)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
      }

  @Test
  fun createToken_sendsCorrectRequestBody() =
      runTest(testDispatcher) {
        val responseBody =
            """
        {
          "success": true,
          "data": {
            "token": "mcp-token-123",
            "name": "My Token",
            "expiresAt": "2025-01-01T00:00:00Z"
          }
        }
        """
                .trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        repository.createToken("My Token", 60)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("mcpCreateToken") == true)
        assertTrue(request.body.readUtf8().contains("\"name\":\"My Token\""))
        assertTrue(request.body.readUtf8().contains("\"ttlDays\":60") == false)
      }

  @Test
  fun revokeToken_sendsCorrectRequestBody() =
      runTest(testDispatcher) {
        val responseBody = """{"success": true}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        repository.revokeToken("token-to-revoke")

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("mcpRevokeToken") == true)
        assertTrue(request.body.readUtf8().contains("\"tokenId\":\"token-to-revoke\""))
      }

  @Test
  fun listTokens_sendsGetRequest() =
      runTest(testDispatcher) {
        val responseBody = """{"success": true, "data": []}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        repository.listTokens()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("mcpListTokens") == true)
      }

  @Test
  fun requests_includeAuthorizationHeader() =
      runTest(testDispatcher) {
        val responseBody = """{"success": true, "data": []}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        repository.listTokens()

        val request = mockWebServer.takeRequest()
        assertEquals("Bearer test-id-token", request.getHeader("Authorization"))
      }
}

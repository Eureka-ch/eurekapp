// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.firebase.auth.FirebaseAuth
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

interface McpTokenRepository {
  suspend fun createToken(name: String, ttlDays: Int = 30): Result<McpToken>

  suspend fun revokeToken(tokenId: String): Result<Unit>

  suspend fun listTokens(): Result<List<McpToken>>
}

class FirebaseMcpTokenRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val functionsBaseUrl: String = "https://us-central1-eureka-app-ch.cloudfunctions.net",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : McpTokenRepository {

  companion object {
    private const val DEFAULT_ERROR = "Unknown error"
  }

  private suspend fun getIdToken(): String {
    val user = auth.currentUser ?: throw SecurityException("User must be authenticated")
    return user.getIdToken(false).await().token ?: throw SecurityException("Failed to get ID token")
  }

  override suspend fun createToken(name: String, ttlDays: Int): Result<McpToken> = runCatching {
    withContext(ioDispatcher) {
      val idToken = getIdToken()
      val body = JSONObject().put("name", name).put("ttlDays", ttlDays).toString()

      val request =
          Request.Builder()
              .url("$functionsBaseUrl/mcpCreateToken")
              .post(body.toRequestBody("application/json".toMediaType()))
              .addHeader("Authorization", "Bearer $idToken")
              .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        val errorBody = response.body?.string() ?: DEFAULT_ERROR
        throw Exception("Failed to create token: $errorBody")
      }

      val responseJson = JSONObject(response.body?.string() ?: "{}")
      if (!responseJson.optBoolean("success", false)) {
        throw Exception(responseJson.optString("error", DEFAULT_ERROR))
      }

      val data = responseJson.getJSONObject("data")
      McpToken(
          tokenId = data.getString("token"),
          name = data.getString("name"),
          expiresAt = Instant.parse(data.getString("expiresAt")))
    }
  }

  override suspend fun revokeToken(tokenId: String): Result<Unit> = runCatching {
    withContext(ioDispatcher) {
      val idToken = getIdToken()
      val body = JSONObject().put("tokenId", tokenId).toString()

      val request =
          Request.Builder()
              .url("$functionsBaseUrl/mcpRevokeToken")
              .post(body.toRequestBody("application/json".toMediaType()))
              .addHeader("Authorization", "Bearer $idToken")
              .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        val errorBody = response.body?.string() ?: DEFAULT_ERROR
        throw Exception("Failed to revoke token: $errorBody")
      }

      val responseJson = JSONObject(response.body?.string() ?: "{}")
      if (!responseJson.optBoolean("success", false)) {
        throw Exception(responseJson.optString("error", DEFAULT_ERROR))
      }
    }
  }

  override suspend fun listTokens(): Result<List<McpToken>> = runCatching {
    withContext(ioDispatcher) {
      val idToken = getIdToken()

      val request =
          Request.Builder()
              .url("$functionsBaseUrl/mcpListTokens")
              .get()
              .addHeader("Authorization", "Bearer $idToken")
              .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        val errorBody = response.body?.string() ?: DEFAULT_ERROR
        throw Exception("Failed to list tokens: $errorBody")
      }

      val responseJson = JSONObject(response.body?.string() ?: "{}")
      if (!responseJson.optBoolean("success", false)) {
        throw Exception(responseJson.optString("error", DEFAULT_ERROR))
      }

      val dataArray = responseJson.getJSONArray("data")
      parseTokenList(dataArray)
    }
  }

  private fun parseTokenList(dataArray: JSONArray): List<McpToken> {
    return (0 until dataArray.length()).map { i ->
      val item = dataArray.getJSONObject(i)
      McpToken(
          tokenId = item.getString("tokenId"),
          name = item.optString("name", ""),
          createdAt =
              item.optString("createdAt").takeIf { it.isNotEmpty() }?.let { Instant.parse(it) },
          expiresAt =
              item.optString("expiresAt").takeIf { it.isNotEmpty() }?.let { Instant.parse(it) },
          lastUsedAt =
              item.optString("lastUsedAt").takeIf { it.isNotEmpty() }?.let { Instant.parse(it) })
    }
  }
}

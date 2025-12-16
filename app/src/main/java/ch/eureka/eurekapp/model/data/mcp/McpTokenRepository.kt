// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import kotlinx.coroutines.tasks.await

data class CreateTokenResult(val token: McpToken, val rawToken: String)

interface McpTokenRepository {
  suspend fun createToken(name: String, ttlDays: Int = 30): Result<CreateTokenResult>

  suspend fun revokeToken(tokenHash: String): Result<Unit>

  suspend fun listTokens(): Result<List<McpToken>>
}

class FirebaseMcpTokenRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : McpTokenRepository {

  private fun getCurrentUserId(): String {
    return auth.currentUser?.uid
        ?: throw IllegalStateException("User must be authenticated to manage MCP tokens")
  }

  override suspend fun createToken(name: String, ttlDays: Int): Result<CreateTokenResult> =
      runCatching {
        val userId = getCurrentUserId()
        val rawToken = generateSecureToken()
        val tokenHash = hashToken(rawToken)
        val now = Timestamp.now()
        val expiresAt = Timestamp(Date(now.toDate().time + ttlDays * 24 * 60 * 60 * 1000L))

        // createdAt is null here - @ServerTimestamp will set it on the server
        val token =
            McpToken(
                userId = userId,
                name = name,
                createdAt = null,
                expiresAt = expiresAt,
                lastUsedAt = null)

        firestore.document(FirestorePaths.mcpTokenPath(tokenHash)).set(token).await()

        token.tokenHash = tokenHash
        CreateTokenResult(token, rawToken)
      }

  override suspend fun revokeToken(tokenHash: String): Result<Unit> = runCatching {
    val userId = getCurrentUserId()
    val docRef = firestore.document(FirestorePaths.mcpTokenPath(tokenHash))
    val doc = docRef.get().await()

    require(doc.exists()) { "Token not found" }

    val tokenUserId = doc.getString("userId")
    if (tokenUserId != userId) {
      throw IllegalAccessException("Cannot revoke another user's token")
    }

    docRef.delete().await()
  }

  override suspend fun listTokens(): Result<List<McpToken>> = runCatching {
    val userId = getCurrentUserId()
    val snapshot =
        firestore
            .collection(FirestorePaths.mcpTokensPath())
            .whereEqualTo("userId", userId)
            .get()
            .await()
    snapshot.documents.mapNotNull { doc ->
      doc.toObject(McpToken::class.java)?.apply { tokenHash = doc.id }
    }
  }

  private fun generateSecureToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return "mcp_" + bytes.joinToString("") { "%02x".format(it) }
  }

  private fun hashToken(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(token.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
  }
}

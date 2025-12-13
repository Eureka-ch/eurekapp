// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import java.util.Date
import kotlinx.coroutines.tasks.await

interface McpTokenRepository {
  suspend fun createToken(name: String, ttlDays: Int = 30): Result<McpToken>

  suspend fun revokeToken(tokenId: String): Result<Unit>

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

  override suspend fun createToken(name: String, ttlDays: Int): Result<McpToken> = runCatching {
    val userId = getCurrentUserId()
    val tokenId = generateSecureToken()
    val now = Timestamp.now()
    val expiresAt = Timestamp(Date(now.toDate().time + ttlDays * 24 * 60 * 60 * 1000L))

    val token =
        McpToken(
            tokenId = tokenId,
            name = name,
            createdAt = now,
            expiresAt = expiresAt,
            lastUsedAt = null)

    firestore.document(FirestorePaths.mcpTokenPath(userId, tokenId)).set(token).await()

    token
  }

  override suspend fun revokeToken(tokenId: String): Result<Unit> = runCatching {
    val userId = getCurrentUserId()
    firestore.document(FirestorePaths.mcpTokenPath(userId, tokenId)).delete().await()
  }

  override suspend fun listTokens(): Result<List<McpToken>> = runCatching {
    val userId = getCurrentUserId()
    val snapshot = firestore.collection(FirestorePaths.mcpTokensPath(userId)).get().await()
    snapshot.documents.mapNotNull { it.toObject(McpToken::class.java) }
  }

  private fun generateSecureToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return "mcp_" + bytes.joinToString("") { "%02x".format(it) }
  }
}

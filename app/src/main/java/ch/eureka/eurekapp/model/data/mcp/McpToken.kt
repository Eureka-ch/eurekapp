// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class McpToken(
    val userId: String = "",
    val name: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val lastUsedAt: Timestamp? = null
) {
  // tokenHash is the SHA-256 hash of the raw token, used as the Firestore document ID.
  // The raw token (e.g., mcp_xxx...) is shown to the user only once at creation
  // and is never stored - we only keep its hash for validation.
  @Transient var tokenHash: String = ""
}

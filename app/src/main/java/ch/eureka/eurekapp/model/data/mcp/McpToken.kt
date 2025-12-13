// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.firebase.Timestamp

data class McpToken(
    val userId: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val lastUsedAt: Timestamp? = null
) {
  // Token hash is used as document ID, not stored as a field
  // The actual token is only returned once at creation and never stored
  @Transient var tokenHash: String = ""
}

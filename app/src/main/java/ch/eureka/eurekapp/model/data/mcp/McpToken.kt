// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.firebase.Timestamp

data class McpToken(
    val tokenId: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val lastUsedAt: Timestamp? = null
)

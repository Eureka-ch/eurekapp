// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import java.time.Instant

data class McpToken(
    val tokenId: String = "",
    val name: String = "",
    val createdAt: Instant? = null,
    val expiresAt: Instant? = null,
    val lastUsedAt: Instant? = null
)

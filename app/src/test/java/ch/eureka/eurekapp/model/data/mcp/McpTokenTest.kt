// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class McpTokenTest {

  @Test
  fun mcpToken_defaultValuesAreCorrect() {
    val token = McpToken()

    assertEquals("", token.tokenId)
    assertEquals("", token.name)
    assertNull(token.createdAt)
    assertNull(token.expiresAt)
    assertNull(token.lastUsedAt)
  }

  @Test
  fun mcpToken_constructorWithAllParametersWorks() {
    val now = Instant.now()
    val expires = now.plusSeconds(86400)
    val lastUsed = now.minusSeconds(3600)

    val token =
        McpToken(
            tokenId = "token-123",
            name = "Test Token",
            createdAt = now,
            expiresAt = expires,
            lastUsedAt = lastUsed)

    assertEquals("token-123", token.tokenId)
    assertEquals("Test Token", token.name)
    assertEquals(now, token.createdAt)
    assertEquals(expires, token.expiresAt)
    assertEquals(lastUsed, token.lastUsedAt)
  }

  @Test
  fun mcpToken_copyWorksCorrectly() {
    val original = McpToken(tokenId = "token-123", name = "Original", createdAt = Instant.now())

    val copied = original.copy(name = "Copied")

    assertEquals("token-123", copied.tokenId)
    assertEquals("Copied", copied.name)
    assertEquals(original.createdAt, copied.createdAt)
  }

  @Test
  fun mcpToken_equalsWorksCorrectly() {
    val now = Instant.now()
    val token1 = McpToken(tokenId = "token-123", name = "Test", createdAt = now)
    val token2 = McpToken(tokenId = "token-123", name = "Test", createdAt = now)

    assertEquals(token1, token2)
  }

  @Test
  fun mcpToken_hashCodeIsConsistent() {
    val now = Instant.now()
    val token1 = McpToken(tokenId = "token-123", name = "Test", createdAt = now)
    val token2 = McpToken(tokenId = "token-123", name = "Test", createdAt = now)

    assertEquals(token1.hashCode(), token2.hashCode())
  }
}

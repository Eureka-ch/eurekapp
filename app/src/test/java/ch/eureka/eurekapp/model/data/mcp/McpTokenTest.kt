// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    val createdAt = Timestamp(1000, 0)
    val expiresAt = Timestamp(2000, 0)
    val lastUsedAt = Timestamp(1500, 0)

    val token =
        McpToken(
            tokenId = "token-123",
            name = "Test Token",
            createdAt = createdAt,
            expiresAt = expiresAt,
            lastUsedAt = lastUsedAt)

    assertEquals("token-123", token.tokenId)
    assertEquals("Test Token", token.name)
    assertEquals(createdAt, token.createdAt)
    assertEquals(expiresAt, token.expiresAt)
    assertEquals(lastUsedAt, token.lastUsedAt)
  }

  @Test
  fun mcpToken_copyWorksCorrectly() {
    val original =
        McpToken(tokenId = "token-123", name = "Original", createdAt = Timestamp(1000, 0))

    val copied = original.copy(name = "Copied")

    assertEquals("token-123", copied.tokenId)
    assertEquals("Copied", copied.name)
    assertEquals(original.createdAt, copied.createdAt)
  }

  @Test
  fun mcpToken_equalsWorksCorrectly() {
    val timestamp = Timestamp(1000, 0)
    val token1 = McpToken(tokenId = "token-123", name = "Test", createdAt = timestamp)
    val token2 = McpToken(tokenId = "token-123", name = "Test", createdAt = timestamp)
    val token3 = McpToken(tokenId = "token-456", name = "Other", createdAt = timestamp)

    assertEquals(token1, token2)
    assertNotEquals(token1, token3)
  }

  @Test
  fun mcpToken_hashCodeIsConsistent() {
    val timestamp = Timestamp(1000, 0)
    val token1 = McpToken(tokenId = "token-123", name = "Test", createdAt = timestamp)
    val token2 = McpToken(tokenId = "token-123", name = "Test", createdAt = timestamp)

    assertEquals(token1.hashCode(), token2.hashCode())
  }

  @Test
  fun mcpToken_toStringContainsFields() {
    val token = McpToken(tokenId = "token-123", name = "My Token")
    val tokenString = token.toString()

    assert(tokenString.contains("token-123"))
    assert(tokenString.contains("My Token"))
  }

  @Test
  fun mcpToken_withNullOptionalFields_setsNullValues() {
    val token =
        McpToken(
            tokenId = "token-123",
            name = "Token",
            createdAt = null,
            expiresAt = null,
            lastUsedAt = null)

    assertNull(token.createdAt)
    assertNull(token.expiresAt)
    assertNull(token.lastUsedAt)
  }
}

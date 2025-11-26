/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MessageEntity].
 *
 * Verifies the data class functionality, including default values in the constructor, full
 * instantiation, equality contracts, and the copy method to ensure 100% coverage.
 */
class MessageEntityTest {

  @Test
  fun `default constructor creates object with expected default values`() {
    val entity =
        MessageEntity(
            messageId = "msg_123",
            text = "Test Note",
            senderId = "user_1",
            createdAtMillis = 1000L,
            referencesJson = "[]")
    assertEquals("msg_123", entity.messageId)
    assertEquals("Test Note", entity.text)
    assertEquals("user_1", entity.senderId)
    assertEquals(1000L, entity.createdAtMillis)
    assertEquals("[]", entity.referencesJson)
    assertEquals(0, entity.localId)
    assertFalse("isPendingSync should be false by default", entity.isPendingSync)
    assertFalse("isPrivacyLocalOnly should be false by default", entity.isPrivacyLocalOnly)
  }

  @Test
  fun `constructor with all arguments creates object correctly`() {
    val entity =
        MessageEntity(
            localId = 55,
            messageId = "msg_456",
            text = "Offline Note",
            senderId = "user_2",
            createdAtMillis = 2000L,
            referencesJson = "{}",
            isPendingSync = true,
            isPrivacyLocalOnly = true)
    assertEquals(55, entity.localId)
    assertEquals("msg_456", entity.messageId)
    assertEquals("Offline Note", entity.text)
    assertEquals("user_2", entity.senderId)
    assertEquals(2000L, entity.createdAtMillis)
    assertEquals("{}", entity.referencesJson)
    assertTrue(entity.isPendingSync)
    assertTrue(entity.isPrivacyLocalOnly)
  }

  @Test
  fun `equals and hashCode contract works correctly`() {
    val entity1 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            referencesJson = "[]")
    val entity2 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            referencesJson = "[]")
    val entity3 =
        MessageEntity(
            localId = 2,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            referencesJson = "[]")
    assertEquals("Entities with same data should be equal", entity1, entity2)
    assertEquals(
        "HashCodes should be equal for equal objects", entity1.hashCode(), entity2.hashCode())
    assertNotEquals("Entities with different data should not be equal", entity1, entity3)
  }

  @Test
  fun `copy creates new instance with modified values`() {
    val original =
        MessageEntity(
            messageId = "original_id",
            text = "Original Text",
            senderId = "user",
            createdAtMillis = 100L,
            referencesJson = "")
    val copy = original.copy(text = "Updated Text", isPendingSync = true)
    assertEquals("Updated Text", copy.text)
    assertTrue(copy.isPendingSync)
    assertEquals(original.messageId, copy.messageId)
    assertEquals(original.senderId, copy.senderId)
  }
}

/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for [MessageEntity].
 *
 * Note: This code was written with help of Claude.
 */
class MessageEntityTest {

  @Test
  fun defaultConstructor_createsObjectWithExpectedDefaultValues() {
    val entity =
        MessageEntity(
            messageId = "msg_123", text = "Test Note", senderId = "user_1", createdAtMillis = 1000L)
    assertEquals("msg_123", entity.messageId)
    assertEquals("Test Note", entity.text)
    assertEquals("user_1", entity.senderId)
    assertEquals(1000L, entity.createdAtMillis)
    assertEquals(0, entity.localId)
  }

  @Test
  fun constructorWithAllArguments_createsObjectCorrectly() {
    val entity =
        MessageEntity(
            localId = 55,
            messageId = "msg_456",
            text = "Offline Note",
            senderId = "user_2",
            createdAtMillis = 2000L)
    assertEquals(55, entity.localId)
    assertEquals("msg_456", entity.messageId)
    assertEquals("Offline Note", entity.text)
    assertEquals("user_2", entity.senderId)
    assertEquals(2000L, entity.createdAtMillis)
  }

  @Test
  fun equalsAndHashCodeContract_worksCorrectly() {
    val entity1 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L)
    val entity2 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L)
    val entity3 =
        MessageEntity(
            localId = 2,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L)
    val entity4 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "different text",
            senderId = "sender",
            createdAtMillis = 100L)

    assertEquals("Entities with same data should be equal", entity1, entity2)
    assertEquals(
        "HashCodes should be equal for equal objects", entity1.hashCode(), entity2.hashCode())

    assertNotEquals("Entities with different localId should not be equal", entity1, entity3)
    assertNotEquals("Entities with different text should not be equal", entity1, entity4)
  }

  @Test
  fun copy_createsNewInstanceWithModifiedValues() {
    val original =
        MessageEntity(
            messageId = "original_id",
            text = "Original Text",
            senderId = "user",
            createdAtMillis = 100L)

    val copy = original.copy(text = "Updated Text")

    assertEquals("Updated Text", copy.text)

    // Verify untouched fields remain same
    assertEquals(original.messageId, copy.messageId)
    assertEquals(original.senderId, copy.senderId)
    assertEquals(original.createdAtMillis, copy.createdAtMillis)
    assertEquals(original.localId, copy.localId)
  }
}

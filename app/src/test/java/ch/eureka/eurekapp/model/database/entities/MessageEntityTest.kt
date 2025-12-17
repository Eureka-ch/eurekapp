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
  fun messageEntity_defaultConstructorCreatesObjectWithExpectedDefaultValues() {
    val entity =
        MessageEntity(
            messageId = "msg_123", text = "Test Note", senderId = "user_1", createdAtMillis = 1000L)
    assertEquals("msg_123", entity.messageId)
    assertEquals("Test Note", entity.text)
    assertEquals("user_1", entity.senderId)
    assertEquals(1000L, entity.createdAtMillis)
    assertEquals(0, entity.localId)
    assertFalse("isPendingSync should be false by default", entity.isPendingSync)
    assertFalse("isPrivacyLocalOnly should be false by default", entity.isPrivacyLocalOnly)
    assertFalse("isDeleted should be false by default", entity.isDeleted)
  }

  @Test
  fun messageEntity_constructorWithAllArgumentsCreatesObjectCorrectly() {
    val entity =
        MessageEntity(
            localId = 55,
            messageId = "msg_456",
            text = "Offline Note",
            senderId = "user_2",
            createdAtMillis = 2000L,
            isPendingSync = true,
            isPrivacyLocalOnly = true,
            isDeleted = true)
    assertEquals(55, entity.localId)
    assertEquals("msg_456", entity.messageId)
    assertEquals("Offline Note", entity.text)
    assertEquals("user_2", entity.senderId)
    assertEquals(2000L, entity.createdAtMillis)
    assertTrue(entity.isPendingSync)
    assertTrue(entity.isPrivacyLocalOnly)
    assertTrue(entity.isDeleted)
  }

  @Test
  fun messageEntity_equalsAndHashCodeContractWorksCorrectly() {
    val entity1 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            isDeleted = false)
    val entity2 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            isDeleted = false)
    val entity3 =
        MessageEntity(
            localId = 2,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            isDeleted = false)
    val entity4 =
        MessageEntity(
            localId = 1,
            messageId = "id",
            text = "text",
            senderId = "sender",
            createdAtMillis = 100L,
            isDeleted = true)

    assertEquals("Entities with same data should be equal", entity1, entity2)
    assertEquals(
        "HashCodes should be equal for equal objects", entity1.hashCode(), entity2.hashCode())

    assertNotEquals("Entities with different localId should not be equal", entity1, entity3)
    assertNotEquals("Entities with different isDeleted should not be equal", entity1, entity4)
  }

  @Test
  fun messageEntity_copyCreatesNewInstanceWithModifiedValues() {
    val original =
        MessageEntity(
            messageId = "original_id",
            text = "Original Text",
            senderId = "user",
            createdAtMillis = 100L)

    val copy = original.copy(text = "Updated Text", isPendingSync = true, isDeleted = true)

    assertEquals("Updated Text", copy.text)
    assertTrue(copy.isPendingSync)
    assertTrue(copy.isDeleted)

    // Verify untouched fields remain same
    assertEquals(original.messageId, copy.messageId)
    assertEquals(original.senderId, copy.senderId)
    assertEquals(original.isPrivacyLocalOnly, copy.isPrivacyLocalOnly)
  }
}

/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Mapper extension functions.
 *
 * Verifies correct conversion between:
 * - [MessageEntity] (Database Model) -> [Message] (Domain Model)
 * - [Message] (Domain Model) -> [MessageEntity] (Database Model)
 *
 * Specifically checks the time unit conversion (Seconds <-> Milliseconds).
 */
class MessageMapperTest {

  @Test
  fun `toDomainModel maps fields correctly`() {
    val entity =
        MessageEntity(
            localId = 123,
            messageId = "msg_123",
            text = "Hello World",
            senderId = "user_1",
            createdAtMillis = 1600000000000L,
            isPendingSync = true,
            isPrivacyLocalOnly = false)
    val domain = entity.toDomainModel()
    assertEquals("msg_123", domain.messageID)
    assertEquals("Hello World", domain.text)
    assertEquals("user_1", domain.senderId)
    assertEquals(1600000000L, domain.createdAt.seconds)
    assertEquals(0, domain.createdAt.nanoseconds)
    assertEquals(0, domain.references.size)
  }

  @Test
  fun `toEntity maps fields correctly`() {
    val timestamp = Timestamp(1700000000L, 0)
    val message =
        Message(
            messageID = "msg_456",
            text = "Domain Message",
            senderId = "user_2",
            createdAt = timestamp,
        )
    val entity = message.toEntity()
    assertEquals("msg_456", entity.messageId)
    assertEquals("Domain Message", entity.text)
    assertEquals("user_2", entity.senderId)
    assertEquals(1700000000000L, entity.createdAtMillis)
    assertEquals(false, entity.isPendingSync)
    assertEquals(false, entity.isPrivacyLocalOnly)
  }
}

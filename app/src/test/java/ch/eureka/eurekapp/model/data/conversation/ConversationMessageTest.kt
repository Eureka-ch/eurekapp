// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

class ConversationMessageTest {

  @Test
  fun conversationMessage_createsMessageWithEmptyValues() {
    val message = ConversationMessage()
    assertEquals("", message.messageId)
    assertEquals("", message.senderId)
    assertEquals("", message.text)
    assertNull(message.createdAt)
    assertNull(message.editedAt)
    assertEquals(false, message.isDeleted)
  }

  @Test
  fun conversationMessage_setsValuesCorrectly() {
    val timestamp = Timestamp.now()
    val editedTimestamp = Timestamp.now()
    val message =
        ConversationMessage(
            messageId = "msg123",
            senderId = "user789",
            text = "Hello world",
            createdAt = timestamp,
            editedAt = editedTimestamp,
            isDeleted = true)

    assertEquals("msg123", message.messageId)
    assertEquals("user789", message.senderId)
    assertEquals("Hello world", message.text)
    assertEquals(timestamp, message.createdAt)
    assertEquals(editedTimestamp, message.editedAt)
    assertEquals(true, message.isDeleted)
  }

  @Test
  fun conversationMessage_worksCorrectly() {
    val timestamp = Timestamp.now()
    val message1 = ConversationMessage(messageId = "msg1", text = "Test", createdAt = timestamp)
    val message2 = ConversationMessage(messageId = "msg1", text = "Test", createdAt = timestamp)
    val message3 = ConversationMessage(messageId = "msg2", text = "Test", createdAt = timestamp)

    assertEquals(message1, message2)
    assertEquals(message1.hashCode(), message2.hashCode())
    assertNotEquals(message1, message3)
  }
}

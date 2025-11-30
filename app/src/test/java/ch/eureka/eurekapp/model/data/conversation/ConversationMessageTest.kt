package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConversationMessageTest {

  @Test
  fun `default constructor creates message with empty values`() {
    val message = ConversationMessage()
    assertEquals("", message.messageId)
    assertEquals("", message.senderId)
    assertEquals("", message.text)
    assertNull(message.createdAt)
  }

  @Test
  fun `constructor with all parameters sets values correctly`() {
    val timestamp = Timestamp.now()
    val message =
        ConversationMessage(
            messageId = "msg123", senderId = "user789", text = "Hello world", createdAt = timestamp)

    assertEquals("msg123", message.messageId)
    assertEquals("user789", message.senderId)
    assertEquals("Hello world", message.text)
    assertEquals(timestamp, message.createdAt)
  }

  @Test
  fun `equals and hashCode work correctly`() {
    val timestamp = Timestamp.now()
    val message1 = ConversationMessage(messageId = "msg1", text = "Test", createdAt = timestamp)
    val message2 = ConversationMessage(messageId = "msg1", text = "Test", createdAt = timestamp)
    val message3 = ConversationMessage(messageId = "msg2", text = "Test", createdAt = timestamp)

    assertEquals(message1, message2)
    assertEquals(message1.hashCode(), message2.hashCode())
    assertNotEquals(message1, message3)
  }
}

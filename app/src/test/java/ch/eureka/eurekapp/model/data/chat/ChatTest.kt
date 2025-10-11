package ch.eureka.eurekapp.model.data.chat

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Test suite for Chat model classes.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class ChatTest {

  @Test
  fun chatChannel_defaultConstructor_createsEmptyChatChannel() {
    val channel = ChatChannel()

    assertEquals("", channel.channelID)
    assertEquals("", channel.projectId)
    assertEquals("", channel.name)
  }

  @Test
  fun chatChannel_withParameters_setsCorrectValues() {
    val channel =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")

    assertEquals("ch123", channel.channelID)
    assertEquals("prj123", channel.projectId)
    assertEquals("General Discussion", channel.name)
  }

  @Test
  fun chatChannel_copy_createsNewInstance() {
    val channel =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")
    val copiedChannel = channel.copy(name = "Announcements")

    assertEquals("ch123", copiedChannel.channelID)
    assertEquals("prj123", copiedChannel.projectId)
    assertEquals("Announcements", copiedChannel.name)
  }

  @Test
  fun chatChannel_equals_comparesCorrectly() {
    val channel1 =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")
    val channel2 =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")
    val channel3 = ChatChannel(channelID = "ch456", projectId = "prj456", name = "Random")

    assertEquals(channel1, channel2)
    assertNotEquals(channel1, channel3)
  }

  @Test
  fun chatChannel_hashCode_isConsistent() {
    val channel1 =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")
    val channel2 =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")

    assertEquals(channel1.hashCode(), channel2.hashCode())
  }

  @Test
  fun chatChannel_toString_containsAllFields() {
    val channel =
        ChatChannel(channelID = "ch123", projectId = "prj123", name = "General Discussion")
    val channelString = channel.toString()

    assert(channelString.contains("ch123"))
    assert(channelString.contains("prj123"))
    assert(channelString.contains("General Discussion"))
  }

  @Test
  fun message_defaultConstructor_createsEmptyMessage() {
    val message = Message()

    assertEquals("", message.messageID)
    assertEquals("", message.text)
    assertEquals("", message.senderId)
    assertEquals(Timestamp(0, 0), message.createdAt)
    assertEquals(emptyList<String>(), message.references)
  }

  @Test
  fun message_withParameters_setsCorrectValues() {
    val timestamp = Timestamp(1000, 0)
    val references = listOf("ref1", "ref2")
    val message =
        Message(
            messageID = "msg123",
            text = "Hello, world!",
            senderId = "user123",
            createdAt = timestamp,
            references = references)

    assertEquals("msg123", message.messageID)
    assertEquals("Hello, world!", message.text)
    assertEquals("user123", message.senderId)
    assertEquals(timestamp, message.createdAt)
    assertEquals(references, message.references)
  }

  @Test
  fun message_copy_createsNewInstance() {
    val message = Message(messageID = "msg123", text = "Hello, world!", senderId = "user123")
    val copiedMessage = message.copy(text = "Updated message")

    assertEquals("msg123", copiedMessage.messageID)
    assertEquals("Updated message", copiedMessage.text)
    assertEquals("user123", copiedMessage.senderId)
  }

  @Test
  fun message_equals_comparesCorrectly() {
    val timestamp = Timestamp(1000, 0)
    val message1 =
        Message(
            messageID = "msg123",
            text = "Hello, world!",
            senderId = "user123",
            createdAt = timestamp)
    val message2 =
        Message(
            messageID = "msg123",
            text = "Hello, world!",
            senderId = "user123",
            createdAt = timestamp)
    val message3 = Message(messageID = "msg456", text = "Different message", senderId = "user456")

    assertEquals(message1, message2)
    assertNotEquals(message1, message3)
  }

  @Test
  fun message_hashCode_isConsistent() {
    val timestamp = Timestamp(1000, 0)
    val message1 =
        Message(
            messageID = "msg123",
            text = "Hello, world!",
            senderId = "user123",
            createdAt = timestamp)
    val message2 =
        Message(
            messageID = "msg123",
            text = "Hello, world!",
            senderId = "user123",
            createdAt = timestamp)

    assertEquals(message1.hashCode(), message2.hashCode())
  }

  @Test
  fun message_toString_containsAllFields() {
    val message = Message(messageID = "msg123", text = "Hello, world!", senderId = "user123")
    val messageString = message.toString()

    assert(messageString.contains("msg123"))
    assert(messageString.contains("Hello, world!"))
    assert(messageString.contains("user123"))
  }
}

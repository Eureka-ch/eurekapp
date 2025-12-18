package ch.eureka.eurekapp.model.data.chat

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

/**
 * Test suite for ChatRepository implementation.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class ChatRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: ChatRepository
  private val testProjectId = "project_chat_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("projects/$testProjectId/chatChannels")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreChatRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun chatRepository_createChannelShouldCreateChannelInFirestore() = runBlocking {
    val projectId = "project_chat_1"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel1", projectId = projectId, name = "General", createdBy = testUserId)

    val result = repository.createChannel(channel)

    assertTrue(result.isSuccess)
    assertEquals("channel1", result.getOrNull())

    val savedChannel =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel1")
            .get()
            .await()
            .toObject(ChatChannel::class.java)

    assertNotNull(savedChannel)
    assertEquals(channel.channelID, savedChannel?.channelID)
    assertEquals(channel.name, savedChannel?.name)
  }

  @Test
  fun chatRepository_getChannelByIdShouldReturnChannelWhenExists() = runBlocking {
    val projectId = "project_chat_2"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel2", projectId = projectId, name = "Random", createdBy = testUserId)
    repository.createChannel(channel)

    val flow = repository.getChannelById(projectId, "channel2")
    val retrievedChannel = flow.first()

    assertNotNull(retrievedChannel)
    assertEquals(channel.channelID, retrievedChannel?.channelID)
    assertEquals(channel.name, retrievedChannel?.name)
  }

  @Test
  fun chatRepository_getChannelByIdShouldReturnNullWhenChannelDoesNotExist() = runBlocking {
    val projectId = "project_chat_3"
    setupTestProject(projectId)

    val flow = repository.getChannelById(projectId, "non_existent_channel")
    val retrievedChannel = flow.first()

    assertNull(retrievedChannel)
  }

  @Test
  fun chatRepository_getChannelsInProjectShouldReturnAllChannels() = runBlocking {
    val projectId = "project_chat_4"
    setupTestProject(projectId)

    val channel1 =
        ChatChannel(
            channelID = "channel3", projectId = projectId, name = "General", createdBy = testUserId)
    val channel2 =
        ChatChannel(
            channelID = "channel4", projectId = projectId, name = "Random", createdBy = testUserId)
    repository.createChannel(channel1)
    repository.createChannel(channel2)

    val flow = repository.getChannelsInProject(projectId)
    val channels = flow.first()

    assertEquals(2, channels.size)
    assertTrue(channels.any { it.channelID == "channel3" })
    assertTrue(channels.any { it.channelID == "channel4" })
  }

  @Test
  fun chatRepository_getChannelsInProjectShouldReturnEmptyListWhenNoChannels() = runBlocking {
    val projectId = "project_chat_5"
    setupTestProject(projectId)

    val flow = repository.getChannelsInProject(projectId)
    val channels = flow.first()

    assertTrue(channels.isEmpty())
  }

  @Test
  fun chatRepository_updateChannelShouldUpdateChannelDetails() = runBlocking {
    val projectId = "project_chat_6"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel7",
            projectId = projectId,
            name = "Original Name",
            createdBy = testUserId)
    repository.createChannel(channel)

    val updatedChannel = channel.copy(name = "Updated Name")
    val result = repository.updateChannel(updatedChannel)

    assertTrue(result.isSuccess)

    val savedChannel =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel7")
            .get()
            .await()
            .toObject(ChatChannel::class.java)

    assertNotNull(savedChannel)
    assertEquals("Updated Name", savedChannel?.name)
  }

  @Test
  fun chatRepository_deleteChannelShouldDeleteChannelFromFirestore() = runBlocking {
    val projectId = "project_chat_7"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel8",
            projectId = projectId,
            name = "To Delete",
            createdBy = testUserId)
    repository.createChannel(channel)

    val result = repository.deleteChannel(projectId, "channel8")

    assertTrue(result.isSuccess)

    val deletedChannel =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel8")
            .get()
            .await()
            .toObject(ChatChannel::class.java)

    assertNull(deletedChannel)
  }

  @Test
  fun chatRepository_sendMessageShouldCreateMessageInChannel() = runBlocking {
    val projectId = "project_chat_8"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel9",
            projectId = projectId,
            name = "Messages",
            createdBy = testUserId)
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg1",
            text = "Hello, world!",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())

    val result = repository.sendMessage(projectId, "channel9", message)

    assertTrue(result.isSuccess)
    assertEquals("msg1", result.getOrNull())

    val savedMessage =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel9")
            .collection("messages")
            .document("msg1")
            .get()
            .await()
            .toObject(Message::class.java)

    assertNotNull(savedMessage)
    assertEquals(message.messageID, savedMessage?.messageID)
    assertEquals(message.text, savedMessage?.text)
    assertEquals(message.senderId, savedMessage?.senderId)
  }

  @Test
  fun chatRepository_getMessagesInChannelShouldReturnAllMessages() = runBlocking {
    val projectId = "project_chat_9"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel10",
            projectId = projectId,
            name = "Messages",
            createdBy = testUserId)
    repository.createChannel(channel)

    val message1 =
        Message(
            messageID = "msg2",
            text = "First message",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    val message2 =
        Message(
            messageID = "msg3",
            text = "Second message",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.sendMessage(projectId, "channel10", message1)
    repository.sendMessage(projectId, "channel10", message2)

    val flow = repository.getMessagesInChannel(projectId, "channel10")
    val messages = flow.first()

    assertEquals(2, messages.size)
    assertTrue(messages.any { it.messageID == "msg2" })
    assertTrue(messages.any { it.messageID == "msg3" })
  }

  @Test
  fun chatRepository_updateMessageShouldUpdateMessageText() = runBlocking {
    val projectId = "project_chat_10"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel11",
            projectId = projectId,
            name = "Messages",
            createdBy = testUserId)
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg4",
            text = "Original text",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.sendMessage(projectId, "channel11", message)

    val updatedMessage = message.copy(text = "Updated text")
    val result = repository.updateMessage(projectId, "channel11", updatedMessage)

    assertTrue(result.isSuccess)

    val savedMessage =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel11")
            .collection("messages")
            .document("msg4")
            .get()
            .await()
            .toObject(Message::class.java)

    assertNotNull(savedMessage)
    assertEquals("Updated text", savedMessage?.text)
  }

  @Test
  fun chatRepository_deleteMessageShouldDeleteMessageFromChannel() = runBlocking {
    val projectId = "project_chat_11"
    setupTestProject(projectId)

    val channel =
        ChatChannel(
            channelID = "channel12",
            projectId = projectId,
            name = "Messages",
            createdBy = testUserId)
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg5",
            text = "To delete",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.sendMessage(projectId, "channel12", message)

    val result = repository.deleteMessage(projectId, "channel12", "msg5")

    assertTrue(result.isSuccess)

    val deletedMessage =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("chatChannels")
            .document("channel12")
            .collection("messages")
            .document("msg5")
            .get()
            .await()
            .toObject(Message::class.java)

    assertNull(deletedMessage)
  }
}

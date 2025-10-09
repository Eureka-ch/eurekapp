package ch.eureka.eurekapp.model.chat

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

class ChatRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: ChatRepository
  private val testWorkspaceId = "workspace_chat_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/chatChannels")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreChatRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createChannel_shouldCreateChannelInFirestore() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel1",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "General")

    val result = repository.createChannel(channel)

    assertTrue(result.isSuccess)
    assertEquals("channel1", result.getOrNull())

    val savedChannel =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
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
  fun getChannelById_shouldReturnChannelWhenExists() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel2",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Random")
    repository.createChannel(channel)

    val flow = repository.getChannelById(testWorkspaceId, "channel2")
    val retrievedChannel = flow.first()

    assertNotNull(retrievedChannel)
    assertEquals(channel.channelID, retrievedChannel?.channelID)
    assertEquals(channel.name, retrievedChannel?.name)
  }

  @Test
  fun getChannelById_shouldReturnNullWhenChannelDoesNotExist() = runBlocking {
    val flow = repository.getChannelById(testWorkspaceId, "non_existent_channel")
    val retrievedChannel = flow.first()

    assertNull(retrievedChannel)
  }

  @Test
  fun getChannelsInWorkspace_shouldReturnAllChannels() = runBlocking {
    val channel1 =
        ChatChannel(
            channelID = "channel3",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "General")
    val channel2 =
        ChatChannel(
            channelID = "channel4",
            workspaceId = testWorkspaceId,
            contextId = "group1",
            contextType = ChatContextType.GROUP,
            name = "Group Chat")
    repository.createChannel(channel1)
    repository.createChannel(channel2)

    val flow = repository.getChannelsInWorkspace(testWorkspaceId)
    val channels = flow.first()

    assertEquals(2, channels.size)
    assertTrue(channels.any { it.channelID == "channel3" })
    assertTrue(channels.any { it.channelID == "channel4" })
  }

  @Test
  fun getChannelsInWorkspace_shouldReturnEmptyListWhenNoChannels() = runBlocking {
    val flow = repository.getChannelsInWorkspace(testWorkspaceId)
    val channels = flow.first()

    assertTrue(channels.isEmpty())
  }

  @Test
  fun getChannelsForContext_shouldReturnChannelsForSpecificContext() = runBlocking {
    val channel1 =
        ChatChannel(
            channelID = "channel5",
            workspaceId = testWorkspaceId,
            contextId = "group1",
            contextType = ChatContextType.GROUP,
            name = "Group 1 Chat")
    val channel2 =
        ChatChannel(
            channelID = "channel6",
            workspaceId = testWorkspaceId,
            contextId = "group2",
            contextType = ChatContextType.GROUP,
            name = "Group 2 Chat")
    repository.createChannel(channel1)
    repository.createChannel(channel2)

    val flow = repository.getChannelsForContext(testWorkspaceId, "group1", ChatContextType.GROUP)
    val channels = flow.first()

    assertEquals(1, channels.size)
    assertEquals("channel5", channels[0].channelID)
  }

  @Test
  fun updateChannel_shouldUpdateChannelDetails() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel7",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Original Name")
    repository.createChannel(channel)

    val updatedChannel = channel.copy(name = "Updated Name")
    val result = repository.updateChannel(updatedChannel)

    assertTrue(result.isSuccess)

    val savedChannel =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("chatChannels")
            .document("channel7")
            .get()
            .await()
            .toObject(ChatChannel::class.java)

    assertNotNull(savedChannel)
    assertEquals("Updated Name", savedChannel?.name)
  }

  @Test
  fun deleteChannel_shouldDeleteChannelFromFirestore() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel8",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "To Delete")
    repository.createChannel(channel)

    val result = repository.deleteChannel(testWorkspaceId, "channel8")

    assertTrue(result.isSuccess)

    val deletedChannel =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("chatChannels")
            .document("channel8")
            .get()
            .await()
            .toObject(ChatChannel::class.java)

    assertNull(deletedChannel)
  }

  @Test
  fun sendMessage_shouldCreateMessageInChannel() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel9",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Messages")
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg1",
            text = "Hello, world!",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())

    val result = repository.sendMessage(testWorkspaceId, "channel9", message)

    assertTrue(result.isSuccess)
    assertEquals("msg1", result.getOrNull())

    val savedMessage =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
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
  fun getMessagesInChannel_shouldReturnAllMessages() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel10",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Messages")
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
    repository.sendMessage(testWorkspaceId, "channel10", message1)
    repository.sendMessage(testWorkspaceId, "channel10", message2)

    val flow = repository.getMessagesInChannel(testWorkspaceId, "channel10")
    val messages = flow.first()

    assertEquals(2, messages.size)
    assertTrue(messages.any { it.messageID == "msg2" })
    assertTrue(messages.any { it.messageID == "msg3" })
  }

  @Test
  fun updateMessage_shouldUpdateMessageText() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel11",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Messages")
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg4",
            text = "Original text",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.sendMessage(testWorkspaceId, "channel11", message)

    val updatedMessage = message.copy(text = "Updated text")
    val result = repository.updateMessage(testWorkspaceId, "channel11", updatedMessage)

    assertTrue(result.isSuccess)

    val savedMessage =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
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
  fun deleteMessage_shouldDeleteMessageFromChannel() = runBlocking {
    val channel =
        ChatChannel(
            channelID = "channel12",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ChatContextType.WORKSPACE,
            name = "Messages")
    repository.createChannel(channel)

    val message =
        Message(
            messageID = "msg5",
            text = "To delete",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.sendMessage(testWorkspaceId, "channel12", message)

    val result = repository.deleteMessage(testWorkspaceId, "channel12", "msg5")

    assertTrue(result.isSuccess)

    val deletedMessage =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
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

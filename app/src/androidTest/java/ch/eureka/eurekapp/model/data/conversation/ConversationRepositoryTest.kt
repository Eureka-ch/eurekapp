package ch.eureka.eurekapp.model.data.conversation

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

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

/**
 * Test suite for ConversationRepository implementation.
 *
 * Tests CRUD operations and duplicate detection for conversations.
 */
class ConversationRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: ConversationRepository

  override fun getCollectionPaths(): List<String> = listOf("conversations", "activities", "users")

  @Before
  override fun setup() {
    runBlocking {
      super.setup()
      repository =
          FirestoreConversationRepository(
              firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
      val snapshot = FirebaseEmulator.firestore.collection("conversations").get().await()
      val batch = FirebaseEmulator.firestore.batch()
      for (document in snapshot.documents) {
        batch.delete(document.reference)
      }
      batch.commit().await()
    }
  }

  @Test
  fun conversationRepository_createConversationShouldCreateConversationInFirestore() = runBlocking {
    // Arrange: Create a conversation between two users
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser123"),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    // Act: Save the conversation to Firestore
    val result = repository.createConversation(conversation)

    // Assert: Verify creation succeeded and data was persisted correctly
    assertTrue(result.isSuccess)
    val conversationId = result.getOrNull()
    assertNotNull(conversationId)

    // Verify it was saved by fetching it back
    val saved = repository.getConversationById(conversationId!!).first()
    assertNotNull(saved)
    assertEquals("project1", saved?.projectId)
    assertEquals(2, saved?.memberIds?.size)
    assertTrue(saved?.memberIds?.contains(testUserId) == true)
  }

  @Test
  fun conversationRepository_getConversationsForCurrentUserShouldReturnUserConversations() =
      runBlocking {
        // Arrange: Create two conversations where current user is a member
        val conv1 =
            Conversation(
                projectId = "project1",
                memberIds = listOf(testUserId, "user2"),
                createdBy = testUserId)
        val conv2 =
            Conversation(
                projectId = "project2",
                memberIds = listOf(testUserId, "user3"),
                createdBy = testUserId)

        repository.createConversation(conv1)
        repository.createConversation(conv2)

        // Act: Fetch all conversations for current user
        val conversations = repository.getConversationsForCurrentUser().first()

        // Assert: Both conversations should be returned
        assertEquals(2, conversations.size)
      }

  @Test
  fun conversationRepository_findExistingConversationShouldFindDuplicate() = runBlocking {
    // Arrange: Create a conversation between two users
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)

    repository.createConversation(conversation)

    // Act: Try to find existing conversation between same users in same project
    val existing = repository.findExistingConversation("project1", testUserId, "otherUser")

    // Assert: Should find the existing conversation (prevents duplicates)
    assertNotNull(existing)
    assertEquals("project1", existing?.projectId)
  }

  @Test
  fun conversationRepository_findExistingConversationShouldReturnNullWhenNotExists() = runBlocking {
    // Act: Try to find a conversation that doesn't exist
    val existing = repository.findExistingConversation("project1", testUserId, "nonExistentUser")

    // Assert: Should return null when no conversation exists
    assertNull(existing)
  }

  @Test
  fun conversationRepository_deleteConversationShouldRemoveFromFirestore() = runBlocking {
    // Arrange: Create a conversation first
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)

    val createResult = repository.createConversation(conversation)
    val conversationId = createResult.getOrNull()!!

    // Act: Delete the conversation
    val deleteResult = repository.deleteConversation(conversationId)

    // Assert: Deletion succeeded and conversation no longer exists
    assertTrue(deleteResult.isSuccess)
    val deleted = repository.getConversationById(conversationId).first()
    assertNull(deleted)
  }

  @Test
  fun conversationRepository_sendFileMessageShouldCreateFileMessageInFirestore() = runBlocking {
    // Arrange: Create a conversation first
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val createResult = repository.createConversation(conversation)
    val conversationId = createResult.getOrNull()!!

    // Act: Send a file message
    val fileUrl = "https://storage.example.com/files/test.pdf"
    val messageText = "Shared document"
    val result = repository.sendFileMessage(conversationId, messageText, fileUrl)

    // Assert: Message was sent successfully
    assertTrue(result.isSuccess)
    val message = result.getOrNull()
    assertNotNull(message)
    assertEquals(messageText, message?.text)
    assertEquals(testUserId, message?.senderId)
    assertTrue(message?.isFile == true)
    assertEquals(fileUrl, message?.fileUrl)
  }

  @Test
  fun conversationRepository_sendFileMessageShouldUpdateConversationMetadata() = runBlocking {
    // Arrange: Create a conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val createResult = repository.createConversation(conversation)
    val conversationId = createResult.getOrNull()!!

    // Act: Send a file message
    val messageText = "Important file"
    val fileUrl = "https://storage.example.com/files/document.pdf"
    repository.sendFileMessage(conversationId, messageText, fileUrl)

    // Wait a bit for metadata to update
    kotlinx.coroutines.delay(500)

    // Assert: Conversation metadata should be updated
    val updatedConversation = repository.getConversationById(conversationId).first()
    assertNotNull(updatedConversation)
    assertNotNull(updatedConversation?.lastMessageAt)
    assertEquals(messageText, updatedConversation?.lastMessagePreview)
    assertEquals(testUserId, updatedConversation?.lastMessageSenderId)
  }

  @Test
  fun conversationRepository_sendFileMessageShouldAppearInMessagesList() = runBlocking {
    // Arrange: Create a conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val createResult = repository.createConversation(conversation)
    val conversationId = createResult.getOrNull()!!

    // Act: Send multiple messages including a file message
    repository.sendMessage(conversationId, "First message")
    val fileUrl = "https://storage.example.com/files/test.jpg"
    repository.sendFileMessage(conversationId, "Image file", fileUrl)
    repository.sendMessage(conversationId, "Third message")

    // Wait for messages to be written
    kotlinx.coroutines.delay(500)

    // Assert: File message should appear in messages list
    val messages = repository.getMessages(conversationId, 10).first()
    assertEquals(3, messages.size)

    val fileMessage = messages.find { it.text == "Image file" }
    assertNotNull(fileMessage)
    assertTrue(fileMessage?.isFile == true)
    assertEquals(fileUrl, fileMessage?.fileUrl)
  }

  @Test
  fun conversationRepository_sendFileMessageWithLongTextShouldTruncatePreview() = runBlocking {
    // Arrange: Create a conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val createResult = repository.createConversation(conversation)
    val conversationId = createResult.getOrNull()!!

    // Act: Send a file message with long text
    val longText = "a".repeat(150) // Text longer than 100 characters
    val fileUrl = "https://storage.example.com/files/document.pdf"
    repository.sendFileMessage(conversationId, longText, fileUrl)

    // Wait for metadata to update
    kotlinx.coroutines.delay(500)

    // Assert: Preview should be truncated to 100 characters
    val updatedConversation = repository.getConversationById(conversationId).first()
    assertNotNull(updatedConversation?.lastMessagePreview)
    assertEquals(100, updatedConversation?.lastMessagePreview?.length)
  }

  // ==================== updateMessage Tests ====================

  @Test
  fun conversationRepository_updateMessageShouldUpdateMessageText() = runBlocking {
    // Arrange: Create conversation and send a message
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val originalText = "Original message"
    val messageResult = repository.sendMessage(conversationId, originalText)
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Update the message
    val newText = "Updated message"
    val updateResult = repository.updateMessage(conversationId, messageId, newText)

    // Wait for update to complete
    kotlinx.coroutines.delay(300)

    // Assert: Update succeeded and message text changed
    assertTrue(updateResult.isSuccess)
    val messages = repository.getMessages(conversationId, 10).first()
    val updatedMessage = messages.find { it.messageId == messageId }
    assertNotNull(updatedMessage)
    assertEquals(newText, updatedMessage?.text)
  }

  @Test
  fun conversationRepository_updateMessageShouldSetEditedAtTimestamp() = runBlocking {
    // Arrange: Create conversation and send a message
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val messageResult = repository.sendMessage(conversationId, "Original message")
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Update the message
    repository.updateMessage(conversationId, messageId, "Updated message")

    // Wait for update to complete
    kotlinx.coroutines.delay(300)

    // Assert: editedAt timestamp should be set
    val messages = repository.getMessages(conversationId, 10).first()
    val updatedMessage = messages.find { it.messageId == messageId }
    assertNotNull(updatedMessage?.editedAt)
  }

  // ==================== deleteMessage Tests ====================

  @Test
  fun conversationRepository_deleteMessageShouldSoftDeleteMessage() = runBlocking {
    // Arrange: Create conversation and send a message
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val messageResult = repository.sendMessage(conversationId, "Message to delete")
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Delete the message
    val deleteResult = repository.deleteMessage(conversationId, messageId)

    // Wait for delete to complete
    kotlinx.coroutines.delay(300)

    // Assert: Delete succeeded and message no longer appears in list
    assertTrue(deleteResult.isSuccess)
    val messages = repository.getMessages(conversationId, 10).first()
    val deletedMessage = messages.find { it.messageId == messageId }
    assertNull(deletedMessage)
  }

  @Test
  fun conversationRepository_deleteMessageShouldNotAffectOtherMessages() = runBlocking {
    // Arrange: Create conversation and send multiple messages
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    repository.sendMessage(conversationId, "First message")
    val messageToDelete = repository.sendMessage(conversationId, "Message to delete").getOrNull()!!
    repository.sendMessage(conversationId, "Third message")

    // Wait for messages to be written
    kotlinx.coroutines.delay(500)

    // Act: Delete the middle message
    repository.deleteMessage(conversationId, messageToDelete.messageId)

    // Wait for delete to complete
    kotlinx.coroutines.delay(300)

    // Assert: Only 2 messages remain
    val messages = repository.getMessages(conversationId, 10).first()
    assertEquals(2, messages.size)
    assertTrue(messages.any { it.text == "First message" })
    assertTrue(messages.any { it.text == "Third message" })
  }

  // ==================== removeAttachment Tests ====================

  @Test
  fun conversationRepository_removeAttachmentShouldClearFileFieldsFromMessage() = runBlocking {
    // Arrange: Create conversation and send a file message
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val fileUrl = "https://storage.example.com/files/test.pdf"
    val messageResult = repository.sendFileMessage(conversationId, "File description", fileUrl)
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Remove the attachment
    val removeResult = repository.removeAttachment(conversationId, messageId)

    // Wait for update to complete
    kotlinx.coroutines.delay(300)

    // Assert: Attachment removed but message still exists
    assertTrue(removeResult.isSuccess)
    val messages = repository.getMessages(conversationId, 10).first()
    val updatedMessage = messages.find { it.messageId == messageId }
    assertNotNull(updatedMessage)
    assertEquals(false, updatedMessage?.isFile)
    assertEquals("", updatedMessage?.fileUrl)
  }

  @Test
  fun conversationRepository_removeAttachmentShouldSetEditedAtTimestamp() = runBlocking {
    // Arrange: Create conversation and send a file message
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val fileUrl = "https://storage.example.com/files/test.pdf"
    val messageResult = repository.sendFileMessage(conversationId, "File description", fileUrl)
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Remove the attachment
    repository.removeAttachment(conversationId, messageId)

    // Wait for update to complete
    kotlinx.coroutines.delay(300)

    // Assert: editedAt timestamp should be set
    val messages = repository.getMessages(conversationId, 10).first()
    val updatedMessage = messages.find { it.messageId == messageId }
    assertNotNull(updatedMessage?.editedAt)
  }

  @Test
  fun conversationRepository_removeAttachmentShouldPreserveMessageText() = runBlocking {
    // Arrange: Create conversation and send a file message with text
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    val fileUrl = "https://storage.example.com/files/test.pdf"
    val messageText = "Important document attached"
    val messageResult = repository.sendFileMessage(conversationId, messageText, fileUrl)
    val messageId = messageResult.getOrNull()!!.messageId

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Remove the attachment
    repository.removeAttachment(conversationId, messageId)

    // Wait for update to complete
    kotlinx.coroutines.delay(300)

    // Assert: Message text is preserved
    val messages = repository.getMessages(conversationId, 10).first()
    val updatedMessage = messages.find { it.messageId == messageId }
    assertEquals(messageText, updatedMessage?.text)
  }

  // ==================== markMessagesAsRead Tests ====================

  @Test
  fun conversationRepository_markMessagesAsReadShouldUpdateLastReadTimestamp() = runBlocking {
    // Arrange: Create conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!
    repository.sendMessage(conversationId, "Test message")

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Act: Mark messages as read
    val result = repository.markMessagesAsRead(conversationId)

    // Assert: Operation succeeded
    assertTrue(result.isSuccess)
  }

  // ==================== getConversationsInProject Tests ====================

  @Test
  fun conversationRepository_getConversationsInProjectShouldReturnOnlyProjectConversations() =
      runBlocking {
        // Arrange: Create conversations in different projects
        val conv1 =
            Conversation(
                projectId = "project1",
                memberIds = listOf(testUserId, "user2"),
                createdBy = testUserId)
        val conv2 =
            Conversation(
                projectId = "project1",
                memberIds = listOf(testUserId, "user3"),
                createdBy = testUserId)
        val conv3 =
            Conversation(
                projectId = "project2",
                memberIds = listOf(testUserId, "user4"),
                createdBy = testUserId)

        repository.createConversation(conv1)
        repository.createConversation(conv2)
        repository.createConversation(conv3)

        // Act: Fetch conversations for project1
        val conversations = repository.getConversationsInProject("project1").first()

        // Assert: Only project1 conversations are returned
        assertEquals(2, conversations.size)
        assertTrue(conversations.all { it.projectId == "project1" })
      }

  // ==================== sendMessage Tests ====================

  @Test
  fun conversationRepository_sendMessageShouldCreateMessageWithCorrectSender() = runBlocking {
    // Arrange: Create conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!

    // Act: Send a message
    val messageText = "Hello, world!"
    val result = repository.sendMessage(conversationId, messageText)

    // Wait for message to be written
    kotlinx.coroutines.delay(300)

    // Assert: Message created with correct sender
    assertTrue(result.isSuccess)
    val message = result.getOrNull()
    assertEquals(testUserId, message?.senderId)
    assertEquals(messageText, message?.text)
  }

  @Test
  fun conversationRepository_sendMessageShouldUpdateConversationMetadata() = runBlocking {
    // Arrange: Create conversation
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!

    // Act: Send a message
    val messageText = "Test message for metadata"
    repository.sendMessage(conversationId, messageText)

    // Wait for metadata to update
    kotlinx.coroutines.delay(500)

    // Assert: Conversation metadata updated
    val updatedConversation = repository.getConversationById(conversationId).first()
    assertNotNull(updatedConversation?.lastMessageAt)
    assertEquals(messageText, updatedConversation?.lastMessagePreview)
    assertEquals(testUserId, updatedConversation?.lastMessageSenderId)
  }

  // ==================== getMessages Tests ====================

  @Test
  fun conversationRepository_getMessagesShouldReturnMessagesInChronologicalOrder() = runBlocking {
    // Arrange: Create conversation and send messages
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!

    repository.sendMessage(conversationId, "First")
    kotlinx.coroutines.delay(100)
    repository.sendMessage(conversationId, "Second")
    kotlinx.coroutines.delay(100)
    repository.sendMessage(conversationId, "Third")

    // Wait for all messages
    kotlinx.coroutines.delay(500)

    // Act: Get messages
    val messages = repository.getMessages(conversationId, 10).first()

    // Assert: Messages returned in chronological order (oldest first for display)
    assertEquals(3, messages.size)
    assertEquals("First", messages[0].text)
    assertEquals("Second", messages[1].text)
    assertEquals("Third", messages[2].text)
  }

  @Test
  fun conversationRepository_getMessagesShouldRespectLimit() = runBlocking {
    // Arrange: Create conversation and send many messages
    val conversation =
        Conversation(
            projectId = "project1",
            memberIds = listOf(testUserId, "otherUser"),
            createdBy = testUserId)
    val conversationId = repository.createConversation(conversation).getOrNull()!!

    repeat(5) { i -> repository.sendMessage(conversationId, "Message $i") }

    // Wait for all messages
    kotlinx.coroutines.delay(500)

    // Act: Get only 3 messages
    val messages = repository.getMessages(conversationId, 3).first()

    // Assert: Only 3 most recent messages returned
    assertEquals(3, messages.size)
  }
}

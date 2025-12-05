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

  override fun getCollectionPaths(): List<String> = listOf("conversations")

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
  fun createConversation_shouldCreateConversationInFirestore() = runBlocking {
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
  fun getConversationsForCurrentUser_shouldReturnUserConversations() = runBlocking {
    // Arrange: Create two conversations where current user is a member
    val conv1 =
        Conversation(
            projectId = "project1", memberIds = listOf(testUserId, "user2"), createdBy = testUserId)
    val conv2 =
        Conversation(
            projectId = "project2", memberIds = listOf(testUserId, "user3"), createdBy = testUserId)

    repository.createConversation(conv1)
    repository.createConversation(conv2)

    // Act: Fetch all conversations for current user
    val conversations = repository.getConversationsForCurrentUser().first()

    // Assert: Both conversations should be returned
    assertEquals(2, conversations.size)
  }

  @Test
  fun findExistingConversation_shouldFindDuplicate() = runBlocking {
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
  fun findExistingConversation_shouldReturnNullWhenNotExists() = runBlocking {
    // Act: Try to find a conversation that doesn't exist
    val existing = repository.findExistingConversation("project1", testUserId, "nonExistentUser")

    // Assert: Should return null when no conversation exists
    assertNull(existing)
  }

  @Test
  fun deleteConversation_shouldRemoveFromFirestore() = runBlocking {
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
  fun sendFileMessage_shouldCreateFileMessageInFirestore() = runBlocking {
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
  fun sendFileMessage_shouldUpdateConversationMetadata() = runBlocking {
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
  fun sendFileMessage_shouldAppearInMessagesList() = runBlocking {
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
  fun sendFileMessage_withLongText_shouldTruncatePreview() = runBlocking {
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
}

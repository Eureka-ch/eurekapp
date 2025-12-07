package ch.eureka.eurekapp.model.data.conversation

import kotlinx.coroutines.flow.Flow

/*
Co-author: GPT-5 Codex
Co-author: Grok
*/

/**
 * Repository interface for conversation operations.
 *
 * Provides methods for creating, fetching, and managing conversations between project members.
 */
interface ConversationRepository {
  /** Get all conversations for the current user with real-time updates */
  fun getConversationsForCurrentUser(): Flow<List<Conversation>>

  /** Get conversations in a specific project for the current user */
  fun getConversationsInProject(projectId: String): Flow<List<Conversation>>

  /** Get a specific conversation by ID */
  fun getConversationById(conversationId: String): Flow<Conversation?>

  /** Check if a conversation already exists between users in a project */
  suspend fun findExistingConversation(projectId: String, vararg userIds: String): Conversation?

  /** Create a new conversation */
  suspend fun createConversation(conversation: Conversation): Result<String>

  /** Delete a conversation */
  suspend fun deleteConversation(conversationId: String): Result<Unit>

  /**
   * Get messages in a conversation with real-time updates.
   *
   * @param conversationId The ID of the conversation.
   * @param limit Maximum number of messages to retrieve (default: 50).
   */
  fun getMessages(conversationId: String, limit: Int = 50): Flow<List<ConversationMessage>>

  /** Send a message to a conversation */
  suspend fun sendMessage(conversationId: String, text: String): Result<ConversationMessage>

  /** Send a file message to a conversation */
  suspend fun sendFileMessage(
      conversationId: String,
      text: String,
      fileUrl: String
  ): Result<ConversationMessage>

  /** Mark all messages in a conversation as read by the current user */
  suspend fun markMessagesAsRead(conversationId: String): Result<Unit>

  /** Update the text of a message. Only the sender can update their own message. */
  suspend fun updateMessage(
      conversationId: String,
      messageId: String,
      newText: String
  ): Result<Unit>

  /** Soft-delete a message. Only the sender can delete their own message. */
  suspend fun deleteMessage(conversationId: String, messageId: String): Result<Unit>

  /** Remove the attachment reference from a message. Does not delete the file from storage. */
  suspend fun removeAttachment(conversationId: String, messageId: String): Result<Unit>
}

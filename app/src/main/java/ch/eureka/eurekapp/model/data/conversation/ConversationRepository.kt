package ch.eureka.eurekapp.model.data.conversation

import kotlinx.coroutines.flow.Flow

/*
Co-author: GPT-5 Codex
*/

/**
 * Repository interface for conversation operations.
 *
 * Provides methods for creating, fetching, and managing 1-on-1 conversations between project
 * members.
 */
interface ConversationRepository {
  /** Get all conversations for the current user with real-time updates */
  fun getConversationsForCurrentUser(): Flow<List<Conversation>>

  /** Get conversations in a specific project for the current user */
  fun getConversationsInProject(projectId: String): Flow<List<Conversation>>

  /** Get a specific conversation by ID */
  fun getConversationById(conversationId: String): Flow<Conversation?>

  /** Check if a conversation already exists between two users in a project */
  suspend fun findExistingConversation(
      projectId: String,
      userId1: String,
      userId2: String
  ): Conversation?

  /** Create a new conversation */
  suspend fun createConversation(conversation: Conversation): Result<String>

  /** Delete a conversation */
  suspend fun deleteConversation(conversationId: String): Result<Unit>
}

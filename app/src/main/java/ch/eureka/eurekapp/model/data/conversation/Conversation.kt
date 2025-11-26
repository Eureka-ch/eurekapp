package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp

/*
Co-author: GPT-5 Codex
*/

/**
 * Data class representing a conversation between project members.
 *
 * Conversations are scoped to a project and can contain multiple members. They are stored as a
 * top-level collection in Firestore for efficient querying across projects.
 *
 * @property conversationId Unique identifier for the conversation.
 * @property projectId ID of the project this conversation belongs to.
 * @property memberIds List of user IDs participating in this conversation.
 * @property createdBy User ID of the person who created this conversation.
 * @property createdAt Timestamp when the conversation was created.
 */
data class Conversation(
    val conversationId: String = "",
    val projectId: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

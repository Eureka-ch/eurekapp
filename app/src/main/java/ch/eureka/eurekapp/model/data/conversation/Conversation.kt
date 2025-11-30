package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

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
 * @property createdAt Timestamp when the conversation was created (set by server).
 * @property lastMessageAt Timestamp of the most recent message.
 * @property lastMessagePreview Preview text of the most recent message.
 * @property lastMessageSenderId User ID of who sent the last message.
 * @property lastReadAt Map of user IDs to their last read timestamp.
 */
data class Conversation(
    val conversationId: String = "",
    val projectId: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val lastMessageAt: Timestamp? = null,
    val lastMessagePreview: String? = null,
    val lastMessageSenderId: String? = null,
    val lastReadAt: Map<String, Timestamp> = emptyMap()
)

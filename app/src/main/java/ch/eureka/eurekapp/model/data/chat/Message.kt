package ch.eureka.eurekapp.model.data.chat

import com.google.firebase.Timestamp

/**
 * Data class representing a message within a chat channel.
 *
 * Messages are stored as a subcollection under each chat channel document. They support references
 * to other entities (tasks, meetings, etc.) for rich conversations.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property messageID Unique identifier for the message.
 * @property text The content of the message.
 * @property senderId User ID of the person who sent this message.
 * @property createdAt Timestamp when the message was sent.
 * @property references List of IDs referencing other entities (tasks, meetings, etc.).
 */
data class Message(
    val messageID: String = "",
    val text: String = "",
    val senderId: String = "",
    val createdAt: Timestamp = Timestamp(0, 0),
    val references: List<String> = emptyList()
)

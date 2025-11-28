package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * Data class representing a message within a conversation.
 *
 * @property messageId Unique identifier for the message.
 * @property conversationId ID of the conversation this message belongs to.
 * @property senderId User ID of the message sender.
 * @property text The message content.
 * @property createdAt Timestamp when the message was sent.
 */
data class ConversationMessage(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

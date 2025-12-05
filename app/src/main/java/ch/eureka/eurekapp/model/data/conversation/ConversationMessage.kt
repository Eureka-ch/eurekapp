package ch.eureka.eurekapp.model.data.conversation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

/**
 * Data class representing a message within a conversation.
 *
 * @property messageId Unique identifier for the message.
 * @property senderId User ID of the message sender.
 * @property text The message content.
 * @property createdAt Timestamp when the message was sent (set by server).
 * @property isFile Whether this message contains a file attachment.
 * @property fileUrl URL to download the file if isFile is true.
 */
data class ConversationMessage(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val isFile: Boolean = false,
    val fileUrl: String = ""
)

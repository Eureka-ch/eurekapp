/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing a note or message stored locally on the device.
 *
 * @property localId The internal Primary Key for the local SQLite database.
 * @property messageId The global unique identifier for the message.
 * @property text The actual content body of the note/message.
 * @property senderId The unique identifier (UID) of the user who created this note.
 * @property createdAtMillis The creation timestamp in milliseconds.
 * @property isPendingSync True if this note needs to be uploaded/updated/deleted in the cloud.
 * @property isPrivacyLocalOnly True if the user explicitly chose "Local" storage for this note.
 * @property isDeleted True if the note is hidden from the user and waiting for cloud deletion.
 */
@Entity(tableName = "local_notes")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val messageId: String,
    val text: String,
    val senderId: String,
    val createdAtMillis: Long,
    val isPendingSync: Boolean = false,
    val isPrivacyLocalOnly: Boolean = false,
    val isDeleted: Boolean = false
)

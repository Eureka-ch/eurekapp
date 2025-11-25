/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import com.google.firebase.Timestamp

/** Mappers to convert between the Database Entity (Room) and the Domain Model (UI/Logic). */

/**
 * Converts the raw database entity into a clean domain model for the UI.
 *
 * This function handles the conversion of primitive SQL types back into rich objects:
 * - Converts [Long] milliseconds back to a Firebase [Timestamp].
 *
 * @return A [Message] object ready to be displayed in the UI.
 * @note Currently, the `references` list is not parsed from JSON and returns an empty list.
 */
fun MessageEntity.toDomainModel(): Message {
  return Message(
      messageID = this.messageId,
      text = this.text,
      senderId = this.senderId,
      // Convert Milliseconds (Long) -> Seconds (for Timestamp)
      createdAt = Timestamp(this.createdAtMillis / 1000, 0),
      references = emptyList())
}

/**
 * Converts the domain model into a database entity optimized for SQLite storage.
 *
 * This function flattens complex objects into primitives supported by Room:
 * - Converts Firebase [Timestamp] into a [Long] (milliseconds).
 * - Serializes lists into Strings (JSON).
 *
 * @return A [MessageEntity] ready to be inserted into the local database.
 * @note Currently, the `references` list is not serialized and is saved as an empty string.
 */
fun Message.toEntity(): MessageEntity {
  return MessageEntity(
      messageId = this.messageID,
      text = this.text,
      senderId = this.senderId,
      // Convert Timestamp (Seconds) -> Milliseconds (Long)
      createdAtMillis = this.createdAt.seconds * 1000,
      referencesJson = "")
}

/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import com.google.firebase.Timestamp

/** Mappers to convert between the Database Entity (Room) and the Domain Model (UI/Logic). */

/**
 * Converts the raw database entity into a clean domain model for the UI.
 *
 * @return A [Message] object ready to be displayed in the UI.
 */
fun MessageEntity.toDomainModel(): Message {
  return Message(
      messageID = this.messageId,
      text = this.text,
      senderId = this.senderId,
      // Convert Milliseconds (Long) -> Seconds (for Timestamp)
      createdAt = Timestamp(this.createdAtMillis / 1000, 0))
}

/**
 * Converts the domain model into a database entity optimized for SQLite storage.
 *
 * @return A [MessageEntity] ready to be inserted into the local database.
 */
fun Message.toEntity(): MessageEntity {
  return MessageEntity(
      messageId = this.messageID,
      text = this.text,
      senderId = this.senderId,
      // Convert Timestamp (Seconds) -> Milliseconds (Long)
      createdAtMillis = this.createdAt.seconds * 1000)
}

/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/** Data Access Object (DAO) for managing [MessageEntity] items in the local Room database. */
@Dao
interface MessageDao {

  /**
   * Observes all local notes for a specific user.
   *
   * @param userId The unique identifier of the user.
   * @return A [Flow] emitting the list of local messages.
   */
  @Query("SELECT * FROM local_notes WHERE senderId = :userId ORDER BY createdAtMillis DESC")
  fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

  /** Retrieves a specific message by its ID and user ID. */
  @Query("SELECT * FROM local_notes WHERE messageId = :messageId AND senderId = :userId LIMIT 1")
  fun getMessageById(messageId: String, userId: String): MessageEntity?

  /** Inserts or updates a single message in the local database. */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessage(note: MessageEntity): Long

  /** Updates the text content of an existing local message. */
  @Query(
      "UPDATE local_notes SET text = :newText WHERE messageId = :messageId AND senderId = :userId")
  fun updateMessageText(messageId: String, userId: String, newText: String)

  /** Permanently removes a message row from the local database. */
  @Query("DELETE FROM local_notes WHERE messageId = :messageId AND senderId = :userId")
  fun deleteMessage(messageId: String, userId: String): Int

  /** Batch deletes messages (used for multi-select). */
  @Query("DELETE FROM local_notes WHERE messageId IN (:messageIds) AND senderId = :userId")
  suspend fun deleteMessages(messageIds: List<String>, userId: String)
}

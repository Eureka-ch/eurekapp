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
   * Observes all messages for a specific user.
   *
   * @param userId The ID of the user.
   * @return A Flow of list of messages.
   */
  @Query("SELECT * FROM local_notes WHERE senderId = :userId ORDER BY createdAtMillis DESC")
  fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

  /**
   * Retrieves a specific message by its ID.
   *
   * @param messageId The unique ID of the message.
   * @param userId The ID of the current user (security check).
   * @return The message entity or null if not found.
   */
  @Query("SELECT * FROM local_notes WHERE messageId = :messageId AND senderId = :userId LIMIT 1")
  fun getMessageById(messageId: String, userId: String): MessageEntity?

  /**
   * Inserts a single message.
   *
   * @param note The message entity to insert.
   * @return The row ID.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessage(note: MessageEntity): Long

  /**
   * Inserts a list of messages efficiently.
   *
   * @param notes The list of message entities to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessages(notes: List<MessageEntity>)

  /**
   * Updates the text of an existing message.
   *
   * @param messageId The ID of the message to update.
   * @param userId The ID of the user.
   * @param newText The new text content.
   * @param isPendingSync Whether this update needs to be synced to cloud.
   */
  @Query(
      "UPDATE local_notes SET text = :newText, isPendingSync = :isPendingSync WHERE messageId = :messageId AND senderId = :userId")
  fun updateMessageText(messageId: String, userId: String, newText: String, isPendingSync: Boolean)

  /**
   * Deletes a message.
   *
   * @param messageId The ID of the message to delete.
   * @param userId The ID of the user.
   * @return The number of rows deleted.
   */
  @Query("DELETE FROM local_notes WHERE messageId = :messageId AND senderId = :userId")
  fun deleteMessage(messageId: String, userId: String): Int

  /**
   * Gets pending sync messages.
   *
   * @param userId The ID of the user.
   * @return List of pending messages.
   */
  @Query(
      "SELECT * FROM local_notes WHERE senderId = :userId AND isPendingSync = 1 AND isPrivacyLocalOnly = 0")
  fun getPendingSyncMessages(userId: String): List<MessageEntity>

  /**
   * Gets the IDs of all messages that are currently pending sync. Used to prevent overwriting local
   * edits with incoming cloud data.
   *
   * @param userId The ID of the user.
   * @return List of message IDs.
   */
  @Query("SELECT messageId FROM local_notes WHERE senderId = :userId AND isPendingSync = 1")
  fun getPendingSyncMessageIds(userId: String): List<String>

  /**
   * Marks a specific message as synced.
   *
   * @param messageId The ID of the message.
   * @param userId The ID of the user.
   * @return Number of rows updated.
   */
  @Query(
      "UPDATE local_notes SET isPendingSync = 0 WHERE messageId = :messageId AND senderId = :userId")
  fun markAsSynced(messageId: String, userId: String): Int

  /**
   * Updates all local-only notes to be ready for cloud upload.
   *
   * @param userId The ID of the user.
   * @return Number of rows updated.
   */
  @Query(
      "UPDATE local_notes SET isPrivacyLocalOnly = 0, isPendingSync = 1 WHERE senderId = :userId AND isPrivacyLocalOnly = 1")
  fun makeAllMessagesPublicForUser(userId: String): Int
}

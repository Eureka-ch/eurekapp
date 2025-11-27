/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [MessageEntity] items in the local Room database.
 * * NOTE: Since these are now blocking calls, they must be called from a background thread
 *   (Dispatchers.IO), which ViewModel and Worker are already doing.
 */
@Dao
interface MessageDao {

  @Query("SELECT * FROM local_notes WHERE senderId = :userId ORDER BY createdAtMillis DESC")
  fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

  /** Inserts a message. Blocking call (Run on Dispatchers.IO). */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessage(note: MessageEntity): Long

  /** Deletes a message. Blocking call (Run on Dispatchers.IO). */
  @Query("DELETE FROM local_notes WHERE messageId = :messageId AND senderId = :userId")
  fun deleteMessage(messageId: String, userId: String): Int

  /** Gets pending sync messages. Blocking call (Run on Dispatchers.IO). */
  @Query(
      "SELECT * FROM local_notes WHERE senderId = :userId AND isPendingSync = 1 AND isPrivacyLocalOnly = 0")
  fun getPendingSyncMessages(userId: String): List<MessageEntity>

  /** Marks a specific message as synced. Blocking call (Run on Dispatchers.IO). */
  @Query(
      "UPDATE local_notes SET isPendingSync = 0 WHERE messageId = :messageId AND senderId = :userId")
  fun markAsSynced(messageId: String, userId: String): Int

  /**
   * Updates all local-only notes to be ready for cloud upload. Blocking call (Run on
   * Dispatchers.IO).
   */
  @Query(
      "UPDATE local_notes SET isPrivacyLocalOnly = 0, isPendingSync = 1 WHERE senderId = :userId AND isPrivacyLocalOnly = 1")
  fun makeAllMessagesPublicForUser(userId: String): Int
}

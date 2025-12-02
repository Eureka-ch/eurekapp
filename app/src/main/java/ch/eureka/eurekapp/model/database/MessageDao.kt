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
 *
 * This interface defines all the database interactions required for the Self-Notes feature,
 * including CRUD operations, sync status management, and soft-delete handling.
 */
@Dao
interface MessageDao {

  /**
   * Observes all visible (non-deleted) messages for a specific user.
   *
   * This query filters out messages marked as `isDeleted = 1` to ensure the UI immediately reflects
   * deletion even if the sync worker hasn't processed the cloud deletion yet.
   *
   * @param userId The unique identifier of the user.
   * @return A [Flow] emitting the list of visible messages, ordered by creation time (newest
   *   first).
   */
  @Query(
      "SELECT * FROM local_notes WHERE senderId = :userId AND isDeleted = 0 ORDER BY createdAtMillis DESC")
  fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

  /**
   * Retrieves a specific message by its ID and user ID.
   *
   * @param messageId The unique identifier of the message.
   * @param userId The unique identifier of the user (security check to prevent cross-user access).
   * @return The [MessageEntity] if found, or null otherwise.
   */
  @Query("SELECT * FROM local_notes WHERE messageId = :messageId AND senderId = :userId LIMIT 1")
  fun getMessageById(messageId: String, userId: String): MessageEntity?

  /**
   * Inserts or updates a single message in the database.
   *
   * If a conflict occurs (same Primary Key), the existing row is replaced.
   *
   * @param note The message entity to insert.
   * @return The row ID of the inserted item.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessage(note: MessageEntity): Long

  /**
   * Inserts or updates a list of messages efficiently in a single transaction.
   *
   * @param notes The list of message entities to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertMessages(notes: List<MessageEntity>)

  /**
   * Updates the text content of an existing message.
   *
   * This is used when the user edits a note. It also updates the sync status.
   *
   * @param messageId The ID of the message to update.
   * @param userId The ID of the user.
   * @param newText The new text content.
   * @param isPendingSync Whether this update needs to be synced to the cloud (true if cloud mode is
   *   enabled).
   */
  @Query(
      "UPDATE local_notes SET text = :newText, isPendingSync = :isPendingSync WHERE messageId = :messageId AND senderId = :userId")
  fun updateMessageText(messageId: String, userId: String, newText: String, isPendingSync: Boolean)

  /**
   * Performs a "Soft Delete" on a message.
   *
   * Instead of removing the row immediately, this marks it as `isDeleted = 1` and `isPendingSync =
   * 1`. This hides the note from the UI (via [getMessagesForUser]) while preserving the data for
   * the background worker to sync the deletion to Firestore.
   *
   * @param messageId The ID of the message to mark as deleted.
   * @param userId The ID of the user.
   */
  @Query(
      "UPDATE local_notes SET isDeleted = 1, isPendingSync = 1 WHERE messageId = :messageId AND senderId = :userId")
  fun markAsDeleted(messageId: String, userId: String)

  /**
   * Permanently removes a message row from the local database.
   *
   * This is used in two scenarios:
   * 1. When deleting a "Local-Only" note (no cloud sync needed).
   * 2. After the background worker successfully deletes the note from Firestore (cleaning up the
   *    tombstone).
   *
   * @param messageId The ID of the message to delete.
   * @param userId The ID of the user.
   * @return The number of rows deleted.
   */
  @Query("DELETE FROM local_notes WHERE messageId = :messageId AND senderId = :userId")
  fun deleteMessage(messageId: String, userId: String): Int

  /**
   * Retrieves all messages that are pending synchronization to the cloud.
   *
   * This includes created, updated, AND soft-deleted notes (where `isDeleted = 1`). It excludes
   * "Local-Only" notes (`isPrivacyLocalOnly = 1`).
   *
   * @param userId The ID of the user.
   * @return A list of pending message entities.
   */
  @Query(
      "SELECT * FROM local_notes WHERE senderId = :userId AND isPendingSync = 1 AND isPrivacyLocalOnly = 0")
  fun getPendingSyncMessages(userId: String): List<MessageEntity>

  /**
   * Retrieves the IDs of all messages currently pending sync.
   *
   * Used by the repository to prevent overwriting local pending edits/deletes with incoming cloud
   * data during a sync-down operation (Conflict Resolution: Local Edits Win).
   *
   * @param userId The ID of the user.
   * @return A list of message IDs.
   */
  @Query("SELECT messageId FROM local_notes WHERE senderId = :userId AND isPendingSync = 1")
  fun getPendingSyncMessageIds(userId: String): List<String>

  /**
   * Marks a specific message as successfully synced.
   *
   * Called after a successful upload or update to Firestore.
   *
   * @param messageId The ID of the message.
   * @param userId The ID of the user.
   * @return The number of rows updated.
   */
  @Query(
      "UPDATE local_notes SET isPendingSync = 0 WHERE messageId = :messageId AND senderId = :userId")
  fun markAsSynced(messageId: String, userId: String): Int

  /**
   * Updates all "Local-Only" notes to be public (cloud-ready) and pending sync.
   *
   * Used when the user toggles the storage mode from "Local" to "Cloud".
   *
   * @param userId The ID of the user.
   * @return The number of rows updated.
   */
  @Query(
      "UPDATE local_notes SET isPrivacyLocalOnly = 0, isPendingSync = 1 WHERE senderId = :userId AND isPrivacyLocalOnly = 1")
  fun makeAllMessagesPublicForUser(userId: String): Int
}

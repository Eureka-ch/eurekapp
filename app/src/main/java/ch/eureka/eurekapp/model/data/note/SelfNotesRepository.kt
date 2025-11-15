package ch.eureka.eurekapp.model.data.note

import ch.eureka.eurekapp.model.data.chat.Message
import kotlinx.coroutines.flow.Flow

/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/
/**
 * Repository interface for managing user's self-notes.
 *
 * Self-notes are personal notes that users write to themselves, stored in Firestore. This
 * repository reuses the [Message] data model from the chat feature to represent individual notes.
 */
interface SelfNotesRepository {
  /**
   * Gets all notes for a specific user with real-time updates.
   *
   * @param userId The ID of the user whose notes to retrieve.
   * @param limit Maximum number of notes to retrieve (default: 100).
   * @return Flow emitting list of notes ordered by creation time (newest first).
   */
  fun getNotesForUser(userId: String, limit: Int = 100): Flow<List<Message>>

  /**
   * Creates a new note for the user.
   *
   * @param userId The ID of the user creating the note.
   * @param message The message object representing the note.
   * @return Result containing the created note ID on success, or error on failure.
   */
  suspend fun createNote(userId: String, message: Message): Result<String>

  /**
   * Deletes a note for the user.
   *
   * @param userId The ID of the user who owns the note.
   * @param noteId The ID of the note to delete.
   * @return Result indicating success or failure.
   */
  suspend fun deleteNote(userId: String, noteId: String): Result<Unit>
}

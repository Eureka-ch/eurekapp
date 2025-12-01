/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.model.data.notes

import ch.eureka.eurekapp.model.data.chat.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user's self-notes.
 *
 * Self-notes are personal notes that users write to themselves, stored in Firestore. This
 * repository reuses the [Message] data model from the chat feature to represent individual notes.
 *
 * Security: All operations use the currently authenticated user's ID internally to prevent
 * unauthorized access to other users' notes.
 */
interface SelfNotesRepository {
  /**
   * Gets all notes for the current authenticated user with real-time updates.
   *
   * @param limit Maximum number of notes to retrieve (default: 100).
   * @return Flow emitting list of notes ordered by creation time (newest first).
   * @throws IllegalStateException if no user is authenticated.
   */
  fun getNotes(limit: Int = 100): Flow<List<Message>>

  /**
   * Creates a new note for the current authenticated user.
   *
   * @param message The message object representing the note.
   * @return Result containing the created note ID on success, or error on failure.
   * @throws IllegalStateException if no user is authenticated.
   */
  suspend fun createNote(message: Message): Result<String>

  /**
   * Updates the content of an existing note.
   *
   * @param messageId The ID of the note to update.
   * @param newText The new text content.
   * @return Result indicating success or failure.
   * @throws IllegalStateException if no user is authenticated.
   */
  suspend fun updateNote(messageId: String, newText: String): Result<Unit>

  /**
   * Deletes a note for the current authenticated user.
   *
   * @param noteId The ID of the note to delete.
   * @return Result indicating success or failure.
   * @throws IllegalStateException if no user is authenticated.
   */
  suspend fun deleteNote(noteId: String): Result<Unit>
}

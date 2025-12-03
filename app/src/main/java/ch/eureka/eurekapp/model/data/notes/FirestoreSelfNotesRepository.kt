/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.model.data.notes

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.chat.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of [SelfNotesRepository].
 *
 * Stores user's self-notes in the path: `users/{userId}/selfNotes/{noteId}` Each note is
 * represented as a [Message] object where the `senderId` equals the `userId`.
 *
 * Security: All operations use the currently authenticated user's ID internally to prevent
 * unauthorized access to other users' notes.
 */
class FirestoreSelfNotesRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SelfNotesRepository {

  private fun getCurrentUserId(): String {
    return auth.currentUser?.uid
        ?: throw IllegalStateException("User must be authenticated to access notes")
  }

  override fun getNotes(limit: Int): Flow<List<Message>> = callbackFlow {
    val userId = getCurrentUserId()
    val listener =
        firestore
            .collection(FirestorePaths.selfNotesPath(userId))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val notes =
                  snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) }
                      ?: emptyList()
              trySend(notes)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createNote(message: Message): Result<String> = runCatching {
    val userId = getCurrentUserId()
    firestore.document(FirestorePaths.selfNotePath(userId, message.messageID)).set(message).await()
    message.messageID
  }

  override suspend fun updateNote(messageId: String, newText: String): Result<Unit> = runCatching {
    val userId = getCurrentUserId()
    firestore
        .document(FirestorePaths.selfNotePath(userId, messageId))
        .update("text", newText)
        .await()
  }

  override suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
    val userId = getCurrentUserId()
    firestore.document(FirestorePaths.selfNotePath(userId, noteId)).delete().await()
  }
}

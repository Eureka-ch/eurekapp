package ch.eureka.eurekapp.model.data.note

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.chat.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/
/**
 * Firestore implementation of [SelfNotesRepository].
 *
 * Stores user's self-notes in the path: `users/{userId}/selfNotes/{noteId}` Each note is
 * represented as a [Message] object where the `senderId` equals the `userId`.
 */
class FirestoreSelfNotesRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SelfNotesRepository {

  override fun getNotesForUser(userId: String, limit: Int): Flow<List<Message>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.USERS)
            .document(userId)
            .collection(FirestorePaths.SELF_NOTES)
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

  override suspend fun createNote(userId: String, message: Message): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.USERS)
        .document(userId)
        .collection(FirestorePaths.SELF_NOTES)
        .document(message.messageID)
        .set(message)
        .await()
    message.messageID
  }

  override suspend fun deleteNote(userId: String, noteId: String): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.USERS)
        .document(userId)
        .collection(FirestorePaths.SELF_NOTES)
        .document(noteId)
        .delete()
        .await()
  }
}

package ch.eureka.eurekapp.model.data.conversation

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * Firestore implementation of ConversationRepository.
 *
 * Conversations are stored as a top-level collection for efficient querying across all projects.
 */
class FirestoreConversationRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ConversationRepository {

  override fun getConversationsForCurrentUser(): Flow<List<Conversation>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .whereArrayContains("memberIds", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val conversations =
                  snapshot?.documents?.mapNotNull { it.toObject(Conversation::class.java) }
                      ?: emptyList()
              trySend(conversations)
            }
    awaitClose { listener.remove() }
  }

  override fun getConversationsInProject(projectId: String): Flow<List<Conversation>> =
      callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
          trySend(emptyList())
          close()
          return@callbackFlow
        }

        val listener =
            firestore
                .collection(FirestorePaths.CONVERSATIONS)
                .whereEqualTo("projectId", projectId)
                .whereArrayContains("memberIds", currentUserId)
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  val conversations =
                      snapshot?.documents?.mapNotNull { it.toObject(Conversation::class.java) }
                          ?: emptyList()
                  trySend(conversations)
                }
        awaitClose { listener.remove() }
      }

  override fun getConversationById(conversationId: String): Flow<Conversation?> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              trySend(snapshot?.toObject(Conversation::class.java))
            }
    awaitClose { listener.remove() }
  }

  override suspend fun findExistingConversation(
      projectId: String,
      userId1: String,
      userId2: String
  ): Conversation? {
    // Query for conversations in this project containing userId1
    val snapshot =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .whereEqualTo("projectId", projectId)
            .whereArrayContains("memberIds", userId1)
            .get()
            .await()

    // Filter locally to find one that also contains userId2
    return snapshot.documents
        .mapNotNull { it.toObject(Conversation::class.java) }
        .find { it.memberIds.contains(userId2) && it.memberIds.size == 2 }
  }

  override suspend fun createConversation(conversation: Conversation): Result<String> =
      runCatching {
        val docRef =
            if (conversation.conversationId.isNotEmpty()) {
              firestore
                  .collection(FirestorePaths.CONVERSATIONS)
                  .document(conversation.conversationId)
            } else {
              firestore.collection(FirestorePaths.CONVERSATIONS).document()
            }

        val conversationWithId = conversation.copy(conversationId = docRef.id)
        docRef.set(conversationWithId).await()
        docRef.id
      }

  override suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
    firestore.collection(FirestorePaths.CONVERSATIONS).document(conversationId).delete().await()
  }
}

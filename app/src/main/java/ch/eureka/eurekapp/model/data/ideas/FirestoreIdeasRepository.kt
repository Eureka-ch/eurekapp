/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.model.data.ideas

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.chat.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreIdeasRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IdeasRepository {

  override fun getIdeasForProject(projectId: String): Flow<List<Idea>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.IDEAS)
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val ideas =
                  snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Idea::class.java)?.copy(ideaId = doc.id)
                  } ?: emptyList()
              trySend(ideas)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createIdea(idea: Idea): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(idea.projectId)
        .collection(FirestorePaths.IDEAS)
        .document(idea.ideaId)
        .set(idea)
        .await()

    idea.ideaId
  }

  override suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit> = runCatching {
    // Delete the idea document
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .delete()
        .await()

    // Delete all messages in the idea (optional cleanup)
    val messagesSnapshot =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.IDEAS)
            .document(ideaId)
            .collection(FirestorePaths.IDEA_MESSAGES)
            .get()
            .await()

    messagesSnapshot.documents.forEach { it.reference.delete() }
  }

  override fun getMessagesForIdea(projectId: String, ideaId: String): Flow<List<Message>> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.IDEAS)
                .document(ideaId)
                .collection(FirestorePaths.IDEA_MESSAGES)
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                  }
                  val messages =
                      snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(messageID = doc.id)
                      } ?: emptyList()
                  trySend(messages)
                }
        awaitClose { listener.remove() }
      }

  override suspend fun sendMessage(
      projectId: String,
      ideaId: String,
      message: Message
  ): Result<Unit> = runCatching {
    // Verify user is authenticated
    auth.currentUser?.uid ?: throw Exception("User not authenticated")

    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .collection(FirestorePaths.IDEA_MESSAGES)
        .document(message.messageID)
        .set(message)
        .await()
  }

  override suspend fun addParticipant(
      projectId: String,
      ideaId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .update("participantIds", FieldValue.arrayUnion(userId))
        .await()
  }
}

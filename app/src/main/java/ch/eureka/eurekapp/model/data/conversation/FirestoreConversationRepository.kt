package ch.eureka.eurekapp.model.data.conversation

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.ActivityLogger
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
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
      vararg userIds: String
  ): Conversation? {
    if (userIds.isEmpty()) return null

    val snapshot =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .whereEqualTo("projectId", projectId)
            .whereArrayContains("memberIds", userIds[0])
            .get()
            .await()

    val userIdSet = userIds.toSet()
    return snapshot.documents
        .mapNotNull { it.toObject(Conversation::class.java) }
        .find { it.memberIds.toSet() == userIdSet }
  }

  override suspend fun createConversation(conversation: Conversation): Result<String> =
      runCatching {
        val currentUserId =
            auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

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

        // Log activity to global feed after successful creation
        if (conversationWithId.projectId.isNotEmpty()) {
          // Get other member names for metadata
          val otherMembers = conversationWithId.memberIds.filter { it != currentUserId }
          val memberNames = otherMembers.mapNotNull { memberId ->
            try {
              val userDoc = firestore
                  .collection(FirestorePaths.USERS)
                  .document(memberId)
                  .get()
                  .await()
              userDoc.getString("displayName")
            } catch (e: Exception) {
              null
            }
          }

          ActivityLogger.logActivity(
              projectId = conversationWithId.projectId,
              activityType = ActivityType.CREATED,
              entityType = EntityType.MESSAGE,
              entityId = docRef.id,
              userId = currentUserId,
              metadata = mapOf(
                  "title" to "Conversation with ${memberNames.joinToString(", ")}",
                  "conversationId" to docRef.id
              )
          )
        }

        docRef.id
      }

  override suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
    firestore.collection(FirestorePaths.CONVERSATIONS).document(conversationId).delete().await()
  }

  override fun getMessages(conversationId: String, limit: Int): Flow<List<ConversationMessage>> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.CONVERSATIONS)
                .document(conversationId)
                .collection(FirestorePaths.CONVERSATION_MESSAGES)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  val messages =
                      snapshot?.documents?.mapNotNull {
                        it.toObject(ConversationMessage::class.java)
                      } ?: emptyList()
                  // Reverse to return oldest-first for display
                  trySend(messages.reversed())
                }
        awaitClose { listener.remove() }
      }

  override suspend fun sendMessage(
      conversationId: String,
      text: String
  ): Result<ConversationMessage> = runCatching {
    val currentUserId =
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    val messagesCollection =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .collection(FirestorePaths.CONVERSATION_MESSAGES)

    val docRef = messagesCollection.document()
    val message = ConversationMessage(messageId = docRef.id, senderId = currentUserId, text = text)

    docRef.set(message).await()

    // Update conversation metadata with server timestamp
    firestore
        .collection(FirestorePaths.CONVERSATIONS)
        .document(conversationId)
        .update(
            mapOf(
                "lastMessageAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "lastMessagePreview" to text.take(100),
                "lastMessageSenderId" to currentUserId))
        .await()

    // Log activity to global feed after successful message send
    val conversationDoc = firestore
        .collection(FirestorePaths.CONVERSATIONS)
        .document(conversationId)
        .get()
        .await()
    val conversation = conversationDoc.toObject(Conversation::class.java)

    // Log activity if conversation exists and has a projectId
    if (conversation != null && conversation.projectId.isNotEmpty()) {
      ActivityLogger.logActivity(
          projectId = conversation.projectId,
          activityType = ActivityType.CREATED,
          entityType = EntityType.MESSAGE,
          entityId = message.messageId,
          userId = currentUserId,
          metadata = mapOf(
              "title" to text.take(50),
              "conversationId" to conversationId
          )
      )
    }

    message
  }

  override suspend fun markMessagesAsRead(conversationId: String): Result<Unit> = runCatching {
    val currentUserId =
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    firestore
        .collection(FirestorePaths.CONVERSATIONS)
        .document(conversationId)
        .update("lastReadAt.$currentUserId", com.google.firebase.Timestamp.now())
        .await()
  }
}

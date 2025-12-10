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
Co-author: Grok
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

  companion object {
    private const val USER_NOT_AUTHENTICATED_ERROR = "User not authenticated"
  }

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
                  snapshot?.documents
                      ?.mapNotNull { it.toObject(Conversation::class.java) }
                      ?.filter { conversation ->
                        // Only show conversations where user sent or received messages
                        conversation.lastMessageAt != null &&
                            (conversation.lastMessageSenderId == currentUserId ||
                                conversation.memberIds.contains(currentUserId))
                      }
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
                      snapshot?.documents
                          ?.mapNotNull { it.toObject(Conversation::class.java) }
                          ?.filter { conversation ->
                            // Only show conversations where user sent or received messages
                            conversation.lastMessageAt != null &&
                                (conversation.lastMessageSenderId == currentUserId ||
                                    conversation.memberIds.contains(currentUserId))
                          }
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
            auth.currentUser?.uid ?: throw IllegalStateException(USER_NOT_AUTHENTICATED_ERROR)

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

        require(!conversationWithId.projectId.isBlank()) {
          "Conversation has blank projectId - malformed data"
        }
        // Log activity to global feed after successful creation
        val memberNames = fetchMemberNames(conversationWithId.memberIds, currentUserId)

        if (memberNames.isNotEmpty()) {
          ActivityLogger.logActivity(
              projectId = conversationWithId.projectId,
              activityType = ActivityType.CREATED,
              entityType = EntityType.MESSAGE,
              entityId = docRef.id,
              userId = currentUserId,
              metadata =
                  mapOf(
                      "title" to "Conversation with ${memberNames.joinToString(", ")}",
                      "conversationId" to docRef.id))
        }
        docRef.id
      }

  override suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
    // Get conversation data before deletion for activity log
    val conversationSnapshot =
        firestore.collection(FirestorePaths.CONVERSATIONS).document(conversationId).get().await()
    val conversation = conversationSnapshot.toObject(Conversation::class.java)

    // Validate conversation data
    require(conversation == null || !conversation.projectId.isBlank()) {
      "Conversation has blank projectId - malformed data"
    }

    // Perform deletion
    firestore.collection(FirestorePaths.CONVERSATIONS).document(conversationId).delete().await()

    // Log activity to global feed after successful deletion
    logConversationDeletion(conversationId, conversation)
  }

  private suspend fun logConversationDeletion(conversationId: String, conversation: Conversation?) {
    val currentUserId = auth.currentUser?.uid ?: return
    if (conversation == null) return

    val memberNames = fetchMemberNames(conversation.memberIds, currentUserId)
    if (memberNames.isEmpty()) return

    ActivityLogger.logActivity(
        projectId = conversation.projectId,
        activityType = ActivityType.DELETED,
        entityType = EntityType.MESSAGE,
        entityId = conversationId,
        userId = currentUserId,
        metadata =
            mapOf(
                "title" to "Conversation with ${memberNames.joinToString(", ")}",
                "conversationId" to conversationId))
  }

  private suspend fun fetchMemberNames(
      memberIds: List<String>,
      currentUserId: String
  ): List<String> {
    val otherMembers = memberIds.filter { it != currentUserId }
    if (otherMembers.isEmpty()) return emptyList()

    return try {
      val usersSnapshot =
          firestore
              .collection(FirestorePaths.USERS)
              .whereIn(com.google.firebase.firestore.FieldPath.documentId(), otherMembers)
              .get()
              .await()
      usersSnapshot.documents.mapNotNull { it.getString("displayName") }
    } catch (e: Exception) {
      android.util.Log.e(
          "FirestoreConversationRepository",
          "Failed to fetch user names for conversation logging",
          e)
      emptyList()
    }
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
                  // Use manual mapping instead of doc.toObject(ConversationMessage::class.java)
                  // to gracefully handle data inconsistencies
                  // Messages sent with old versions of the app will have missing fields
                  val messages =
                      snapshot?.documents?.mapNotNull { doc ->
                        try {
                          ConversationMessage(
                              messageId = doc.id,
                              senderId = doc.getString("senderId") ?: "",
                              text = doc.getString("text") ?: "",
                              createdAt = doc.getTimestamp("createdAt"),
                              isFile = doc.getBoolean("isFile") ?: false,
                              fileUrl = doc.getString("fileUrl") ?: "")
                        } catch (e: Exception) {
                          null // Skip malformed messages silently
                        }
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
        auth.currentUser?.uid ?: throw IllegalStateException(USER_NOT_AUTHENTICATED_ERROR)

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
    val conversationDoc =
        firestore.collection(FirestorePaths.CONVERSATIONS).document(conversationId).get().await()
    val conversation = conversationDoc.toObject(Conversation::class.java)

    // Log activity if conversation exists and has a projectId
    if (conversation != null && conversation.projectId.isNotEmpty()) {
      ActivityLogger.logActivity(
          projectId = conversation.projectId,
          activityType = ActivityType.CREATED,
          entityType = EntityType.MESSAGE,
          entityId = message.messageId,
          userId = currentUserId,
          metadata = mapOf("title" to text.take(50), "conversationId" to conversationId))
    }

    message
  }

  override suspend fun sendFileMessage(
      conversationId: String,
      text: String,
      fileUrl: String
  ): Result<ConversationMessage> = runCatching {
    val currentUserId =
        auth.currentUser?.uid ?: throw IllegalStateException(USER_NOT_AUTHENTICATED_ERROR)

    val messagesCollection =
        firestore
            .collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .collection(FirestorePaths.CONVERSATION_MESSAGES)

    val docRef = messagesCollection.document()
    val message =
        ConversationMessage(
            messageId = docRef.id,
            senderId = currentUserId,
            text = text,
            isFile = true,
            fileUrl = fileUrl)

    val messageData =
        hashMapOf(
            "senderId" to currentUserId,
            "text" to text,
            "isFile" to true,
            "fileUrl" to fileUrl,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())

    docRef.set(messageData).await()

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

    message
  }

  override suspend fun markMessagesAsRead(conversationId: String): Result<Unit> = runCatching {
    val currentUserId =
        auth.currentUser?.uid ?: throw IllegalStateException(USER_NOT_AUTHENTICATED_ERROR)

    firestore
        .collection(FirestorePaths.CONVERSATIONS)
        .document(conversationId)
        .update("lastReadAt.$currentUserId", com.google.firebase.Timestamp.now())
        .await()
  }
}

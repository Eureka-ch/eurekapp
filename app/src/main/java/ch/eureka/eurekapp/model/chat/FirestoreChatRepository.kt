package ch.eureka.eurekapp.model.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatRepository {

  override fun getChannelById(workspaceId: String, channelId: String): Flow<ChatChannel?> =
      callbackFlow {
        val listener =
            firestore
                .collection("workspaces")
                .document(workspaceId)
                .collection("chatChannels")
                .document(channelId)
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  trySend(snapshot?.toObject(ChatChannel::class.java))
                }
        awaitClose { listener.remove() }
      }

  override fun getChannelsInWorkspace(workspaceId: String): Flow<List<ChatChannel>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("chatChannels")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val channels =
                  snapshot?.documents?.mapNotNull { it.toObject(ChatChannel::class.java) }
                      ?: emptyList()
              trySend(channels)
            }
    awaitClose { listener.remove() }
  }

  override fun getChannelsForContext(
      workspaceId: String,
      contextId: String,
      contextType: ChatContextType
  ): Flow<List<ChatChannel>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("chatChannels")
            .whereEqualTo("contextId", contextId)
            .whereEqualTo("contextType", contextType.name)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val channels =
                  snapshot?.documents?.mapNotNull { it.toObject(ChatChannel::class.java) }
                      ?: emptyList()
              trySend(channels)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createChannel(channel: ChatChannel): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(channel.workspaceId)
        .collection("chatChannels")
        .document(channel.channelID)
        .set(channel)
        .await()
    channel.channelID
  }

  override suspend fun updateChannel(channel: ChatChannel): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(channel.workspaceId)
        .collection("chatChannels")
        .document(channel.channelID)
        .set(channel)
        .await()
  }

  override suspend fun deleteChannel(workspaceId: String, channelId: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("chatChannels")
            .document(channelId)
            .delete()
            .await()
      }

  override fun getMessagesInChannel(
      workspaceId: String,
      channelId: String,
      limit: Int
  ): Flow<List<Message>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("chatChannels")
            .document(channelId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val messages =
                  snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) }
                      ?: emptyList()
              trySend(messages)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun sendMessage(
      workspaceId: String,
      channelId: String,
      message: Message
  ): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("chatChannels")
        .document(channelId)
        .collection("messages")
        .document(message.messageID)
        .set(message)
        .await()
    message.messageID
  }

  override suspend fun updateMessage(
      workspaceId: String,
      channelId: String,
      message: Message
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("chatChannels")
        .document(channelId)
        .collection("messages")
        .document(message.messageID)
        .set(message)
        .await()
  }

  override suspend fun deleteMessage(
      workspaceId: String,
      channelId: String,
      messageId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("chatChannels")
        .document(channelId)
        .collection("messages")
        .document(messageId)
        .delete()
        .await()
  }
}

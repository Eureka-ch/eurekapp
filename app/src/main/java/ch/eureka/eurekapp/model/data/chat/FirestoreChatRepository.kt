package ch.eureka.eurekapp.model.data.chat

import ch.eureka.eurekapp.model.data.FirestorePaths
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

  override fun getChannelById(projectId: String, channelId: String): Flow<ChatChannel?> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.CHAT_CHANNELS)
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

  override fun getChannelsInProject(projectId: String): Flow<List<ChatChannel>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.CHAT_CHANNELS)
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
        .collection(FirestorePaths.PROJECTS)
        .document(channel.projectId)
        .collection(FirestorePaths.CHAT_CHANNELS)
        .document(channel.channelID)
        .set(channel)
        .await()
    channel.channelID
  }

  override suspend fun updateChannel(channel: ChatChannel): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(channel.projectId)
        .collection(FirestorePaths.CHAT_CHANNELS)
        .document(channel.channelID)
        .set(channel)
        .await()
  }

  override suspend fun deleteChannel(projectId: String, channelId: String): Result<Unit> =
      runCatching {
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.CHAT_CHANNELS)
            .document(channelId)
            .delete()
            .await()
      }

  override fun getMessagesInChannel(
      projectId: String,
      channelId: String,
      limit: Int
  ): Flow<List<Message>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.CHAT_CHANNELS)
            .document(channelId)
            .collection(FirestorePaths.MESSAGES)
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
      projectId: String,
      channelId: String,
      message: Message
  ): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.CHAT_CHANNELS)
        .document(channelId)
        .collection(FirestorePaths.MESSAGES)
        .document(message.messageID)
        .set(message)
        .await()
    message.messageID
  }

  override suspend fun updateMessage(
      projectId: String,
      channelId: String,
      message: Message
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.CHAT_CHANNELS)
        .document(channelId)
        .collection(FirestorePaths.MESSAGES)
        .document(message.messageID)
        .set(message)
        .await()
  }

  override suspend fun deleteMessage(
      projectId: String,
      channelId: String,
      messageId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.CHAT_CHANNELS)
        .document(channelId)
        .collection(FirestorePaths.MESSAGES)
        .document(messageId)
        .delete()
        .await()
  }
}

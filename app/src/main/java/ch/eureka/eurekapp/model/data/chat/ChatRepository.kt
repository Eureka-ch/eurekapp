package ch.eureka.eurekapp.model.data.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
  /** Get channel by ID with real-time updates */
  fun getChannelById(projectId: String, channelId: String): Flow<ChatChannel?>

  /** Get all channels in project with real-time updates */
  fun getChannelsInProject(projectId: String): Flow<List<ChatChannel>>

  /** Create a new channel */
  suspend fun createChannel(channel: ChatChannel): Result<String>

  /** Update channel */
  suspend fun updateChannel(channel: ChatChannel): Result<Unit>

  /** Delete channel */
  suspend fun deleteChannel(projectId: String, channelId: String): Result<Unit>

  /** Get messages in channel with real-time updates */
  fun getMessagesInChannel(
      projectId: String,
      channelId: String,
      limit: Int = 50
  ): Flow<List<Message>>

  /** Send a message */
  suspend fun sendMessage(projectId: String, channelId: String, message: Message): Result<String>

  /** Update a message */
  suspend fun updateMessage(projectId: String, channelId: String, message: Message): Result<Unit>

  /** Delete a message */
  suspend fun deleteMessage(projectId: String, channelId: String, messageId: String): Result<Unit>
}

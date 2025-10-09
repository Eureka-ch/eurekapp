package ch.eureka.eurekapp.model.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
  /** Get channel by ID with real-time updates */
  fun getChannelById(workspaceId: String, channelId: String): Flow<ChatChannel?>

  /** Get all channels in workspace with real-time updates */
  fun getChannelsInWorkspace(workspaceId: String): Flow<List<ChatChannel>>

  /** Get channels for specific context (workspace, group, or project) with real-time updates */
  fun getChannelsForContext(
      workspaceId: String,
      contextId: String,
      contextType: ChatContextType
  ): Flow<List<ChatChannel>>

  /** Create a new channel */
  suspend fun createChannel(channel: ChatChannel): Result<String>

  /** Update channel */
  suspend fun updateChannel(channel: ChatChannel): Result<Unit>

  /** Delete channel */
  suspend fun deleteChannel(workspaceId: String, channelId: String): Result<Unit>

  /** Get messages in channel with real-time updates */
  fun getMessagesInChannel(
      workspaceId: String,
      channelId: String,
      limit: Int = 50
  ): Flow<List<Message>>

  /** Send a message */
  suspend fun sendMessage(workspaceId: String, channelId: String, message: Message): Result<String>

  /** Update a message */
  suspend fun updateMessage(workspaceId: String, channelId: String, message: Message): Result<Unit>

  /** Delete a message */
  suspend fun deleteMessage(workspaceId: String, channelId: String, messageId: String): Result<Unit>
}

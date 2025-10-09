package ch.eureka.eurekapp.model.chat

import ch.eureka.eurekapp.model.annotations.firestore.*

enum class ChatContextType {
  WORKSPACE,
  GROUP,
  PROJECT
}

@CollectionPath("workspaces/{workspaceId}/chatChannels")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null")
data class ChatChannel(
    @Required @Immutable val channelID: String,
    @Required @Immutable val workspaceId: String, // For nested path
    @Required @Immutable val contextId: String, // workspaceId, groupId, or projectId
    @Required @Immutable val contextType: ChatContextType,
    @Required @Length(min = 1, max = 100) val name: String
)

package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*

enum class ChatContextType {
    WORKSPACE,
    GROUP,
    PROJECT
}

@CollectionPath("chatChannels")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null"
)
data class ChatChannel(
    @Required
    @Immutable
    val channelID: String,

    @Required
    @Length(min = 1, max = 100)
    val name: String,

    @Required
    @Immutable
    val contextId: String,

    @Required
    @Immutable
    val contextType: ChatContextType
)

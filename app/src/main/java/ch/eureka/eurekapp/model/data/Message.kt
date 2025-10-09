package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*
import com.google.firebase.Timestamp

@CollectionPath("chatChannels/{channelId}/messages")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null && request.auth.uid == request.resource.data.senderId",
    update = "request.auth != null && request.auth.uid == resource.data.senderId",
    delete = "request.auth != null && request.auth.uid == resource.data.senderId"
)
data class Message(
    @Required
    @Immutable
    val messageID: String,

    @Required
    @Length(min = 1, max = 5000)
    val text: String,

    @Required
    @Immutable
    val senderId: String,

    @Required
    @ServerTimestamp
    val createdAt: Timestamp,

    val references: List<String> // References to other messages, tasks, etc.
)

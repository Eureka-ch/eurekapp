package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*
import com.google.firebase.Timestamp

@CollectionPath("users")
@Rules(
    read = "request.auth != null && request.auth.uid == resource.id",
    create = "request.auth != null && request.auth.uid == request.resource.id",
    update = "request.auth != null && request.auth.uid == resource.id",
    delete = "false"
)
data class User(
    @Required
    @Immutable
    val uid: String,

    @Required
    @Length(min = 1, max = 100)
    val displayName: String,

    @Required
    val email: String,

    val photoUrl: String,

    @ServerTimestamp
    val lastActive: Timestamp
)

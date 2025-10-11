package ch.eureka.eurekapp.model.data.user

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val lastActive: Timestamp = Timestamp(0, 0)
)

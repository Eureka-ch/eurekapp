package ch.eureka.eurekapp.model.data.chat

import com.google.firebase.Timestamp

data class Message(
    val messageID: String = "",
    val text: String = "",
    val senderId: String = "",
    val createdAt: Timestamp = Timestamp(0, 0),
    val references: List<String> = emptyList()
)

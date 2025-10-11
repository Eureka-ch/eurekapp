package ch.eureka.eurekapp.model.data.task

import com.google.firebase.Timestamp

data class Task(
    val taskID: String = "",
    val templateId: String = "",
    val projectId: String = "",
    val assignedUserIds: List<String> = emptyList(),
    val dueDate: Timestamp? = null,
    val createdBy: String = ""
)

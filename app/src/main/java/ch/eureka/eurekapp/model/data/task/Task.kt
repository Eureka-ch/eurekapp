package ch.eureka.eurekapp.model.data.task

import com.google.firebase.Timestamp

data class Task(
    val taskID: String = "",
    val templateId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val assignedUserIds: List<String> = emptyList(),
    val dueDate: Timestamp? = null,
    val attachmentUrls: List<String> = emptyList(),
    val customData: Map<String, Any> = emptyMap(),
    val createdBy: String = ""
)

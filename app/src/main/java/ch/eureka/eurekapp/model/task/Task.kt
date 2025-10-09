package ch.eureka.eurekapp.model.task

import ch.eureka.eurekapp.model.annotations.firestore.*
import com.google.firebase.Timestamp

@CollectionPath("workspaces/{workspaceId}/groups/{groupId}/projects/{projectId}/tasks")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null")
data class Task(
    @Required @Immutable val taskID: String = "",
    @Required @Immutable val templateId: String = "",
    @Required @Immutable val projectId: String = "",
    @Required @Immutable val groupId: String = "",
    @Required @Immutable val workspaceId: String = "",
    @Required val assignedUserIds: List<String> = emptyList(),
    val dueDate: Timestamp? = null
)

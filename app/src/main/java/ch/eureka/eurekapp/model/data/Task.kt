package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*
import java.sql.Timestamp

@CollectionPath("workspaces/{workspaceId}/groups/{groupId}/projects/{projectId}/tasks")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null"
)
data class Task(
    @Required
    @Immutable
    val taskID: String,

    @Required
    @Immutable
    val templateId: String,

    @Required
    @Immutable
    val projectId: String,

    @Required
    val assignedUserIds: List<String>,

    val dueDate: Timestamp
)

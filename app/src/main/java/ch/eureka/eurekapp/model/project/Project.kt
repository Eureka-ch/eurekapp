package ch.eureka.eurekapp.model.project

import ch.eureka.eurekapp.model.annotations.firestore.*

@CollectionPath("workspaces/{workspaceId}/groups/{groupId}/projects")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null")
data class Project(
    @Required @Immutable val projectId: String = "",
    @Required @Immutable val groupId: String = "",
    @Required @Immutable val workspaceId: String = "", // Denormalized for easier access
    @Required @Length(min = 1, max = 100) val name: String = "",
    @Length(max = 1000) val description: String = "",
    @Required val status: String = "" // open, in_progress, completed, archived
)

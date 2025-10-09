package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*

@CollectionPath("workspaces/{workspaceId}/groups/{groupId}/projects")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null"
)
data class Project(
    @Required
    @Immutable
    val projectID: String,

    @Required
    @Immutable
    val groupId: String,

    @Required
    @Length(min = 1, max = 100)
    val name: String,

    @Length(max = 1000)
    val description: String,

    @Required
    val status: String
)

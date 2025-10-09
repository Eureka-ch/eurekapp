package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*

@CollectionPath("workspaces/{workspaceId}/taskTemplates")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null"
)
data class TaskTemplate(
    @Required
    @Immutable
    val templateID: String,

    @Required
    @Length(min = 1, max = 100)
    val title: String,

    @Length(max = 1000)
    val description: String,

    val definedFields: Map<String, Any>
)

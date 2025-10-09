package ch.eureka.eurekapp.model.template

import ch.eureka.eurekapp.model.annotations.firestore.*

enum class TemplateContextType {
  WORKSPACE,
  GROUP,
  PROJECT
}

@CollectionPath("workspaces/{workspaceId}/taskTemplates")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null")
data class TaskTemplate(
    @Required @Immutable val templateID: String,
    @Required @Immutable val workspaceId: String, // For nested path
    @Required @Immutable val contextId: String, // workspaceId, groupId, or projectId
    @Required @Immutable val contextType: TemplateContextType,
    @Required @Length(min = 1, max = 100) val title: String,
    @Length(max = 1000) val description: String = "",
    val definedFields: Map<String, Any> = emptyMap() // Custom field definitions
)

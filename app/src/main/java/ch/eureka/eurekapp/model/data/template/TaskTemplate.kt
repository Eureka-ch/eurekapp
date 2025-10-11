package ch.eureka.eurekapp.model.data.template

data class TaskTemplate(
    val templateID: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val definedFields: Map<String, Any> = emptyMap(), // Custom field definitions
    val createdBy: String = ""
)

package ch.eureka.eurekapp.model.data.project

data class Project(
    val projectId: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "", // open, in_progress, completed, archived
    val createdBy: String = ""
)

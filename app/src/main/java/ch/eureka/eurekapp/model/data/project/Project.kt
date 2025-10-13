package ch.eureka.eurekapp.model.data.project

/**
 * Data class representing a project in the application.
 *
 * Projects are the top-level organizational unit containing tasks, meetings, members, and other
 * resources. Each project has members with specific roles stored in a subcollection.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property projectId Unique identifier for the project.
 * @property name The name of the project.
 * @property description Detailed description of the project's goals and scope.
 * @property status Current status of the project (open, in_progress, completed, archived).
 * @property createdBy User ID of the person who created this project.
 */
data class Project(
    val projectId: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "",
    val createdBy: String = ""
)

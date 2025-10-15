package ch.eureka.eurekapp.model.project

import ch.eureka.eurekapp.model.annotations.firestore.*
import com.google.firebase.Timestamp

enum class ProjectStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}

@CollectionPath("projects")
@Rules(
    read = "request.auth != null",
    create = "request.auth != null",
    update = "request.auth != null",
    delete = "request.auth != null")
data class Project(
    @Required @Immutable val projectId: String = "",
    @Required @Immutable val createdBy: String = "",
    @Required val memberIds: List<String> = listOf<String>(),
    @Required @Immutable val createdAt: Timestamp = Timestamp.now(),
    @Required @Length(min = 1, max = 100) val name: String = "",
    @Length(max = 1000) val description: String = "",
    @Required val status: ProjectStatus = ProjectStatus.OPEN // open, in_progress, completed, archived
)

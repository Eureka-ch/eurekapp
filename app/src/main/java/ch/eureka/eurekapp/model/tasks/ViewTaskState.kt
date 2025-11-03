package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.TaskStatus

// Portions of this code were generated with the help of Grok.

data class ViewTaskState(
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val projectId: String = "",
    val availableProjects: List<Project> = emptyList(),
    val attachmentUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val taskSaved: Boolean = false,
    val errorMsg: String? = null,
    val taskId: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val isLoading: Boolean = false
)

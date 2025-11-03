package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.TaskStatus

// Portions of this code were generated with the help of Grok.

data class ViewTaskState(
    override val title: String = "",
    override val description: String = "",
    override val dueDate: String = "",
    override val projectId: String = "",
    override val availableProjects: List<Project> = emptyList(),
    override val attachmentUris: List<Uri> = emptyList(),
    override val isSaving: Boolean = false,
    override val taskSaved: Boolean = false,
    override val errorMsg: String? = null,
    val taskId: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val isLoading: Boolean = false
) : TaskStateCommon

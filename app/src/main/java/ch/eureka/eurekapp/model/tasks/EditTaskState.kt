package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.TaskStatus

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Co-Authored-By: Claude <noreply@anthropic.com>
Portions of this code were generated with the help of Grok.
*/

/** State for the CreateTaskScreen screen. This state holds the data needed to create a new Task */
data class EditTaskState(
    override val title: String = "",
    override val description: String = "",
    override val dueDate: String = "",
    val reminderTime: String = "",
    val templateId: String? = null,
    override val projectId: String = "",
    override val availableProjects: List<Project> = emptyList(),
    val taskId: String = "",
    val assignedUserIds: List<String> = emptyList(),
    override val attachmentUris: List<Uri> = emptyList(),
    val attachmentUrls: List<String> = emptyList(),
    val deletedAttachmentUrls: Set<String> = emptySet(),
    val status: TaskStatus = TaskStatus.TODO,
    val customData: Map<String, Any> = emptyMap(),
    override val isSaving: Boolean = false,
    override val taskSaved: Boolean = false,
    val isDeleting: Boolean = false,
    val taskDeleted: Boolean = false,
    override val errorMsg: String? = null
) : TaskStateReadWrite

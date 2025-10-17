package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.task.TaskStatus

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/** State for the CreateTaskScreen screen. This state holds the data needed to create a new Task */
data class CreateTaskState(
    val templateId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val assignedUserIds: List<String> = emptyList(),
    val dueDate: String = "",
    val attachmentUris: List<Uri> = emptyList(),
    val customData: Map<String, Any> = emptyMap(),
    val errorMsg: String? = null,
    val isSaving: Boolean = false,
    val taskSaved: Boolean = false
)

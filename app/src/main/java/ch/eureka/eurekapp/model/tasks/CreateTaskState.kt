package ch.eureka.eurekapp.model.tasks

import android.net.Uri

/*
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
Co-Authored-By: Claude <noreply@anthropic.com>
Portions of this code were generated with the help of Grok.
*/

/** State for the CreateTaskScreen screen. This state holds the data needed to create a new Task */
data class CreateTaskState(
    override val title: String = "",
    override val description: String = "",
    override val dueDate: String = "",
    override val projectId: String = "",
    override val attachmentUris: List<Uri> = emptyList(),
    override val isSaving: Boolean = false,
    override val taskSaved: Boolean = false,
    override val errorMsg: String? = null
) : TaskStateCommon

package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User

/*
 * Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
 * Portions of this code were generated with the help of Claude <noreply@anthropic.com>, Grok, and GPT-5 Codex.
 * Co-Authored-By: Claude Sonnet 4.5
 */

/** State for the CreateTaskScreen screen. This state holds the data needed to create a new Task */
data class CreateTaskState(
    override val title: String = "",
    override val description: String = "",
    override val dueDate: String = "",
    val reminderTime: String = "",
    override val projectId: String = "",
    override val availableProjects: List<Project> = emptyList(),
    override val availableUsers: List<User> = emptyList(),
    override val selectedAssignedUserIds: List<String> = emptyList(),
    override val attachmentUris: List<Uri> = emptyList(),
    override val isSaving: Boolean = false,
    override val taskSaved: Boolean = false,
    override val errorMsg: String? = null,
    override val dependingOnTasks: List<String> = emptyList(),
    val templateId: String? = null,
    val customData: TaskCustomData = TaskCustomData(),
    val availableTemplates: List<TaskTemplate> = emptyList(),
    val selectedTemplate: TaskTemplate? = null
) : TaskStateReadWrite

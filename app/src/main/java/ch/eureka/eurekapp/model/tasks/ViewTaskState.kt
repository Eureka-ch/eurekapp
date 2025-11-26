// Portions of this code were generated with the help of Grok nad GPT-5.
package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User

data class ViewTaskState(
    override val title: String = "",
    override val description: String = "",
    override val dueDate: String = "",
    override val projectId: String = "",
    override val attachmentUris: List<Uri> = emptyList(),
    override val errorMsg: String? = null,
    val taskId: String = "",
    val attachmentUrls: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val isLoading: Boolean = false,
    val isConnected: Boolean = true,
    val assignedUsers: List<User> = emptyList(),
    val urlsToDownload: List<String> = emptyList(),
    val effectiveAttachments: List<Any> = emptyList()
) : TaskStateRead

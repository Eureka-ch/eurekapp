// Portions of this code were generated with the help of Grok and GPT-5.
package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import androidx.core.net.toUri
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.downloads.DownloadedFile

data class DownloadProgress(
    val isDownloading: Boolean = false,
    val downloadedCount: Int = 0,
    val totalToDownload: Int = 0
)

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
    val downloadedAttachmentUrls: Set<String> = emptySet(),
    val selectedTemplate: TaskTemplate? = null,
    val customData: TaskCustomData = TaskCustomData(),
    val downloadedFiles: List<DownloadedFile> = emptyList(),
    val downloadProgress: DownloadProgress = DownloadProgress()
) : TaskStateRead {

  private val urlToUriMap: Map<String, Uri>
    get() = downloadedFiles.associate { it.url to it.localPath.toUri() }

  val effectiveAttachments: List<Attachment>
    get() = buildList {
      if (isConnected) {
        // Online: show all remote URLs
        attachmentUrls.forEach { metadata -> add(Attachment.parseAttachment(metadata)) }
      } else {
        // Offline: show downloaded files as Local, undownloaded as Remote
        attachmentUrls.forEach { metadata ->
          val url = metadata.substringBefore("|")
          val localUri = urlToUriMap[url]
          if (localUri != null) {
            add(Attachment.Local(localUri))
          } else {
            add(Attachment.parseAttachment(metadata))
          }
        }
      }
      // Add local URIs (e.g., newly taken photos)
      attachmentUris.forEach { uri -> add(Attachment.Local(uri)) }
    }
}

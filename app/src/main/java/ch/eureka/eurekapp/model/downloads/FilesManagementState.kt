// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.net.Uri

data class FileItem(
    val file: DownloadedFile,
    val displayName: String,
    val isImage: Boolean,
    val uri: Uri
)

data class FilesManagementState(val files: List<FileItem> = emptyList())

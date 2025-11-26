// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class FilesManagementViewModel(
    private val dao: DownloadedFileDao,
    private val application: Application
) : ViewModel() {

    val uiState: StateFlow<FilesManagementState> = dao.getAll()
        .map { files ->
            FilesManagementState(files = files.map { file -> processFile(file) })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FilesManagementState())

    private fun processFile(file: DownloadedFile): FileItem {
        val displayName = Uri.decode(file.fileName).substringAfterLast('/').substringBefore('?')
        val extension = displayName.substringAfterLast('.', "").lowercase()
        val isImage = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        val uri = if (file.localPath.startsWith("content://") || file.localPath.startsWith("file://")) {
            file.localPath.toUri()
        } else {
            FileProvider.getUriForFile(application, "${application.packageName}.fileprovider", File(file.localPath))
        }
        return FileItem(file = file, displayName = displayName, isImage = isImage, uri = uri)
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch {
            dao.delete(fileItem.file)
        }
    }

    fun openFile(fileItem: FileItem) {
        val extension = fileItem.displayName.substringAfterLast('.', "").lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileItem.uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pm = application.packageManager
        val activities = pm.queryIntentActivities(intent, 0)
        if (activities.isNotEmpty()) {
            val chooser = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(chooser)
        } else {
            Toast.makeText(application, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }
}

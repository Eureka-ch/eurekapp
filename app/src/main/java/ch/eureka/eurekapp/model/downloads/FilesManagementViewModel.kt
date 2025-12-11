// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.app.Application
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.net.URLDecoder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FilesManagementViewModel(
    private val dao: DownloadedFileDao,
    private val application: Application,
    private val mimeTypeResolver: (String) -> String? = { extension ->
      MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
) : ViewModel() {

  val uiState: StateFlow<FilesManagementState> =
      dao.getAll()
          .map { files -> FilesManagementState(files = files.map { file -> processFile(file) }) }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FilesManagementState())

  private fun processFile(file: DownloadedFile): FileItem {
    val displayName = URLDecoder.decode(file.fileName, "UTF-8")
    val extension = displayName.substringAfterLast('.', "").lowercase()
    val isImage = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    val uri =
        if (file.localPath.startsWith("content://") || file.localPath.startsWith("file://")) {
          file.localPath.toUri()
        } else {
          FileProvider.getUriForFile(
              application, "${application.packageName}.fileprovider", File(file.localPath))
        }
    return FileItem(file = file, displayName = displayName, isImage = isImage, uri = uri)
  }

  fun deleteFile(fileItem: FileItem, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val file = File(fileItem.file.localPath)
      val fileDeleted =
          if (file.exists()) {
            file.delete()
          } else {
            true // If file doesn't exist, consider it "deleted"
          }
      dao.delete(fileItem.file)
      onComplete(fileDeleted)
    }
  }

  fun getOpenFileIntent(fileItem: FileItem): Intent {
    val extension = fileItem.displayName.substringAfterLast('.', "").lowercase()
    val mimeType = mimeTypeResolver(extension) ?: "*/*"

    return Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(fileItem.uri, mimeType)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}

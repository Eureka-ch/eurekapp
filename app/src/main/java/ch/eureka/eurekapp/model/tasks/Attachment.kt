// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.data.file.StorageHelpers

sealed interface Attachment {
  data class Remote(val url: String, val name: String, val mimeType: String) : Attachment

  data class Local(val uri: Uri) : Attachment

  companion object {
    fun parseAttachment(metadata: String): Remote {
      val parts = metadata.split("|")
      return if (parts.size == 3) {
        Remote(parts[0], parts[1], parts[2])
      } else {
        // Fallback for old format (just URL) - try to infer MIME type from URL
        val fileName = metadata.substringAfterLast("/").substringBefore("?")
        val mimeType = StorageHelpers.getContentTypeFromPath(fileName)
        Remote(metadata, fileName, mimeType)
      }
    }
  }
}

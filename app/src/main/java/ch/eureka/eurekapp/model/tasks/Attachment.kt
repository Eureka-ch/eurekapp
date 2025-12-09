// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.tasks

import android.net.Uri

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
        val mimeType = getMimeTypeFromUrl(metadata) ?: "application/octet-stream"
        val fileName = metadata.substringAfterLast("/").substringBefore("?")
        Remote(metadata, fileName, mimeType)
      }
    }

    fun getMimeTypeFromUrl(url: String): String? {
      val cleanUrl = url.substringBefore("?")
      return when {
        cleanUrl.endsWith(".jpg", ignoreCase = true) ||
            cleanUrl.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        cleanUrl.endsWith(".png", ignoreCase = true) -> "image/png"
        cleanUrl.endsWith(".gif", ignoreCase = true) -> "image/gif"
        cleanUrl.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
        cleanUrl.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        cleanUrl.endsWith(".txt", ignoreCase = true) -> "text/plain"
        else -> null
      }
    }
  }
}

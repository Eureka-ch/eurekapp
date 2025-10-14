/*
 * This file was co-authored by Claude Code (Anthropic AI assistant).
 */
package ch.eureka.eurekapp.model.data.file

/**
 * Helper functions for Firebase Storage operations.
 *
 * This object provides utility functions for storage-related tasks such as MIME type detection.
 */
object StorageHelpers {

  /**
   * Determine the MIME content type from a file path based on its extension.
   *
   * Supports a wide range of common file types including:
   * - Text files (txt, html, css, js, json, xml)
   * - Images (jpg, png, gif, svg, webp, bmp, ico)
   * - Documents (pdf, doc, docx, xls, xlsx, ppt, pptx)
   * - Archives (zip, tar, gz, 7z)
   * - Media files (mp3, wav, mp4, avi)
   *
   * @param path The file path (e.g., "projects/proj1/file.txt")
   * @return The MIME type (e.g., "text/plain"), defaults to "application/octet-stream" for unknown
   *   types
   */
  fun getContentTypeFromPath(path: String): String {
    val extension = path.substringAfterLast('.', "").lowercase()
    return when (extension) {
      "txt" -> "text/plain"
      "html",
      "htm" -> "text/html"
      "css" -> "text/css"
      "js" -> "application/javascript"
      "json" -> "application/json"
      "xml" -> "application/xml"
      "jpg",
      "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      "svg" -> "image/svg+xml"
      "webp" -> "image/webp"
      "bmp" -> "image/bmp"
      "ico" -> "image/x-icon"
      "pdf" -> "application/pdf"
      "doc" -> "application/msword"
      "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      "xls" -> "application/vnd.ms-excel"
      "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      "ppt" -> "application/vnd.ms-powerpoint"
      "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
      "zip" -> "application/zip"
      "tar" -> "application/x-tar"
      "gz" -> "application/gzip"
      "7z" -> "application/x-7z-compressed"
      "mp3" -> "audio/mpeg"
      "wav" -> "audio/wav"
      "mp4" -> "video/mp4"
      "avi" -> "video/x-msvideo"
      else -> "application/octet-stream"
    }
  }
}

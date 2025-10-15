/*
 * This test file was co-authored by Claude Code (Anthropic AI assistant).
 */
package ch.eureka.eurekapp.model.data.file

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Comprehensive test suite for StorageHelpers MIME type detection.
 *
 * Tests all supported file extensions to ensure correct MIME type mapping. This achieves 100% code
 * coverage for the getContentTypeFromPath function.
 */
class StorageHelpersTest {

  @Test
  fun getContentTypeFromPath_textPlain() {
    assertEquals("text/plain", StorageHelpers.getContentTypeFromPath("file.txt"))
    assertEquals("text/plain", StorageHelpers.getContentTypeFromPath("path/to/document.txt"))
    assertEquals("text/plain", StorageHelpers.getContentTypeFromPath("file.TXT"))
  }

  @Test
  fun getContentTypeFromPath_textHtml() {
    assertEquals("text/html", StorageHelpers.getContentTypeFromPath("file.html"))
    assertEquals("text/html", StorageHelpers.getContentTypeFromPath("file.htm"))
    assertEquals("text/html", StorageHelpers.getContentTypeFromPath("path/to/page.HTML"))
  }

  @Test
  fun getContentTypeFromPath_textCss() {
    assertEquals("text/css", StorageHelpers.getContentTypeFromPath("style.css"))
    assertEquals("text/css", StorageHelpers.getContentTypeFromPath("path/styles.CSS"))
  }

  @Test
  fun getContentTypeFromPath_applicationJavaScript() {
    assertEquals("application/javascript", StorageHelpers.getContentTypeFromPath("script.js"))
    assertEquals("application/javascript", StorageHelpers.getContentTypeFromPath("app.JS"))
  }

  @Test
  fun getContentTypeFromPath_applicationJson() {
    assertEquals("application/json", StorageHelpers.getContentTypeFromPath("data.json"))
    assertEquals("application/json", StorageHelpers.getContentTypeFromPath("config.JSON"))
  }

  @Test
  fun getContentTypeFromPath_applicationXml() {
    assertEquals("application/xml", StorageHelpers.getContentTypeFromPath("data.xml"))
    assertEquals("application/xml", StorageHelpers.getContentTypeFromPath("file.XML"))
  }

  @Test
  fun getContentTypeFromPath_imageJpeg() {
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("photo.jpg"))
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("image.jpeg"))
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("pic.JPG"))
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("pic.JPEG"))
  }

  @Test
  fun getContentTypeFromPath_imagePng() {
    assertEquals("image/png", StorageHelpers.getContentTypeFromPath("icon.png"))
    assertEquals("image/png", StorageHelpers.getContentTypeFromPath("screenshot.PNG"))
  }

  @Test
  fun getContentTypeFromPath_imageGif() {
    assertEquals("image/gif", StorageHelpers.getContentTypeFromPath("animation.gif"))
    assertEquals("image/gif", StorageHelpers.getContentTypeFromPath("emoji.GIF"))
  }

  @Test
  fun getContentTypeFromPath_imageSvg() {
    assertEquals("image/svg+xml", StorageHelpers.getContentTypeFromPath("logo.svg"))
    assertEquals("image/svg+xml", StorageHelpers.getContentTypeFromPath("icon.SVG"))
  }

  @Test
  fun getContentTypeFromPath_imageWebp() {
    assertEquals("image/webp", StorageHelpers.getContentTypeFromPath("image.webp"))
    assertEquals("image/webp", StorageHelpers.getContentTypeFromPath("photo.WEBP"))
  }

  @Test
  fun getContentTypeFromPath_imageBmp() {
    assertEquals("image/bmp", StorageHelpers.getContentTypeFromPath("bitmap.bmp"))
    assertEquals("image/bmp", StorageHelpers.getContentTypeFromPath("image.BMP"))
  }

  @Test
  fun getContentTypeFromPath_imageIcon() {
    assertEquals("image/x-icon", StorageHelpers.getContentTypeFromPath("favicon.ico"))
    assertEquals("image/x-icon", StorageHelpers.getContentTypeFromPath("icon.ICO"))
  }

  @Test
  fun getContentTypeFromPath_applicationPdf() {
    assertEquals("application/pdf", StorageHelpers.getContentTypeFromPath("document.pdf"))
    assertEquals("application/pdf", StorageHelpers.getContentTypeFromPath("report.PDF"))
  }

  @Test
  fun getContentTypeFromPath_applicationMsword() {
    assertEquals("application/msword", StorageHelpers.getContentTypeFromPath("document.doc"))
    assertEquals("application/msword", StorageHelpers.getContentTypeFromPath("file.DOC"))
  }

  @Test
  fun getContentTypeFromPath_applicationDocx() {
    assertEquals(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        StorageHelpers.getContentTypeFromPath("document.docx"))
    assertEquals(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        StorageHelpers.getContentTypeFromPath("file.DOCX"))
  }

  @Test
  fun getContentTypeFromPath_applicationExcel() {
    assertEquals(
        "application/vnd.ms-excel", StorageHelpers.getContentTypeFromPath("spreadsheet.xls"))
    assertEquals("application/vnd.ms-excel", StorageHelpers.getContentTypeFromPath("data.XLS"))
  }

  @Test
  fun getContentTypeFromPath_applicationXlsx() {
    assertEquals(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        StorageHelpers.getContentTypeFromPath("spreadsheet.xlsx"))
    assertEquals(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        StorageHelpers.getContentTypeFromPath("data.XLSX"))
  }

  @Test
  fun getContentTypeFromPath_applicationPowerpoint() {
    assertEquals(
        "application/vnd.ms-powerpoint", StorageHelpers.getContentTypeFromPath("presentation.ppt"))
    assertEquals(
        "application/vnd.ms-powerpoint", StorageHelpers.getContentTypeFromPath("slides.PPT"))
  }

  @Test
  fun getContentTypeFromPath_applicationPptx() {
    assertEquals(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        StorageHelpers.getContentTypeFromPath("presentation.pptx"))
    assertEquals(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        StorageHelpers.getContentTypeFromPath("slides.PPTX"))
  }

  @Test
  fun getContentTypeFromPath_applicationZip() {
    assertEquals("application/zip", StorageHelpers.getContentTypeFromPath("archive.zip"))
    assertEquals("application/zip", StorageHelpers.getContentTypeFromPath("files.ZIP"))
  }

  @Test
  fun getContentTypeFromPath_applicationTar() {
    assertEquals("application/x-tar", StorageHelpers.getContentTypeFromPath("archive.tar"))
    assertEquals("application/x-tar", StorageHelpers.getContentTypeFromPath("backup.TAR"))
  }

  @Test
  fun getContentTypeFromPath_applicationGzip() {
    assertEquals("application/gzip", StorageHelpers.getContentTypeFromPath("archive.gz"))
    assertEquals("application/gzip", StorageHelpers.getContentTypeFromPath("compressed.GZ"))
  }

  @Test
  fun getContentTypeFromPath_application7z() {
    assertEquals("application/x-7z-compressed", StorageHelpers.getContentTypeFromPath("archive.7z"))
    assertEquals("application/x-7z-compressed", StorageHelpers.getContentTypeFromPath("files.7Z"))
  }

  @Test
  fun getContentTypeFromPath_audioMpeg() {
    assertEquals("audio/mpeg", StorageHelpers.getContentTypeFromPath("song.mp3"))
    assertEquals("audio/mpeg", StorageHelpers.getContentTypeFromPath("audio.MP3"))
  }

  @Test
  fun getContentTypeFromPath_audioWav() {
    assertEquals("audio/wav", StorageHelpers.getContentTypeFromPath("sound.wav"))
    assertEquals("audio/wav", StorageHelpers.getContentTypeFromPath("recording.WAV"))
  }

  @Test
  fun getContentTypeFromPath_videoMp4() {
    assertEquals("video/mp4", StorageHelpers.getContentTypeFromPath("movie.mp4"))
    assertEquals("video/mp4", StorageHelpers.getContentTypeFromPath("video.MP4"))
  }

  @Test
  fun getContentTypeFromPath_videoAvi() {
    assertEquals("video/x-msvideo", StorageHelpers.getContentTypeFromPath("movie.avi"))
    assertEquals("video/x-msvideo", StorageHelpers.getContentTypeFromPath("video.AVI"))
  }

  @Test
  fun getContentTypeFromPath_applicationOctetStream_unknownExtension() {
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("file.unknown"))
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("file.xyz"))
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("file.RANDOM"))
  }

  @Test
  fun getContentTypeFromPath_applicationOctetStream_noExtension() {
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("file"))
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("README"))
    assertEquals("application/octet-stream", StorageHelpers.getContentTypeFromPath("path/to/noext"))
  }

  @Test
  fun getContentTypeFromPath_handlesComplexPaths() {
    assertEquals(
        "application/pdf",
        StorageHelpers.getContentTypeFromPath("projects/proj-123/tasks/task-456/document.pdf"))
    assertEquals(
        "image/png",
        StorageHelpers.getContentTypeFromPath(
            "users/user-789/profile/pictures/avatar.v2.final.png"))
    assertEquals(
        "text/plain",
        StorageHelpers.getContentTypeFromPath("meetings/meeting-001/notes.backup.txt"))
  }

  @Test
  fun getContentTypeFromPath_handlesMultipleDots() {
    assertEquals("application/pdf", StorageHelpers.getContentTypeFromPath("file.backup.old.pdf"))
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("photo.2024.01.15.jpg"))
    assertEquals("text/plain", StorageHelpers.getContentTypeFromPath("readme.v2.3.txt"))
  }

  @Test
  fun getContentTypeFromPath_caseInsensitive() {
    assertEquals("text/plain", StorageHelpers.getContentTypeFromPath("file.TxT"))
    assertEquals("image/jpeg", StorageHelpers.getContentTypeFromPath("photo.JpG"))
    assertEquals("application/pdf", StorageHelpers.getContentTypeFromPath("doc.PdF"))
    assertEquals("video/mp4", StorageHelpers.getContentTypeFromPath("video.Mp4"))
  }
}

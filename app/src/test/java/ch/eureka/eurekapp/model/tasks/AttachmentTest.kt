// Portions of this code were generated with the help of Grok.
// This code was written with help of Claude.
package ch.eureka.eurekapp.model.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AttachmentTest {

  @Test
  fun parseAttachment_returnsRemoteAttachmentWithFullMetadata() {
    val metadata = "https://example.com/file.pdf|Document.pdf|application/pdf"

    val result = Attachment.parseAttachment(metadata)

    assertEquals("https://example.com/file.pdf", result.url)
    assertEquals("Document.pdf", result.name)
    assertEquals("application/pdf", result.mimeType)
  }

  @Test
  fun parseAttachment_infersMetadataWithUrlOnly() {
    val metadata = "https://example.com/image.jpg"

    val result = Attachment.parseAttachment(metadata)

    assertEquals("https://example.com/image.jpg", result.url)
    assertEquals("image.jpg", result.name)
    assertEquals("image/jpeg", result.mimeType)
  }

  @Test
  fun parseAttachment_extractsFileNameWithUrlAndQueryParams() {
    val metadata = "https://example.com/video.mp4?token=abc123"

    val result = Attachment.parseAttachment(metadata)

    assertEquals("https://example.com/video.mp4?token=abc123", result.url)
    assertEquals("video.mp4", result.name)
    assertEquals("video/mp4", result.mimeType)
  }

  @Test
  fun parseAttachment_usesFallbackMimeTypeWithUnknownExtension() {
    val metadata = "https://example.com/file.xyz"

    val result = Attachment.parseAttachment(metadata)

    assertEquals("https://example.com/file.xyz", result.url)
    assertEquals("file.xyz", result.name)
    assertEquals("application/octet-stream", result.mimeType)
  }
}

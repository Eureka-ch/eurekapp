// Portions of this code were generated with the help of Grok.
// This code was written with help of Claude.
package ch.eureka.eurekapp.screens.subscreens.tasks

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.tasks.Attachment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommonComponentsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun attachmentsList_displaysImageAttachment() {
    val imageUri = Uri.parse("content://test/image.jpg")
    val attachments = listOf(Attachment.Local(imageUri))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments, onDelete = {}, isReadOnly = false, isConnected = true)
    }

    // Use testTag for PhotoViewer component
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ATTACHMENT).assertExists()
  }

  @Test
  fun attachmentsList_displaysVideoAttachment() {
    val videoUrl = "https://example.com/video.mp4"
    val attachments = listOf(Attachment.Remote(videoUrl, "video.mp4", "video/mp4"))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments, onDelete = {}, isReadOnly = false, isConnected = true)
    }

    composeTestRule.onNodeWithContentDescription("Video file").assertIsDisplayed()
  }

  @Test
  fun attachmentsList_displaysPdfAttachment() {
    val pdfUrl = "https://example.com/document.pdf"
    val attachments = listOf(Attachment.Remote(pdfUrl, "document.pdf", "application/pdf"))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments, onDelete = {}, isReadOnly = false, isConnected = true)
    }

    composeTestRule.onNodeWithContentDescription("PDF file").assertIsDisplayed()
  }

  @Test
  fun attachmentsList_displaysGenericDocumentAttachment() {
    val docUrl = "https://example.com/document.txt"
    val attachments = listOf(Attachment.Remote(docUrl, "document.txt", "text/plain"))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments, onDelete = {}, isReadOnly = false, isConnected = true)
    }

    composeTestRule.onNodeWithContentDescription("Document file").assertIsDisplayed()
  }

  @Test
  fun attachmentsList_displaysOfflineMessageForUndownloadedAttachment() {
    val remoteUrl = "https://example.com/document.pdf"
    val attachments = listOf(Attachment.Remote(remoteUrl, "document.pdf", "application/pdf"))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments,
          onDelete = {},
          isReadOnly = false,
          isConnected = false, // Offline
          downloadedUrls = emptySet()) // Not downloaded
    }

    // Use testTag for offline message text
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ATTACHMENT_OFFLINE_MESSAGE).assertExists()
  }

  @Test
  fun attachmentsList_displaysMultipleAttachments() {
    val imageUrl = "https://example.com/image.jpg"
    val videoUrl = "https://example.com/video.mp4"
    val pdfUrl = "https://example.com/document.pdf"

    val attachments =
        listOf(
            Attachment.Remote(imageUrl, "image.jpg", "image/jpeg"),
            Attachment.Remote(videoUrl, "video.mp4", "video/mp4"),
            Attachment.Remote(pdfUrl, "document.pdf", "application/pdf"))

    composeTestRule.setContent {
      AttachmentsList(
          attachments = attachments, onDelete = {}, isReadOnly = false, isConnected = true)
    }

    // Verify all three attachment types are displayed
    composeTestRule.onAllNodesWithTag(CommonTaskTestTags.ATTACHMENT)[0].assertExists()
    composeTestRule.onAllNodesWithTag(CommonTaskTestTags.ATTACHMENT)[1].assertExists()
    composeTestRule.onAllNodesWithTag(CommonTaskTestTags.ATTACHMENT)[2].assertExists()
  }
}

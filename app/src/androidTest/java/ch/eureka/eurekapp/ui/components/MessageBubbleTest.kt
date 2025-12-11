package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

@RunWith(AndroidJUnit4::class)
class MessageBubbleTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun messageBubble_displaysMessageText() {
    composeTestRule.setContent {
      MessageBubble(text = "Hello, world!", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.TEXT).assertTextEquals("Hello, world!")
  }

  @Test
  fun messageBubble_displaysTimestamp() {
    composeTestRule.setContent {
      MessageBubble(text = "Test message", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.TIMESTAMP).assertIsDisplayed()
  }

  @Test
  fun messageBubble_sentMessageIsDisplayed() {
    composeTestRule.setContent {
      MessageBubble(text = "Sent", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()
  }

  @Test
  fun messageBubble_receivedMessageIsDisplayed() {
    composeTestRule.setContent {
      MessageBubble(text = "Received", timestamp = Timestamp.now(), isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()
  }

  @Test
  fun messageBubble_fileMessageShowsDownloadButton() {
    composeTestRule.setContent {
      MessageBubble(
          text = "File message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true,
          fileAttachment =
              MessageBubbleFileAttachment(isFile = true, fileUrl = "https://example.com/file.pdf"))
    }
    composeTestRule.onNodeWithContentDescription("Download file").assertIsDisplayed()
  }

  @Test
  fun messageBubble_imageFileShowsPreview() {
    composeTestRule.setContent {
      MessageBubble(
          text = "Image message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true,
          fileAttachment =
              MessageBubbleFileAttachment(isFile = true, fileUrl = "https://example.com/image.jpg"))
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.PHOTO_VIEWER).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Download file").assertIsDisplayed()
  }

  @Test
  fun messageBubble_showsEditedIndicatorWhenEditedAtIsSet() {
    composeTestRule.setContent {
      MessageBubble(
          text = "Edited message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true,
          editedAt = Timestamp.now())
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.EDITED_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun messageBubble_doesNotShowEditedIndicatorWhenEditedAtIsNull() {
    composeTestRule.setContent {
      MessageBubble(text = "Regular message", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.EDITED_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun messageBubble_withSenderPhotoUrlDisplaysAsyncImage() {
    composeTestRule.setContent {
      MessageBubble(
          senderPhotoUrl = "https://example.com/photo.jpg",
          text = "Test",
          timestamp = Timestamp.now(),
          isFromCurrentUser = false)
    }
    composeTestRule
        .onNodeWithContentDescription("Profile picture of https://example.com/photo.jpg")
        .assertExists()
  }

  @Test
  fun messageBubble_withEmptySenderPhotoUrlDoesNotDisplayAsyncImage() {
    composeTestRule.setContent {
      MessageBubble(text = "Test", timestamp = Timestamp.now(), isFromCurrentUser = false)
    }
    composeTestRule
        .onNodeWithContentDescription("Profile picture of ", substring = true)
        .assertDoesNotExist()
  }

  @Test
  fun messageBubble_withSenderDisplayNameDisplaysText() {
    composeTestRule.setContent {
      MessageBubble(
          senderDisplayName = "John Doe",
          text = "Test",
          timestamp = Timestamp.now(),
          isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithText("John Doe").assertExists()
  }

  @Test
  fun messageBubble_fromCurrentUserRendersInCorrectOrder() {
    composeTestRule.setContent {
      MessageBubble(
          senderPhotoUrl = "https://example.com/photo.jpg",
          senderDisplayName = "Me",
          text = "My message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithText("Me").assertExists()
    composeTestRule.onNodeWithText("My message").assertExists()
  }

  @Test
  fun messageBubble_notFromCurrentUserRendersInCorrectOrder() {
    composeTestRule.setContent {
      MessageBubble(
          senderPhotoUrl = "https://example.com/photo.jpg",
          senderDisplayName = "Other",
          text = "Their message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithText("Other").assertExists()
    composeTestRule.onNodeWithText("Their message").assertExists()
  }

  @Test
  fun messageBubble_withBothPhotoAndDisplayNameRendersBoth() {
    composeTestRule.setContent {
      MessageBubble(
          senderPhotoUrl = "https://example.com/photo.jpg",
          senderDisplayName = "Complete User",
          text = "Message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithText("Complete User").assertExists()
    composeTestRule
        .onNodeWithContentDescription("Profile picture of https://example.com/photo.jpg")
        .assertExists()
  }
}

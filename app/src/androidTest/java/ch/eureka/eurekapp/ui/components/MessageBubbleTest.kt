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
  fun messageBubble_sentMessage_isDisplayed() {
    composeTestRule.setContent {
      MessageBubble(text = "Sent", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()
  }

  @Test
  fun messageBubble_receivedMessage_isDisplayed() {
    composeTestRule.setContent {
      MessageBubble(text = "Received", timestamp = Timestamp.now(), isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()
  }

  @Test
  fun messageBubble_fileMessage_showsDownloadButton() {
    composeTestRule.setContent {
      MessageBubble(
          text = "File message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true,
          isFile = true,
          fileUrl = "https://example.com/file.pdf")
    }
    composeTestRule.onNodeWithContentDescription("Download file").assertIsDisplayed()
  }

  @Test
  fun messageBubble_imageFile_showsPreview() {
    composeTestRule.setContent {
      MessageBubble(
          text = "Image message",
          timestamp = Timestamp.now(),
          isFromCurrentUser = true,
          isFile = true,
          fileUrl = "https://example.com/image.jpg")
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.PHOTO_VIEWER).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Download file").assertIsDisplayed()
  }
}

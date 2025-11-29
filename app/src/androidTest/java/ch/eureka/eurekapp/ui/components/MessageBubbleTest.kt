package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
  fun messageBubble_sentAndReceivedMessages_areDisplayed() {
    composeTestRule.setContent {
      MessageBubble(text = "Sent", timestamp = Timestamp.now(), isFromCurrentUser = true)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()

    composeTestRule.setContent {
      MessageBubble(text = "Received", timestamp = Timestamp.now(), isFromCurrentUser = false)
    }
    composeTestRule.onNodeWithTag(MessageBubbleTestTags.BUBBLE).assertIsDisplayed()
  }
}

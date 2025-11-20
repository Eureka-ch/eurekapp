package ch.eureka.eurekapp.ui.notes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.chat.Message
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
*/

@RunWith(AndroidJUnit4::class)
class SelfNoteMessageBubbleTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testUserId = "test-user-id"

  private fun createTestMessage(
      text: String = "Test note content",
      timestamp: Timestamp = Timestamp.now()
  ) =
      Message(
          messageID = "msg-1",
          text = text,
          senderId = testUserId,
          createdAt = timestamp,
          references = emptyList())

  @Test
  fun selfNoteMessageBubble_displaysMessageText() {
    val message = createTestMessage(text = "Hello, this is a test note")

    composeTestRule.setContent { SelfNoteMessageBubble(message = message) }

    composeTestRule
        .onNodeWithTag(SelfNoteMessageBubbleTestTags.MESSAGE_TEXT)
        .assertTextEquals("Hello, this is a test note")
  }

  @Test
  fun selfNoteMessageBubble_displaysTimestamp() {
    val message = createTestMessage()

    composeTestRule.setContent { SelfNoteMessageBubble(message = message) }

    composeTestRule
        .onNodeWithTag(SelfNoteMessageBubbleTestTags.MESSAGE_TIMESTAMP)
        .assertIsDisplayed()
  }

  @Test
  fun selfNoteMessageBubble_handlesMultilineText() {
    val multilineText = "Line 1\nLine 2\nLine 3"
    val message = createTestMessage(text = multilineText)

    composeTestRule.setContent { SelfNoteMessageBubble(message = message) }

    composeTestRule
        .onNodeWithTag(SelfNoteMessageBubbleTestTags.MESSAGE_TEXT)
        .assertTextEquals(multilineText)
  }
}

/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.ui.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.chat.Message
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    composeTestRule.setContent { SelfNoteMessageBubble(message = message, isSelected = false) }

    composeTestRule.onNodeWithText("Hello, this is a test note").assertIsDisplayed()
  }

  @Test
  fun selfNoteMessageBubble_handlesMultilineText() {
    val multilineText = "Line 1\nLine 2\nLine 3"
    val message = createTestMessage(text = multilineText)

    composeTestRule.setContent { SelfNoteMessageBubble(message = message, isSelected = false) }

    composeTestRule.onNodeWithText(multilineText).assertIsDisplayed()
  }

  @Test
  fun selfNoteMessageBubble_handlesClick() {
    val message = createTestMessage(text = "Click me")
    var isClicked = false

    composeTestRule.setContent {
      SelfNoteMessageBubble(message = message, isSelected = false, onClick = { isClicked = true })
    }

    composeTestRule.onNodeWithText("Click me").performClick()

    assertTrue("onClick lambda should be triggered", isClicked)
  }

  @Test
  fun selfNoteMessageBubble_handlesLongClick() {
    val message = createTestMessage(text = "Long press me")
    var isLongClicked = false

    composeTestRule.setContent {
      SelfNoteMessageBubble(
          message = message, isSelected = false, onLongClick = { isLongClicked = true })
    }

    composeTestRule.onNodeWithText("Long press me").performTouchInput { longClick() }

    assertTrue("onLongClick lambda should be triggered", isLongClicked)
  }

  @Test
  fun selfNoteMessageBubble_rendersCorrectlyWhenSelected() {
    val message = createTestMessage(text = "Selected Note")

    composeTestRule.setContent { SelfNoteMessageBubble(message = message, isSelected = true) }
    composeTestRule.onNodeWithText("Selected Note").assertIsDisplayed()
  }
}

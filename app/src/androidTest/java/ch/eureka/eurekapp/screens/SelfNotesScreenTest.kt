package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.MessageInputField
import ch.eureka.eurekapp.ui.notes.SelfNoteMessageBubble
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
*/

@RunWith(AndroidJUnit4::class)
class SelfNotesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testUserId = "test-user-id"

  private val testMessage =
      Message(
          messageID = "msg-1",
          text = "Test note content",
          senderId = testUserId,
          createdAt = Timestamp.now(),
          references = emptyList())

  @Test
  fun messageInputFieldDisplaysInSelfNotesContext() {
    composeTestRule.setContent {
      MessageInputField(
          message = "",
          onMessageChange = {},
          onSend = {},
          isSending = false,
          placeholder = "Write a note to yourself...")
    }

    composeTestRule.onNodeWithText("Write a note to yourself...").assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.NOTE_INPUT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.SEND_BUTTON).assertIsDisplayed()
  }

  @Test
  fun selfNoteMessageBubbleDisplaysCorrectly() {
    composeTestRule.setContent { SelfNoteMessageBubble(message = testMessage) }

    composeTestRule.onNodeWithText("Test note content").assertIsDisplayed()
  }

  @Test
  fun sendButtonDisabledWhenEmpty() {
    composeTestRule.setContent {
      MessageInputField(
          message = "",
          onMessageChange = {},
          onSend = {},
          isSending = false,
          placeholder = "Write a note to yourself...")
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.SEND_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun sendButtonEnabledWithText() {
    composeTestRule.setContent {
      MessageInputField(
          message = "Test note",
          onMessageChange = {},
          onSend = {},
          isSending = false,
          placeholder = "Write a note to yourself...")
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.SEND_BUTTON).assertIsEnabled()
  }
}

package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.components.MessageInputFieldTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
*/

@RunWith(AndroidJUnit4::class)
class MessageInputFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun messageInputFieldDisplaysCustomPlaceholder() {
    composeTestRule.setContent {
      MessageInputField(
          message = "",
          onMessageChange = {},
          onSend = {},
          isSending = false,
          placeholder = "Custom placeholder")
    }

    composeTestRule.onNodeWithText("Custom placeholder").assertIsDisplayed()
  }

  @Test
  fun messageInputFieldSendButtonDisabledWhenMessageEmpty() {
    composeTestRule.setContent {
      MessageInputField(message = "", onMessageChange = {}, onSend = {}, isSending = false)
    }

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun messageInputFieldSendButtonEnabledWhenMessageNotEmpty() {
    composeTestRule.setContent {
      MessageInputField(message = "Hello", onMessageChange = {}, onSend = {}, isSending = false)
    }

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsEnabled()
  }

  @Test
  fun messageInputFieldCallsOnMessageChangeWhenTextEntered() {
    var changedText = ""

    composeTestRule.setContent {
      MessageInputField(
          message = "", onMessageChange = { changedText = it }, onSend = {}, isSending = false)
    }
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).performTextInput("Hello")

    assert(changedText == "Hello")
  }

  @Test
  fun messageInputFieldCallsOnSendWhenSendButtonClicked() {
    var sendCalled = false

    composeTestRule.setContent {
      MessageInputField(
          message = "Hello",
          onMessageChange = {},
          onSend = { sendCalled = true },
          isSending = false)
    }
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).performClick()

    assert(sendCalled)
  }

  @Test
  fun messageInputFieldStateChangesWorkCorrectly() {
    var message by mutableStateOf("")
    var sendCalled = false

    composeTestRule.setContent {
      MessageInputField(
          message = message,
          onMessageChange = { message = it },
          onSend = { sendCalled = true },
          isSending = false)
    }

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsNotEnabled()

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).performTextInput("Test")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsEnabled()

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).performClick()

    assert(sendCalled)
  }
}

/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.components.MessageInputFieldTestTags
import ch.eureka.eurekapp.ui.notes.SelfNoteMessageBubble
import ch.eureka.eurekapp.ui.notes.SelfNotesScreen
import ch.eureka.eurekapp.ui.notes.SelfNotesScreenTestTags
import ch.eureka.eurekapp.ui.notes.SelfNotesUIState
import ch.eureka.eurekapp.ui.notes.SelfNotesViewModel
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsDisplayed()
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

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsNotEnabled()
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

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).assertIsEnabled()
  }

  @Test
  fun loadingState_displaysProgressIndicator() {
    val mockViewModel = mockk<SelfNotesViewModel>(relaxed = true)
    val uiState = MutableStateFlow(SelfNotesUIState(isLoading = true))
    every { mockViewModel.uiState } returns uiState
    composeTestRule.setContent { SelfNotesScreen(viewModel = mockViewModel) }
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.NOTES_LIST).assertDoesNotExist()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.EMPTY_STATE).assertDoesNotExist()
  }

  @Test
  fun emptyState_displaysPlaceholder() {
    val mockViewModel = mockk<SelfNotesViewModel>(relaxed = true)
    val uiState = MutableStateFlow(SelfNotesUIState(isLoading = false, notes = emptyList()))
    every { mockViewModel.uiState } returns uiState
    composeTestRule.setContent { SelfNotesScreen(viewModel = mockViewModel) }
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes yet. Start writing!").assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun notesState_displaysList() {
    val mockViewModel = mockk<SelfNotesViewModel>(relaxed = true)
    val notesList = listOf(testMessage)
    val uiState = MutableStateFlow(SelfNotesUIState(isLoading = false, notes = notesList))
    every { mockViewModel.uiState } returns uiState
    composeTestRule.setContent { SelfNotesScreen(viewModel = mockViewModel) }
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.NOTES_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test note content").assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.EMPTY_STATE).assertDoesNotExist()
  }

  @Test
  fun storageToggle_displaysAndWorks() {
    val mockViewModel = mockk<SelfNotesViewModel>(relaxed = true)
    val uiStateFlow =
        MutableStateFlow(SelfNotesUIState(isLoading = false, isCloudStorageEnabled = false))
    every { mockViewModel.uiState } returns uiStateFlow
    composeTestRule.setContent { SelfNotesScreen(viewModel = mockViewModel) }
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).assertIsOff()
    composeTestRule.onNodeWithText("Local").assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).performClick()
    verify { mockViewModel.toggleStorageMode(true) }
    uiStateFlow.value = SelfNotesUIState(isLoading = false, isCloudStorageEnabled = true)
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).assertIsOn()
    composeTestRule.onNodeWithText("Cloud").assertIsDisplayed()
  }
}

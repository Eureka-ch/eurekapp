/* Portions of this file were written with the help of Gemini and Claude. */
package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.components.MessageInputFieldTestTags
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

/**
 * UI Test suite for [ch.eureka.eurekapp.ui.notes.SelfNotesScreen]. Covers:
 * - Loading / Empty / Content states
 * - Input field interactions
 * - Storage mode toggle
 * - Multi-selection mode (Contextual Action Bar)
 * - Bulk deletion and Editing
 */
@RunWith(AndroidJUnit4::class)
class SelfNotesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testUserId = "test-user-id"

  private fun createMessage(id: String, text: String) =
      Message(
          messageID = id,
          text = text,
          senderId = testUserId,
          createdAt = Timestamp.now(),
          references = emptyList())

  private val mockViewModel = mockk<SelfNotesViewModel>(relaxed = true)
  private val uiStateFlow = MutableStateFlow(SelfNotesUIState())

  private fun setupScreen() {
    every { mockViewModel.uiState } returns uiStateFlow
    composeTestRule.setContent { SelfNotesScreen(viewModel = mockViewModel) }
  }

  @Test
  fun selfNotesScreen_loadingStateDisplaysProgressIndicator() {
    uiStateFlow.value = SelfNotesUIState(isLoading = true)
    setupScreen()

    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.NOTES_LIST).assertDoesNotExist()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.EMPTY_STATE).assertDoesNotExist()
  }

  @Test
  fun selfNotesScreen_emptyStateDisplaysPlaceholder() {
    uiStateFlow.value = SelfNotesUIState(isLoading = false, notes = emptyList())
    setupScreen()

    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes yet. Start writing!").assertIsDisplayed()
  }

  @Test
  fun selfNotesScreen_notesStateDisplaysList() {
    val notes = listOf(createMessage("1", "First Note"), createMessage("2", "Second Note"))
    uiStateFlow.value = SelfNotesUIState(isLoading = false, notes = notes)
    setupScreen()

    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.NOTES_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("First Note").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Note").assertIsDisplayed()
  }

  @Test
  fun selfNotesScreen_inputFieldHandlesTextEntryAndSending() {
    setupScreen()

    val inputText = "My new note"
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).performTextInput(inputText)

    verify { mockViewModel.updateMessage(inputText) }

    uiStateFlow.value = uiStateFlow.value.copy(currentMessage = inputText)

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).performClick()
    verify { mockViewModel.sendNote() }
  }

  @Test
  fun selfNotesScreen_storageToggleDisplaysAndWorks() {
    uiStateFlow.value = SelfNotesUIState(isCloudStorageEnabled = false)
    setupScreen()

    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).assertIsOff()
    composeTestRule.onNodeWithText("Local").assertIsDisplayed()

    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).performClick()
    verify { mockViewModel.toggleStorageMode(true) }

    uiStateFlow.value = SelfNotesUIState(isCloudStorageEnabled = true)
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.TOGGLE_SWITCH).assertIsOn()
    composeTestRule.onNodeWithText("Cloud").assertIsDisplayed()
  }

  @Test
  fun selfNotesScreen_longClickOnNoteTogglesSelection() {
    val notes = listOf(createMessage("1", "Long press me"))
    uiStateFlow.value = SelfNotesUIState(notes = notes)
    setupScreen()

    composeTestRule.onNodeWithText("Long press me").performTouchInput { longClick() }

    verify { mockViewModel.toggleSelection("1") }
  }

  @Test
  fun selfNotesScreen_selectionModeDisplaysContextualTopBarAndHidesInput() {
    val notes = listOf(createMessage("1", "Note 1"), createMessage("2", "Note 2"))
    uiStateFlow.value = SelfNotesUIState(notes = notes, selectedNoteIds = setOf("1", "2"))
    setupScreen()

    composeTestRule.onNodeWithText("2 Selected").assertIsDisplayed()

    composeTestRule.onNode(hasContentDescription("Cancel Selection")).assertIsDisplayed()
    composeTestRule.onNode(hasContentDescription("Delete Selected")).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).assertDoesNotExist()
  }

  @Test
  fun selfNotesScreen_selectionModeShowsEditButtonOnlyForSingleSelection() {
    val notes = listOf(createMessage("1", "Note 1"), createMessage("2", "Note 2"))

    uiStateFlow.value = SelfNotesUIState(notes = notes, selectedNoteIds = setOf("1"))
    setupScreen()
    composeTestRule.onNode(hasContentDescription("Edit Note")).assertIsDisplayed()

    uiStateFlow.value = SelfNotesUIState(notes = notes, selectedNoteIds = setOf("1", "2"))
    composeTestRule.onNode(hasContentDescription("Edit Note")).assertDoesNotExist()
  }

  @Test
  fun selfNotesScreen_selectionModeActionsTriggerViewModel() {
    val notes = listOf(createMessage("1", "Note 1"))
    uiStateFlow.value = SelfNotesUIState(notes = notes, selectedNoteIds = setOf("1"))
    setupScreen()

    composeTestRule.onNode(hasContentDescription("Edit Note")).performClick()
    verify { mockViewModel.startEditing(match { it.messageID == "1" }) }

    composeTestRule.onNode(hasContentDescription("Delete Selected")).performClick()
    verify { mockViewModel.deleteSelectedNotes() }

    composeTestRule.onNode(hasContentDescription("Cancel Selection")).performClick()
    verify { mockViewModel.clearSelection() }
  }

  @Test
  fun selfNotesScreen_editModeChangesUiState() {
    val notes = listOf(createMessage("1", "Note 1"))
    uiStateFlow.value =
        SelfNotesUIState(notes = notes, editingMessageId = "1", currentMessage = "Note 1 content")
    setupScreen()

    composeTestRule.onNodeWithText("Editing Note").assertIsDisplayed()

    composeTestRule.onNode(hasContentDescription("Cancel Edit")).assertIsDisplayed()

    composeTestRule.onNode(hasContentDescription("Cancel Edit")).performClick()
    verify { mockViewModel.cancelEditing() }

    uiStateFlow.value = uiStateFlow.value.copy(currentMessage = "")
    composeTestRule.onNodeWithText("Edit your note...").assertIsDisplayed()
  }
}

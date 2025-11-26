// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class TemplateBasicInfoSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun templateBasicInfoSection_displaysAllFields() {
    composeTestRule.setContent {
      TemplateBasicInfoSection(
          title = "",
          description = "",
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {})
    }

    composeTestRule.onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Template Title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Description (optional)").assertIsDisplayed()
  }

  @Test
  fun templateBasicInfoSection_displaysInitialValues() {
    composeTestRule.setContent {
      TemplateBasicInfoSection(
          title = "Test Template",
          description = "Test Description",
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {})
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .assertTextContains("Test Template")
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertTextContains("Test Description")
  }

  @Test
  fun templateBasicInfoSection_titleChangeCallbackInvoked() {
    var capturedTitle = ""

    composeTestRule.setContent {
      var title by remember { mutableStateOf("") }
      TemplateBasicInfoSection(
          title = title,
          description = "",
          titleError = null,
          onTitleChange = {
            title = it
            capturedTitle = it
          },
          onDescriptionChange = {})
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("New Title")

    assert(capturedTitle == "New Title") { "Expected 'New Title' but got '$capturedTitle'" }
  }

  @Test
  fun templateBasicInfoSection_descriptionChangeCallbackInvoked() {
    var capturedDescription = ""

    composeTestRule.setContent {
      var description by remember { mutableStateOf("") }
      TemplateBasicInfoSection(
          title = "",
          description = description,
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {
            description = it
            capturedDescription = it
          })
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextInput("New Description")

    assert(capturedDescription == "New Description") {
      "Expected 'New Description' but got '$capturedDescription'"
    }
  }

  @Test
  fun templateBasicInfoSection_displaysTitleError() {
    composeTestRule.setContent {
      TemplateBasicInfoSection(
          title = "",
          description = "",
          titleError = "Title is required",
          onTitleChange = {},
          onDescriptionChange = {})
    }

    composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
  }

  @Test
  fun templateBasicInfoSection_hidesErrorWhenNull() {
    composeTestRule.setContent {
      TemplateBasicInfoSection(
          title = "Valid Title",
          description = "",
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {})
    }

    composeTestRule.onNodeWithText("Title is required", substring = true).assertDoesNotExist()
  }

  @Test
  fun templateBasicInfoSection_titleUpdatesReactively() {
    composeTestRule.setContent {
      var title by remember { mutableStateOf("Initial") }
      TemplateBasicInfoSection(
          title = title,
          description = "",
          titleError = null,
          onTitleChange = { title = it },
          onDescriptionChange = {})
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .assertTextContains("Initial")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextReplacement("Updated")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .assertTextContains("Updated")
  }

  @Test
  fun templateBasicInfoSection_descriptionUpdatesReactively() {
    composeTestRule.setContent {
      var description by remember { mutableStateOf("Initial Description") }
      TemplateBasicInfoSection(
          title = "",
          description = description,
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = { description = it })
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertTextContains("Initial Description")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextReplacement("Updated Description")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertTextContains("Updated Description")
  }

  @Test
  fun templateBasicInfoSection_errorTogglesCorrectly() {
    composeTestRule.setContent {
      var title by remember { mutableStateOf("") }
      var titleError by remember { mutableStateOf<String?>(null) }
      TemplateBasicInfoSection(
          title = title,
          description = "",
          titleError = titleError,
          onTitleChange = {
            title = it
            titleError = if (it.isEmpty()) "Required" else null
          },
          onDescriptionChange = {})
    }

    // Initially no error shown
    composeTestRule.onNodeWithText("Required", substring = true).assertDoesNotExist()

    // Type some text - error should not appear
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performTextInput("Test")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Required", substring = true).assertDoesNotExist()

    // Clear the text - error should appear
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performTextReplacement("")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Required").assertIsDisplayed()
  }

  @Test
  fun templateBasicInfoSection_multipleInputsWorkIndependently() {
    var capturedTitle = ""
    var capturedDescription = ""

    composeTestRule.setContent {
      var title by remember { mutableStateOf("") }
      var description by remember { mutableStateOf("") }
      TemplateBasicInfoSection(
          title = title,
          description = description,
          titleError = null,
          onTitleChange = {
            title = it
            capturedTitle = it
          },
          onDescriptionChange = {
            description = it
            capturedDescription = it
          })
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Title Value")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextInput("Description Value")

    assert(capturedTitle == "Title Value") { "Expected 'Title Value' but got '$capturedTitle'" }
    assert(capturedDescription == "Description Value") {
      "Expected 'Description Value' but got '$capturedDescription'"
    }
  }

  @Test
  fun templateBasicInfoSection_emptyValuesAllowed() {
    composeTestRule.setContent {
      TemplateBasicInfoSection(
          title = "",
          description = "",
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {})
    }

    composeTestRule.onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()
  }

  @Test
  fun templateBasicInfoSection_longTextHandled() {
    val longText = "a".repeat(500)
    var capturedDescription = ""

    composeTestRule.setContent {
      var description by remember { mutableStateOf("") }
      TemplateBasicInfoSection(
          title = "",
          description = description,
          titleError = null,
          onTitleChange = {},
          onDescriptionChange = {
            description = it
            capturedDescription = it
          })
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextInput(longText)

    assert(capturedDescription == longText) {
      "Expected long text of length ${longText.length} but got length ${capturedDescription.length}"
    }
  }
}

package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for SelectOptionsEditor.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class SelectOptionsEditorTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))

  @Test
  fun selectOptionsEditor_displaysAllOptions() {
    composeTestRule.setContent {
      SelectOptionsEditor(options = testOptions, onOptionsChange = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("option_value_opt1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("option_label_opt1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("option_value_opt2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("option_label_opt2").assertIsDisplayed()
  }

  @Test
  fun selectOptionsEditor_addButton_addsOption() {
    var updatedOptions = testOptions
    composeTestRule.setContent {
      SelectOptionsEditor(
          options = updatedOptions, onOptionsChange = { updatedOptions = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("add_option_button").performClick()
    assertEquals(3, updatedOptions.size)
  }

  @Test
  fun selectOptionsEditor_deleteButton_removesOption() {
    val threeOptions = testOptions + SelectOption("opt3", "Option 3")
    var updatedOptions = threeOptions
    composeTestRule.setContent {
      SelectOptionsEditor(
          options = updatedOptions, onOptionsChange = { updatedOptions = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("option_delete_opt1").performClick()
    assertEquals(2, updatedOptions.size)
  }

  @Test
  fun selectOptionsEditor_deleteButton_disabledWhenTwoOptions() {
    composeTestRule.setContent {
      SelectOptionsEditor(options = testOptions, onOptionsChange = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("option_delete_opt1").assertIsNotEnabled()
  }

  @Test
  fun selectOptionsEditor_valueInput_updatesOption() {
    var updatedOptions = testOptions
    composeTestRule.setContent {
      SelectOptionsEditor(
          options = updatedOptions, onOptionsChange = { updatedOptions = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("option_value_opt1").performTextInput("x")
    assertEquals("xopt1", updatedOptions[0].value)
  }

  @Test
  fun selectOptionsEditor_labelInput_updatesOption() {
    var updatedOptions = testOptions
    composeTestRule.setContent {
      SelectOptionsEditor(
          options = updatedOptions, onOptionsChange = { updatedOptions = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("option_label_opt1").performTextInput("X")
    assertEquals("XOption 1", updatedOptions[0].label)
  }

  @Test
  fun selectOptionsEditor_disabled_disablesAllInputs() {
    composeTestRule.setContent {
      SelectOptionsEditor(options = testOptions, onOptionsChange = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("option_value_opt1").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("option_label_opt1").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("add_option_button").assertIsNotEnabled()
  }
}

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertTrue

/**
 * Test utilities for field configuration components. Provides reusable functions to reduce test
 * code duplication.
 */
object FieldConfigurationTestUtils {

  /**
   * Assert that multiple nodes with given test tags are displayed.
   *
   * @param composeTestRule the compose test rule
   * @param tags vararg of test tag strings to check
   */
  fun assertNodesDisplayed(composeTestRule: ComposeContentTestRule, vararg tags: String) {
    tags.forEach { tag -> composeTestRule.onNodeWithTag(tag).assertIsDisplayed() }
  }

  /**
   * Assert that multiple nodes with given test tags are not enabled.
   *
   * @param composeTestRule the compose test rule
   * @param tags vararg of test tag strings to check
   */
  fun assertNodesDisabled(composeTestRule: ComposeContentTestRule, vararg tags: String) {
    tags.forEach { tag -> composeTestRule.onNodeWithTag(tag).assertIsNotEnabled() }
  }

  /**
   * Assert that a text error message does not exist.
   *
   * @param composeTestRule the compose test rule
   * @param errorMessage the error message text to check
   */
  fun assertNoError(composeTestRule: ComposeContentTestRule, errorMessage: String) {
    composeTestRule.onNodeWithText(errorMessage).assertDoesNotExist()
  }

  /**
   * Assert that a text error message is displayed.
   *
   * @param composeTestRule the compose test rule
   * @param errorMessage the error message text to check
   */
  fun assertErrorDisplayed(composeTestRule: ComposeContentTestRule, errorMessage: String) {
    composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
  }

  /**
   * Create standard test options for select fields.
   *
   * @param count number of options to create (default 2)
   * @return list of SelectOption objects
   */
  fun createTestOptions(count: Int = 2): List<SelectOption> {
    return (1..count).map { SelectOption("opt$it", "Option $it") }
  }

  /**
   * Perform text input on a node with proper null handling.
   *
   * @param composeTestRule the compose test rule
   * @param testTag the test tag of the node
   * @param text the text to input
   */
  fun inputText(composeTestRule: ComposeContentTestRule, testTag: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performTextInput(text)
  }

  /**
   * Clear text input on a node.
   *
   * @param composeTestRule the compose test rule
   * @param testTag the test tag of the node
   */
  fun clearText(composeTestRule: ComposeContentTestRule, testTag: String) {
    composeTestRule.onNodeWithTag(testTag).performTextClearance()
  }

  /**
   * Clear text and then input new text on a node.
   *
   * @param composeTestRule the compose test rule
   * @param testTag the test tag of the node
   * @param text the text to input after clearing
   */
  fun clearAndInputText(composeTestRule: ComposeContentTestRule, testTag: String, text: String) {
    clearText(composeTestRule, testTag)
    inputText(composeTestRule, testTag, text)
  }

  /**
   * Click a node by test tag.
   *
   * @param composeTestRule the compose test rule
   * @param testTag the test tag of the node
   * @return the node interaction
   */
  fun clickNode(
      composeTestRule: ComposeContentTestRule,
      testTag: String
  ): SemanticsNodeInteraction {
    return composeTestRule.onNodeWithTag(testTag).performClick()
  }

  /**
   * Helper to test input updates with type checking. Sets up content, performs input, and verifies
   * the callback was triggered.
   *
   * @param T the type that will be updated
   * @param composeTestRule the compose test rule
   * @param content composable content to test
   * @param capturedUpdate a holder for the captured update (will be mutated)
   * @param testTag the test tag to perform input on
   * @param inputValue the value to input
   * @param clearFirst whether to clear the field before inputting (default false)
   * @param verify verification lambda to check the captured update
   */
  inline fun <reified T> testInputUpdate(
      composeTestRule: ComposeContentTestRule,
      noinline content: @Composable (onUpdate: (T) -> Unit) -> Unit,
      capturedUpdate: MutableList<T>,
      testTag: String,
      inputValue: String,
      clearFirst: Boolean = false,
      crossinline verify: (T) -> Unit
  ) {
    composeTestRule.setContent { content { update -> capturedUpdate.add(update) } }

    if (clearFirst) {
      clearAndInputText(composeTestRule, testTag, inputValue)
    } else {
      inputText(composeTestRule, testTag, inputValue)
    }

    assertTrue("Update callback was not triggered", capturedUpdate.isNotEmpty())
    verify(capturedUpdate.last())
  }

  /**
   * Helper to test checkbox toggle with type checking.
   *
   * @param T the type that will be updated
   * @param composeTestRule the compose test rule
   * @param content composable content to test
   * @param capturedUpdate a holder for the captured update (will be mutated)
   * @param checkboxTag the test tag of the checkbox
   * @param verify verification lambda to check the captured update
   */
  inline fun <reified T> testCheckboxToggle(
      composeTestRule: ComposeContentTestRule,
      noinline content: @Composable (onUpdate: (T) -> Unit) -> Unit,
      capturedUpdate: MutableList<T>,
      checkboxTag: String,
      crossinline verify: (T) -> Unit
  ) {
    composeTestRule.setContent { content { update -> capturedUpdate.add(update) } }

    clickNode(composeTestRule, checkboxTag)

    assertTrue("Update callback was not triggered", capturedUpdate.isNotEmpty())
    verify(capturedUpdate.last())
  }

  /**
   * Test that a component displays correctly when disabled.
   *
   * @param composeTestRule the compose test rule
   * @param content composable content to test
   * @param tags all test tags that should be disabled
   */
  fun testDisabledState(
      composeTestRule: ComposeContentTestRule,
      content: @Composable () -> Unit,
      vararg tags: String
  ) {
    composeTestRule.setContent { content() }
    assertNodesDisabled(composeTestRule, *tags)
  }

  /**
   * Test that a component displays all its fields.
   *
   * @param composeTestRule the compose test rule
   * @param content composable content to test
   * @param tags all test tags that should be displayed
   */
  fun testDisplaysAllFields(
      composeTestRule: ComposeContentTestRule,
      content: @Composable () -> Unit,
      vararg tags: String
  ) {
    composeTestRule.setContent { content() }
    assertNodesDisplayed(composeTestRule, *tags)
  }
}

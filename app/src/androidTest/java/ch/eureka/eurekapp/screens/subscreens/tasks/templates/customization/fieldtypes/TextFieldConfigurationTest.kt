package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/** Android UI tests for TextFieldConfiguration. */
class TextFieldConfigurationTest : BaseFieldConfigurationTest() {

  @Test
  fun textFieldConfigurationDisplaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
        },
        TextFieldConfigurationTestTags.MAX_LENGTH,
        TextFieldConfigurationTestTags.MIN_LENGTH,
        TextFieldConfigurationTestTags.PLACEHOLDER)
  }

  @Test
  fun textFieldConfigurationMaxLengthInputUpdatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = TextFieldConfigurationTestTags.MAX_LENGTH,
        inputValue = "100") { updated ->
          assertions.assertPropertyEquals(100, updated, { it.maxLength }, "maxLength")
        }
  }

  @Test
  fun textFieldConfigurationMinLengthInputUpdatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = TextFieldConfigurationTestTags.MIN_LENGTH,
        inputValue = "5") { updated ->
          assertions.assertPropertyEquals(5, updated, { it.minLength }, "minLength")
        }
  }

  @Test
  fun textFieldConfigurationPlaceholderInputUpdatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = TextFieldConfigurationTestTags.PLACEHOLDER,
        inputValue = "Enter text") { updated ->
          assertions.assertPropertyEquals("Enter text", updated, { it.placeholder }, "placeholder")
        }
  }

  @Test
  fun textFieldConfigurationMinLessThanMaxNoError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 10), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(composeTestRule, "Max length must be ≥ min length")
  }

  @Test
  fun textFieldConfigurationMinEqualToMaxNoError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 5), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(composeTestRule, "Max length must be ≥ min length")
  }

  @Test
  fun textFieldConfigurationDisabledAllFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = false)
        },
        TextFieldConfigurationTestTags.MAX_LENGTH,
        TextFieldConfigurationTestTags.MIN_LENGTH,
        TextFieldConfigurationTestTags.PLACEHOLDER)
  }

  @Test
  fun textFieldConfigurationWithPlaceholderDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(placeholder = "Type here"), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.PLACEHOLDER)
  }

  @Test
  fun textFieldConfigurationWithMaxLengthDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(maxLength = 50), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.MAX_LENGTH)
  }

  @Test
  fun textFieldConfigurationWithMinLengthDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 3), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.MIN_LENGTH)
  }

  @Test
  fun textFieldConfiguration_invalidMaxLengthInputShowsErrorAndDoesNotUpdate() {
    val updates = mutableListOf<FieldType.Text>()
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updates.add(it) }, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MAX_LENGTH).performTextInput("abc")
    composeTestRule.onNodeWithText("Invalid number").assertIsDisplayed()
    assert(updates.isEmpty()) { "Model should not be updated with invalid input" }
  }

  @Test
  fun textFieldConfiguration_invalidMinLengthInputShowsErrorAndDoesNotUpdate() {
    val updates = mutableListOf<FieldType.Text>()
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updates.add(it) }, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MIN_LENGTH).performTextInput("xyz")
    composeTestRule.onNodeWithText("Invalid number").assertIsDisplayed()
    assert(updates.isEmpty()) { "Model should not be updated with invalid input" }
  }

  @Test
  fun textFieldConfiguration_zeroMaxLengthShowsError() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MAX_LENGTH).performTextInput("0")
    composeTestRule.onNodeWithText("Max length must be positive").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_negativeMinLengthShowsError() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MIN_LENGTH).performTextInput("-1")
    composeTestRule.onNodeWithText("Min length must be non-negative").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_emptyMaxLengthSetsToNull() {
    val updates = mutableListOf<FieldType.Text>()
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(maxLength = 100),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, TextFieldConfigurationTestTags.MAX_LENGTH)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.maxLength }, "maxLength")
  }

  @Test
  fun textFieldConfiguration_emptyMinLengthSetsToNull() {
    val updates = mutableListOf<FieldType.Text>()
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 10),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, TextFieldConfigurationTestTags.MIN_LENGTH)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.minLength }, "minLength")
  }

  @Test
  fun textFieldConfiguration_zeroMinLengthAccepted() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = TextFieldConfigurationTestTags.MIN_LENGTH,
        inputValue = "0") { updated ->
          assertions.assertPropertyEquals(0, updated, { it.minLength }, "minLength")
        }
  }
}

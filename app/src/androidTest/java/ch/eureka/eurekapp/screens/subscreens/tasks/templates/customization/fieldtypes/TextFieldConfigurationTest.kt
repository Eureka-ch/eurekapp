package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

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

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
  }

  @Test
  fun textFieldConfigurationMinEqualToMaxNoError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 5), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
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
}

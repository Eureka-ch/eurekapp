// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/** Android UI tests for TextFieldConfiguration. */
class TextFieldConfigurationTest : BaseFieldConfigurationTest() {

  @Test
  fun textFieldConfiguration_displaysAllFields() {
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
  fun textFieldConfiguration_maxLengthInputUpdatesType() {
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
  fun textFieldConfiguration_minLengthInputUpdatesType() {
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
  fun textFieldConfiguration_placeholderInputUpdatesType() {
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
  fun textFieldConfiguration_minLessThanMaxNoError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 10), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
  }

  @Test
  fun textFieldConfiguration_minEqualToMaxNoError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 5), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
  }

  @Test
  fun textFieldConfiguration_disabledAllFieldsDisabled() {
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
  fun textFieldConfiguration_withPlaceholderDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(placeholder = "Type here"), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.PLACEHOLDER)
  }

  @Test
  fun textFieldConfiguration_withMaxLengthDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(maxLength = 50), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.MAX_LENGTH)
  }

  @Test
  fun textFieldConfiguration_withMinLengthDisplays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 3), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, TextFieldConfigurationTestTags.MIN_LENGTH)
  }
}

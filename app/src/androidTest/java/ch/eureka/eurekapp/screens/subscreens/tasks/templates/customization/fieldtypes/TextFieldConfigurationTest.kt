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
        "text_max_length",
        "text_min_length",
        "text_placeholder")
  }

  @Test
  fun textFieldConfiguration_maxLengthInput_updatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "text_max_length",
        inputValue = "100") { updated ->
          assertions.assertPropertyEquals(100, updated, { it.maxLength }, "maxLength")
        }
  }

  @Test
  fun textFieldConfiguration_minLengthInput_updatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "text_min_length",
        inputValue = "5") { updated ->
          assertions.assertPropertyEquals(5, updated, { it.minLength }, "minLength")
        }
  }

  @Test
  fun textFieldConfiguration_placeholderInput_updatesType() {
    val updates = mutableListOf<FieldType.Text>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "text_placeholder",
        inputValue = "Enter text") { updated ->
          assertions.assertPropertyEquals("Enter text", updated, { it.placeholder }, "placeholder")
        }
  }

  @Test
  fun textFieldConfiguration_minLessThanMax_noError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 10), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
  }

  @Test
  fun textFieldConfiguration_minEqualToMax_noError() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 5), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum length must be less than or equal to maximum length")
  }

  @Test
  fun textFieldConfiguration_disabled_allFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = false)
        },
        "text_max_length",
        "text_min_length",
        "text_placeholder")
  }

  @Test
  fun textFieldConfiguration_withPlaceholder_displays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(placeholder = "Type here"), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, "text_placeholder")
  }

  @Test
  fun textFieldConfiguration_withMaxLength_displays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(maxLength = 50), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, "text_max_length")
  }

  @Test
  fun textFieldConfiguration_withMinLength_displays() {
    this.composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 3), onUpdate = {}, enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, "text_min_length")
  }
}

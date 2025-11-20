package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.screens.subscreens.tasks.DateFieldTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.MultiSelectFieldTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.NumberFieldTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.SingleSelectFieldTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.TextFieldComponentTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/**
 * Android UI tests for CommonFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class CommonFieldConfigurationTest : BaseFieldConfigurationTest() {

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_field",
          label = "Test Field",
          type = FieldType.Text(),
          required = false,
          description = "Test description")

  @Test
  fun commonFieldConfiguration_displaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          CommonFieldConfiguration(field = testFieldDefinition, onFieldUpdate = {}, enabled = true)
        },
        "field_label_input",
        "field_description_input",
        "field_required_checkbox")
  }

  @Test
  fun commonFieldConfiguration_requiredCheckboxTogglesField() {
    val updates = mutableListOf<FieldDefinition>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          CommonFieldConfiguration(
              field = testFieldDefinition.copy(required = false),
              onFieldUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "field_required_checkbox") { updated ->
          assertions.assertBooleanTrue(updated, { it.required }, "required should be true")
        }
  }

  @Test
  fun commonFieldConfiguration_requiredCheckboxUnchecksCorrectly() {
    val updates = mutableListOf<FieldDefinition>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          CommonFieldConfiguration(
              field = testFieldDefinition.copy(required = true),
              onFieldUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "field_required_checkbox") { updated ->
          assertions.assertBooleanFalse(updated, { it.required }, "required should be false")
        }
  }

  @Test
  fun commonFieldConfiguration_disabledAllFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          CommonFieldConfiguration(field = testFieldDefinition, onFieldUpdate = {}, enabled = false)
        },
        "field_label_input",
        "field_description_input",
        "field_required_checkbox")
  }

  @Test
  fun commonFieldConfiguration_nullDescriptionShowsEmpty() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          CommonFieldConfiguration(
              field = testFieldDefinition.copy(description = null),
              onFieldUpdate = {},
              enabled = true)
        },
        "field_description_input")
  }

  @Test
  fun defaultValueInput_textFieldDisplays() {
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text()),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(
        composeTestRule, TextFieldComponentTestTags.input(testFieldDefinition.id))
  }

  @Test
  fun defaultValueInput_numberFieldDisplays() {
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Number()),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, NumberFieldTestTags.input(testFieldDefinition.id))
  }

  @Test
  fun defaultValueInput_dateFieldDisplays() {
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Date()),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(composeTestRule, DateFieldTestTags.button(testFieldDefinition.id))
  }

  @Test
  fun defaultValueInput_singleSelectFieldDisplays() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.SingleSelect(options)),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(
        composeTestRule, SingleSelectFieldTestTags.dropdown(testFieldDefinition.id))
  }

  @Test
  fun defaultValueInput_multiSelectFieldDisplays() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.MultiSelect(options)),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(
        composeTestRule, MultiSelectFieldTestTags.chips(testFieldDefinition.id))
  }

  @Test
  fun defaultValueInput_disabledShowsViewMode() {
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text()),
          onFieldUpdate = {},
          enabled = false)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_textFieldWithoutDefaultDisplays() {
    this.composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text(), defaultValue = null),
          onFieldUpdate = {},
          enabled = true)
    }

    utils.assertNodesDisplayed(
        composeTestRule, TextFieldComponentTestTags.input(testFieldDefinition.id))
  }
}

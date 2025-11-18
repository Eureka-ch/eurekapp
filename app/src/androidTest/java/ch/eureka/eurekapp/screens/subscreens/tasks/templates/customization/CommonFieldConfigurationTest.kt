package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for CommonFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class CommonFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_field",
          label = "Test Field",
          type = FieldType.Text(),
          required = false,
          description = "Test description")

  @Test
  fun commonFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      CommonFieldConfiguration(field = testFieldDefinition, onFieldUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_description_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsDisplayed()
  }

  @Test
  fun commonFieldConfiguration_requiredCheckbox_togglesField() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testFieldDefinition.copy(required = false),
          onFieldUpdate = { updatedField = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("field_required_checkbox").performClick()
    assertNotNull(updatedField)
    assertTrue(updatedField?.required == true)
  }

  @Test
  fun commonFieldConfiguration_requiredCheckbox_unchecksCorrectly() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testFieldDefinition.copy(required = true),
          onFieldUpdate = { updatedField = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("field_required_checkbox").performClick()
    assertNotNull(updatedField)
    assertFalse(updatedField?.required == true)
  }

  @Test
  fun commonFieldConfiguration_disabled_allFieldsDisabled() {
    composeTestRule.setContent {
      CommonFieldConfiguration(field = testFieldDefinition, onFieldUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("field_label_input").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("field_description_input").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsNotEnabled()
  }

  @Test
  fun commonFieldConfiguration_nullDescription_showsEmpty() {
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testFieldDefinition.copy(description = null), onFieldUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_description_input").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_textField_displays() {
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text()),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_numberField_displays() {
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Number()),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_dateField_displays() {
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Date()),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_singleSelectField_displays() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.SingleSelect(options)),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_multiSelectField_displays() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.MultiSelect(options)),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_disabled_showsViewMode() {
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text()),
          onFieldUpdate = {},
          enabled = false)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }

  @Test
  fun defaultValueInput_textField_withoutDefault_displays() {
    composeTestRule.setContent {
      DefaultValueInput(
          field = testFieldDefinition.copy(type = FieldType.Text(), defaultValue = null),
          onFieldUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithText("Default Value").assertIsDisplayed()
  }
}

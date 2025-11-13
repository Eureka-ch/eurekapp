package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for CommonFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class CommonFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testField =
      FieldDefinition(id = "test", label = "Test", type = FieldType.Text(), required = false)

  @Test
  fun commonFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      CommonFieldConfiguration(field = testField, onFieldUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_description_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsDisplayed()
    composeTestRule.onNodeWithText("Default Value (optional)").assertIsDisplayed()
  }

  @Test
  fun commonFieldConfiguration_labelInput_updatesField() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testField, onFieldUpdate = { updatedField = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_label_input").performTextInput("X")
    assertNotNull(updatedField)
    assertEquals("XTest", updatedField?.label)
  }

  @Test
  fun commonFieldConfiguration_descriptionInput_updatesField() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testField, onFieldUpdate = { updatedField = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_description_input").performTextInput("Description")
    assertNotNull(updatedField)
    assertEquals("Description", updatedField?.description)
  }

  @Test
  fun commonFieldConfiguration_requiredCheckbox_updatesField() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      CommonFieldConfiguration(
          field = testField, onFieldUpdate = { updatedField = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("field_required_checkbox").performClick()
    assertNotNull(updatedField)
    assertEquals(true, updatedField?.required)
  }
}

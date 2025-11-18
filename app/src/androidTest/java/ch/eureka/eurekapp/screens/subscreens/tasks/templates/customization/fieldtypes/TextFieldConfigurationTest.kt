package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for TextFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class TextFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun textFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_max_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_min_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_placeholder").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_maxLengthInput_updatesType() {
    var updatedType: FieldType.Text? = null
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_max_length").performTextInput("100")
    assertNotNull(updatedType)
    assertEquals(100, updatedType?.maxLength)
  }

  @Test
  fun textFieldConfiguration_minLengthInput_updatesType() {
    var updatedType: FieldType.Text? = null
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_min_length").performTextInput("5")
    assertNotNull(updatedType)
    assertEquals(5, updatedType?.minLength)
  }

  @Test
  fun textFieldConfiguration_placeholderInput_updatesType() {
    var updatedType: FieldType.Text? = null
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_placeholder").performTextInput("Enter text")
    assertNotNull(updatedType)
    assertEquals("Enter text", updatedType?.placeholder)
  }

  @Test
  fun textFieldConfiguration_minLessThanMax_noError() {
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 10), onUpdate = {}, enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum length must be less than or equal to maximum length")
        .assertDoesNotExist()
  }

  @Test
  fun textFieldConfiguration_minEqualToMax_noError() {
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 5, maxLength = 5), onUpdate = {}, enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum length must be less than or equal to maximum length")
        .assertDoesNotExist()
  }

  @Test
  fun textFieldConfiguration_disabled_allFieldsDisabled() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("text_max_length").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("text_min_length").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("text_placeholder").assertIsNotEnabled()
  }

  @Test
  fun textFieldConfiguration_withPlaceholder_displays() {
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(placeholder = "Type here"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_placeholder").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_withMaxLength_displays() {
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(maxLength = 50), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_max_length").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_withMinLength_displays() {
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(minLength = 3), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_min_length").assertIsDisplayed()
  }
}

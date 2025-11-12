package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for TextFieldComponent.
 *
 * Portions of this code were generated with the help of AI.
 */
class TextFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_text",
          label = "Test Text Field",
          type = FieldType.Text(maxLength = 100, placeholder = "Enter text"),
          required = false)

  @Test
  fun textFieldComponent_editMode_showsInputField() {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("text_field_input_test_text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editMode_allowsTextInput() {
    var capturedValue: FieldValue.TextValue? = null

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = { capturedValue = it },
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("text_field_input_test_text").performTextInput("Hello")

    assertNotNull(capturedValue)
    assertEquals("Hello", capturedValue?.value)
  }

  @Test
  fun textFieldComponent_editMode_displaysPlaceholder() {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Enter text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editMode_showsCharacterCount_whenMaxLengthSet() {
    val fieldWithMaxLength = testFieldDefinition.copy(type = FieldType.Text(maxLength = 100))

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = fieldWithMaxLength,
          value = FieldValue.TextValue("Test"),
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("4 / 100").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewMode_showsValueText() {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.TextValue("Sample Text"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithTag("text_field_value_test_text").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewMode_doesNotShowInputField() {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.TextValue("Sample Text"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithTag("text_field_input_test_text").assertDoesNotExist()
  }

  @Test
  fun textFieldComponent_toggleableMode_showsToggleButton() {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    }

    composeTestRule.onNodeWithTag("field_toggle_test_text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onModeToggle = { toggleCalled = true })
    }

    composeTestRule.onNodeWithTag("field_toggle_test_text").performClick()

    assert(toggleCalled)
  }

  @Test
  fun textFieldComponent_requiredField_showsAsterisk() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Test Text Field *").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withDescription_showsDescription() {
    val fieldWithDescription = testFieldDefinition.copy(description = "Enter your text here")

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = fieldWithDescription,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Enter your text here").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withConstraints_showsHints() {
    val fieldWithConstraints =
        testFieldDefinition.copy(type = FieldType.Text(maxLength = 100, minLength = 10))

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = fieldWithConstraints,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Max 100 characters • Min 10 characters").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationEnabled_showsErrors() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = true)
    }

    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = false)
    }

    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }
}

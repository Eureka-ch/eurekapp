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

  private fun setFieldContent(
      fieldDef: FieldDefinition = testFieldDefinition,
      value: FieldValue.TextValue? = null,
      onValueChange: (FieldValue.TextValue) -> Unit = {},
      mode: FieldInteractionMode = FieldInteractionMode.EditOnly,
      showValidationErrors: Boolean = false,
      onModeToggle: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          onModeToggle = onModeToggle)
    }
  }

  @Test
  fun textFieldComponent_editMode_showsInputField() {
    setFieldContent()
    composeTestRule.onNodeWithTag("text_field_input_test_text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editMode_allowsTextInput() {
    var capturedValue: FieldValue.TextValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag("text_field_input_test_text").performTextInput("Hello")

    assertNotNull(capturedValue)
    assertEquals("Hello", capturedValue?.value)
  }

  @Test
  fun textFieldComponent_editMode_displaysPlaceholder() {
    setFieldContent()
    composeTestRule.onNodeWithText("Enter text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editMode_showsCharacterCount_whenMaxLengthSet() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Text(maxLength = 100)),
        value = FieldValue.TextValue("Test"))
    composeTestRule.onNodeWithText("4 / 100").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewMode_showsValueText() {
    setFieldContent(
        value = FieldValue.TextValue("Sample Text"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag("text_field_value_test_text").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewMode_doesNotShowInputField() {
    setFieldContent(
        value = FieldValue.TextValue("Sample Text"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag("text_field_input_test_text").assertDoesNotExist()
  }

  @Test
  fun textFieldComponent_toggleableMode_showsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag("field_toggle_test_text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        onModeToggle = { toggleCalled = true })

    composeTestRule.onNodeWithTag("field_toggle_test_text").performClick()
    assert(toggleCalled)
  }

  @Test
  fun textFieldComponent_requiredField_showsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Text Field *").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withDescription_showsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Enter your text here"))
    composeTestRule.onNodeWithText("Enter your text here").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withConstraints_showsHints() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Text(maxLength = 100, minLength = 10)))
    composeTestRule.onNodeWithText("Max 100 characters • Min 10 characters").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationEnabled_showsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }
}

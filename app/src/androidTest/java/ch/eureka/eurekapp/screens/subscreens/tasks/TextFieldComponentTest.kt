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
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
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
      callbacks: FieldCallbacks = FieldCallbacks()
  ) {
    composeTestRule.setContent {
      TextFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          callbacks = callbacks)
    }
  }

  @Test
  fun textFieldComponent_editModeShowsInputField() {
    setFieldContent()
    composeTestRule.onNodeWithTag(TextFieldComponentTestTags.input("test_text")).assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editModeAllowsTextInput() {
    var capturedValue: FieldValue.TextValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(TextFieldComponentTestTags.input("test_text"))
        .performTextInput("Hello")

    assertNotNull(capturedValue)
    assertEquals("Hello", capturedValue?.value)
  }

  @Test
  fun textFieldComponent_editModeDisplaysPlaceholder() {
    setFieldContent()
    composeTestRule.onNodeWithText("Enter text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_editModeShowsCharacterCountWhenMaxLengthSet() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Text(maxLength = 100)),
        value = FieldValue.TextValue("Test"))
    composeTestRule.onNodeWithText("4 / 100").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewModeShowsValueText() {
    setFieldContent(
        value = FieldValue.TextValue("Sample Text"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(TextFieldComponentTestTags.value("test_text")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Text").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_viewModeDoesNotShowInputField() {
    setFieldContent(
        value = FieldValue.TextValue("Sample Text"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(TextFieldComponentTestTags.input("test_text"))
        .assertDoesNotExist()
  }

  @Test
  fun textFieldComponent_toggleableModeShowsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_text")).assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_toggleableModeCallsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_text")).performClick()
    assert(toggleCalled)
  }

  @Test
  fun textFieldComponent_requiredFieldShowsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Text Field *").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withDescriptionShowsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Enter your text here"))
    composeTestRule.onNodeWithText("Enter your text here").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_withConstraintsShowsHints() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Text(maxLength = 100, minLength = 10)))
    composeTestRule.onNodeWithText("Max 100 characters • Min 10 characters").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationEnabledShowsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun textFieldComponent_whenValidationDisabledDoesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }
}

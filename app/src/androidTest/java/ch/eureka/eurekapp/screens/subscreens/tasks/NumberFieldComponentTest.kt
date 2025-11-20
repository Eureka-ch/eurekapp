package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for NumberFieldComponent.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class NumberFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_number",
          label = "Test Number Field",
          type = FieldType.Number(min = 0.0, max = 100.0, decimals = 2, unit = "kg"),
          required = false)

  private fun setFieldContent(
      fieldDef: FieldDefinition = testFieldDefinition,
      value: FieldValue.NumberValue? = null,
      onValueChange: (FieldValue.NumberValue) -> Unit = {},
      mode: FieldInteractionMode = FieldInteractionMode.EditOnly,
      showValidationErrors: Boolean = false,
      callbacks: FieldCallbacks = FieldCallbacks()
  ) {
    composeTestRule.setContent {
      NumberFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          callbacks = callbacks)
    }
  }

  @Test
  fun numberFieldComponent_editModeShowsInputField() {
    setFieldContent()
    composeTestRule.onNodeWithTag(NumberFieldTestTags.input("test_number")).assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_editModeAllowsNumericInput() {
    var capturedValue: FieldValue.NumberValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(NumberFieldTestTags.input("test_number")).performTextInput("42.5")

    assertNotNull(capturedValue)
    assertEquals(42.5, capturedValue?.value ?: 0.0, 0.001)
  }

  @Test
  fun numberFieldComponent_editModeAllowsNegativeNumbers() {
    var capturedValue: FieldValue.NumberValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(NumberFieldTestTags.input("test_number"))
        .performTextInput("-15.3")

    assertNotNull(capturedValue)
    assertEquals(-15.3, capturedValue?.value ?: 0.0, 0.001)
  }

  @Test
  fun numberFieldComponent_editModeAllowsIntegerInput() {
    var capturedValue: FieldValue.NumberValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(NumberFieldTestTags.input("test_number")).performTextInput("25")

    assertNotNull(capturedValue)
    assertEquals(25.0, capturedValue?.value ?: 0.0, 0.001)
  }

  @Test
  fun numberFieldComponent_editModeDisplaysUnitSuffix() {
    setFieldContent()
    composeTestRule.onNodeWithText("kg").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_viewModeShowsFormattedValue() {
    setFieldContent(value = FieldValue.NumberValue(42.567), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(NumberFieldTestTags.value("test_number")).assertIsDisplayed()
    composeTestRule.onNodeWithText("42.57 kg").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_viewModeFormatsWithCorrectDecimals() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Number(decimals = 1, unit = "m")),
        value = FieldValue.NumberValue(3.14159),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("3.1 m").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_viewModeFormatsWithoutDecimals() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Number(decimals = 0)),
        value = FieldValue.NumberValue(42.789),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("43").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_viewModeDisplaysUnitSuffix() {
    setFieldContent(value = FieldValue.NumberValue(50.0), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("50.00 kg").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_viewModeDoesNotShowInputField() {
    setFieldContent(value = FieldValue.NumberValue(42.5), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(NumberFieldTestTags.input("test_number")).assertDoesNotExist()
  }

  @Test
  fun numberFieldComponent_toggleableModeShowsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_number")).assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_toggleableModeCallsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_number")).performClick()
    assert(toggleCalled)
  }

  @Test
  fun numberFieldComponent_requiredFieldShowsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Number Field *").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_withDescriptionShowsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Enter a numeric value"))
    composeTestRule.onNodeWithText("Enter a numeric value").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_withConstraintsShowsHints() {
    setFieldContent()
    composeTestRule.onNodeWithText("Range: 0.0 - 100.0 • Unit: kg").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_withMinOnlyShowsMinHint() {
    setFieldContent(fieldDef = testFieldDefinition.copy(type = FieldType.Number(min = 5.0)))
    composeTestRule.onNodeWithText("Min: 5.0").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_withMaxOnlyShowsMaxHint() {
    setFieldContent(fieldDef = testFieldDefinition.copy(type = FieldType.Number(max = 100.0)))
    composeTestRule.onNodeWithText("Max: 100.0").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_whenValidationEnabledShowsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_whenValidationDisabledDoesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun numberFieldComponent_withoutUnitDisplaysValueWithoutUnit() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Number(decimals = 2)),
        value = FieldValue.NumberValue(42.5),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("42.50").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_withZeroValueDisplaysZero() {
    setFieldContent(value = FieldValue.NumberValue(0.0), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("0.00 kg").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_nullDecimalsFormatsAsZeroDecimals() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Number(decimals = null)),
        value = FieldValue.NumberValue(42.567),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("43").assertIsDisplayed()
  }

  @Test
  fun numberFieldComponent_toggleableSaveCallsOnSaveCallback() {
    var saveCalled = false
    setFieldContent(
        value = FieldValue.NumberValue(42.0),
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onSave = { saveCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_number")).performClick()
    assert(saveCalled)
  }

  @Test
  fun numberFieldComponent_toggleableCancelCallsOnCancelCallback() {
    var cancelCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onCancel = { cancelCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_number")).performClick()
    assert(cancelCalled)
  }

  @Test
  fun numberFieldComponent_editModeClearingInputSetsNullValue() {
    var capturedValue: FieldValue.NumberValue? = null
    setFieldContent(value = FieldValue.NumberValue(42.0), onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(NumberFieldTestTags.input("test_number"))
        .performTextReplacement("")

    assertNotNull(capturedValue)
    assertEquals(null, capturedValue?.value)
  }
}

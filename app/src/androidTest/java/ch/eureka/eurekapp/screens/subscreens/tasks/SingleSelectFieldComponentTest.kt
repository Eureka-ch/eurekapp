package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class SingleSelectFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(
          SelectOption("low", "Low", "Low priority"),
          SelectOption("medium", "Medium", "Medium priority"),
          SelectOption("high", "High", "High priority"))

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_select",
          label = "Test Single Select",
          type = FieldType.SingleSelect(options = testOptions, allowCustom = false),
          required = false)

  private fun setFieldContent(
      fieldDef: FieldDefinition = testFieldDefinition,
      value: FieldValue.SingleSelectValue? = null,
      onValueChange: (FieldValue.SingleSelectValue) -> Unit = {},
      mode: FieldInteractionMode = FieldInteractionMode.EditOnly,
      showValidationErrors: Boolean = false,
      callbacks: FieldCallbacks = FieldCallbacks()
  ) {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          callbacks = callbacks)
    }
  }

  @Test
  fun singleSelectFieldComponent_editMode_showsDropdown() {
    setFieldContent()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_menuOpensOnClick() {
    setFieldContent()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_field_menu_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_displaysAllOptions() {
    setFieldContent()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_low").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_medium").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_high").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_displaysOptionDescriptions() {
    setFieldContent()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_low").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_medium").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_high").assertIsDisplayed()
    composeTestRule.onNodeWithText("Low priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("Medium priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("High priority").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_selectingOptionUpdatesValue() {
    var capturedValue: FieldValue.SingleSelectValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_high").performClick()

    assertNotNull(capturedValue)
    assertEquals("high", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_editMode_selectedValueDisplaysInTextField() {
    setFieldContent(value = FieldValue.SingleSelectValue("medium"))
    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_showsSelectedValue() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("high"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag("single_select_field_value_test_select").assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_doesNotShowDropdown() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("low"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_displaysOptionLabel() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("medium"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_allowsCustomInput() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    var capturedValue: FieldValue.SingleSelectValue? = null
    setFieldContent(fieldDef = fieldWithCustom, onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_custom").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("single_select_field_input_test_select").performClick()
    composeTestRule
        .onNodeWithTag("single_select_field_input_test_select")
        .performTextInput("custom")

    assertNotNull(capturedValue)
    assertEquals("custom", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_displaysCustomValue() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    setFieldContent(
        fieldDef = fieldWithCustom,
        value = FieldValue.SingleSelectValue("custom_value"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("custom_value").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_toggleableMode_showsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag("field_toggle_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag("field_toggle_test_select").performClick()
    assert(toggleCalled)
  }

  @Test
  fun singleSelectFieldComponent_requiredField_showsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Single Select *").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_withDescription_showsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Choose a priority level"))
    composeTestRule.onNodeWithText("Choose a priority level").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_showsConstraintHint() {
    setFieldContent()
    composeTestRule.onNodeWithText("3 options").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_showsConstraintHint() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithText("3 options (custom values allowed)").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationEnabled_showsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_showsCustomValueOption() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_custom").assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom value").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomFalse_doesNotShowCustomValueOption() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = false)))
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_custom").assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_customMode_showsPlaceholder() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_custom").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Enter custom value").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_switchFromPredefinedToCustom() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    var capturedValue: FieldValue.SingleSelectValue? = null
    setFieldContent(
        fieldDef = fieldWithCustom,
        value = FieldValue.SingleSelectValue("low"),
        onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithText("Low").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_custom").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Enter custom value").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_field_input_test_select").performClick()
    composeTestRule
        .onNodeWithTag("single_select_field_input_test_select")
        .performTextInput("my custom value")

    assertNotNull(capturedValue)
    assertEquals("my custom value", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_switchFromCustomToPredefined() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    var currentValue: FieldValue.SingleSelectValue? by
        mutableStateOf(FieldValue.SingleSelectValue("custom text"))
    setFieldContent(
        fieldDef = fieldWithCustom, value = currentValue, onValueChange = { currentValue = it })

    composeTestRule.onNodeWithText("custom text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_high").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()

    assertNotNull(currentValue)
    assertEquals("high", currentValue?.value)
  }
}

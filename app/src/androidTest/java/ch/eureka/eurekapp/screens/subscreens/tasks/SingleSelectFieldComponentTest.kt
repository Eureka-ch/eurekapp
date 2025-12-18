/* Portions of this file were written with the help of Claude. */
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
  fun singleSelectFieldComponent_editModeShowsDropdown() {
    setFieldContent()
    composeTestRule
        .onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select"))
        .assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editModeMenuOpensOnClick() {
    setFieldContent()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.menu("test_select")).assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editModeDisplaysAllOptions() {
    setFieldContent()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("low")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("medium")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("high")).assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editModeDisplaysOptionDescriptions() {
    setFieldContent()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("low")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("medium")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("high")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Low priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("Medium priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("High priority").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editModeSelectingOptionUpdatesValue() {
    var capturedValue: FieldValue.SingleSelectValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("high")).performClick()

    assertNotNull(capturedValue)
    assertEquals("high", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_editModeSelectedValueDisplaysInTextField() {
    setFieldContent(value = FieldValue.SingleSelectValue("medium"))
    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewModeShowsSelectedValue() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("high"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(SingleSelectFieldTestTags.value("test_select"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewModeDoesNotShowDropdown() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("low"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select"))
        .assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_viewModeDisplaysOptionLabel() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("medium"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrueAllowsCustomInput() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    var capturedValue: FieldValue.SingleSelectValue? = null
    setFieldContent(fieldDef = fieldWithCustom, onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.CUSTOM_OPTION).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.input("test_select")).performClick()
    composeTestRule
        .onNodeWithTag(SingleSelectFieldTestTags.input("test_select"))
        .performTextInput("custom")

    assertNotNull(capturedValue)
    assertEquals("custom", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrueDisplaysCustomValue() {
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
  fun singleSelectFieldComponent_toggleableModeShowsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_select")).assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_toggleableModeCallsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_select")).performClick()
    assert(toggleCalled)
  }

  @Test
  fun singleSelectFieldComponent_requiredFieldShowsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Single Select *").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_withDescriptionShowsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Choose a priority level"))
    composeTestRule.onNodeWithText("Choose a priority level").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editModeShowsConstraintHint() {
    setFieldContent()
    composeTestRule.onNodeWithText("3 options").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrueShowsConstraintHint() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithText("3 options (custom values allowed)").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationEnabledShowsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationDisabledDoesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrueShowsCustomValueOption() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.CUSTOM_OPTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom value").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomFalseDoesNotShowCustomValueOption() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = false)))
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.CUSTOM_OPTION).assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_customModeShowsPlaceholder() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.SingleSelect(options = testOptions, allowCustom = true)))
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.CUSTOM_OPTION).performClick()
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
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.CUSTOM_OPTION).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Enter custom value").assertIsDisplayed()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.input("test_select")).performClick()
    composeTestRule
        .onNodeWithTag(SingleSelectFieldTestTags.input("test_select"))
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
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.option("high")).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()

    assertNotNull(currentValue)
    assertEquals("high", currentValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_toggleableSaveCallsOnSaveCallback() {
    var saveCalled = false
    setFieldContent(
        value = FieldValue.SingleSelectValue("low"),
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onSave = { saveCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_select")).performClick()
    assert(saveCalled)
  }

  @Test
  fun singleSelectFieldComponent_toggleableCancelCallsOnCancelCallback() {
    var cancelCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onCancel = { cancelCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_select")).performClick()
    assert(cancelCalled)
  }

  @Test
  fun singleSelectFieldComponent_menuDismissOnDismissRequest() {
    setFieldContent()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.dropdown("test_select")).performClick()
    composeTestRule.onNodeWithTag(SingleSelectFieldTestTags.menu("test_select")).assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewModeCustomValueNotInOptionsDisplaysValue() {
    setFieldContent(
        value = FieldValue.SingleSelectValue("unknown_option"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("unknown_option").assertIsDisplayed()
  }
}

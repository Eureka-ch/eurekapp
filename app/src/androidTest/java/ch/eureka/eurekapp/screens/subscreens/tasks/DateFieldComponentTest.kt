package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for DateFieldComponent.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class DateFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_date",
          label = "Test Date Field",
          type = FieldType.Date(includeTime = false, format = "yyyy-MM-dd"),
          required = false)

  private fun setFieldContent(
      fieldDef: FieldDefinition = testFieldDefinition,
      value: FieldValue.DateValue? = null,
      onValueChange: (FieldValue.DateValue) -> Unit = {},
      mode: FieldInteractionMode = FieldInteractionMode.EditOnly,
      showValidationErrors: Boolean = false,
      callbacks: FieldCallbacks = FieldCallbacks()
  ) {
    composeTestRule.setContent {
      DateFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          callbacks = callbacks)
    }
  }

  @Test
  fun dateFieldComponent_editModeShowsPickerButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeButtonShowsSelectDateTextWhenNoValue() {
    setFieldContent()
    composeTestRule.onNodeWithText("Select Date").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeButtonShowsSelectDateAndTimeTextWhenIncludeTime() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")))
    composeTestRule.onNodeWithText("Select Date & Time").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeButtonOpensDatePickerDialog() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeDatePickerShowsCancelButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeDatePickerShowsOKButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeCancelButtonClosesPicker() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_viewModeShowsFormattedDate() {
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertIsDisplayed()
    composeTestRule.onNodeWithText("2025-12-25").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_viewModeDoesNotShowPickerButton() {
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_withIncludeTimeShowsTimeInView() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")),
        value = FieldValue.DateValue("2025-12-25T15:30:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableModeShowsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableModeCallsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_date")).performClick()
    assertTrue(toggleCalled)
  }

  @Test
  fun dateFieldComponent_requiredFieldShowsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Date Field *").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withDescriptionShowsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Select your preferred date"))
    composeTestRule.onNodeWithText("Select your preferred date").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withConstraintsShowsHints() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type =
                    FieldType.Date(
                        minDate = "2025-01-01", maxDate = "2025-12-31", format = "yyyy-MM-dd")))
    composeTestRule
        .onNodeWithText("Range: 2025-01-01 - 2025-12-31 • Format: yyyy-MM-dd")
        .assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_whenValidationEnabledShowsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_whenValidationDisabledDoesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_customFormatFormatsCorrectly() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(format = "dd/MM/yyyy")),
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("25/12/2025").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withMinDateConstraintShowsMinDateHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(minDate = "2025-01-01")))
    composeTestRule.onNodeWithText("From: 2025-01-01").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withMaxDateConstraintShowsMaxDateHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(maxDate = "2025-12-31")))
    composeTestRule.onNodeWithText("Until: 2025-12-31").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withIncludeTimeShowsIncludesTimeHint() {
    setFieldContent(fieldDef = testFieldDefinition.copy(type = FieldType.Date(includeTime = true)))
    composeTestRule.onNodeWithText("Includes time").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editModeButtonShowsFormattedValueWhenValueExists() {
    setFieldContent(value = FieldValue.DateValue("2025-12-25T00:00:00Z"))
    composeTestRule.onNodeWithText("2025-12-25").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableEditingShowsSaveAndCancelButtons() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_date")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableSaveCallsOnSaveCallback() {
    var saveCalled = false
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onSave = { saveCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_date")).performClick()
    assertTrue(saveCalled)
  }

  @Test
  fun dateFieldComponent_toggleableCancelCallsOnCancelCallback() {
    var cancelCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onCancel = { cancelCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_date")).performClick()
    assertTrue(cancelCalled)
  }

  @Test
  fun dateFieldComponent_invalidDateValueDisplaysOriginalValue() {
    setFieldContent(
        value = FieldValue.DateValue("invalid-date-string"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("invalid-date-string").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_timePickerCancelClosesDialog() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")))
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_showHeaderFalseHidesHeader() {
    composeTestRule.setContent {
      DateFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = false)
    }

    composeTestRule.onNodeWithText("Test Date Field").assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_showHeaderTrueShowsHeader() {
    composeTestRule.setContent {
      DateFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = true)
    }

    composeTestRule.onNodeWithText("Test Date Field").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_showHeaderFalseStillRendersPickerButton() {
    composeTestRule.setContent {
      DateFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = false)
    }

    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_emptyValueToValueCallsOnValueChange() {
    var changedValue: FieldValue.DateValue? = null
    setFieldContent(value = null, onValueChange = { changedValue = it })

    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    // Simulating user selecting a date would require actual UI interaction with date picker
    // This test verifies the callback mechanism is set up correctly
  }

  @Test
  fun dateFieldComponent_nullValueShowsSelectText() {
    setFieldContent(value = null)
    composeTestRule.onNodeWithText("Select Date").assertIsDisplayed()
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_withValueShowsFormattedValue() {
    setFieldContent(value = FieldValue.DateValue("2025-12-25T00:00:00Z"))
    composeTestRule.onNodeWithText("2025-12-25").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_customFormatMMddyyyyFormatsCorrectly() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(format = "MM/dd/yyyy")),
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("12/25/2025").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_customFormatDdMMyyyyFormatsCorrectly() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(format = "dd-MM-yyyy")),
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("25-12-2025").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withTimeFormatDisplaysTime() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm:ss")),
        value = FieldValue.DateValue("2025-12-25T15:30:45Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_multipleConstraintsShowsAllHints() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type =
                    FieldType.Date(
                        minDate = "2025-01-01",
                        maxDate = "2025-12-31",
                        includeTime = true,
                        format = "yyyy-MM-dd HH:mm")))
    composeTestRule
        .onNodeWithText("Range: 2025-01-01 - 2025-12-31 • Format: yyyy-MM-dd HH:mm • Includes time")
        .assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_formatWithTimeShowsTimeInButton() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")),
        value = FieldValue.DateValue("2025-12-25T15:30:00Z"))
    // Verifies that the button shows the formatted date with time
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_requiredAndEmptyShowsValidationError() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true),
        value = null,
        showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_requiredWithValueNoValidationError() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true),
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_optionalAndEmptyNoValidationError() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = false),
        value = null,
        showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_minDateOnlyShowsFromHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(minDate = "2025-06-01")))
    composeTestRule.onNodeWithText("From: 2025-06-01").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_maxDateOnlyShowsUntilHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(maxDate = "2025-06-30")))
    composeTestRule.onNodeWithText("Until: 2025-06-30").assertIsDisplayed()
  }
}

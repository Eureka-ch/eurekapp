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
  fun dateFieldComponent_editMode_showsPickerButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_buttonShowsSelectDateText_whenNoValue() {
    setFieldContent()
    composeTestRule.onNodeWithText("Select Date").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_buttonShowsSelectDateAndTimeText_whenIncludeTime() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")))
    composeTestRule.onNodeWithText("Select Date & Time").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_buttonOpensDatePickerDialog() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_datePickerShowsCancelButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_datePickerShowsOKButton() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_cancelButtonClosesPicker() {
    setFieldContent()
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_viewMode_showsFormattedDate() {
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertIsDisplayed()
    composeTestRule.onNodeWithText("2025-12-25").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_viewMode_doesNotShowPickerButton() {
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_withIncludeTime_showsTimeInView() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")),
        value = FieldValue.DateValue("2025-12-25T15:30:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(DateFieldTestTags.value("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableMode_showsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.toggle("test_date")).performClick()
    assertTrue(toggleCalled)
  }

  @Test
  fun dateFieldComponent_requiredField_showsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Date Field *").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withDescription_showsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Select your preferred date"))
    composeTestRule.onNodeWithText("Select your preferred date").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withConstraints_showsHints() {
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
  fun dateFieldComponent_whenValidationEnabled_showsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun dateFieldComponent_customFormat_formatsCorrectly() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(format = "dd/MM/yyyy")),
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("25/12/2025").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withMinDateConstraint_showsMinDateHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(minDate = "2025-01-01")))
    composeTestRule.onNodeWithText("From: 2025-01-01").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withMaxDateConstraint_showsMaxDateHint() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(type = FieldType.Date(maxDate = "2025-12-31")))
    composeTestRule.onNodeWithText("Until: 2025-12-31").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_withIncludeTime_showsIncludesTimeHint() {
    setFieldContent(fieldDef = testFieldDefinition.copy(type = FieldType.Date(includeTime = true)))
    composeTestRule.onNodeWithText("Includes time").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_editMode_buttonShowsFormattedValue_whenValueExists() {
    setFieldContent(value = FieldValue.DateValue("2025-12-25T00:00:00Z"))
    composeTestRule.onNodeWithText("2025-12-25").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableEditing_showsSaveAndCancelButtons() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true))
    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_date")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_date")).assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_toggleableSave_callsOnSaveCallback() {
    var saveCalled = false
    setFieldContent(
        value = FieldValue.DateValue("2025-12-25T00:00:00Z"),
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onSave = { saveCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.save("test_date")).performClick()
    assertTrue(saveCalled)
  }

  @Test
  fun dateFieldComponent_toggleableCancel_callsOnCancelCallback() {
    var cancelCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
        callbacks = FieldCallbacks(onCancel = { cancelCalled = true }))

    composeTestRule.onNodeWithTag(BaseFieldTestTags.cancel("test_date")).performClick()
    assertTrue(cancelCalled)
  }

  @Test
  fun dateFieldComponent_invalidDateValue_displaysOriginalValue() {
    setFieldContent(
        value = FieldValue.DateValue("invalid-date-string"), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("invalid-date-string").assertIsDisplayed()
  }

  @Test
  fun dateFieldComponent_timePickerCancel_closesDialog() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.Date(includeTime = true, format = "yyyy-MM-dd HH:mm")))
    composeTestRule.onNodeWithTag(DateFieldTestTags.button("test_date")).performClick()
    composeTestRule.onNodeWithTag(DateFieldTestTags.DATE_PICKER_DIALOG).assertIsDisplayed()
  }
}

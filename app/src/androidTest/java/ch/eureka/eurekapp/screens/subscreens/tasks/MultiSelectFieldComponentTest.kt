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
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MultiSelectFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(
          SelectOption("option1", "Option 1", "First option"),
          SelectOption("option2", "Option 2", "Second option"),
          SelectOption("option3", "Option 3", "Third option"))

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_multi_select",
          label = "Test Multi Select",
          type = FieldType.MultiSelect(options = testOptions, allowCustom = false),
          required = false)

  private fun setFieldContent(
      fieldDef: FieldDefinition = testFieldDefinition,
      value: FieldValue.MultiSelectValue? = null,
      onValueChange: (FieldValue.MultiSelectValue) -> Unit = {},
      mode: FieldInteractionMode = FieldInteractionMode.EditOnly,
      showValidationErrors: Boolean = false,
      callbacks: FieldCallbacks = FieldCallbacks()
  ) {
    composeTestRule.setContent {
      MultiSelectFieldComponent(
          fieldDefinition = fieldDef,
          value = value,
          onValueChange = onValueChange,
          mode = mode,
          showValidationErrors = showValidationErrors,
          callbacks = callbacks)
    }
  }

  @Test
  fun multiSelectFieldComponent_editMode_showsChipOptions() {
    setFieldContent()
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.chips("test_multi_select"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 3").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_editMode_clickingChipTogglesSelection() {
    var capturedValue: FieldValue.MultiSelectValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).performClick()

    assertNotNull(capturedValue)
    assertEquals(1, capturedValue?.values?.size)
    assertTrue(capturedValue?.values?.contains("option1") == true)
  }

  @Test
  fun multiSelectFieldComponent_editMode_multipleSelectionsWork() {
    var capturedValue: FieldValue.MultiSelectValue? = null
    setFieldContent(onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).performClick()
    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option2")).performClick()

    assertNotNull(capturedValue)
    assertEquals(2, capturedValue?.values?.size)
    assertTrue(capturedValue?.values?.contains("option1") == true)
    assertTrue(capturedValue?.values?.contains("option2") == true)
  }

  @Test
  fun multiSelectFieldComponent_editMode_clickingSelectedChipDeselectsIt() {
    var capturedValue: FieldValue.MultiSelectValue? =
        FieldValue.MultiSelectValue(listOf("option1", "option2"))
    setFieldContent(value = capturedValue, onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).performClick()

    assertNotNull(capturedValue)
    assertEquals(1, capturedValue?.values?.size)
    assertTrue(capturedValue?.values?.contains("option2") == true)
    assertTrue(capturedValue?.values?.contains("option1") == false)
  }

  @Test
  fun multiSelectFieldComponent_viewMode_showsSelectedValuesAsChips() {
    setFieldContent(
        value = FieldValue.MultiSelectValue(listOf("option1", "option2")),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.value("test_multi_select"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option2")).assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_viewMode_chipsAreNonInteractive() {
    var valueChanged = false
    setFieldContent(
        value = FieldValue.MultiSelectValue(listOf("option1")),
        onValueChange = { valueChanged = true },
        mode = FieldInteractionMode.ViewOnly)

    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).performClick()
    assertTrue(!valueChanged)
  }

  @Test
  fun multiSelectFieldComponent_viewMode_emptySelections_showsEmptyText() {
    setFieldContent(mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.value("test_multi_select"))
        .assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_viewMode_doesNotShowEditChips() {
    setFieldContent(
        value = FieldValue.MultiSelectValue(listOf("option1")),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.chips("test_multi_select"))
        .assertDoesNotExist()
  }

  @Test
  fun multiSelectFieldComponent_allowCustomTrue_showsCustomInput() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)))
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_allowCustomTrue_canAddCustomValue() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.MultiSelect(options = testOptions, allowCustom = true))
    var capturedValue: FieldValue.MultiSelectValue? = null
    setFieldContent(fieldDef = fieldWithCustom, onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .performTextInput("Custom Value")
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .performClick()

    assertNotNull(capturedValue)
    assertEquals(1, capturedValue?.values?.size)
    assertTrue(capturedValue?.values?.contains("Custom Value") == true)
  }

  @Test
  fun multiSelectFieldComponent_allowCustomFalse_doesNotShowCustomInput() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = false)))
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .assertDoesNotExist()
  }

  @Test
  fun multiSelectFieldComponent_toggleableMode_showsToggleButton() {
    setFieldContent(mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    composeTestRule.onNodeWithTag("field_toggle_test_multi_select").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false
    setFieldContent(
        mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
        callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true }))

    composeTestRule.onNodeWithTag("field_toggle_test_multi_select").performClick()
    assertTrue(toggleCalled)
  }

  @Test
  fun multiSelectFieldComponent_requiredField_showsAsterisk() {
    setFieldContent(fieldDef = testFieldDefinition.copy(required = true))
    composeTestRule.onNodeWithText("Test Multi Select *").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_withDescription_showsDescription() {
    setFieldContent(fieldDef = testFieldDefinition.copy(description = "Select one or more options"))
    composeTestRule.onNodeWithText("Select one or more options").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_withConstraints_showsHints() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type =
                    FieldType.MultiSelect(
                        options = testOptions,
                        minSelections = 1,
                        maxSelections = 2,
                        allowCustom = true)))
    composeTestRule.onNodeWithText("3 options • Select 1-2 • Custom allowed").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_whenValidationEnabled_showsErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = true)
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    setFieldContent(
        fieldDef = testFieldDefinition.copy(required = true), showValidationErrors = false)
    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }

  @Test
  fun multiSelectFieldComponent_customValue_displayedInViewMode() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)),
        value = FieldValue.MultiSelectValue(listOf("option1", "Custom Value")),
        mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("option1")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("Custom Value")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom Value").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_minMaxConstraints_hintShowsRange() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type =
                    FieldType.MultiSelect(
                        options =
                            listOf(
                                SelectOption("tag1", "Tag 1"),
                                SelectOption("tag2", "Tag 2"),
                                SelectOption("tag3", "Tag 3"),
                                SelectOption("tag4", "Tag 4"),
                                SelectOption("tag5", "Tag 5")),
                        minSelections = 1,
                        maxSelections = 3)))
    composeTestRule.onNodeWithText("5 options • Select 1-3").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_customValueClearsAfterAdding() {
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)))
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .performTextInput("Test")
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .performClick()
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldComponent_customValueTrimsWhitespace() {
    var capturedValue: FieldValue.MultiSelectValue? = null
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)),
        onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .performTextInput("  Test Value  ")
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .performClick()

    assertNotNull(capturedValue)
    assertTrue(capturedValue?.values?.contains("Test Value") == true)
  }

  @Test
  fun multiSelectFieldComponent_customValuePreventsDuplicates() {
    var capturedValue: FieldValue.MultiSelectValue? =
        FieldValue.MultiSelectValue(listOf("Custom Value"))
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)),
        value = capturedValue,
        onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .performTextInput("Custom Value")
    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customAdd("test_multi_select"))
        .performClick()

    assertEquals(1, capturedValue?.values?.size)
  }

  @Test
  fun multiSelectFieldComponent_canRemoveCustomValue() {
    var capturedValue: FieldValue.MultiSelectValue? =
        FieldValue.MultiSelectValue(listOf("option1", "CustomValue"))
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)),
        value = capturedValue,
        onValueChange = { capturedValue = it })

    composeTestRule.onNodeWithTag(MultiSelectFieldTestTags.chip("CustomValue")).performClick()

    assertNotNull(capturedValue)
    assertEquals(1, capturedValue?.values?.size)
    assertTrue(capturedValue?.values?.contains("option1") == true)
    assertTrue(capturedValue?.values?.contains("CustomValue") == false)
  }

  @Test
  fun multiSelectFieldComponent_emptyCustomValueDoesNotAdd() {
    var capturedValue: FieldValue.MultiSelectValue? = null
    setFieldContent(
        fieldDef =
            testFieldDefinition.copy(
                type = FieldType.MultiSelect(options = testOptions, allowCustom = true)),
        onValueChange = { capturedValue = it })

    composeTestRule
        .onNodeWithTag(MultiSelectFieldTestTags.customInput("test_multi_select"))
        .performTextInput("   ")

    assertEquals(null, capturedValue)
  }

  @Test
  fun multiSelectFieldComponent_viewMode_emptyList_showsNoneText() {
    setFieldContent(
        value = FieldValue.MultiSelectValue(emptyList()), mode = FieldInteractionMode.ViewOnly)
    composeTestRule.onNodeWithText("None").assertIsDisplayed()
  }
}

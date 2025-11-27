// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TemplateFieldListItemTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testTextField =
      FieldDefinition(
          id = "test_text",
          label = "Test Text Field",
          type = FieldType.Text(),
          required = true,
          description = "Test description")

  private val testNumberField =
      FieldDefinition(
          id = "test_number",
          label = "Test Number Field",
          type = FieldType.Number(),
          required = false)

  private val testDateField =
      FieldDefinition(
          id = "test_date", label = "Test Date Field", type = FieldType.Date(), required = false)

  private val testSingleSelectField =
      FieldDefinition(
          id = "test_single",
          label = "Test Single Select",
          type =
              FieldType.SingleSelect(
                  options =
                      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
          required = false)

  private val testMultiSelectField =
      FieldDefinition(
          id = "test_multi",
          label = "Test Multi Select",
          type =
              FieldType.MultiSelect(
                  options =
                      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
          required = false)

  @Test
  fun collapsedState_displaysFieldLabelAndType() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Test Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Text").assertIsDisplayed()
  }

  @Test
  fun collapsedState_displaysExpandIcon() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Expand").assertIsDisplayed()
  }

  @Test
  fun collapsedState_displaysErrorIcon_whenErrorPresent() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = "Validation error",
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Error").assertIsDisplayed()
  }

  @Test
  fun collapsedState_doesNotDisplayErrorIcon_whenNoError() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Error").assertDoesNotExist()
  }

  @Test
  fun collapsedState_callsOnExpand_whenClicked() {
    var expandCalled = false
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = { expandCalled = true },
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Test Text Field").performClick()
    assertTrue(expandCalled)
  }

  @Test
  fun expandedState_displaysCommonFieldConfiguration() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_description_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysTextFieldConfiguration_forTextField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("text_max_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_min_length").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysNumberFieldConfiguration_forNumberField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testNumberField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("number_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_max").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysDateFieldConfiguration_forDateField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testDateField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("date_include_time").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysSingleSelectConfiguration_forSingleSelectField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testSingleSelectField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysMultiSelectConfiguration_forMultiSelectField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testMultiSelectField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("multi_select_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_max").assertIsDisplayed()
  }

  @Test
  fun expandedState_displaysActionButtons() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Save").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Duplicate").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
  }

  @Test
  fun expandedState_doesNotDisplayExpandIcon() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Expand").assertIsNotDisplayed()
  }

  @Test
  fun expandedState_saveButtonCallsOnFieldChangeAndOnSave() {
    var fieldChangeCalled = false
    var fieldChangeValue: FieldDefinition? = null
    var saveCalled = false

    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {
                    fieldChangeCalled = true
                    fieldChangeValue = it
                  },
                  onSave = { saveCalled = true },
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Save").performClick()
    assertTrue(fieldChangeCalled)
    assertTrue(saveCalled)
    assertEquals(testTextField, fieldChangeValue)
  }

  @Test
  fun expandedState_cancelButtonCallsOnCancel() {
    var cancelCalled = false
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = { cancelCalled = true },
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Cancel").performClick()
    assertTrue(cancelCalled)
  }

  @Test
  fun expandedState_deleteButtonCallsOnDelete() {
    var deleteCalled = false
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = { deleteCalled = true },
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Delete").performClick()
    assertTrue(deleteCalled)
  }

  @Test
  fun expandedState_duplicateButtonCallsOnDuplicate() {
    var duplicateCalled = false
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = { duplicateCalled = true })) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Duplicate").performClick()
    assertTrue(duplicateCalled)
  }

  @Test
  fun expandedState_doesNotCallOnExpand_whenHeaderClicked() {
    var expandCalled = false
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = { expandCalled = true },
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    // Header is not clickable when expanded
    assertTrue(!expandCalled)
  }

  @Test
  fun dragHandle_isDisplayed() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Drag").assertIsDisplayed()
  }

  @Test
  fun requiredField_displaysCorrectly() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField.copy(required = true),
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Test Text Field").assertIsDisplayed()
  }

  @Test
  fun nonRequiredField_displaysCorrectly() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField.copy(required = false),
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Test Text Field").assertIsDisplayed()
  }

  @Test
  fun fieldTypeIcon_displaysCorrectIcon_forTextField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Text").assertIsDisplayed()
  }

  @Test
  fun fieldTypeIcon_displaysCorrectIcon_forNumberField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testNumberField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Number").assertIsDisplayed()
  }

  @Test
  fun fieldTypeIcon_displaysCorrectIcon_forDateField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testDateField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Date").assertIsDisplayed()
  }

  @Test
  fun fieldTypeIcon_displaysCorrectIcon_forSingleSelectField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testSingleSelectField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Single Select").assertIsDisplayed()
  }

  @Test
  fun fieldTypeIcon_displaysCorrectIcon_forMultiSelectField() {
    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testMultiSelectField,
          isExpanded = false,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = {},
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithText("Multi Select").assertIsDisplayed()
  }

  @Test
  fun saveButton_callsOnFieldChangeBeforeOnSave() {
    val callOrder = mutableListOf<String>()

    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = { callOrder.add("fieldChange") },
                  onSave = { callOrder.add("save") },
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals(listOf("fieldChange", "save"), callOrder)
  }

  @Test
  fun editingLabel_updatesLocalFieldState_andPassesToCallback() {
    var capturedField: FieldDefinition? = null

    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = { capturedField = it },
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("field_label_input").performTextReplacement("Updated Label")
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals("Updated Label", capturedField?.label)
  }

  @Test
  fun editingDescription_updatesLocalFieldState_andPassesToCallback() {
    var capturedField: FieldDefinition? = null

    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField,
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = { capturedField = it },
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule
        .onNodeWithTag("field_description_input")
        .performTextReplacement("New description")
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertEquals("New description", capturedField?.description)
  }

  @Test
  fun togglingRequired_updatesLocalFieldState_andPassesToCallback() {
    var capturedField: FieldDefinition? = null

    composeTestRule.setContent {
      TemplateFieldListItem(
          field = testTextField.copy(required = false),
          isExpanded = true,
          error = null,
          callbacks =
              TemplateFieldCallbacks(
                  onExpand = {},
                  onFieldChange = { capturedField = it },
                  onSave = {},
                  onCancel = {},
                  onDelete = {},
                  onDuplicate = {})) {
            Icon(Icons.Default.DragHandle, "Drag")
          }
    }

    composeTestRule.onNodeWithTag("field_required_checkbox").performClick()
    composeTestRule.onNodeWithContentDescription("Save").performClick()

    assertTrue(capturedField?.required == true)
  }
}

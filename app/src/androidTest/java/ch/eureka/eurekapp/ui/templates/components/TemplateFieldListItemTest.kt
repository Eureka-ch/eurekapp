// Portions of this code were generated with the help of Claude Sonnet 4.5, and Grok.

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
  fun templateFieldListItem_collapsedStateDisplaysFieldLabelAndType() {
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
  fun templateFieldListItem_collapsedStateDisplaysExpandIcon() {
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
  fun templateFieldListItem_collapsedStateDisplaysErrorIconWhenErrorPresent() {
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
  fun templateFieldListItem_collapsedStateDoesNotDisplayErrorIconWhenNoError() {
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
  fun templateFieldListItem_collapsedStateCallsOnExpandWhenClicked() {
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
  fun templateFieldListItem_expandedStateDisplaysCommonFieldConfiguration() {
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
  fun templateFieldListItem_expandedStateDisplaysTextFieldConfigurationForTextField() {
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
  fun templateFieldListItem_expandedStateDisplaysNumberFieldConfigurationForNumberField() {
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
  fun templateFieldListItem_expandedStateDisplaysDateFieldConfigurationForDateField() {
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
  fun templateFieldListItem_expandedStateDisplaysSingleSelectConfigurationForSingleSelectField() {
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
  fun templateFieldListItem_expandedStateDisplaysMultiSelectConfigurationForMultiSelectField() {
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
  fun templateFieldListItem_expandedStateDisplaysActionButtons() {
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
  fun templateFieldListItem_expandedStateDoesNotDisplayExpandIcon() {
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
  fun templateFieldListItem_expandedStateSaveButtonCallsOnFieldChangeAndOnSave() {
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
  fun templateFieldListItem_expandedStateCancelButtonCallsOnCancel() {
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
  fun templateFieldListItem_expandedStateDeleteButtonCallsOnDelete() {
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
  fun templateFieldListItem_expandedStateDuplicateButtonCallsOnDuplicate() {
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
  fun templateFieldListItem_expandedStateDoesNotCallOnExpandWhenHeaderClicked() {
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
  fun templateFieldListItem_dragHandleIsDisplayed() {
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
  fun templateFieldListItem_requiredFieldDisplaysCorrectly() {
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
  fun templateFieldListItem_nonRequiredFieldDisplaysCorrectly() {
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
  fun templateFieldListItem_fieldTypeIconDisplaysCorrectIconForTextField() {
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
  fun templateFieldListItem_fieldTypeIconDisplaysCorrectIconForNumberField() {
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
  fun templateFieldListItem_fieldTypeIconDisplaysCorrectIconForDateField() {
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
  fun templateFieldListItem_fieldTypeIconDisplaysCorrectIconForSingleSelectField() {
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
  fun templateFieldListItem_fieldTypeIconDisplaysCorrectIconForMultiSelectField() {
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
  fun templateFieldListItem_saveButtonCallsOnFieldChangeBeforeOnSave() {
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
  fun templateFieldListItem_editingLabelUpdatesLocalFieldStateAndPassesToCallback() {
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
  fun templateFieldListItem_editingDescriptionUpdatesLocalFieldStateAndPassesToCallback() {
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
  fun templateFieldListItem_togglingRequiredUpdatesLocalFieldStateAndPassesToCallback() {
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

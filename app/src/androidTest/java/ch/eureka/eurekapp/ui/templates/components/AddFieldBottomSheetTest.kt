// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import org.junit.Rule
import org.junit.Test

class AddFieldBottomSheetTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun addFieldBottomSheet_displaysTypeSelector() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Select Field Type").assertIsDisplayed()
    composeTestRule.onNodeWithText("Text").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    composeTestRule.onNodeWithText("Single Select").assertIsDisplayed()
    composeTestRule.onNodeWithText("Multi Select").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_selectingTextTypeShowsFieldEditor() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Text").performClick()

    composeTestRule.onNodeWithText("Configure Field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_selectingNumberTypeShowsFieldEditor() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Number").performClick()

    composeTestRule.onNodeWithText("Configure Field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_selectingDateTypeShowsFieldEditor() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Date").performClick()

    composeTestRule.onNodeWithText("Configure Field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_selectingSingleSelectTypeShowsFieldEditor() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Single Select").performClick()

    composeTestRule.onNodeWithText("Configure Field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_selectingMultiSelectTypeShowsFieldEditor() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Multi Select").performClick()

    composeTestRule.onNodeWithText("Configure Field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_cancelButtonCallsOnDismiss() {
    var dismissCalled = false
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = { dismissCalled = true }, onFieldCreated = {})
    }

    composeTestRule.onNodeWithText("Text").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()

    assert(dismissCalled) { "onDismiss should be called when Cancel is clicked" }
  }

  @Test
  fun addFieldBottomSheet_addButtonCallsOnFieldCreated() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Text").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField != null) { "onFieldCreated should be called with a field" }
    assert(createdField?.label == "New Field") {
      "Created field should have default label 'New Field'"
    }
    assert(createdField?.type is FieldType.Text) { "Created field should be Text type" }
  }

  @Test
  fun addFieldBottomSheet_addButtonCallsOnDismiss() {
    var dismissCalled = false
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = { dismissCalled = true }, onFieldCreated = {})
    }

    composeTestRule.onNodeWithText("Number").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(dismissCalled) { "onDismiss should be called when Add is clicked" }
  }

  @Test
  fun addFieldBottomSheet_fieldEditorAllowsLabelEditing() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Text").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("field_label_input").performTextReplacement("Custom Label")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.label == "Custom Label") {
      "Created field should have custom label 'Custom Label'"
    }
  }

  @Test
  fun addFieldBottomSheet_fieldEditorAllowsDescriptionEditing() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Number").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("field_description_input").performTextInput("Test Description")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.description == "Test Description") {
      "Created field should have description 'Test Description'"
    }
  }

  @Test
  fun addFieldBottomSheet_fieldEditorAllowsRequiredToggle() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Date").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("field_required_checkbox").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.required == true) { "Created field should be marked as required" }
  }

  @Test
  fun addFieldBottomSheet_textFieldShowsSpecificConfiguration() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Text").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("text_max_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_min_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_placeholder").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_numberFieldShowsSpecificConfiguration() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Number").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("number_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_decimals").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_dateFieldShowsSpecificConfiguration() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Date").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("date_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_max").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_singleSelectFieldShowsSpecificConfiguration() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Single Select").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_multiSelectFieldShowsSpecificConfiguration() {
    composeTestRule.setContent { AddFieldBottomSheet(onDismiss = {}, onFieldCreated = {}) }

    composeTestRule.onNodeWithText("Multi Select").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("multi_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun addFieldBottomSheet_createsNumberFieldWithCorrectType() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Number").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.type is FieldType.Number) { "Created field should be Number type" }
  }

  @Test
  fun addFieldBottomSheet_createsDateFieldWithCorrectType() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Date").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.type is FieldType.Date) { "Created field should be Date type" }
  }

  @Test
  fun addFieldBottomSheet_createsSingleSelectFieldWithCorrectType() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Single Select").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.type is FieldType.SingleSelect) {
      "Created field should be SingleSelect type"
    }
  }

  @Test
  fun addFieldBottomSheet_createsMultiSelectFieldWithCorrectType() {
    var createdField: FieldDefinition? = null
    composeTestRule.setContent {
      AddFieldBottomSheet(onDismiss = {}, onFieldCreated = { createdField = it })
    }

    composeTestRule.onNodeWithText("Multi Select").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add").performClick()
    composeTestRule.waitForIdle()

    assert(createdField?.type is FieldType.MultiSelect) {
      "Created field should be MultiSelect type"
    }
  }
}

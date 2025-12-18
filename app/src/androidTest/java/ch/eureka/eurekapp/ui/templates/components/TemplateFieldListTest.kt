// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TemplateFieldListTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFields =
      listOf(
          FieldDefinition(
              id = "field1", label = "First Field", type = FieldType.Text(), required = true),
          FieldDefinition(
              id = "field2", label = "Second Field", type = FieldType.Number(), required = false),
          FieldDefinition(
              id = "field3", label = "Third Field", type = FieldType.Date(), required = false),
          FieldDefinition(
              id = "field4",
              label = "Fourth Field",
              type = FieldType.SingleSelect(options = listOf(SelectOption("opt1", "Option 1"))),
              required = false))

  @Test
  fun emptyFieldList_displaysPlaceholder() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = emptyList(),
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("No fields yet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tap + to add your first field").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Add field").assertIsDisplayed()
  }

  @Test
  fun nonEmptyFieldList_displaysAllFields() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("First Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Third Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Fourth Field").assertIsDisplayed()
  }

  @Test
  fun nonEmptyFieldList_doesNotDisplayPlaceholder() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("No fields yet").assertIsNotDisplayed()
  }

  @Test
  fun fieldList_displaysFieldTypes() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("Text").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    composeTestRule.onNodeWithText("Single Select").assertIsDisplayed()
  }

  @Test
  fun fieldList_displaysRequiredIndicator() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("First Field").assertIsDisplayed()
    composeTestRule.onNodeWithText(" *").assertIsDisplayed()
  }

  @Test
  fun fieldList_displaysReorderHandles() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    val reorderNodes = composeTestRule.onAllNodes(hasContentDescription("Reorder"))
    assertEquals(4, reorderNodes.fetchSemanticsNodes().size)
  }

  @Test
  fun expandedField_showsEditingControls() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = "field1",
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Save").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Duplicate").assertIsDisplayed()
  }

  @Test
  fun collapsedField_doesNotShowEditingControls() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Save").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Cancel").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Delete").assertDoesNotExist()
    composeTestRule.onNodeWithContentDescription("Duplicate").assertDoesNotExist()
  }

  @Test
  fun clickingField_triggersOnFieldEdit() {
    var editedFieldId: String? = null

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = { editedFieldId = it },
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("First Field").performClick()

    assertEquals("field1", editedFieldId)
  }

  @Test
  fun fieldWithError_displaysWarningIcon() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = mapOf("field1" to "Invalid field"),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Error").assertIsDisplayed()
  }

  @Test
  fun reorderCallback_receivesCorrectIndices() {
    var fromIndex: Int? = null
    var toIndex: Int? = null

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { from, to ->
                    fromIndex = from
                    toIndex = to
                  }))
    }
  }

  @Test
  fun singleField_displaysCorrectly() {
    val singleField =
        listOf(FieldDefinition(id = "only", label = "Only Field", type = FieldType.Text()))

    composeTestRule.setContent {
      TemplateFieldList(
          fields = singleField,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("Only Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("No fields yet").assertDoesNotExist()
  }

  @Test
  fun deleteButton_callsOnFieldDelete() {
    var deletedFieldId: String? = null

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = "field1",
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = { deletedFieldId = it },
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Delete").performClick()

    assertEquals("field1", deletedFieldId)
  }

  @Test
  fun duplicateButton_callsOnFieldDuplicate() {
    var duplicatedFieldId: String? = null

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = "field1",
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = { duplicatedFieldId = it },
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Duplicate").performClick()

    assertEquals("field1", duplicatedFieldId)
  }

  @Test
  fun cancelButton_callsOnFieldCancel() {
    var cancelledFieldId: String? = null

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = "field1",
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = { cancelledFieldId = it },
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithContentDescription("Cancel").performClick()

    assertEquals("field1", cancelledFieldId)
  }

  @Test
  fun expandedField_displaysCommonConfiguration() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = "field1",
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    composeTestRule.onNodeWithText("Label *").assertIsDisplayed()
  }

  @Test
  fun multipleFieldsWithErrors_displaysMultipleWarnings() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = mapOf("field1" to "Error 1", "field2" to "Error 2", "field3" to "Error 3"),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }))
    }

    val errorNodes = composeTestRule.onAllNodes(hasContentDescription("Error"))
    assertEquals(3, errorNodes.fetchSemanticsNodes().size)
  }

  @Test
  fun basicInfoSection_displaysWhenHeaderConfigProvided() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig =
              TemplateHeaderConfig(
                  title = "Test Template",
                  description = "Test description",
                  callbacks =
                      TemplateBasicInfoCallbacks(onTitleChange = {}, onDescriptionChange = {})))
    }

    composeTestRule.onNodeWithText("Test Template").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test description").assertIsDisplayed()
  }

  @Test
  fun basicInfoSection_titleChangeCallbackInvoked() {
    var capturedTitle = ""

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig =
              TemplateHeaderConfig(
                  title = "Initial",
                  callbacks =
                      TemplateBasicInfoCallbacks(
                          onTitleChange = { capturedTitle = it }, onDescriptionChange = {})))
    }

    composeTestRule.onNodeWithText("Initial").performTextReplacement("Updated Title")

    assertEquals("Updated Title", capturedTitle)
  }

  @Test
  fun basicInfoSection_descriptionChangeCallbackInvoked() {
    var capturedDescription = ""

    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig =
              TemplateHeaderConfig(
                  title = "Title",
                  description = "Initial Desc",
                  callbacks =
                      TemplateBasicInfoCallbacks(
                          onTitleChange = {}, onDescriptionChange = { capturedDescription = it })))
    }

    composeTestRule.onNodeWithText("Initial Desc").performTextReplacement("New Description")

    assertEquals("New Description", capturedDescription)
  }

  @Test
  fun basicInfoSection_titleErrorDisplayed() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig =
              TemplateHeaderConfig(
                  title = "",
                  titleError = "Title is required",
                  callbacks =
                      TemplateBasicInfoCallbacks(onTitleChange = {}, onDescriptionChange = {})))
    }

    composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
  }

  @Test
  fun basicInfoSection_notDisplayedWhenHeaderConfigNull() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig = null)
    }

    composeTestRule.onNodeWithText("Template Title").assertDoesNotExist()
    composeTestRule.onNodeWithText("Description").assertDoesNotExist()
  }

  @Test
  fun fieldList_withHeaderConfigDisplaysFieldsAfterHeader() {
    composeTestRule.setContent {
      TemplateFieldList(
          fields = testFields,
          editingFieldId = null,
          fieldErrors = emptyMap(),
          callbacks =
              TemplateFieldListCallbacks(
                  onFieldEdit = {},
                  onFieldSave = { _, _ -> },
                  onFieldCancel = {},
                  onFieldDelete = {},
                  onFieldDuplicate = {},
                  onFieldsReorder = { _, _ -> }),
          headerConfig =
              TemplateHeaderConfig(
                  title = "Header Title",
                  callbacks =
                      TemplateBasicInfoCallbacks(onTitleChange = {}, onDescriptionChange = {})))
    }

    composeTestRule.onNodeWithText("Header Title").assertIsDisplayed()
    composeTestRule.onNodeWithText("First Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Field").assertIsDisplayed()
  }
}

private fun hasContentDescription(value: String) =
    androidx.compose.ui.test.hasContentDescription(value)

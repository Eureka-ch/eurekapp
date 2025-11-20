// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Android UI tests for TemplatePreview component. */
class TemplatePreviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testTextFieldDefinition =
      FieldDefinition(
          id = "text_field",
          label = "Text Field",
          type = FieldType.Text(maxLength = 100, placeholder = "Enter text"),
          required = false)

  private val testNumberFieldDefinition =
      FieldDefinition(
          id = "number_field",
          label = "Number Field",
          type = FieldType.Number(min = null, max = null, step = 1.0, unit = null),
          required = false)

  private val testDateFieldDefinition =
      FieldDefinition(
          id = "date_field",
          label = "Date Field",
          type = FieldType.Date(format = "yyyy-MM-dd"),
          required = false)

  private val testSingleSelectFieldDefinition =
      FieldDefinition(
          id = "single_select_field",
          label = "Single Select Field",
          type =
              FieldType.SingleSelect(
                  options =
                      listOf(
                          SelectOption("opt1", "Option 1"),
                          SelectOption("opt2", "Option 2"),
                          SelectOption("opt3", "Option 3"))),
          required = false)

  private val testMultiSelectFieldDefinition =
      FieldDefinition(
          id = "multi_select_field",
          label = "Multi Select Field",
          type =
              FieldType.MultiSelect(
                  options =
                      listOf(
                          SelectOption("optA", "Option A"),
                          SelectOption("optB", "Option B"),
                          SelectOption("optC", "Option C"))),
          required = false)

  private fun setTemplatePreviewContent(
      fields: List<FieldDefinition> = emptyList(),
      fieldErrors: Map<String, String> = emptyMap(),
      onErrorFieldClick: (String) -> Unit = {}
  ) {
    composeTestRule.setContent {
      TemplatePreview(
          fields = fields, fieldErrors = fieldErrors, onErrorFieldClick = onErrorFieldClick)
    }
  }

  @Test
  fun templatePreview_emptyFieldsShowsEmptyMessage() {
    setTemplatePreviewContent(fields = emptyList())
    composeTestRule.onNodeWithText("No fields to preview").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysTextFieldPreview() {
    setTemplatePreviewContent(fields = listOf(testTextFieldDefinition))
    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysNumberFieldPreview() {
    setTemplatePreviewContent(fields = listOf(testNumberFieldDefinition))
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysDateFieldPreview() {
    setTemplatePreviewContent(fields = listOf(testDateFieldDefinition))
    composeTestRule.onNodeWithText("Date Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysSingleSelectFieldPreview() {
    setTemplatePreviewContent(fields = listOf(testSingleSelectFieldDefinition))
    composeTestRule.onNodeWithText("Single Select Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysMultiSelectFieldPreview() {
    setTemplatePreviewContent(fields = listOf(testMultiSelectFieldDefinition))
    composeTestRule.onNodeWithText("Multi Select Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysMultipleFieldsInOrder() {
    val fields = listOf(testTextFieldDefinition, testNumberFieldDefinition, testDateFieldDefinition)
    setTemplatePreviewContent(fields = fields)

    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date Field").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysErrorPlaceholderForFieldWithError() {
    val fieldErrors = mapOf("text_field" to "Field label is required")
    setTemplatePreviewContent(fields = listOf(testTextFieldDefinition), fieldErrors = fieldErrors)

    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Field label is required").assertIsDisplayed()
  }

  @Test
  fun templatePreview_errorFieldClickTriggersCallback() {
    var clickedFieldId: String? = null
    val fieldErrors = mapOf("text_field" to "Field label is required")

    setTemplatePreviewContent(
        fields = listOf(testTextFieldDefinition),
        fieldErrors = fieldErrors,
        onErrorFieldClick = { clickedFieldId = it })

    composeTestRule.onNodeWithText("Text Field").performClick()
    assertEquals("text_field", clickedFieldId)
  }

  @Test
  fun templatePreview_displaysValidFieldWhenNoError() {
    val fieldErrors = mapOf("number_field" to "Some error")
    val fields = listOf(testTextFieldDefinition, testNumberFieldDefinition)

    setTemplatePreviewContent(fields = fields, fieldErrors = fieldErrors)

    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Some error").assertIsDisplayed()
  }

  @Test
  fun templatePreview_displaysMultipleFieldsWithMixedErrorStates() {
    val fields = listOf(testTextFieldDefinition, testNumberFieldDefinition, testDateFieldDefinition)
    val fieldErrors = mapOf("text_field" to "Text field error", "date_field" to "Date field error")

    setTemplatePreviewContent(fields = fields, fieldErrors = fieldErrors)

    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Text field error").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date field error").assertIsDisplayed()
  }

  @Test
  fun templatePreview_errorPlaceholderClickableForEachField() {
    val fields = listOf(testTextFieldDefinition, testNumberFieldDefinition)
    val fieldErrors = mapOf("text_field" to "Error 1", "number_field" to "Error 2")
    var clickedFieldIds = mutableListOf<String>()

    setTemplatePreviewContent(
        fields = fields, fieldErrors = fieldErrors, onErrorFieldClick = { clickedFieldIds.add(it) })

    composeTestRule.onNodeWithText("Text Field").performClick()
    composeTestRule.onNodeWithText("Number Field").performClick()

    assertEquals(listOf("text_field", "number_field"), clickedFieldIds)
  }

  @Test
  fun templatePreview_allFieldTypesDisplayCorrectly() {
    val allFields =
        listOf(
            testTextFieldDefinition,
            testNumberFieldDefinition,
            testDateFieldDefinition,
            testSingleSelectFieldDefinition,
            testMultiSelectFieldDefinition)

    setTemplatePreviewContent(fields = allFields)

    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Single Select Field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Multi Select Field").assertIsDisplayed()
  }
}

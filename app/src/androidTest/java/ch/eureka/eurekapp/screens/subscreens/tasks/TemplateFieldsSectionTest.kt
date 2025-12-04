package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Rule
import org.junit.Test

class TemplateFieldsSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun returnsEarlyWhenTemplateIsNull() {
    composeTestRule.setContent {
      TemplateFieldsSection(
          template = null, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag(TemplateFieldsSectionTestTags.SECTION).assertDoesNotExist()
  }

  @Test
  fun returnsEarlyWhenFieldsEmpty() {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Empty Template",
            definedFields = TaskTemplateSchema(emptyList()))

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag(TemplateFieldsSectionTestTags.SECTION).assertDoesNotExist()
  }

  @Test
  fun displaysSectionTitleWhenTemplateHasFields() {
    val template = createTemplateWithTextField()

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule.onNodeWithText("Template Fields").assertIsDisplayed()
    composeTestRule.onNodeWithTag(TemplateFieldsSectionTestTags.SECTION).assertIsDisplayed()
  }

  @Test
  fun rendersTextFieldCorrectly() {
    val template = createTemplateWithTextField()

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule
        .onNodeWithTag(TemplateFieldsSectionTestTags.field("text-field-1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Text Field").assertIsDisplayed()
  }

  @Test
  fun rendersNumberFieldCorrectly() {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Number Template",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "number-field-1",
                            label = "Number Field",
                            type = FieldType.Number()))))

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule
        .onNodeWithTag(TemplateFieldsSectionTestTags.field("number-field-1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Number Field").assertIsDisplayed()
  }

  @Test
  fun rendersDateFieldCorrectly() {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Date Template",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "date-field-1", label = "Date Field", type = FieldType.Date()))))

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule
        .onNodeWithTag(TemplateFieldsSectionTestTags.field("date-field-1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Date Field").assertIsDisplayed()
  }

  @Test
  fun rendersSingleSelectCorrectly() {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Single Select Template",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "single-select-1",
                            label = "Single Select Field",
                            type =
                                FieldType.SingleSelect(
                                    options =
                                        listOf(
                                            SelectOption(value = "opt1", label = "Option 1"),
                                            SelectOption(value = "opt2", label = "Option 2")))))))

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule
        .onNodeWithTag(TemplateFieldsSectionTestTags.field("single-select-1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Single Select Field").assertIsDisplayed()
  }

  @Test
  fun rendersMultiSelectCorrectly() {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Multi Select Template",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "multi-select-1",
                            label = "Multi Select Field",
                            type =
                                FieldType.MultiSelect(
                                    options =
                                        listOf(
                                            SelectOption(value = "opt1", label = "Option 1"),
                                            SelectOption(value = "opt2", label = "Option 2")))))))

    composeTestRule.setContent {
      TemplateFieldsSection(
          template = template, customData = TaskCustomData(), onFieldValueChange = { _, _ -> })
    }

    composeTestRule
        .onNodeWithTag(TemplateFieldsSectionTestTags.field("multi-select-1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Multi Select Field").assertIsDisplayed()
  }

  private fun createTemplateWithTextField() =
      TaskTemplate(
          templateID = "t1",
          title = "Text Template",
          definedFields =
              TaskTemplateSchema(
                  listOf(
                      FieldDefinition(
                          id = "text-field-1", label = "Text Field", type = FieldType.Text()))))
}

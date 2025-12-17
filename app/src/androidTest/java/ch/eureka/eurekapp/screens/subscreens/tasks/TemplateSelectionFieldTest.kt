/*
 * Co-Authored-By: Claude Sonnet 4.5
 * Co-Authored-By: Grok
 */
package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TemplateSelectionFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testTemplates =
      listOf(
          TaskTemplate(templateID = "template-1", title = "Bug Report"),
          TaskTemplate(templateID = "template-2", title = "Feature Request"),
          TaskTemplate(templateID = "template-3", title = "Documentation"))

  @Test
  fun templateSelectionField_displaysDropdownWhenTemplatesExist() {
    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = {})
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.DROPDOWN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun templateSelectionField_hidesDropdownWhenNoTemplatesExist() {
    composeTestRule.setContent {
      TemplateSelectionField(
          templates = emptyList(),
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = {})
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.DROPDOWN).assertDoesNotExist()
    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun templateSelectionField_displaysSelectedTemplateTitle() {
    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = "template-2",
          onTemplateSelected = {},
          onCreateTemplate = {})
    }

    composeTestRule
        .onNodeWithTag(TemplateSelectionTestTags.DROPDOWN)
        .assertTextEquals("Feature Request")
  }

  @Test
  fun templateSelectionField_displaysNoTemplateWhenNoneSelected() {
    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = {})
    }

    composeTestRule
        .onNodeWithTag(TemplateSelectionTestTags.DROPDOWN)
        .assertTextEquals("No template")
  }

  @Test
  fun templateSelectionField_callsOnTemplateSelectedWhenTemplateClicked() {
    var selectedId: String? = "initial"

    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = null,
          onTemplateSelected = { selectedId = it },
          onCreateTemplate = {})
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.DROPDOWN).performClick()
    composeTestRule
        .onNodeWithTag(TemplateSelectionTestTags.templateOptionTag("template-1"))
        .performClick()

    assertEquals("template-1", selectedId)
  }

  @Test
  fun templateSelectionField_callsOnTemplateSelectedWithNullWhenNoTemplateClicked() {
    var selectedId: String? = "initial"

    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = "template-1",
          onTemplateSelected = { selectedId = it },
          onCreateTemplate = {})
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.DROPDOWN).performClick()
    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.NO_TEMPLATE_OPTION).performClick()

    assertNull(selectedId)
  }

  @Test
  fun templateSelectionField_callsOnCreateTemplateWhenButtonClicked() {
    var createCalled = false

    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = { createCalled = true })
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).performClick()

    assertEquals(true, createCalled)
  }

  @Test
  fun templateSelectionField_showsCreateButtonEvenWhenNoTemplates() {
    var createCalled = false

    composeTestRule.setContent {
      TemplateSelectionField(
          templates = emptyList(),
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = { createCalled = true })
    }

    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).performClick()

    assertEquals(true, createCalled)
  }

  @Test
  fun templateSelectionField_displaysFieldLabel() {
    composeTestRule.setContent {
      TemplateSelectionField(
          templates = testTemplates,
          selectedTemplateId = null,
          onTemplateSelected = {},
          onCreateTemplate = {})
    }

    composeTestRule.onNodeWithText("Use Template").assertIsDisplayed()
  }
}

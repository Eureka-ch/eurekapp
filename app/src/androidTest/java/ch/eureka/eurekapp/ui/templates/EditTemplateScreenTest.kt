// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.ui.templates

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class EditTemplateScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createFakeRepository(template: TaskTemplate? = null) =
      object : TaskTemplateRepository {
        private val templateFlow = MutableStateFlow(template)

        override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
            templateFlow

        override fun getTemplatesInProject(projectId: String) =
            MutableStateFlow(emptyList<TaskTemplate>())

        override suspend fun createTemplate(template: TaskTemplate) =
            Result.success(template.templateID)

        override suspend fun updateTemplate(template: TaskTemplate) = Result.success(Unit)

        override suspend fun deleteTemplate(projectId: String, templateId: String) =
            Result.success(Unit)
      }

  @Test
  fun editTemplateScreen_displaysCorrectTitle() {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields =
                TaskTemplateSchema(
                    listOf(FieldDefinition(id = "f1", label = "Name", type = FieldType.Text()))),
            createdBy = "")
    val viewModel = EditTemplateViewModel(createFakeRepository(template), "p1", "t1")

    composeTestRule.setContent {
      EditTemplateScreen(onNavigateBack = {}, onTemplateSaved = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Edit Template").assertIsDisplayed()
  }

  @Test
  fun editTemplateScreen_showsLoadingOrErrorWhenNoTemplate() {
    val viewModel = EditTemplateViewModel(createFakeRepository(null), "p1", "t1")

    composeTestRule.setContent {
      EditTemplateScreen(onNavigateBack = {}, onTemplateSaved = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Template not found").assertIsDisplayed()
  }

  @Test
  fun editTemplateScreen_displaysBackButton() {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields =
                TaskTemplateSchema(
                    listOf(FieldDefinition(id = "f1", label = "Name", type = FieldType.Text()))),
            createdBy = "")
    val viewModel = EditTemplateViewModel(createFakeRepository(template), "p1", "t1")

    composeTestRule.setContent {
      EditTemplateScreen(onNavigateBack = {}, onTemplateSaved = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
  }

  @Test
  fun editTemplateScreen_backButtonTriggersCallback() {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields =
                TaskTemplateSchema(
                    listOf(FieldDefinition(id = "f1", label = "Name", type = FieldType.Text()))),
            createdBy = "")
    val viewModel = EditTemplateViewModel(createFakeRepository(template), "p1", "t1")
    var backPressed = false

    composeTestRule.setContent {
      EditTemplateScreen(
          onNavigateBack = { backPressed = true }, onTemplateSaved = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

    assert(backPressed)
  }

  @Test
  fun editTemplateScreen_displaysTabs() {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields =
                TaskTemplateSchema(
                    listOf(FieldDefinition(id = "f1", label = "Name", type = FieldType.Text()))),
            createdBy = "")
    val viewModel = EditTemplateViewModel(createFakeRepository(template), "p1", "t1")

    composeTestRule.setContent {
      EditTemplateScreen(onNavigateBack = {}, onTemplateSaved = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Configure").assertIsDisplayed()
    composeTestRule.onNodeWithText("Preview").assertIsDisplayed()
  }
}

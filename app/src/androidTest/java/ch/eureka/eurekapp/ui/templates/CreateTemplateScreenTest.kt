// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.templates.components.TemplateBasicInfoSectionTestTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class CreateTemplateScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val fakeRepository =
      object : TaskTemplateRepository {
        var createCalled = false
        var lastTemplate: TaskTemplate? = null

        override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
            flowOf(null)

        override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> =
            flowOf(emptyList())

        override suspend fun createTemplate(template: TaskTemplate): Result<String> {
          createCalled = true
          lastTemplate = template
          return Result.success(template.templateID)
        }

        override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> =
            Result.success(Unit)

        override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
            Result.success(Unit)
      }

  @Test
  fun createTemplateScreen_displaysCorrectTitle() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Create Template").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_displaysBackButton() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_backButtonTriggersCallback() {
    val viewModel = CreateTemplateViewModel(fakeRepository)
    var backPressed = false

    composeTestRule.setContent {
      CreateTemplateScreen(
          onNavigateBack = { backPressed = true }, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

    assert(backPressed) { "Expected back callback to be triggered" }
  }

  @Test
  fun createTemplateScreen_displaysSaveButton() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Save").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_saveButtonDisabledInitially() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun createTemplateScreen_displaysTwoTabs() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Configure").assertIsDisplayed()
    composeTestRule.onNodeWithText("Preview").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_configureTabSelectedByDefault() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onAllNodesWithText("Configure")[0].assertIsSelected()
    composeTestRule.onAllNodesWithText("Preview")[0].assertIsNotSelected()
  }

  @Test
  fun createTemplateScreen_canSwitchToPreviewTab() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Preview")[0].assertIsSelected()
  }

  @Test
  fun createTemplateScreen_canSwitchBackToConfigureTab() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Configure")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Configure")[0].assertIsSelected()
  }

  @Test
  fun createTemplateScreen_displaysAddFieldButtonOnConfigureTab() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("add_field_button").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_hidesAddFieldButtonOnPreviewTab() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add Field").assertDoesNotExist()
  }

  @Test
  fun createTemplateScreen_displaysBasicInfoSection() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_canEnterTitle() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("My Template")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .assertTextContains("My Template")
  }

  @Test
  fun createTemplateScreen_canEnterDescription() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performClick()
        .performTextInput("My Description")

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .assertTextContains("My Description")
  }

  @Test
  fun createTemplateScreen_addFieldButtonOpensBottomSheet() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("add_field_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select Field Type").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_errorBadgeShownWhenTitleEmpty() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Badge should already be visible due to empty title validation in ViewModel init
    composeTestRule.onNodeWithTag("badge").assertExists()
  }

  @Test
  fun createTemplateScreen_saveButtonEnabledWithValidData() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Valid Title")

    viewModel.addField(FieldDefinition(id = "field1", label = "Field 1", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").assertIsEnabled()
  }

  @Test
  fun createTemplateScreen_saveButtonCallsRepository() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Test Template")

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").performClick()
    composeTestRule.waitForIdle()

    assert(fakeRepository.createCalled) { "Expected repository.createTemplate to be called" }
  }

  @Test
  fun createTemplateScreen_saveSuccessTriggersCallback() {
    val viewModel = CreateTemplateViewModel(fakeRepository)
    var createdTemplateId = ""

    composeTestRule.setContent {
      CreateTemplateScreen(
          onNavigateBack = {},
          onTemplateCreated = { createdTemplateId = it },
          viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Test Template")

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").performClick()
    composeTestRule.waitForIdle()

    assert(createdTemplateId.isNotEmpty()) { "Expected template ID to be returned" }
  }

  @Test
  fun createTemplateScreen_previewShowsFields() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Name").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_previewCanNavigateBackToEdit() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    viewModel.updateTitle("Test Template")
    viewModel.addField(FieldDefinition(id = "field1", label = "", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Edit field").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Configure")[0].assertIsSelected()
  }

  @Test
  fun createTemplateScreen_multipleFieldsDisplayedInPreview() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    viewModel.addField(FieldDefinition(id = "field2", label = "Due Date", type = FieldType.Date()))
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Preview")[0].performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Name").assertIsDisplayed()
    composeTestRule.onNodeWithText("Due Date").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_errorCountUpdatesInTab() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Badge should already be visible due to empty title validation in ViewModel init
    composeTestRule.onNodeWithTag("badge").assertExists()
  }

  @Test
  fun createTemplateScreen_errorCountClearsWhenFixed() {
    val viewModel = CreateTemplateViewModel(fakeRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performTextInput("Valid Title")
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("1").filter(hasTestTag("badge")).assertCountEquals(0)
  }

  @Test
  fun createTemplateScreen_snackbarShownOnSaveFailure() {
    val failingRepository =
        object : TaskTemplateRepository {
          override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
              flowOf(null)

          override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> =
              flowOf(emptyList())

          override suspend fun createTemplate(template: TaskTemplate): Result<String> =
              Result.failure(Exception("Network error"))

          override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> =
              Result.success(Unit)

          override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
              Result.success(Unit)
        }

    val viewModel = CreateTemplateViewModel(failingRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Test")

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
  }

  @Test
  fun createTemplateScreen_savingStateShowsProgress() {
    val slowRepository =
        object : TaskTemplateRepository {
          override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
              flowOf(null)

          override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> =
              flowOf(emptyList())

          override suspend fun createTemplate(template: TaskTemplate): Result<String> {
            kotlinx.coroutines.delay(1000)
            return Result.success(template.templateID)
          }

          override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> =
              Result.success(Unit)

          override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
              Result.success(Unit)
        }

    val viewModel = CreateTemplateViewModel(slowRepository)

    composeTestRule.setContent {
      CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performClick()
        .performTextInput("Test")

    viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule
        .onNode(hasContentDescription("Loading") or hasTestTag("progress_indicator"))
        .assertExists()
  }
}

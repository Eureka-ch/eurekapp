package ch.eureka.eurekapp.screen.createprojectscreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreenTestTags
import org.junit.Rule
import org.junit.Test

class CreateProjectScreenTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun testTextInputFieldsInCreateProjectScreenTest() {
    composeRule.setContent { CreateProjectScreen() }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.OPEN))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.ARCHIVED))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.COMPLETED))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.IN_PROGRESS))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CHECKBOX_LINK_GITHUB_REPOSITORY)
        .performClick()
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CHECKBOX_ENABLE_GOOGLE_DRIVE_FOLDER_TEST_TAG)
        .performClick()
  }

  @Test
  fun testDatePicker() {
    composeRule.setContent { CreateProjectScreen() }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CALENDAR_ICON_BUTTON_START)
        .performClick()
        .assertIsDisplayed()
    composeRule.waitForIdle()
  }

  @Test fun createProject() {}
}

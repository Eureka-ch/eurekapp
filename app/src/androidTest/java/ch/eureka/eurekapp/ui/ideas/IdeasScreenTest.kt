/* Portions of this file were written with the help of GPT-5 Codex, Gemini, and Claude. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import org.junit.Rule
import org.junit.Test

class IdeasScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val testProject = Project(projectId = "p1", name = "Test Project")
  private val testIdea =
      Idea(ideaId = "i1", projectId = "p1", createdBy = "user1", title = "Test Idea")

  private fun setContentWithMockViewModel(
      projects: List<Project> = listOf(testProject),
      selectedProject: Project? = null,
      ideas: List<Idea> = emptyList(),
      viewMode: IdeasViewMode = IdeasViewMode.LIST,
      isLoading: Boolean = false
  ): MockIdeasViewModel {
    val mockViewModel = MockIdeasViewModel()
    mockViewModel.setAvailableProjects(projects)
    mockViewModel.setSelectedProject(selectedProject)
    mockViewModel.setIdeas(ideas)
    mockViewModel.setViewMode(viewMode)
    mockViewModel.setIsLoading(isLoading)
    composeTestRule.setContent { IdeasScreen(viewModel = mockViewModel) }
    return mockViewModel
  }

  @Test
  fun ideasScreen_displaysScreen() {
    setContentWithMockViewModel()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun ideasScreen_displaysProjectSelector() {
    setContentWithMockViewModel()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.PROJECT_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun ideasScreen_projectSelectorShowsSelectedProject() {
    setContentWithMockViewModel(selectedProject = testProject)
    composeTestRule.onNodeWithText("Test Project").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_projectSelectorShowsPlaceholderWhenNoSelection() {
    setContentWithMockViewModel(selectedProject = null)
    composeTestRule.onNodeWithText("Select a project").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_fabDisplayedInListMode() {
    setContentWithMockViewModel(selectedProject = testProject, viewMode = IdeasViewMode.LIST)
    composeTestRule.onNodeWithTag("createIdeaButton").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_fabNotDisplayedInConversationMode() {
    setContentWithMockViewModel(
        selectedProject = testProject, viewMode = IdeasViewMode.CONVERSATION)
    composeTestRule.onNodeWithTag("createIdeaButton").assertDoesNotExist()
  }

  @Test
  fun ideasScreen_fabClickOpensCreateDialog() {
    setContentWithMockViewModel(selectedProject = testProject, viewMode = IdeasViewMode.LIST)
    composeTestRule.onNodeWithTag("createIdeaButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Create New Idea").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_projectDropdownDisplaysProjects() {
    val projects =
        listOf(
            Project(projectId = "p1", name = "Project 1"),
            Project(projectId = "p2", name = "Project 2"))
    setContentWithMockViewModel(projects = projects)

    composeTestRule.onNodeWithTag(IdeasScreenTestTags.PROJECT_SELECTOR).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Project 2").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_projectDropdownWithNoProjectsShowsMessage() {
    setContentWithMockViewModel(projects = emptyList())
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.PROJECT_SELECTOR).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No projects available").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_loadingShowsLoadingIndicator() {
    setContentWithMockViewModel(selectedProject = testProject, isLoading = true)
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun ideasScreen_withIdeasDisplaysIdeasList() {
    setContentWithMockViewModel(
        selectedProject = testProject, ideas = listOf(testIdea), viewMode = IdeasViewMode.LIST)
    composeTestRule.onNodeWithTag("ideasList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
  }
}

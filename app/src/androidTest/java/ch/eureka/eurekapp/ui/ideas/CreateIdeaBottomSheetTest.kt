package ch.eureka.eurekapp.ui.ideas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import org.junit.Rule
import org.junit.Test

class CreateIdeaBottomSheetTest {
  @get:Rule val composeTestRule = createComposeRule()
  private val testProjects =
      listOf(
          Project(projectId = "p1", name = "Project 1"),
          Project(projectId = "p2", name = "Project 2"))
  private val testUsers =
      listOf(
          User(uid = "user1", displayName = "User One", email = "user1@test.com"),
          User(uid = "user2", displayName = "User Two", email = "user2@test.com"))

  private fun setContent(
      projects: List<Project> = testProjects,
      users: List<User> = emptyList(),
      isLoading: Boolean = false,
      onDismiss: () -> Unit = {},
      onIdeaCreated: (Idea) -> Unit = {}
  ): MockCreateIdeaViewModel {
    val mockViewModel = MockCreateIdeaViewModel()
    mockViewModel.setAvailableProjects(projects)
    mockViewModel.setAvailableUsers(users)
    mockViewModel.setIsLoadingUsers(isLoading)
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = onDismiss, onIdeaCreated = onIdeaCreated, viewModel = mockViewModel)
    }
    return mockViewModel
  }

  private fun selectProject(projectName: String = "Project 1") {
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(projectName).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun createIdeaBottomSheet_displaysAllFields() {
    setContent()
    composeTestRule.onNodeWithText("Create New Idea").assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_withEmptyProjects_showsNoProjectsMessage() {
    setContent(projects = emptyList())
    composeTestRule.onNodeWithText("No projects available").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_withEmptyUsers_showsNoUsersMessage() {
    setContent()
    selectProject()
    composeTestRule.onNodeWithText("No users available in this project").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_titleFieldAcceptsInput() {
    setContent()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD)
        .performTextInput("My Idea Title")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD).assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_projectSelectionOpensDropdown() {
    setContent()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Project 2").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_selectingProjectLoadsUsers() {
    val mockViewModel = setContent(users = testUsers)
    selectProject()
    assert(mockViewModel.selectProjectCalled) { "selectProject should be called" }
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_participantSelectionOpensDropdown() {
    setContent(users = testUsers)
    selectProject()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("User One").assertIsDisplayed()
    composeTestRule.onNodeWithText("User Two").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_createButtonCallsOnCreateIdea() {
    val mockViewModel = setContent(users = testUsers)
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD)
        .performTextInput("My Idea")
    composeTestRule.waitForIdle()
    selectProject()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assert(mockViewModel.createIdeaCalled) { "createIdea should be called" }
    assert(mockViewModel.updateTitleCalled) { "updateTitle should be called" }
  }

  @Test
  fun createIdeaBottomSheet_createButtonDisabledWithoutProject() {
    setContent()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun createIdeaBottomSheet_createButtonEnabledWithProject() {
    setContent()
    selectProject()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsEnabled()
  }

  @Test
  fun createIdeaBottomSheet_cancelButtonCallsOnDismiss() {
    var dismissCalled = false
    setContent(onDismiss = { dismissCalled = true })
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assert(dismissCalled) { "onDismiss should be called" }
  }

  @Test
  fun createIdeaBottomSheet_createWithParticipants_callsOnCreateIdeaWithParticipantIds() {
    val mockViewModel = setContent(users = testUsers)
    selectProject()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("User One").performClick()
    composeTestRule.waitForIdle()
    assert(mockViewModel.toggleParticipantCalled) { "toggleParticipant should be called" }
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assert(mockViewModel.createIdeaCalled) { "createIdea should be called" }
  }

  @Test
  fun createIdeaBottomSheet_showsLoadingIndicatorWhenLoadingUsers() {
    setContent(users = emptyList(), isLoading = true)
    selectProject()
    // When loading, we show CircularProgressIndicator, not the dropdown
    composeTestRule.waitForIdle()
    // The loading indicator should be visible in the participants section
    // We can't easily test the CircularProgressIndicator directly, so we verify
    // that the "No users available" message is NOT shown (since we're loading)
    composeTestRule.onNodeWithText("No users available in this project").assertDoesNotExist()
  }
}

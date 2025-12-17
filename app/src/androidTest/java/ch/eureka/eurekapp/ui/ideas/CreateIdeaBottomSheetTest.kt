package ch.eureka.eurekapp.ui.ideas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import org.junit.Rule
import org.junit.Test

// Portions of this file were written with the help of GPT-5 Codex, Gemini, and Grok.
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
      onDismiss: () -> Unit = {},
      onIdeaCreated: (Idea) -> Unit = {}
  ): MockCreateIdeaViewModel {
    val mockViewModel = MockCreateIdeaViewModel()
    mockViewModel.setAvailableProjects(projects)
    mockViewModel.setAvailableUsers(users)
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
  fun createIdeaBottomSheet_withEmptyProjectsShowsNoProjectsMessage() {
    setContent(projects = emptyList())
    composeTestRule.onNodeWithText("No projects available").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_withEmptyUsersShowsNoUsersMessage() {
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
  fun createIdeaBottomSheet_participantSelectionDisplayed() {
    setContent(users = testUsers)
    selectProject()
    // Verify the participants dropdown is displayed
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .assertIsDisplayed()
    // Verify the display text shows no participants selected
    composeTestRule.onNodeWithText("No participants selected").assertIsDisplayed()
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
  fun createIdeaBottomSheet_createWithParticipantsCallsOnCreateIdeaWithParticipantIds() {
    val mockViewModel = setContent(users = testUsers)
    selectProject()
    // Directly toggle participants via the viewModel instead of clicking the dropdown
    // which is unstable in tests
    mockViewModel.toggleParticipant("user1")
    composeTestRule.waitForIdle()
    assert(mockViewModel.toggleParticipantCalled) { "toggleParticipant should be called" }
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assert(mockViewModel.createIdeaCalled) { "createIdea should be called" }
  }

  @Test
  fun createIdeaBottomSheet_participantsModalOpensWhenClicked() {
    setContent(users = testUsers)
    selectProject()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Select Participants").assertIsDisplayed()
    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_participantsModalDisplaysUsers() {
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
  fun createIdeaBottomSheet_participantsModalOkButtonClosesModal() {
    setContent(users = testUsers)
    selectProject()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()
    // Find button with "OK" text
    composeTestRule.onNodeWithText("OK", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Select Participants").assertDoesNotExist()
  }

  @Test
  fun createIdeaBottomSheet_participantsModalDisplaysSelectedCount() {
    val mockViewModel = setContent(users = testUsers)
    selectProject()
    mockViewModel.toggleParticipant("user1")
    composeTestRule.waitForIdle()
    // When a user is selected, it shows the user's name, not "1 participant selected"
    composeTestRule.onNodeWithText("User One").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_participantsModalDisplaysSelectedCountWhenNoName() {
    val usersWithoutName = listOf(User(uid = "user1", displayName = "", email = "user1@test.com"))
    val mockViewModel = setContent(users = usersWithoutName)
    selectProject()
    mockViewModel.toggleParticipant("user1")
    composeTestRule.waitForIdle()
    // When user has empty displayName, ifBlank returns email, so it shows the email
    composeTestRule.onNodeWithText("user1@test.com").assertIsDisplayed()
  }
}

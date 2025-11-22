package ch.eureka.eurekapp.ui.conversation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * UI tests for CreateConversationScreen.
 *
 * Tests verify screen layout, dropdown behavior, and button states.
 */
@RunWith(AndroidJUnit4::class)
class CreateConversationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @Test
  fun createConversationScreen_displaysTitle() {
    // Arrange: Set up screen with default state
    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel())
    }

    // Assert: Screen title is displayed
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("New Conversation").assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_displaysProjectDropdown() {
    // Arrange: State with available projects
    val state = CreateConversationState(projects = listOf(Project(projectId = "p1", name = "Proj")))

    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel(state))
    }

    // Assert: Project dropdown is visible for selection
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN).assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_memberDropdownHidden_whenNoProjectSelected() {
    // Arrange: No project selected yet
    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel())
    }

    // Assert: Member dropdown should not appear until project is selected
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN).assertDoesNotExist()
  }

  @Test
  fun createConversationScreen_memberDropdownShown_whenProjectSelected() {
    // Arrange: Project selected with available members
    val project = Project(projectId = "p1", name = "Test Project")
    val members =
        listOf(MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John")))
    val state = CreateConversationState(selectedProject = project, members = members)

    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel(state))
    }

    // Assert: Member dropdown appears after project selection
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN).assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_createButtonDisabled_whenNoSelection() {
    // Arrange: Neither project nor member selected
    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel())
    }

    // Assert: Create button should be disabled until both are selected
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun createConversationScreen_createButtonEnabled_whenBothSelected() {
    // Arrange: Both project and member selected, online
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state =
        CreateConversationState(
            selectedProject = project,
            selectedMember = member,
            members = listOf(member),
            isConnected = true)

    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel(state))
    }

    // Assert: Create button is enabled when ready
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).assertIsEnabled()
  }

  @Test
  fun createConversationScreen_showsNoMembersMessage_whenEmpty() {
    // Arrange: Project selected but no other members in project
    val project = Project(projectId = "p1", name = "Test Project")
    val state =
        CreateConversationState(selectedProject = project, members = emptyList(), isLoadingMembers = false)

    composeTestRule.setContent {
      CreateConversationScreen(
          onConversationCreated = {},
          onNavigateBack = {},
          viewModel = createMockViewModel(state))
    }

    // Assert: User sees message explaining no members available
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.NO_MEMBERS_MESSAGE).assertIsDisplayed()
  }

  /** Helper to create a mock ViewModel with predefined state */
  private fun createMockViewModel(
      state: CreateConversationState = CreateConversationState()
  ): CreateConversationViewModel {
    return object : CreateConversationViewModel() {
      override val uiState = MutableStateFlow(state)
    }
  }
}

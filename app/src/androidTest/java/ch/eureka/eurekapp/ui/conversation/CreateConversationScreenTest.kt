/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

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
import ch.eureka.eurekapp.ui.components.ProjectDropDownMenuTestTag
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
  fun createConversationScreen_memberDropdownShownWhenProjectSelected() {
    // Arrange: Project selected with available members
    val project = Project(projectId = "p1", name = "Test Project")
    val members =
        listOf(MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John")))
    val state = CreateConversationState(selectedProject = project, members = members)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    // Assert: Member dropdown appears after project selection
    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN)
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_createButtonEnabledWhenBothSelected() {
    // Arrange: Both project and member selected, online
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state =
        CreateConversationState(
            selectedProject = project,
            selectedMembers = listOf(member),
            members = listOf(member),
            isConnected = true)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    // Assert: Create button is enabled when ready
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).assertIsEnabled()
  }

  @Test
  fun createConversationScreen_showsNoMembersMessageWhenEmpty() {
    // Arrange: Project selected but no other members in project
    val project = Project(projectId = "p1", name = "Test Project")
    val state =
        CreateConversationState(
            selectedProject = project, members = emptyList(), isLoadingMembers = false)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    // Assert: User sees message explaining no members available
    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.NO_MEMBERS_MESSAGE)
        .assertIsDisplayed()
  }

  /** Helper to create a mock ViewModel with predefined state */
  private fun createMockViewModel(
      state: CreateConversationState = CreateConversationState()
  ): CreateConversationViewModel {
    return object : CreateConversationViewModel() {
      override val uiState = MutableStateFlow(state)
    }
  }

  @Test
  fun createConversationScreen_displaysTitle() {
    composeTestRule.setContent {
      CreateConversationScreen(onNavigateToConversation = {}, viewModel = createMockViewModel())
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.TITLE).assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_displaysProjectDropdown() {
    composeTestRule.setContent {
      CreateConversationScreen(onNavigateToConversation = {}, viewModel = createMockViewModel())
    }

    composeTestRule
        .onNodeWithTag(ProjectDropDownMenuTestTag.PROJECT_DROPDOWN_MENU)
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_showsLoadingIndicatorWhenLoadingProjects() {
    val state = CreateConversationState(isLoadingProjects = true)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(ProjectDropDownMenuTestTag.PROJECT_DROPDOWN_MENU)
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_showsLoadingIndicatorWhenLoadingMembers() {
    val project = Project(projectId = "p1", name = "Test Project")
    val state = CreateConversationState(selectedProject = project, isLoadingMembers = true)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_projectDropdownCanBeExpanded() {
    val project = Project(projectId = "p1", name = "Test Project")
    val state = CreateConversationState(projects = listOf(project))

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule.onNodeWithTag(ProjectDropDownMenuTestTag.PROJECT_DROPDOWN_MENU).performClick()

    composeTestRule.onNodeWithTag(ProjectDropDownMenuTestTag.DROPDOWN_MENU_ITEM).assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_memberDropdownCanBeExpanded() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state = CreateConversationState(selectedProject = project, members = listOf(member))

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN).performClick()

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN_ITEM)
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_memberDropdownItemCanBeClicked() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state = CreateConversationState(selectedProject = project, members = listOf(member))

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN).performClick()
    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN_ITEM)
        .performClick()
  }

  @Test
  fun createConversationScreen_createButtonDisabledWhenNoProjectSelected() {
    val state = CreateConversationState(selectedProject = null)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createConversationScreen_createButtonDisabledWhenNoMemberSelected() {
    val project = Project(projectId = "p1", name = "Test Project")
    val state = CreateConversationState(selectedProject = project, selectedMembers = emptyList())

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createConversationScreen_createButtonDisabledWhenCreating() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state =
        CreateConversationState(
            selectedProject = project,
            selectedMembers = listOf(member),
            isCreating = true,
            isConnected = true)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createConversationScreen_createButtonDisabledWhenOffline() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state =
        CreateConversationState(
            selectedProject = project, selectedMembers = listOf(member), isConnected = false)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createConversationScreen_showsOfflineMessageWhenNotConnected() {
    val state = CreateConversationState(isConnected = false)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithText("You are offline. Cannot create conversations.")
        .assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_createButtonCanBeClickedWhenEnabled() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val state =
        CreateConversationState(
            selectedProject = project,
            selectedMembers = listOf(member),
            members = listOf(member),
            isConnected = true)

    var createClicked = false
    val viewModel = createMockViewModelWithAction(state) { createClicked = true }

    composeTestRule.setContent {
      CreateConversationScreen(onNavigateToConversation = {}, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).performClick()

    assert(createClicked)
  }

  @Test
  fun createConversationScreen_navigatesWhenConversationCreated() {
    val state = CreateConversationState(navigateToConversationId = "conv123")
    var navigatedToId: String? = null

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = { navigatedToId = it }, viewModel = createMockViewModel(state))
    }

    composeTestRule.waitUntil(timeoutMillis = 1000) { navigatedToId != null }

    assert(navigatedToId == "conv123")
  }

  @Test
  fun createConversationScreen_memberDropdownShowsSelectedCount() {
    val project = Project(projectId = "p1", name = "Test Project")
    val member1 = MemberDisplayData(Member(userId = "u1"), User(uid = "u1", displayName = "John"))
    val member2 = MemberDisplayData(Member(userId = "u2"), User(uid = "u2", displayName = "Jane"))
    val state =
        CreateConversationState(
            selectedProject = project,
            members = listOf(member1, member2),
            selectedMembers = listOf(member1, member2))

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule.onNodeWithText("2 members selected").assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_projectDropdownShowsProjectNameWhenSelected() {
    val project = Project(projectId = "p1", name = "My Amazing Project")
    val state = CreateConversationState(selectedProject = project)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule.onNodeWithText("My Amazing Project").assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_memberDropdownNotShownWhenNoProjectSelected() {
    val state = CreateConversationState(selectedProject = null)

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {}, viewModel = createMockViewModel(state))
    }

    composeTestRule
        .onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN)
        .assertDoesNotExist()
  }

  @Test
  fun createConversationScreen_displaysBackButton() {
    composeTestRule.setContent {
      CreateConversationScreen(onNavigateToConversation = {}, viewModel = createMockViewModel())
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createConversationScreen_backButtonTriggersCallback() {
    var backClicked = false

    composeTestRule.setContent {
      CreateConversationScreen(
          onNavigateToConversation = {},
          onBackClick = { backClicked = true },
          viewModel = createMockViewModel())
    }

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.BACK_BUTTON).performClick()

    assert(backClicked)
  }

  /** Helper to create a mock ViewModel with action callback */
  private fun createMockViewModelWithAction(
      state: CreateConversationState = CreateConversationState(),
      onCreateConversation: () -> Unit = {}
  ): CreateConversationViewModel {
    return object : CreateConversationViewModel() {
      override val uiState = MutableStateFlow(state)

      override fun createConversation() {
        onCreateConversation()
      }
    }
  }
}

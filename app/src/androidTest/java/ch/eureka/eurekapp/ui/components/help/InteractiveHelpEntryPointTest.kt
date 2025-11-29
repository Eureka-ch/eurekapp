package ch.eureka.eurekapp.ui.components.help

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.notifications.NotificationSettingsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of AI(chatGPT) and Claude Sonnet 4.5
class InteractiveHelpEntryPointTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: UserRepository
  private lateinit var viewModel: NotificationSettingsViewModel

  private val mockUser =
      User(
          uid = "test123",
          displayName = "Test User",
          email = "test@test.com",
          photoUrl = "",
          notificationSettings =
              mapOf(UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP.name to true),
          fcmToken = "token123")

  @Before
  fun setup() {
    mockUserRepository = mockk(relaxed = true)
    every { mockUserRepository.getCurrentUser() } returns flowOf(mockUser)
    coEvery { mockUserRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = NotificationSettingsViewModel(mockUserRepository)
  }

  @Test
  fun helpChipIsDisplayedWhenHelpIsEnabled() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW, notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").assertIsDisplayed()
  }

  @Test
  fun helpDialogOpensWhenChipIsClicked() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "John",
          notificationSettingsViewModel = viewModel)
    }

    // Click the Guide chip
    composeTestRule.onNodeWithText("Guide").performClick()

    // Verify dialog title is displayed
    composeTestRule.onNodeWithText("Welcome John 👋").assertIsDisplayed()

    // Verify intro text is displayed
    composeTestRule
        .onNodeWithText("Hey John, let's take a quick tour of the important overview.")
        .assertIsDisplayed()

    // Verify help steps are displayed
    composeTestRule.onNodeWithText("Summary cards").assertIsDisplayed()
    composeTestRule.onNodeWithText("Quick actions").assertIsDisplayed()
    composeTestRule.onNodeWithText("Interactive sections").assertIsDisplayed()

    // Verify footer message
    composeTestRule
        .onNodeWithText("You can disable this help from Preferences > Notifications.")
        .assertIsDisplayed()
  }

  @Test
  fun createTaskContextDisplaysDependenciesExplanation() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.CREATE_TASK,
          userProvidedName = "Alice",
          notificationSettingsViewModel = viewModel)
    }

    // Click the Guide chip
    composeTestRule.onNodeWithText("Guide").performClick()

    // Verify dialog title
    composeTestRule.onNodeWithText("Guided creation").assertIsDisplayed()

    // Verify dependencies step is displayed
    composeTestRule.onNodeWithText("Task dependencies").assertIsDisplayed()

    // Verify dependencies explanation contains key terms
    composeTestRule.onNodeWithText("execution order", substring = true).assertIsDisplayed()
  }

  @Test
  fun helpChipIsNotDisplayedWhenHelpIsDisabled() {
    val userWithHelpDisabled =
        mockUser.copy(
            notificationSettings =
                mapOf(UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP.name to false))

    every { mockUserRepository.getCurrentUser() } returns flowOf(userWithHelpDisabled)
    val disabledViewModel = NotificationSettingsViewModel(mockUserRepository)

    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          notificationSettingsViewModel = disabledViewModel)
    }

    // Verify Guide chip is not displayed
    composeTestRule.onNodeWithText("Guide").assertDoesNotExist()
  }

  @Test
  fun homeOverviewContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Welcome", substring = true).assertIsDisplayed()
  }

  @Test
  fun tasksContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.TASKS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Task management", substring = true).assertIsDisplayed()
  }

  @Test
  fun meetingsContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.MEETINGS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Mastering meetings", substring = true).assertIsDisplayed()
  }

  @Test
  fun projectsContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.PROJECTS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Project view", substring = true).assertIsDisplayed()
  }

  @Test
  fun createTaskContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.CREATE_TASK,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Guided creation", substring = true).assertIsDisplayed()
  }
}

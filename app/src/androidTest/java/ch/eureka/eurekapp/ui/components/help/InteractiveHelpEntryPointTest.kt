package ch.eureka.eurekapp.ui.components.help

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.runner.RunWith

// Portions of this code were generated with the help of AI(chatGPT, Claude Sonnet 4.5, and Grok)
@RunWith(AndroidJUnit4::class)
class InteractiveHelpEntryPointTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: UserRepository
  private lateinit var notificationSettingsViewModel: NotificationSettingsViewModel
  private lateinit var helpViewModel: InteractiveHelpViewModel

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
    notificationSettingsViewModel = NotificationSettingsViewModel(mockUserRepository)
    // Create help ViewModel with mock function to avoid Firebase calls
    helpViewModel = InteractiveHelpViewModel(getCurrentUserDisplayName = { null })
  }

  @Test
  fun interactiveHelpEntryPoint_helpChipIsDisplayedWhenHelpIsEnabled() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_helpDialogOpensWhenChipIsClicked() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "John",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
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
  fun interactiveHelpEntryPoint_createTaskContextDisplaysDependenciesExplanation() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.CREATE_TASK,
          userProvidedName = "Alice",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    // Click the Guide chip
    composeTestRule.onNodeWithText("Guide").performClick()

    // Verify dialog title
    composeTestRule.onNodeWithText("Guided creation").assertIsDisplayed()

    // Verify task templates step is displayed
    composeTestRule.onNodeWithText("Task templates").assertIsDisplayed()

    // Verify dependencies step is displayed
    composeTestRule.onNodeWithText("Task dependencies").assertIsDisplayed()

    // Verify dependencies explanation contains key terms
    composeTestRule.onNodeWithText("execution order", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_helpChipIsNotDisplayedWhenHelpIsDisabled() {
    val userWithHelpDisabled =
        mockUser.copy(
            notificationSettings =
                mapOf(UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP.name to false))

    every { mockUserRepository.getCurrentUser() } returns flowOf(userWithHelpDisabled)
    val disabledViewModel = NotificationSettingsViewModel(mockUserRepository)

    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = disabledViewModel,
          helpViewModel = helpViewModel)
    }

    // Verify Guide chip is not displayed
    composeTestRule.onNodeWithText("Guide").assertDoesNotExist()
  }

  @Test
  fun interactiveHelpEntryPoint_homeOverviewContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.HOME_OVERVIEW,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Welcome", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_tasksContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.TASKS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Task management", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_meetingsContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.MEETINGS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Mastering meetings", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("File attachments", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_projectsContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.PROJECTS,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Project view", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_createTaskContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.CREATE_TASK,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Guided creation", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_filesManagementContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.FILES_MANAGEMENT,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("File management", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("View files", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Open files", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete files", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_meetingVotesContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.MEETING_VOTES,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Meeting proposals voting", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("View proposals", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Confirm votes", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_tokenEntryContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.TOKEN_ENTRY,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Join with token", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Get your token", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Enter token", substring = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Tap the 'Validate' button", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_viewTaskContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.VIEW_TASK,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Viewing task details", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Task information", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("View dependencies", substring = true).assertIsDisplayed()
  }

  @Test
  fun interactiveHelpEntryPoint_notesContextDisplaysCorrectContent() {
    composeTestRule.setContent {
      InteractiveHelpEntryPoint(
          helpContext = HelpContext.NOTES,
          userProvidedName = "TestUser",
          notificationSettingsViewModel = notificationSettingsViewModel,
          helpViewModel = helpViewModel)
    }

    composeTestRule.onNodeWithText("Guide").performClick()
    composeTestRule.onNodeWithText("Personal notes", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cloud vs Local", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Add notes", substring = true).assertIsDisplayed()
    // Verify cloud sync explanation
    composeTestRule
        .onNodeWithText("automatically synchronized", substring = true)
        .assertIsDisplayed()
  }
}

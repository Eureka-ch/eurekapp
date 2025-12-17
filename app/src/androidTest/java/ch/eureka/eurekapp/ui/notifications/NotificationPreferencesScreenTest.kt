// Portions of this code were generated with the help of Claude Sonnet 4.5, and Grok.
package ch.eureka.eurekapp.ui.notifications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

class NotificationPreferencesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: UserRepository
  private lateinit var viewModel: NotificationSettingsViewModel

  private val mockUser =
      User(
          uid = "test123",
          displayName = "Test User",
          email = "test@test.com",
          photoUrl = "",
          notificationSettings = emptyMap(),
          fcmToken = "token123")

  @Before
  fun setup() {
    mockUserRepository = mockk(relaxed = true)
    every { mockUserRepository.getCurrentUser() } returns flowOf(mockUser)
    coEvery { mockUserRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = NotificationSettingsViewModel(mockUserRepository)
  }

  @Test
  fun notificationPreferencesScreen_displaysCorrectly() {
    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel, onFinishedSettingNotifications = {})
    }

    composeTestRule.onNodeWithTag(NotificationPreferencesTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_backButtonWorks() {
    var backClicked = false

    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel,
          onFinishedSettingNotifications = { backClicked = true })
    }

    composeTestRule.onNodeWithTag(NotificationPreferencesTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun notificationPreferencesScreen_displaysMeetingCategory() {
    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel, onFinishedSettingNotifications = {})
    }

    composeTestRule.onNodeWithText("Meeting Notifications:").assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_displaysMessageCategory() {
    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel, onFinishedSettingNotifications = {})
    }

    composeTestRule.onNodeWithText("Message Notifications:").assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_displaysGeneralCategory() {
    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel, onFinishedSettingNotifications = {})
    }

    composeTestRule.onNodeWithText("General Notifications:").assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_notificationOptionsCategoryDisplaysTitle() {
    composeTestRule.setContent {
      NotificationOptionsCategory(title = "Test Category", optionsList = emptyList())
    }

    composeTestRule.onNodeWithText("Test Category").assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_notificationOptionsCategoryDisplaysOptions() {
    val options =
        listOf(
            NotificationSettingState(
                UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY, true, {}))

    composeTestRule.setContent {
      NotificationOptionsCategory(title = "Test", optionsList = options)
    }

    composeTestRule
        .onNodeWithText(UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY.displayName)
        .assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_optionBooleanSwitchDisplaysTitle() {
    composeTestRule.setContent {
      OptionBooleanSwitch(value = true, title = "Test Option", onValueChange = {})
    }

    composeTestRule.onNodeWithText("Test Option").assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_optionBooleanSwitchIsDisplayed() {
    composeTestRule.setContent {
      OptionBooleanSwitch(value = false, title = "Test Switch", onValueChange = {})
    }

    composeTestRule
        .onNodeWithTag("${NotificationPreferencesTestTags.OPTION_SWITCH}_Test Switch")
        .assertIsDisplayed()
  }

  @Test
  fun notificationPreferencesScreen_optionBooleanSwitchCanBeToggled() {
    var switchValue = false

    composeTestRule.setContent {
      OptionBooleanSwitch(
          value = switchValue, title = "Toggle Test", onValueChange = { switchValue = it })
    }

    composeTestRule
        .onNodeWithTag("${NotificationPreferencesTestTags.OPTION_SWITCH}_switch_Toggle Test")
        .performClick()

    assert(switchValue)
  }

  @Test
  fun notificationPreferencesScreen_notificationSettingStateDataClassWorks() {
    val state =
        NotificationSettingState(UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY, true, {})

    assert(state.userNotificationSettingsKey == UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY)
    assert(state.value)
  }

  @Test
  fun notificationPreferencesScreen_allNotificationKeysDisplayed() {
    composeTestRule.setContent {
      NotificationPreferencesScreen(
          notificationSettingsViewModel = viewModel, onFinishedSettingNotifications = {})
    }

    // Verify all notification setting keys are displayed
    UserNotificationSettingsKeys.entries.forEach { key ->
      composeTestRule.onNodeWithText(key.displayName).assertIsDisplayed()
    }
  }
}

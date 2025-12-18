/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

@RunWith(AndroidJUnit4::class)
class OverviewProjectScreenTest {

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
  fun overviewProjectScreen_displaysContent() {
    composeTestRule.setContent { OverviewProjectScreen(projectId = "test-project-id") }

    composeTestRule.waitForIdle()

    // Verify screen content is displayed
    composeTestRule
        .onNodeWithTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Overview Projects Screen: test-project-id").assertIsDisplayed()
  }

  @Test
  fun overviewProjectScreen_displaysHelpChip() {
    composeTestRule.setContent { OverviewProjectScreen(projectId = "test-project-id") }

    composeTestRule.waitForIdle()

    // Verify help chip is displayed
    composeTestRule.onNodeWithText("Guide").assertIsDisplayed()
  }

  @Test
  fun overviewProjectScreen_helpDialogOpens() {
    composeTestRule.setContent { OverviewProjectScreen(projectId = "test-project-id") }

    composeTestRule.waitForIdle()

    // Click the Guide chip
    composeTestRule.onNodeWithText("Guide").performClick()

    // Verify dialog title is displayed
    composeTestRule.onNodeWithText("Project view", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Project context", substring = true).assertIsDisplayed()
  }
}

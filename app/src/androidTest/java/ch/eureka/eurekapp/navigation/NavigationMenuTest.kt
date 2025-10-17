package ch.eureka.eurekapp.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.screens.IdeasScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.profile.ProfileScreenTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NavigationMenuTest : TestCase() {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testNavigationBottomBarComponent() {
    val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
    composeTestRule.setContent {
      val navigationController = rememberNavController()
      BottomBarNavigationComponent(navigationController, currentScreen)
    }

    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun testAllPages() {
    val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
    composeTestRule.setContent { NavigationMenu() }

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.IDEAS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()
  }
}

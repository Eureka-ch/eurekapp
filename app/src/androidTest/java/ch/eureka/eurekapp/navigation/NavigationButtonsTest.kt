package ch.eureka.eurekapp.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.screens.CreateTaskScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NavigationButtonsTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testCreateTask() {
    val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
    composeTestRule.setContent { NavigationMenu() }

    // Setup and initial state check
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    // The main part of the test
    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.CREATE_TASK_TEXT).assertIsDisplayed()
  }
}

package ch.eureka.eurekapp.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.MainActivity
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NavigationMenuTest: TestCase() {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigationBottomBarComponent(){
        val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
        composeTestRule.setContent {
            val navigationController = rememberNavController()
            BottomBarNavigationComponent(
                navigationController, currentScreen
            )
        }

        composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON.name).assertIsDisplayed()
    }

}
package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaBottomNavTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaBottomNav displays all navigation items`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Tasks", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()

    composeTestRule.onNodeWithText("Ideas").assertIsDisplayed()

    composeTestRule.onNodeWithText("Home").assertIsDisplayed()

    composeTestRule.onNodeWithText("Meetings").assertIsDisplayed()

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { EurekaBottomNav(currentRoute = "Home", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }
}

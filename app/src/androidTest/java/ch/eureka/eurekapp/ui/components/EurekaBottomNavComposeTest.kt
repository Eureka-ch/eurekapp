package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests Compose UI efficaces pour EurekaBottomNav */
@RunWith(AndroidJUnit4::class)
class EurekaBottomNavComposeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaBottomNavRendersWithDefaultItems() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Tasks",
            onNavigate = {},
            navItems =
                listOf(NavItem("Tasks", null), NavItem("Ideas", null), NavItem("Home", null)))
      }
    }

    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ideas").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }

  @Test
  fun eurekaBottomNavShowsSelectedState() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Ideas",
            onNavigate = {},
            navItems = listOf(NavItem("Tasks", null), NavItem("Ideas", null)))
      }
    }

    composeTestRule.onNodeWithText("Ideas").assertIsDisplayed()
  }
}

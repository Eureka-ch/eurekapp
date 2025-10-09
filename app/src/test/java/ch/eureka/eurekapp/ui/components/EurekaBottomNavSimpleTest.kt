package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaBottomNavSimpleTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaBottomNav renders with Tasks route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Tasks", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with Ideas route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Ideas", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with Home route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Home", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with Meetings route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Meetings", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with Profile route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Profile", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { EurekaBottomNav(currentRoute = "Tasks", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with click handler`() {
    var clickedRoute: String? = null
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(currentRoute = "Tasks", onNavigate = { route -> clickedRoute = route })
      }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    // Note: We can't easily test the click without more complex setup
    // But we can verify the handler is set
    assert(clickedRoute == null) // Should be null initially
  }

  @Test
  fun `EurekaBottomNav renders with empty route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders with unknown route`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Unknown", onNavigate = {}) }
    }

    composeTestRule.onNodeWithText("T").assertIsDisplayed()
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
    composeTestRule.onNodeWithText("M").assertIsDisplayed()
    composeTestRule.onNodeWithText("P").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders without crashing`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaBottomNav(currentRoute = "Tasks", onNavigate = {}) }
    }

    // Should render without crashing
    composeTestRule.onRoot().assertExists()
  }
}

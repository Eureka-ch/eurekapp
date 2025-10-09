package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaBottomNav
import ch.eureka.eurekapp.ui.components.NavItem
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaBottomNavTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaBottomNavRendersDefaultItems() {
    var clickedRoute: String? = null

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Tasks", 
            onNavigate = { clickedRoute = it },
            navItems = listOf(
                NavItem("Tasks", null),
                NavItem("Ideas", null),
                NavItem("Home", null)
            )
        )
      }
    }

    // Check that all default nav items are displayed
    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ideas").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Meetings").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
  }

  @Test
  fun eurekaBottomNavShowsSelectedState() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { 
        EurekaBottomNav(
            currentRoute = "Ideas", 
            onNavigate = {},
            navItems = listOf(
                NavItem("Tasks", null),
                NavItem("Ideas", null),
                NavItem("Home", null)
            )
        )
      }
    }

    // Check that the selected item is marked as selected
    composeTestRule.onNodeWithText("Ideas").assertIsSelected()
  }

  @Test
  fun eurekaBottomNavHandlesClickEvents() {
    var clickedRoute: String? = null

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Tasks", 
            onNavigate = { clickedRoute = it },
            navItems = listOf(
                NavItem("Tasks", null),
                NavItem("Ideas", null),
                NavItem("Home", null)
            )
        )
      }
    }

    // Click on Ideas
    composeTestRule.onNodeWithText("Ideas").performClick()

    // Verify the callback was called
    assert(clickedRoute == "Ideas")
  }

  @Test
  fun eurekaBottomNavWorksWithCustomNavItems() {
    val customItems = listOf(NavItem("Custom1", null), NavItem("Custom2", null))

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(currentRoute = "Custom1", onNavigate = {}, navItems = customItems)
      }
    }

    // Check that custom items are displayed
    composeTestRule.onNodeWithText("Custom1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Custom2").assertIsDisplayed()

    // Check that default items are not displayed
    composeTestRule.onNodeWithText("Tasks").assertDoesNotExist()
  }

  @Test
  fun eurekaBottomNavWorksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { 
        EurekaBottomNav(
            currentRoute = "Home", 
            onNavigate = {},
            navItems = listOf(
                NavItem("Tasks", null),
                NavItem("Ideas", null),
                NavItem("Home", null)
            )
        )
      }
    }

    // Check that nav items are displayed in dark mode
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
  }

  @Test
  fun eurekaBottomNavShowsFirstLetterAsIcon() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { 
        EurekaBottomNav(
            currentRoute = "Tasks", 
            onNavigate = {},
            navItems = listOf(
                NavItem("Tasks", null),
                NavItem("Ideas", null),
                NavItem("Home", null)
            )
        )
      }
    }

    // Check that first letters are displayed as icons
    composeTestRule.onNodeWithText("T").assertIsDisplayed() // Tasks
    composeTestRule.onNodeWithText("I").assertIsDisplayed() // Ideas
    composeTestRule.onNodeWithText("H").assertIsDisplayed() // Home
    composeTestRule.onNodeWithText("M").assertIsDisplayed() // Meetings
    composeTestRule.onNodeWithText("P").assertIsDisplayed() // Profile
  }
}

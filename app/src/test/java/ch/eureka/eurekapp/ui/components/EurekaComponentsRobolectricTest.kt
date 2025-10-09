package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests Compose UI avec Robolectric pour éviter le besoin d'émulateur Ces tests fonctionnent sans
 * appareil connecté
 */
@RunWith(AndroidJUnit4::class)
class EurekaComponentsRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTaskCard renders correctly with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Robolectric Test Task",
            isCompleted = false,
            dueDate = "2024-01-15",
            assignee = "Test User")
      }
    }

    composeTestRule.onNodeWithText("Robolectric Test Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("⏰ 2024-01-15").assertIsDisplayed()
    composeTestRule.onNodeWithText("👤 Test User").assertIsDisplayed()
  }

  @Test
  fun `EurekaTaskCard renders completed state`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Completed Task", isCompleted = true)
      }
    }

    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("✓").assertIsDisplayed()
  }

  @Test
  fun `EurekaTaskCard renders with priority and category`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Tags", priority = "High", category = "Work")
      }
    }

    composeTestRule.onNodeWithText("Task with Tags").assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
    composeTestRule.onNodeWithText("Work").assertIsDisplayed()
  }

  @Test
  fun `EurekaTaskCard renders with progress`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Progress", progressText = "75%", progressValue = 0.75f)
      }
    }

    composeTestRule.onNodeWithText("Task with Progress").assertIsDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
  }

  @Test
  fun `EurekaBottomNav renders correctly with Robolectric`() {
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
  fun `EurekaInfoCard renders correctly with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Robolectric Info", primaryValue = "100", secondaryValue = "Additional Info")
      }
    }

    composeTestRule.onNodeWithText("Robolectric Info").assertIsDisplayed()
    composeTestRule.onNodeWithText("100").assertIsDisplayed()
    composeTestRule.onNodeWithText("Additional Info").assertIsDisplayed()
  }

  @Test
  fun `EurekaInfoCard renders with icon`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Info with Icon", primaryValue = "42", iconText = "📊")
      }
    }

    composeTestRule.onNodeWithText("Info with Icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("42").assertIsDisplayed()
    composeTestRule.onNodeWithText("📊").assertIsDisplayed()
  }

  @Test
  fun `EurekaInfoCard renders without optional values`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaInfoCard(title = "Simple Info", primaryValue = "123") }
    }

    composeTestRule.onNodeWithText("Simple Info").assertIsDisplayed()
    composeTestRule.onNodeWithText("123").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders all types with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag("Success", StatusType.SUCCESS)
        EurekaStatusTag("Warning", StatusType.WARNING)
        EurekaStatusTag("Error", StatusType.ERROR)
        EurekaStatusTag("Info", StatusType.INFO)
      }
    }

    composeTestRule.onNodeWithText("Success").assertIsDisplayed()
    composeTestRule.onNodeWithText("Warning").assertIsDisplayed()
    composeTestRule.onNodeWithText("Error").assertIsDisplayed()
    composeTestRule.onNodeWithText("Info").assertIsDisplayed()
  }

  @Test
  fun `EurekaFilterBar renders correctly with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(
            options = listOf("All", "Active", "Completed"),
            selectedOption = "All",
            onOptionSelected = {})
      }
    }

    composeTestRule.onNodeWithText("All").assertIsDisplayed()
    composeTestRule.onNodeWithText("Active").assertIsDisplayed()
    composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
  }

  @Test
  fun `EurekaTopBar renders correctly with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar("Robolectric App") }
    }

    composeTestRule.onNodeWithText("Robolectric App").assertIsDisplayed()
  }

  @Test
  fun `All components work together with Robolectric`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTopBar("Complete Test")
        EurekaInfoCard(title = "Test Info", primaryValue = "42")
        EurekaStatusTag("Test Status", StatusType.INFO)
        EurekaTaskCard(title = "Test Task", isCompleted = false)
        EurekaFilterBar(
            options = listOf("All", "Active"), selectedOption = "All", onOptionSelected = {})
        EurekaBottomNav(
            currentRoute = "Tasks",
            onNavigate = {},
            navItems = listOf(NavItem("Tasks", null), NavItem("Ideas", null)))
      }
    }

    // Vérifier que tous les composants principaux sont affichés
    composeTestRule.onNodeWithText("Complete Test").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Info").assertIsDisplayed()
    composeTestRule.onNodeWithText("42").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Status").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("All").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
  }
}

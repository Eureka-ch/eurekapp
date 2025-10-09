package ch.eureka.eurekapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests massifs pour tous les composants - previews et edge cases */
@RunWith(AndroidJUnit4::class)
class AllComponentsPreviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ========== EurekaBottomNav Tests ==========

  @Test
  fun `EurekaBottomNav with 1 item`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Home",
            onNavigate = {},
            navItems = listOf(NavItem("Home", Icons.Default.Home)))
      }
    }
  }

  @Test
  fun `EurekaBottomNav with 5 items`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "A",
            onNavigate = {},
            navItems =
                listOf(
                    NavItem("A", null),
                    NavItem("B", null),
                    NavItem("C", null),
                    NavItem("D", null),
                    NavItem("E", null)))
      }
    }
  }

  @Test
  fun `EurekaBottomNav with 10 items`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaBottomNav(
            currentRoute = "Item1",
            onNavigate = {},
            navItems = (1..10).map { NavItem("Item$it", null) })
      }
    }
  }

  // ========== EurekaFilterBar Tests ==========

  @Test
  fun `EurekaFilterBar with 10 options`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(
            options = (1..10).map { "Option$it" },
            selectedOption = "Option1",
            onOptionSelected = {})
      }
    }
  }

  @Test
  fun `EurekaFilterBar with very long option names`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(
            options =
                listOf(
                    "VeryLongOptionNameThatGoesOnAndOnAndOn",
                    "AnotherVeryLongOptionName",
                    "ShortlyNamed"),
            selectedOption = "ShortlyNamed",
            onOptionSelected = {})
      }
    }
  }

  // ========== EurekaInfoCard Tests ==========

  @Test
  fun `EurekaInfoCard all parameters filled`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Full Card",
            primaryValue = "100",
            secondaryValue = "Secondary",
            iconText = "🔥")
      }
    }
  }

  @Test
  fun `EurekaInfoCard with very long text`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Very Long Title That Should Wrap Or Truncate",
            primaryValue = "Very Long Primary Value Text",
            secondaryValue = "Very Long Secondary Value Text",
            iconText = "🌍")
      }
    }
  }

  @Test
  fun `EurekaInfoCard with numeric values`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Stats",
            primaryValue = "1234567890",
            secondaryValue = "0.123456789",
            iconText = "📊")
      }
    }
  }

  // ========== EurekaStatusTag Tests ==========

  @Test
  fun `EurekaStatusTag all types in sequence`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Success", type = StatusType.SUCCESS)
        EurekaStatusTag(text = "Error", type = StatusType.ERROR)
        EurekaStatusTag(text = "Warning", type = StatusType.WARNING)
        EurekaStatusTag(text = "Info", type = StatusType.INFO)
      }
    }
  }

  @Test
  fun `EurekaStatusTag with empty text`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "", type = StatusType.SUCCESS) }
    }
  }

  @Test
  fun `EurekaStatusTag with very long text`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "VeryLongTagTextThatMightNotFit", type = StatusType.INFO)
      }
    }
  }

  // ========== EurekaTaskCard Tests ==========

  @Test
  fun `EurekaTaskCard fully loaded`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Full Task",
            dueDate = "2024-12-31",
            assignee = "John Doe",
            priority = "High",
            category = "Work",
            progressText = "75%",
            progressValue = 0.75f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }
  }

  @Test
  fun `EurekaTaskCard completed state`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Completed Task", isCompleted = true, onToggleComplete = {})
      }
    }
  }

  @Test
  fun `EurekaTaskCard with all optional params`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Task",
            dueDate = "Tomorrow",
            assignee = "Me",
            priority = "Low",
            category = "Personal",
            progressText = "50%",
            progressValue = 0.5f)
      }
    }
  }

  @Test
  fun `EurekaTaskCard with no optional params`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTaskCard(title = "Minimal Task") }
    }
  }

  // ========== EurekaTopBar Tests ==========

  @Test
  fun `EurekaTopBar multiple variations`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTopBar(title = "Title 1")
        EurekaTopBar(title = "Title 2")
        EurekaTopBar(title = "Title 3")
      }
    }
  }

  @Test
  fun `EurekaTopBar with special unicode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = "🚀 EUREKA 🚀") }
    }
  }

  // ========== Combined Component Tests ==========

  @Test
  fun `All components together light theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTopBar(title = "Dashboard")
        EurekaInfoCard(title = "Tasks", primaryValue = "10", iconText = "✓")
        EurekaStatusTag(text = "Active", type = StatusType.SUCCESS)
        EurekaTaskCard(title = "Sample Task")
        EurekaFilterBar(
            options = listOf("All", "Active"), selectedOption = "All", onOptionSelected = {})
        EurekaBottomNav(
            currentRoute = "Home", onNavigate = {}, navItems = listOf(NavItem("Home", null)))
      }
    }
  }

  @Test
  fun `All components together dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaTopBar(title = "Dashboard")
        EurekaInfoCard(title = "Tasks", primaryValue = "10", iconText = "✓")
        EurekaStatusTag(text = "Active", type = StatusType.SUCCESS)
        EurekaTaskCard(title = "Sample Task")
        EurekaFilterBar(
            options = listOf("All", "Active"), selectedOption = "All", onOptionSelected = {})
        EurekaBottomNav(
            currentRoute = "Home", onNavigate = {}, navItems = listOf(NavItem("Home", null)))
      }
    }
  }
}

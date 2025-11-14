package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests d'intégration efficaces pour les composants UI Teste les composants ensemble avec le thème
 */
@RunWith(AndroidJUnit4::class)
class UIComponentsIntegrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun allUIComponentsRenderWithEurekaTheme() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test que tous les composants principaux se rendent
        EurekaTopBar(modifier = Modifier, title = "Test App")
        EurekaInfoCard(title = "Test Info", primaryValue = "42", secondaryValue = "Additional info")
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

    // Vérifier que les éléments clés sont affichés
    composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Info").assertIsDisplayed()
    composeTestRule.onNodeWithText("42").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Status").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("All").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
  }

  @Test
  fun componentsWorkInDarkTheme() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaTopBar(modifier = Modifier, title = "Dark Theme Test")
        EurekaInfoCard(title = "Dark Info", primaryValue = "100")
        EurekaStatusTag("Dark Status", StatusType.SUCCESS)
      }
    }

    composeTestRule.onNodeWithText("Dark Theme Test").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Info").assertIsDisplayed()
    composeTestRule.onNodeWithText("100").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Status").assertIsDisplayed()
  }

  @Test
  fun statusTagRendersAllStatusTypes() {
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
}

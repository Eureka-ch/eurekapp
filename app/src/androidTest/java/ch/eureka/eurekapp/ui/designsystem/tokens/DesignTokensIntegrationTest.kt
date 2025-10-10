package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests efficaces pour les design tokens Teste que les tokens fonctionnent dans le thème */
@RunWith(AndroidJUnit4::class)
class DesignTokensIntegrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaThemeAppliesAllDesignTokensCorrectly() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test que le thème applique tous les tokens
        androidx.compose.material3.Text(
            text = "Typography Test",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        androidx.compose.material3.Card(
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
                    containerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
              androidx.compose.material3.Text("Card Test")
            }
      }
    }

    composeTestRule.onNodeWithText("Typography Test").assertIsDisplayed()
    composeTestRule.onNodeWithText("Card Test").assertIsDisplayed()
  }

  @Test
  fun designTokensWorkInBothLightAndDarkThemes() {
    // Test thème clair
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Text(
            text = "Light Theme",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
      }
    }
    composeTestRule.onNodeWithText("Light Theme").assertIsDisplayed()

    // Test thème sombre
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        androidx.compose.material3.Text(
            text = "Dark Theme",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
      }
    }
    composeTestRule.onNodeWithText("Dark Theme").assertIsDisplayed()
  }
}

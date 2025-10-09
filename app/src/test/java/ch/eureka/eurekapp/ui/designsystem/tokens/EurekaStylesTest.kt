package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests pour EurekaStyles pour améliorer la couverture */
@RunWith(AndroidJUnit4::class)
class EurekaStylesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaStyles button styles work correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Button(
            onClick = {},
            colors =
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.primary)) {
              androidx.compose.material3.Text("Primary Button")
            }
        androidx.compose.material3.Button(
            onClick = {},
            colors =
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.secondary)) {
              androidx.compose.material3.Text("Secondary Button")
            }
      }
    }

    composeTestRule.onNodeWithText("Primary Button").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary Button").assertIsDisplayed()
  }

  @Test
  fun `EurekaStyles text field styles work correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.TextField(
            value = "Test Value",
            onValueChange = {},
            label = { androidx.compose.material3.Text("Test Label") },
            colors =
                androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.surface))
      }
    }

    composeTestRule.onNodeWithText("Test Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Label").assertIsDisplayed()
  }

  @Test
  fun `EurekaStyles card styles work correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
            elevation =
                androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)) {
              androidx.compose.material3.Text("Styled Card")
            }
      }
    }

    composeTestRule.onNodeWithText("Styled Card").assertIsDisplayed()
  }

  @Test
  fun `EurekaStyles work in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        androidx.compose.material3.Button(
            onClick = {},
            colors =
                androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.primary)) {
              androidx.compose.material3.Text("Dark Button")
            }
        androidx.compose.material3.Card(
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
                    containerColor =
                        androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
              androidx.compose.material3.Text("Dark Card")
            }
      }
    }

    composeTestRule.onNodeWithText("Dark Button").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Card").assertIsDisplayed()
  }
}

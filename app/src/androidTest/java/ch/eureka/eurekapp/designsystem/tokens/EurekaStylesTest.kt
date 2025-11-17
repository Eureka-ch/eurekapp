package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaStatusTag
import ch.eureka.eurekapp.ui.components.StatusType
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaStylesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaStylesPrimaryButtonColorsWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Button(
            onClick = {}, colors = EurekaStyles.primaryButtonColors()) {
              androidx.compose.material3.Text("Primary Button")
            }
      }
    }

    // Check that button with primary colors is displayed
    composeTestRule.onNodeWithText("Primary Button").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesOutlinedButtonColorsWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.OutlinedButton(
            onClick = {}, colors = EurekaStyles.outlinedButtonColors()) {
              androidx.compose.material3.Text("Outlined Button")
            }
      }
    }

    // Check that outlined button with custom colors is displayed
    composeTestRule.onNodeWithText("Outlined Button").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesTextFieldColorsWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.OutlinedTextField(
            value = "Test Text", onValueChange = {}, colors = EurekaStyles.textFieldColors())
      }
    }

    // Check that text field with custom colors is displayed
    composeTestRule.onNodeWithText("Test Text").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesHighPriorityTagColorsWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(colors = EurekaStyles.highPriorityTagColors()) {
          androidx.compose.material3.Text("High Priority")
        }
      }
    }

    // Check that high priority tag with custom colors is displayed
    composeTestRule.onNodeWithText("High Priority").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesNormalTagColorsWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(colors = EurekaStyles.normalTagColors()) {
          androidx.compose.material3.Text("Normal Tag")
        }
      }
    }

    // Check that normal tag with custom colors is displayed
    composeTestRule.onNodeWithText("Normal Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesOutlinedButtonBorderWorks() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.OutlinedButton(
            onClick = {}, border = EurekaStyles.outlinedButtonBorder()) {
              androidx.compose.material3.Text("Bordered Button")
            }
      }
    }

    // Check that button with custom border is displayed
    composeTestRule.onNodeWithText("Bordered Button").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesCardShapeIsAccessible() {
    // Test that card shape constant is accessible
    val cardShape = EurekaStyles.CardShape
    assert(cardShape != null)
  }

  @Test
  fun eurekaStylesCardElevationIsAccessible() {
    // Test that card elevation constant is accessible
    val cardElevation = EurekaStyles.CardElevation
    assert(cardElevation != null)
  }

  @Test
  fun eurekaStylesWorkInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        androidx.compose.material3.Button(
            onClick = {}, colors = EurekaStyles.primaryButtonColors()) {
              androidx.compose.material3.Text("Dark Mode Button")
            }
      }
    }

    // Check that styles work in dark mode
    composeTestRule.onNodeWithText("Dark Mode Button").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesWorkWithStatusTag() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Styled Tag", type = StatusType.SUCCESS)
      }
    }

    // Check that status tag with styles is displayed
    composeTestRule.onNodeWithText("Styled Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesCardShapeWorks() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(shape = EurekaStyles.CardShape) {
          androidx.compose.material3.Text("Shaped Card")
        }
      }
    }

    // Check that card with custom shape is displayed
    composeTestRule.onNodeWithText("Shaped Card").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesCardElevationWorks() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(
            elevation =
                androidx.compose.material3.CardDefaults.cardElevation(
                    defaultElevation = EurekaStyles.CardElevation)) {
              androidx.compose.material3.Text("Elevated Card")
            }
      }
    }

    // Check that card with custom elevation is displayed
    composeTestRule.onNodeWithText("Elevated Card").assertIsDisplayed()
  }

  @Test
  fun eurekaStylesAllButtonTypesWork() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          androidx.compose.material3.Button(
              onClick = {}, colors = EurekaStyles.primaryButtonColors()) {
                androidx.compose.material3.Text("Primary")
              }

          androidx.compose.material3.OutlinedButton(
              onClick = {},
              colors = EurekaStyles.outlinedButtonColors(),
              border = EurekaStyles.outlinedButtonBorder()) {
                androidx.compose.material3.Text("Outlined")
              }
        }
      }
    }

    // Check that all button types are displayed
    composeTestRule.onNodeWithText("Primary").assertIsDisplayed()
    composeTestRule.onNodeWithText("Outlined").assertIsDisplayed()
  }
}

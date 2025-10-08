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
class EurekaFilterBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaFilterBar displays all filter options`() {
    val filterOptions = listOf("Me", "Team", "This week", "All")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(options = filterOptions, selectedOption = "Me", onOptionSelected = {})
      }
    }

    composeTestRule.onNodeWithText("Me").assertIsDisplayed()

    composeTestRule.onNodeWithText("Team").assertIsDisplayed()

    composeTestRule.onNodeWithText("This week").assertIsDisplayed()

    composeTestRule.onNodeWithText("All").assertIsDisplayed()
  }

  @Test
  fun `EurekaFilterBar renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaFilterBar(
            options = listOf("Option 1", "Option 2"),
            selectedOption = "Option 1",
            onOptionSelected = {})
      }
    }

    composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
  }
}

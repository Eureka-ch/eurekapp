package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaFilterBar
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaFilterBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaFilterBarRendersOptions() {
    val options = listOf("All", "Active", "Completed")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(options = options, selectedOption = "Active", onOptionSelected = {})
      }
    }

    // Check that all options are displayed
    composeTestRule.onNodeWithText("All").assertIsDisplayed()
    composeTestRule.onNodeWithText("Active").assertIsDisplayed()
    composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
  }

  @Test
  fun eurekaFilterBarShowsSelectedOption() {
    val options = listOf("All", "Active", "Completed")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(options = options, selectedOption = "Completed", onOptionSelected = {})
      }
    }

    // Check that the selected option is marked as selected
    composeTestRule.onNodeWithText("Completed").assertIsSelected()
  }

  @Test
  fun eurekaFilterBarHandlesClickEvents() {
    val options = listOf("All", "Active", "Completed")
    var selectedOption: String? = null

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(
            options = options, selectedOption = "All", onOptionSelected = { selectedOption = it })
      }
    }

    // Click on Active
    composeTestRule.onNodeWithText("Active").performClick()

    // Verify the callback was called
    assert(selectedOption == "Active")
  }

  @Test
  fun eurekaFilterBarWorksWithSingleOption() {
    val options = listOf("Only Option")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(options = options, selectedOption = "Only Option", onOptionSelected = {})
      }
    }

    // Check that the single option is displayed and selected
    composeTestRule.onNodeWithText("Only Option").assertIsDisplayed()
    composeTestRule.onNodeWithText("Only Option").assertIsSelected()
  }


  @Test
  fun eurekaFilterBarWorksInDarkMode() {
    val options = listOf("Light", "Dark")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaFilterBar(options = options, selectedOption = "Dark", onOptionSelected = {})
      }
    }

    // Check that options are displayed in dark mode
    composeTestRule.onNodeWithText("Light").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark").assertIsSelected()
  }

  @Test
  fun eurekaFilterBarHandlesMultipleClicks() {
    val options = listOf("A", "B", "C")
    var clickCount = 0
    var lastSelected: String? = null

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaFilterBar(
            options = options,
            selectedOption = "A",
            onOptionSelected = {
              clickCount++
              lastSelected = it
            })
      }
    }

    // Click on different options
    composeTestRule.onNodeWithText("B").performClick()
    composeTestRule.onNodeWithText("C").performClick()
    composeTestRule.onNodeWithText("A").performClick()

    // Verify all clicks were handled
    assert(clickCount == 3)
    assert(lastSelected == "A")
  }
}

package ch.eureka.eurekapp.ui

import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.

/**
 * Tests for the BackButton component to ensure it:
 * 1. Renders correctly
 * 2. Is clickable
 * 3. Triggers the onClick callback
 * 4. Has proper accessibility
 */
class BackButtonTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun backButtonIsDisplayed() {
    composeTestRule.setContent {
      BackButton(
          onClick = {},
          modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun backButtonHasCorrectContentDescription() {
    composeTestRule.setContent {
      BackButton(
          onClick = {},
          modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
    }

    composeTestRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
  }

  @Test
  fun backButtonTriggersOnClickWhenClicked() {
    var clicked = false

    composeTestRule.setContent {
      BackButton(
          onClick = { clicked = true },
          modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick()

    assert(clicked) { "BackButton onClick callback was not triggered" }
  }

  @Test
  fun backButtonTriggersOnClickMultipleClicks() {
    var clickCount = 0

    composeTestRule.setContent {
      BackButton(
          onClick = { clickCount++ },
          modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
    }

    // Click multiple times
    repeat(3) { composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick() }

    assert(clickCount == 3) {
      "BackButton should have been clicked 3 times, but was clicked $clickCount times"
    }
  }

  @Test
  fun backButtonInEurekaTopBarIsDisplayed() {
    composeTestRule.setContent {
      EurekaTopBar(
          title = "Test Screen",
          navigationIcon = {
            BackButton(
                onClick = {},
                modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
          })
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun backButtonInEurekaTopBarTriggersOnClick() {
    var clicked = false

    composeTestRule.setContent {
      EurekaTopBar(
          title = "Test Screen",
          navigationIcon = {
            BackButton(
                onClick = { clicked = true },
                modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
          })
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick()

    assert(clicked) { "BackButton in EurekaTopBar onClick callback was not triggered" }
  }

  @Test
  fun backButtonInEurekaTopBarHasCorrectContentDescription() {
    composeTestRule.setContent {
      EurekaTopBar(
          title = "Test Screen",
          navigationIcon = {
            BackButton(
                onClick = {},
                modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
          })
    }

    composeTestRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
  }

  @Test
  fun backButtonInEurekaTopBarMultipleClicks() {
    var clickCount = 0

    composeTestRule.setContent {
      EurekaTopBar(
          title = "Test Screen",
          navigationIcon = {
            BackButton(
                onClick = { clickCount++ },
                modifier = androidx.compose.ui.Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
          })
    }

    repeat(3) { composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick() }

    assert(clickCount == 3) {
      "BackButton in EurekaTopBar should have been clicked 3 times, but was clicked $clickCount times"
    }
  }
}

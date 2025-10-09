package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaStatusTag
import ch.eureka.eurekapp.ui.components.StatusType
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaStatusTagExtendedTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaStatusTagRendersSuccessType() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Success Tag", type = StatusType.SUCCESS)
      }
    }

    // Check that success tag is displayed
    composeTestRule.onNodeWithText("Success Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersWarningType() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Warning Tag", type = StatusType.WARNING)
      }
    }

    // Check that warning tag is displayed
    composeTestRule.onNodeWithText("Warning Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersErrorType() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Error Tag", type = StatusType.ERROR)
      }
    }

    // Check that error tag is displayed
    composeTestRule.onNodeWithText("Error Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersInfoType() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "Info Tag", type = StatusType.INFO) }
    }

    // Check that info tag is displayed
    composeTestRule.onNodeWithText("Info Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersDefaultType() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "Default Tag") }
    }

    // Check that default tag (INFO) is displayed
    composeTestRule.onNodeWithText("Default Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersEmptyText() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "", type = StatusType.SUCCESS) }
    }

    // Check that empty text is handled gracefully
    composeTestRule.onNodeWithText("").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersLongText() {
    val longText = "This is a very long status tag text that should still be displayed correctly"

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = longText, type = StatusType.INFO) }
    }

    // Check that long text is displayed
    composeTestRule.onNodeWithText(longText).assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersSpecialCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Special Chars: @#$%^&*()", type = StatusType.WARNING)
      }
    }

    // Check that special characters are displayed
    composeTestRule.onNodeWithText("Special Chars: @#$%^&*()").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersUnicodeCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Caracteres Especiales: ñáéíóú", type = StatusType.ERROR)
      }
    }

    // Check that unicode characters are displayed
    composeTestRule.onNodeWithText("Caracteres Especiales: ñáéíóú").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersNumbers() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Tag 123", type = StatusType.SUCCESS)
      }
    }

    // Check that numbers are displayed
    composeTestRule.onNodeWithText("Tag 123").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersEmojis() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "🎯 Tag with Emoji", type = StatusType.INFO)
      }
    }

    // Check that emojis are displayed
    composeTestRule.onNodeWithText("🎯 Tag with Emoji").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagWorksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaStatusTag(text = "Dark Mode Tag", type = StatusType.SUCCESS)
      }
    }

    // Check that tag works in dark mode
    composeTestRule.onNodeWithText("Dark Mode Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersMultipleInstances() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Row {
          EurekaStatusTag(text = "First Tag", type = StatusType.SUCCESS)
          EurekaStatusTag(text = "Second Tag", type = StatusType.ERROR)
        }
      }
    }

    // Check that multiple tags are displayed
    composeTestRule.onNodeWithText("First Tag").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersAllTypes() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          EurekaStatusTag(text = "Success", type = StatusType.SUCCESS)
          EurekaStatusTag(text = "Warning", type = StatusType.WARNING)
          EurekaStatusTag(text = "Error", type = StatusType.ERROR)
          EurekaStatusTag(text = "Info", type = StatusType.INFO)
        }
      }
    }

    // Check that all types are displayed
    composeTestRule.onNodeWithText("Success").assertIsDisplayed()
    composeTestRule.onNodeWithText("Warning").assertIsDisplayed()
    composeTestRule.onNodeWithText("Error").assertIsDisplayed()
    composeTestRule.onNodeWithText("Info").assertIsDisplayed()
  }

  @Test
  fun eurekaStatusTagRendersWithDifferentTexts() {
    val texts = listOf("Active", "Pending", "Completed", "Cancelled", "In Progress")

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          texts.forEach { text -> EurekaStatusTag(text = text, type = StatusType.INFO) }
        }
      }
    }

    // Check that all texts are displayed
    texts.forEach { text -> composeTestRule.onNodeWithText(text).assertIsDisplayed() }
  }
}

package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaInfoCardExtendedTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaInfoCardRendersWithIconText() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Card with Icon", primaryValue = "Value", iconText = "📊")
      }
    }

    // Check that card with icon is displayed
    composeTestRule.onNodeWithText("Card with Icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("📊").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersWithSecondaryValue() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Card with Secondary", primaryValue = "Primary", secondaryValue = "Secondary")
      }
    }

    // Check that card with secondary value is displayed
    composeTestRule.onNodeWithText("Card with Secondary").assertIsDisplayed()
    composeTestRule.onNodeWithText("Primary").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersWithAllFields() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Complete Card",
            primaryValue = "Main Value",
            secondaryValue = "Sub Value",
            iconText = "🎯")
      }
    }

    // Check that complete card is displayed
    composeTestRule.onNodeWithText("Complete Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Main Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sub Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("🎯").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersWithoutIconText() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Card without Icon", primaryValue = "Value")
      }
    }

    // Check that card without icon is displayed
    composeTestRule.onNodeWithText("Card without Icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersWithoutSecondaryValue() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Card without Secondary", primaryValue = "Value")
      }
    }

    // Check that card without secondary value is displayed
    composeTestRule.onNodeWithText("Card without Secondary").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersEmptyStrings() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "", primaryValue = "", secondaryValue = "", iconText = "")
      }
    }

    // Check that card with empty strings is handled gracefully
    composeTestRule.onNodeWithText("").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersLongText() {
    val longTitle = "This is a very long title that should still be displayed correctly"
    val longValue = "This is a very long primary value that should still be displayed correctly"
    val longSecondary =
        "This is a very long secondary value that should still be displayed correctly"

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = longTitle,
            primaryValue = longValue,
            secondaryValue = longSecondary,
            iconText = "📈")
      }
    }

    // Check that long text is displayed
    composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    composeTestRule.onNodeWithText(longValue).assertIsDisplayed()
    composeTestRule.onNodeWithText(longSecondary).assertIsDisplayed()
    composeTestRule.onNodeWithText("📈").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersSpecialCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Title with Special Chars: @#$%^&*()",
            primaryValue = "Value with Special Chars: @#$%^&*()",
            secondaryValue = "Secondary with Special Chars: @#$%^&*()",
            iconText = "🎯")
      }
    }

    // Check that special characters are displayed
    composeTestRule.onNodeWithText("Title with Special Chars: @#$%^&*()").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value with Special Chars: @#$%^&*()").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary with Special Chars: @#$%^&*()").assertIsDisplayed()
    composeTestRule.onNodeWithText("🎯").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersUnicodeCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Título con Caracteres Especiales: ñáéíóú",
            primaryValue = "Valor con Caracteres Especiales: ñáéíóú",
            secondaryValue = "Secundario con Caracteres Especiales: ñáéíóú",
            iconText = "🌍")
      }
    }

    // Check that unicode characters are displayed
    composeTestRule.onNodeWithText("Título con Caracteres Especiales: ñáéíóú").assertIsDisplayed()
    composeTestRule.onNodeWithText("Valor con Caracteres Especiales: ñáéíóú").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Secundario con Caracteres Especiales: ñáéíóú")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("🌍").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersNumbers() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Title 123",
            primaryValue = "Value 456",
            secondaryValue = "Secondary 789",
            iconText = "1️⃣")
      }
    }

    // Check that numbers are displayed
    composeTestRule.onNodeWithText("Title 123").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value 456").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary 789").assertIsDisplayed()
    composeTestRule.onNodeWithText("1️⃣").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardWorksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaInfoCard(
            title = "Dark Mode Card",
            primaryValue = "Dark Value",
            secondaryValue = "Dark Secondary",
            iconText = "🌙")
      }
    }

    // Check that card works in dark mode
    composeTestRule.onNodeWithText("Dark Mode Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Secondary").assertIsDisplayed()
    composeTestRule.onNodeWithText("🌙").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCardRendersMultipleInstances() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          EurekaInfoCard(title = "First Card", primaryValue = "First Value")
          EurekaInfoCard(title = "Second Card", primaryValue = "Second Value")
        }
      }
    }

    // Check that multiple cards are displayed
    composeTestRule.onNodeWithText("First Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("First Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Value").assertIsDisplayed()
  }
}

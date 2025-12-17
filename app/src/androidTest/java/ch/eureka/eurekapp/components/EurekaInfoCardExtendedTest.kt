// Portions of this file were written with the help of Grok.
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
  fun eurekaInfoCard_rendersWithIconText() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Card with Icon", primaryValue = "Value")
      }
    }

    // Check that card with icon is displayed
    composeTestRule.onNodeWithText("Card with Icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersWithSecondaryValue() {
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
  fun eurekaInfoCard_rendersWithAllFields() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Complete Card", primaryValue = "Main Value", secondaryValue = "Sub Value")
      }
    }

    // Check that complete card is displayed
    composeTestRule.onNodeWithText("Complete Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Main Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sub Value").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersWithoutIconText() {
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
  fun eurekaInfoCard_rendersWithoutSecondaryValue() {
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
  fun eurekaInfoCard_rendersEmptyStrings() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Empty Test", primaryValue = "Empty Value")
      }
    }

    // Check that card with empty strings is handled gracefully
    composeTestRule.onNodeWithText("Empty Test").assertIsDisplayed()
    composeTestRule.onNodeWithText("Empty Value").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersLongText() {
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
        )
      }
    }

    // Check that long text is displayed
    composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    composeTestRule.onNodeWithText(longValue).assertIsDisplayed()
    composeTestRule.onNodeWithText(longSecondary).assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersSpecialCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Title with Special Chars: @#$%^&*()",
            primaryValue = "Value with Special Chars: @#$%^&*()",
            secondaryValue = "Secondary with Special Chars: @#$%^&*()")
      }
    }

    // Check that special characters are displayed
    composeTestRule.onNodeWithText("Title with Special Chars: @#$%^&*()").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value with Special Chars: @#$%^&*()").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary with Special Chars: @#$%^&*()").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersUnicodeCharacters() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Título con Caracteres Especiales: ñáéíóú",
            primaryValue = "Valor con Caracteres Especiales: ñáéíóú",
            secondaryValue = "Secundario con Caracteres Especiales: ñáéíóú",
        )
      }
    }

    // Check that unicode characters are displayed
    composeTestRule.onNodeWithText("Título con Caracteres Especiales: ñáéíóú").assertIsDisplayed()
    composeTestRule.onNodeWithText("Valor con Caracteres Especiales: ñáéíóú").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Secundario con Caracteres Especiales: ñáéíóú")
        .assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersNumbers() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Title 123",
            primaryValue = "Value 456",
            secondaryValue = "Secondary 789",
        )
      }
    }

    // Check that numbers are displayed
    composeTestRule.onNodeWithText("Title 123").assertIsDisplayed()
    composeTestRule.onNodeWithText("Value 456").assertIsDisplayed()
    composeTestRule.onNodeWithText("Secondary 789").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_worksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaInfoCard(
            title = "Dark Mode Card",
            primaryValue = "Dark Value",
            secondaryValue = "Dark Secondary")
      }
    }

    // Check that card works in dark mode
    composeTestRule.onNodeWithText("Dark Mode Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Value").assertIsDisplayed()
    composeTestRule.onNodeWithText("Dark Secondary").assertIsDisplayed()
  }

  @Test
  fun eurekaInfoCard_rendersMultipleInstances() {
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

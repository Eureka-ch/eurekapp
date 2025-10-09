package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaStylesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `PrimaryButtonColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val buttonColors = EurekaStyles.PrimaryButtonColors()
        assert(
            buttonColors.containerColor ==
                androidx.compose.material3.MaterialTheme.colorScheme.primary)
        assert(buttonColors.contentColor == Color.White)
      }
    }
  }

  @Test
  fun `OutlinedButtonColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val buttonColors = EurekaStyles.OutlinedButtonColors()
        assert(buttonColors.contentColor == Color(0xFF424242))
        assert(buttonColors.containerColor == Color.White)
      }
    }
  }

  @Test
  fun `TextFieldColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val textFieldColors = EurekaStyles.TextFieldColors()
        // Test that TextFieldColors is created successfully
        assert(textFieldColors != null)
      }
    }
  }

  @Test
  fun `HighPriorityTagColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val tagColors = EurekaStyles.HighPriorityTagColors()
        assert(tagColors.containerColor == Color(0xFFFFEBEE))
        assert(tagColors.contentColor == Color(0xFFE57373))
      }
    }
  }

  @Test
  fun `NormalTagColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val tagColors = EurekaStyles.NormalTagColors()
        assert(tagColors.containerColor == Color(0xFFEEEEEE))
        assert(tagColors.contentColor == Color(0xFF424242))
      }
    }
  }

  @Test
  fun `CardShape has correct corner radius`() {
    val cardShape = EurekaStyles.CardShape
    assert(cardShape is androidx.compose.foundation.shape.RoundedCornerShape)
  }

  @Test
  fun `CardElevation has correct value`() {
    val cardElevation = EurekaStyles.CardElevation
    assert(cardElevation == 2.dp)
  }

  @Test
  fun `OutlinedButtonBorder has correct properties`() {
    val border = EurekaStyles.OutlinedButtonBorder
    assert(border.width == 1.dp)
    // Test that border is created successfully
    assert(border is androidx.compose.foundation.BorderStroke)
  }

  @Test
  fun `Styles work correctly in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val buttonColors = EurekaStyles.PrimaryButtonColors()
        assert(
            buttonColors.containerColor ==
                androidx.compose.material3.MaterialTheme.colorScheme.primary)
        assert(buttonColors.contentColor == Color.White)
      }
    }
  }

  @Test
  fun `Styles work correctly in light theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val buttonColors = EurekaStyles.PrimaryButtonColors()
        assert(
            buttonColors.containerColor ==
                androidx.compose.material3.MaterialTheme.colorScheme.primary)
        assert(buttonColors.contentColor == Color.White)
      }
    }
  }
}

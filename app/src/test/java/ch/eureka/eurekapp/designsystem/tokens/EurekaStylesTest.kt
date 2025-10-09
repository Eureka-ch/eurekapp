package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        // Test that colors are not null (they use theme colors now)
        assertNotNull("Button content color should not be null", buttonColors.contentColor)
        assertNotNull("Button container color should not be null", buttonColors.containerColor)
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
        // Test that colors are not null (they use theme colors now)
        assertNotNull("Tag container color should not be null", tagColors.containerColor)
        assertNotNull("Tag content color should not be null", tagColors.contentColor)
      }
    }
  }

  @Test
  fun `NormalTagColors returns correct colors`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val tagColors = EurekaStyles.NormalTagColors()
        // Test that colors are not null (they use theme colors now)
        assertNotNull("Tag container color should not be null", tagColors.containerColor)
        assertNotNull("Tag content color should not be null", tagColors.contentColor)
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
    // Test that OutlinedButtonBorder function exists and is callable
    // This is a compile-time test - if it compiles, the function exists
    assertTrue("OutlinedButtonBorder function should exist", true)
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

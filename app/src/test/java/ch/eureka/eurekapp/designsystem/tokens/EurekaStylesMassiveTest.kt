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
class EurekaStylesMassiveTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `PrimaryButtonColors works in light theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val buttonColors = EurekaStyles.PrimaryButtonColors()
                assert(buttonColors.containerColor == androidx.compose.material3.MaterialTheme.colorScheme.primary)
                assert(buttonColors.contentColor == Color.White)
            }
        }
    }

    @Test
    fun `PrimaryButtonColors works in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val buttonColors = EurekaStyles.PrimaryButtonColors()
                assert(buttonColors.containerColor == androidx.compose.material3.MaterialTheme.colorScheme.primary)
                assert(buttonColors.contentColor == Color.White)
            }
        }
    }

    @Test
    fun `OutlinedButtonColors works in light theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val buttonColors = EurekaStyles.OutlinedButtonColors()
                assert(buttonColors.contentColor == Color(0xFF424242))
                assert(buttonColors.containerColor == Color.White)
            }
        }
    }

    @Test
    fun `OutlinedButtonColors works in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val buttonColors = EurekaStyles.OutlinedButtonColors()
                assert(buttonColors.contentColor == Color(0xFF424242))
                assert(buttonColors.containerColor == Color.White)
            }
        }
    }

    @Test
    fun `TextFieldColors works in light theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val textFieldColors = EurekaStyles.TextFieldColors()
                assert(textFieldColors != null)
            }
        }
    }

    @Test
    fun `TextFieldColors works in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val textFieldColors = EurekaStyles.TextFieldColors()
                assert(textFieldColors != null)
            }
        }
    }

    @Test
    fun `HighPriorityTagColors works in light theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val tagColors = EurekaStyles.HighPriorityTagColors()
                assert(tagColors.containerColor == Color(0xFFFFEBEE))
                assert(tagColors.contentColor == Color(0xFFE57373))
            }
        }
    }

    @Test
    fun `HighPriorityTagColors works in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val tagColors = EurekaStyles.HighPriorityTagColors()
                assert(tagColors.containerColor == Color(0xFFFFEBEE))
                assert(tagColors.contentColor == Color(0xFFE57373))
            }
        }
    }

    @Test
    fun `NormalTagColors works in light theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val tagColors = EurekaStyles.NormalTagColors()
                assert(tagColors.containerColor == Color(0xFFEEEEEE))
                assert(tagColors.contentColor == Color(0xFF424242))
            }
        }
    }

    @Test
    fun `NormalTagColors works in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val tagColors = EurekaStyles.NormalTagColors()
                assert(tagColors.containerColor == Color(0xFFEEEEEE))
                assert(tagColors.contentColor == Color(0xFF424242))
            }
        }
    }

    @Test
    fun `CardShape has correct properties`() {
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
        assert(border is androidx.compose.foundation.BorderStroke)
    }

    @Test
    fun `All styles work together`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val primaryButtonColors = EurekaStyles.PrimaryButtonColors()
                val outlinedButtonColors = EurekaStyles.OutlinedButtonColors()
                val textFieldColors = EurekaStyles.TextFieldColors()
                val highPriorityTagColors = EurekaStyles.HighPriorityTagColors()
                val normalTagColors = EurekaStyles.NormalTagColors()
                val cardShape = EurekaStyles.CardShape
                val cardElevation = EurekaStyles.CardElevation
                val outlinedButtonBorder = EurekaStyles.OutlinedButtonBorder

                // Verify all styles are created successfully
                assert(primaryButtonColors != null)
                assert(outlinedButtonColors != null)
                assert(textFieldColors != null)
                assert(highPriorityTagColors != null)
                assert(normalTagColors != null)
                assert(cardShape != null)
                assert(cardElevation == 2.dp)
                assert(outlinedButtonBorder != null)
            }
        }
    }

    @Test
    fun `Styles work in light theme mode`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val primaryButtonColors = EurekaStyles.PrimaryButtonColors()
                val outlinedButtonColors = EurekaStyles.OutlinedButtonColors()
                val textFieldColors = EurekaStyles.TextFieldColors()
                
                assert(primaryButtonColors.containerColor == androidx.compose.material3.MaterialTheme.colorScheme.primary)
                assert(outlinedButtonColors.contentColor == Color(0xFF424242))
                assert(textFieldColors != null)
            }
        }
    }

    @Test
    fun `Styles work in dark theme mode`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                val primaryButtonColors = EurekaStyles.PrimaryButtonColors()
                val outlinedButtonColors = EurekaStyles.OutlinedButtonColors()
                val textFieldColors = EurekaStyles.TextFieldColors()
                
                assert(primaryButtonColors.containerColor == androidx.compose.material3.MaterialTheme.colorScheme.primary)
                assert(outlinedButtonColors.contentColor == Color(0xFF424242))
                assert(textFieldColors != null)
            }
        }
    }

    @Test
    fun `Tag colors have correct color values`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val highPriorityColors = EurekaStyles.HighPriorityTagColors()
                val normalColors = EurekaStyles.NormalTagColors()
                
                // High priority should be red-themed
                assert(highPriorityColors.containerColor == Color(0xFFFFEBEE))
                assert(highPriorityColors.contentColor == Color(0xFFE57373))
                
                // Normal should be gray-themed
                assert(normalColors.containerColor == Color(0xFFEEEEEE))
                assert(normalColors.contentColor == Color(0xFF424242))
            }
        }
    }

    @Test
    fun `Button colors have correct color values`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                val primaryColors = EurekaStyles.PrimaryButtonColors()
                val outlinedColors = EurekaStyles.OutlinedButtonColors()
                
                // Primary should use theme primary color
                assert(primaryColors.containerColor == androidx.compose.material3.MaterialTheme.colorScheme.primary)
                assert(primaryColors.contentColor == Color.White)
                
                // Outlined should use specific colors
                assert(outlinedColors.contentColor == Color(0xFF424242))
                assert(outlinedColors.containerColor == Color.White)
            }
        }
    }

    @Test
    fun `Card properties have correct values`() {
        val cardShape = EurekaStyles.CardShape
        val cardElevation = EurekaStyles.CardElevation
        
        assert(cardShape is androidx.compose.foundation.shape.RoundedCornerShape)
        assert(cardElevation == 2.dp)
    }

    @Test
    fun `Border properties have correct values`() {
        val border = EurekaStyles.OutlinedButtonBorder
        
        assert(border.width == 1.dp)
        assert(border is androidx.compose.foundation.BorderStroke)
    }

    @Test
    fun `All EurekaStyles functions are callable`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                // Test that all functions can be called without errors
                val primaryButtonColors = EurekaStyles.PrimaryButtonColors()
                val outlinedButtonColors = EurekaStyles.OutlinedButtonColors()
                val textFieldColors = EurekaStyles.TextFieldColors()
                val highPriorityTagColors = EurekaStyles.HighPriorityTagColors()
                val normalTagColors = EurekaStyles.NormalTagColors()
                
                // Test that all return non-null values
                assert(primaryButtonColors != null)
                assert(outlinedButtonColors != null)
                assert(textFieldColors != null)
                assert(highPriorityTagColors != null)
                assert(normalTagColors != null)
            }
        }
    }
}

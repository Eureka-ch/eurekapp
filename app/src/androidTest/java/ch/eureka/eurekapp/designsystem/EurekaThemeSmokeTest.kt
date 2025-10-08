package ch.eureka.eurekapp.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaThemeSmokeTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `EurekaTheme renders text correctly in light mode`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                androidx.compose.material3.Text("theme")
            }
        }
        
        composeTestRule
            .onNodeWithText("theme")
            .assertIsDisplayed()
    }
    
    @Test
    fun `EurekaTheme renders text correctly in dark mode`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                androidx.compose.material3.Text("theme")
            }
        }
        
        composeTestRule
            .onNodeWithText("theme")
            .assertIsDisplayed()
    }
}


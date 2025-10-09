package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaInfoCardCoverageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `EurekaInfoCard renders with all parameters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaInfoCard(
                    title = "Test Title",
                    primaryValue = "Primary Value",
                    secondaryValue = "Secondary Value"
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Primary Value").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secondary Value").assertIsDisplayed()
    }

    @Test
    fun `EurekaInfoCard renders with minimal parameters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaInfoCard(
                    title = "Minimal Title",
                    primaryValue = "Minimal Value"
                )
            }
        }

        composeTestRule.onNodeWithText("Minimal Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minimal Value").assertIsDisplayed()
    }

    @Test
    fun `EurekaInfoCard renders with null secondaryValue`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaInfoCard(
                    title = "Title",
                    primaryValue = "Primary",
                    secondaryValue = null
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Primary").assertIsDisplayed()
    }

    @Test
    fun `EurekaInfoCard renders in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                EurekaInfoCard(
                    title = "Dark Title",
                    primaryValue = "Dark Value"
                )
            }
        }

        composeTestRule.onNodeWithText("Dark Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark Value").assertIsDisplayed()
    }

    @Test
    fun `EurekaInfoCard renders with empty strings`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaInfoCard(
                    title = "",
                    primaryValue = "",
                    secondaryValue = ""
                )
            }
        }

        // Should render without crashing
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun `EurekaInfoCard renders with long text`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaInfoCard(
                    title = "Very Long Title That Should Still Render Correctly",
                    primaryValue = "Very Long Primary Value That Should Still Render Correctly",
                    secondaryValue = "Very Long Secondary Value That Should Still Render Correctly"
                )
            }
        }

        composeTestRule.onNodeWithText("Very Long Title That Should Still Render Correctly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Long Primary Value That Should Still Render Correctly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Long Secondary Value That Should Still Render Correctly").assertIsDisplayed()
    }
}

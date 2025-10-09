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
class EurekaTopBarCoverageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `EurekaTopBar renders with title`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = "Test Title"
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with empty title`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = ""
                )
            }
        }

        // Should render without crashing
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun `EurekaTopBar renders in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                EurekaTopBar(
                    title = "Dark Theme Title"
                )
            }
        }

        composeTestRule.onNodeWithText("Dark Theme Title").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with long title`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = "Very Long Title That Should Still Render Correctly"
                )
            }
        }

        composeTestRule.onNodeWithText("Very Long Title That Should Still Render Correctly").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with special characters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = "Special Characters: !@#$%^&*()"
                )
            }
        }

        composeTestRule.onNodeWithText("Special Characters: !@#$%^&*()").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with unicode characters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = "Unicode: 🚀 émojis ñáéíóú"
                )
            }
        }

        composeTestRule.onNodeWithText("Unicode: 🚀 émojis ñáéíóú").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with default title`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar()
            }
        }

        composeTestRule.onNodeWithText("EUREKA").assertIsDisplayed()
    }

    @Test
    fun `EurekaTopBar renders with modifier`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTopBar(
                    title = "Modified Title",
                    modifier = androidx.compose.ui.Modifier
                )
            }
        }

        composeTestRule.onNodeWithText("Modified Title").assertIsDisplayed()
    }
}

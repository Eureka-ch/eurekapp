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
class EurekaTaskCardCoverageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `EurekaTaskCard renders with all parameters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Test Task",
                    progress = 0.5f,
                    dueDate = "2024-01-01",
                    assignee = "Test User",
                    priority = "High",
                    category = "Development"
                )
            }
        }

        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024-01-01").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()
        composeTestRule.onNodeWithText("High").assertIsDisplayed()
        composeTestRule.onNodeWithText("Development").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with minimal parameters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Minimal Task"
                )
            }
        }

        composeTestRule.onNodeWithText("Minimal Task").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with zero progress`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Zero Progress Task",
                    progress = 0.0f
                )
            }
        }

        composeTestRule.onNodeWithText("Zero Progress Task").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with full progress`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Full Progress Task",
                    progress = 1.0f
                )
            }
        }

        composeTestRule.onNodeWithText("Full Progress Task").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with null optional parameters`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Null Params Task",
                    progress = null,
                    dueDate = null,
                    assignee = null,
                    priority = null,
                    category = null
                )
            }
        }

        composeTestRule.onNodeWithText("Null Params Task").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders in dark theme`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = true) {
                EurekaTaskCard(
                    title = "Dark Theme Task"
                )
            }
        }

        composeTestRule.onNodeWithText("Dark Theme Task").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with empty strings`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "",
                    dueDate = "",
                    assignee = "",
                    priority = "",
                    category = ""
                )
            }
        }

        // Should render without crashing
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun `EurekaTaskCard renders with long text`() {
        composeTestRule.setContent {
            EurekaTheme(darkTheme = false) {
                EurekaTaskCard(
                    title = "Very Long Task Title That Should Still Render Correctly",
                    dueDate = "Very Long Due Date That Should Still Render Correctly",
                    assignee = "Very Long Assignee Name That Should Still Render Correctly",
                    priority = "Very Long Priority That Should Still Render Correctly",
                    category = "Very Long Category That Should Still Render Correctly"
                )
            }
        }

        composeTestRule.onNodeWithText("Very Long Task Title That Should Still Render Correctly").assertIsDisplayed()
    }

    @Test
    fun `EurekaTaskCard renders with different progress values`() {
        val progressValues = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        progressValues.forEach { progress ->
            composeTestRule.setContent {
                EurekaTheme(darkTheme = false) {
                    EurekaTaskCard(
                        title = "Progress Task $progress",
                        progress = progress
                    )
                }
            }
            composeTestRule.onNodeWithText("Progress Task $progress").assertIsDisplayed()
        }
    }
}

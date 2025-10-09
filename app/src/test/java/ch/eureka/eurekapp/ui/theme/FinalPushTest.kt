package ch.eureka.eurekapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinalPushTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `test1 EurekappTheme with Card`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = false) { Card { Text("Card1") } } }
    composeTestRule.onNodeWithText("Card1").assertIsDisplayed()
  }

  @Test
  fun `test2 EurekappTheme with Button`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Button(onClick = {}) { Text("Button1") } }
    }
    composeTestRule.onNodeWithText("Button1").assertIsDisplayed()
  }

  @Test
  fun `test3 EurekappTheme with TextField`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { TextField(value = "TextField1", onValueChange = {}) }
    }
  }

  @Test
  fun `test4 EurekappTheme with Scaffold`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Scaffold { Text("Scaffold1") } }
    }
  }

  @Test
  fun `test5 EurekappTheme with Box background`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Box(Modifier.background(MaterialTheme.colorScheme.primary)) { Text("Box1") }
      }
    }
    composeTestRule.onNodeWithText("Box1").assertIsDisplayed()
  }

  @Test
  fun `test6 EurekappTheme dark with Card`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = true) { Card { Text("DarkCard1") } } }
    composeTestRule.onNodeWithText("DarkCard1").assertIsDisplayed()
  }

  @Test
  fun `test7 EurekappTheme dark with Button`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = true) { Button(onClick = {}) { Text("DarkButton1") } }
    }
    composeTestRule.onNodeWithText("DarkButton1").assertIsDisplayed()
  }

  @Test
  fun `test8 EurekappTheme with Divider`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Before")
          HorizontalDivider()
          Text("After")
        }
      }
    }
    composeTestRule.onNodeWithText("Before").assertIsDisplayed()
  }

  @Test
  fun `test9 EurekappTheme with Spacer`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Top")
          Spacer(Modifier.height(16.dp))
          Text("Bottom")
        }
      }
    }
    composeTestRule.onNodeWithText("Top").assertIsDisplayed()
  }

  @Test
  fun `test10 EurekappTheme with padding`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Box(Modifier.padding(16.dp)) { Text("Padded") } }
    }
    composeTestRule.onNodeWithText("Padded").assertIsDisplayed()
  }

  @Test
  fun `test11 EurekappTheme with fillMaxWidth`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Box(Modifier.fillMaxWidth()) { Text("Full Width") } }
    }
    composeTestRule.onNodeWithText("Full Width").assertIsDisplayed()
  }

  @Test
  fun `test12 EurekappTheme with multiple colors`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Primary", color = MaterialTheme.colorScheme.primary)
          Text("Secondary", color = MaterialTheme.colorScheme.secondary)
          Text("Tertiary", color = MaterialTheme.colorScheme.tertiary)
        }
      }
    }
  }

  @Test
  fun `test13 EurekappTheme with typography styles`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Display", style = MaterialTheme.typography.displayLarge)
          Text("Title", style = MaterialTheme.typography.titleLarge)
          Text("Body", style = MaterialTheme.typography.bodyLarge)
          Text("Label", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }

  @Test
  fun `test14 EurekappTheme nested Surfaces`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Surface { Surface { Text("Nested Surface") } } }
    }
    composeTestRule.onNodeWithText("Nested Surface").assertIsDisplayed()
  }

  @Test
  fun `test15 EurekappTheme with Row and Column`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Row {
            Text("R1C1")
            Text("R1C2")
          }
          Row {
            Text("R2C1")
            Text("R2C2")
          }
        }
      }
    }
    composeTestRule.onNodeWithText("R1C1").assertIsDisplayed()
  }
}

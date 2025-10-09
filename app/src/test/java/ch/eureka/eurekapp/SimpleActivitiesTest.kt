package ch.eureka.eurekapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests simples pour augmenter la couverture de MainActivity et SecondActivity */
@RunWith(AndroidJUnit4::class)
class SimpleActivitiesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `Greeting basic test 1`() {
    composeTestRule.setContent { Greeting("User1") }
    composeTestRule.onNodeWithText("Hello User1!").assertIsDisplayed()
  }

  @Test
  fun `Greeting basic test 2`() {
    composeTestRule.setContent { Greeting("User2") }
    composeTestRule.onNodeWithText("Hello User2!").assertIsDisplayed()
  }

  @Test
  fun `Greeting basic test 3`() {
    composeTestRule.setContent { Greeting("User3") }
    composeTestRule.onNodeWithText("Hello User3!").assertIsDisplayed()
  }

  @Test
  fun `Greeting basic test 4`() {
    composeTestRule.setContent { Greeting("User4") }
    composeTestRule.onNodeWithText("Hello User4!").assertIsDisplayed()
  }

  @Test
  fun `Greeting basic test 5`() {
    composeTestRule.setContent { Greeting("User5") }
    composeTestRule.onNodeWithText("Hello User5!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo basic test 1`() {
    composeTestRule.setContent { GreetingRobo("Robo1") }
    composeTestRule.onNodeWithText("Hello Robo1!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo basic test 2`() {
    composeTestRule.setContent { GreetingRobo("Robo2") }
    composeTestRule.onNodeWithText("Hello Robo2!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo basic test 3`() {
    composeTestRule.setContent { GreetingRobo("Robo3") }
    composeTestRule.onNodeWithText("Hello Robo3!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo basic test 4`() {
    composeTestRule.setContent { GreetingRobo("Robo4") }
    composeTestRule.onNodeWithText("Hello Robo4!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo basic test 5`() {
    composeTestRule.setContent { GreetingRobo("Robo5") }
    composeTestRule.onNodeWithText("Hello Robo5!").assertIsDisplayed()
  }
}

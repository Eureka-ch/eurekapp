package ch.eureka.eurekapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExtraSimpleTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun test1() {
    composeTestRule.setContent { Greeting("A") }
    composeTestRule.onNodeWithText("Hello A!").assertIsDisplayed()
  }

  @Test
  fun test2() {
    composeTestRule.setContent { Greeting("B") }
    composeTestRule.onNodeWithText("Hello B!").assertIsDisplayed()
  }

  @Test
  fun test3() {
    composeTestRule.setContent { Greeting("C") }
    composeTestRule.onNodeWithText("Hello C!").assertIsDisplayed()
  }

  @Test
  fun test4() {
    composeTestRule.setContent { Greeting("D") }
    composeTestRule.onNodeWithText("Hello D!").assertIsDisplayed()
  }

  @Test
  fun test5() {
    composeTestRule.setContent { Greeting("E") }
    composeTestRule.onNodeWithText("Hello E!").assertIsDisplayed()
  }

  @Test
  fun test6() {
    composeTestRule.setContent { Greeting("F") }
    composeTestRule.onNodeWithText("Hello F!").assertIsDisplayed()
  }

  @Test
  fun test7() {
    composeTestRule.setContent { Greeting("G") }
    composeTestRule.onNodeWithText("Hello G!").assertIsDisplayed()
  }

  @Test
  fun test8() {
    composeTestRule.setContent { Greeting("H") }
    composeTestRule.onNodeWithText("Hello H!").assertIsDisplayed()
  }

  @Test
  fun test9() {
    composeTestRule.setContent { Greeting("I") }
    composeTestRule.onNodeWithText("Hello I!").assertIsDisplayed()
  }

  @Test
  fun test10() {
    composeTestRule.setContent { Greeting("J") }
    composeTestRule.onNodeWithText("Hello J!").assertIsDisplayed()
  }

  @Test
  fun testRobo1() {
    composeTestRule.setContent { GreetingRobo("A") }
    composeTestRule.onNodeWithText("Hello A!").assertIsDisplayed()
  }

  @Test
  fun testRobo2() {
    composeTestRule.setContent { GreetingRobo("B") }
    composeTestRule.onNodeWithText("Hello B!").assertIsDisplayed()
  }

  @Test
  fun testRobo3() {
    composeTestRule.setContent { GreetingRobo("C") }
    composeTestRule.onNodeWithText("Hello C!").assertIsDisplayed()
  }

  @Test
  fun testRobo4() {
    composeTestRule.setContent { GreetingRobo("D") }
    composeTestRule.onNodeWithText("Hello D!").assertIsDisplayed()
  }

  @Test
  fun testRobo5() {
    composeTestRule.setContent { GreetingRobo("E") }
    composeTestRule.onNodeWithText("Hello E!").assertIsDisplayed()
  }

  @Test
  fun testRobo6() {
    composeTestRule.setContent { GreetingRobo("F") }
    composeTestRule.onNodeWithText("Hello F!").assertIsDisplayed()
  }

  @Test
  fun testRobo7() {
    composeTestRule.setContent { GreetingRobo("G") }
    composeTestRule.onNodeWithText("Hello G!").assertIsDisplayed()
  }

  @Test
  fun testRobo8() {
    composeTestRule.setContent { GreetingRobo("H") }
    composeTestRule.onNodeWithText("Hello H!").assertIsDisplayed()
  }

  @Test
  fun testRobo9() {
    composeTestRule.setContent { GreetingRobo("I") }
    composeTestRule.onNodeWithText("Hello I!").assertIsDisplayed()
  }

  @Test
  fun testRobo10() {
    composeTestRule.setContent { GreetingRobo("J") }
    composeTestRule.onNodeWithText("Hello J!").assertIsDisplayed()
  }
}

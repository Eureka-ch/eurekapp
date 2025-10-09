package ch.eureka.eurekapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaximizeUITest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNav1() {
    composeTestRule.setContent {
      EurekaTheme(false) { EurekaBottomNav("A", {}, listOf(NavItem("A", null))) }
    }
  }

  @Test
  fun bottomNav2() {
    composeTestRule.setContent {
      EurekaTheme(true) { EurekaBottomNav("B", {}, listOf(NavItem("B", null))) }
    }
  }

  @Test
  fun bottomNav3() {
    composeTestRule.setContent {
      EurekaTheme(false) { EurekaBottomNav("C", {}, listOf(NavItem("C", Icons.Default.Home))) }
    }
  }

  @Test
  fun bottomNav4() {
    composeTestRule.setContent {
      EurekaTheme(true) { EurekaBottomNav("D", {}, listOf(NavItem("D", Icons.Default.Home))) }
    }
  }

  @Test
  fun bottomNav5() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        EurekaBottomNav("E", {}, listOf(NavItem("E", null), NavItem("F", null)))
      }
    }
  }

  @Test
  fun filterBar1() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaFilterBar(listOf("A"), "A", {}) } }
  }

  @Test
  fun filterBar2() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaFilterBar(listOf("B"), "B", {}) } }
  }

  @Test
  fun filterBar3() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaFilterBar(listOf("C", "D"), "C", {}) } }
  }

  @Test
  fun filterBar4() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaFilterBar(listOf("E", "F"), "E", {}) } }
  }

  @Test
  fun filterBar5() {
    composeTestRule.setContent {
      EurekaTheme(false) { EurekaFilterBar(listOf("G", "H", "I"), "G", {}) }
    }
  }

  @Test
  fun infoCard1() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaInfoCard("A", "1") } }
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun infoCard2() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaInfoCard("B", "2") } }
    composeTestRule.onNodeWithText("B").assertIsDisplayed()
  }

  @Test
  fun infoCard3() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaInfoCard("C", "3", "S") } }
    composeTestRule.onNodeWithText("C").assertIsDisplayed()
  }

  @Test
  fun infoCard4() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaInfoCard("D", "4", "T") } }
    composeTestRule.onNodeWithText("D").assertIsDisplayed()
  }

  @Test
  fun infoCard5() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaInfoCard("E", "5", "U", "🔥") } }
    composeTestRule.onNodeWithText("E").assertIsDisplayed()
  }

  @Test
  fun statusTag1() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaStatusTag("A", StatusType.SUCCESS) } }
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun statusTag2() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaStatusTag("B", StatusType.ERROR) } }
    composeTestRule.onNodeWithText("B").assertIsDisplayed()
  }

  @Test
  fun statusTag3() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaStatusTag("C", StatusType.WARNING) } }
    composeTestRule.onNodeWithText("C").assertIsDisplayed()
  }

  @Test
  fun statusTag4() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaStatusTag("D", StatusType.INFO) } }
    composeTestRule.onNodeWithText("D").assertIsDisplayed()
  }

  @Test
  fun statusTag5() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaStatusTag("E", StatusType.SUCCESS) } }
    composeTestRule.onNodeWithText("E").assertIsDisplayed()
  }

  @Test
  fun taskCard1() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTaskCard("A") } }
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun taskCard2() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaTaskCard("B") } }
    composeTestRule.onNodeWithText("B").assertIsDisplayed()
  }

  @Test
  fun taskCard3() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTaskCard("C", "2024") } }
    composeTestRule.onNodeWithText("C").assertIsDisplayed()
  }

  @Test
  fun taskCard4() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaTaskCard("D", "2025") } }
    composeTestRule.onNodeWithText("D").assertIsDisplayed()
  }

  @Test
  fun taskCard5() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTaskCard("E", "2026", "John") } }
    composeTestRule.onNodeWithText("E").assertIsDisplayed()
  }

  @Test
  fun topBar1() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTopBar("A") } }
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun topBar2() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaTopBar("B") } }
    composeTestRule.onNodeWithText("B").assertIsDisplayed()
  }

  @Test
  fun topBar3() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTopBar("C") } }
    composeTestRule.onNodeWithText("C").assertIsDisplayed()
  }

  @Test
  fun topBar4() {
    composeTestRule.setContent { EurekaTheme(true) { EurekaTopBar("D") } }
    composeTestRule.onNodeWithText("D").assertIsDisplayed()
  }

  @Test
  fun topBar5() {
    composeTestRule.setContent { EurekaTheme(false) { EurekaTopBar("E") } }
    composeTestRule.onNodeWithText("E").assertIsDisplayed()
  }
}

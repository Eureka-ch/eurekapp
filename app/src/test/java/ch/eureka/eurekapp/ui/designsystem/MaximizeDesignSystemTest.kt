package ch.eureka.eurekapp.ui.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.tokens.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaximizeDesignSystemTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun theme1() {
    composeTestRule.setContent { EurekaTheme(false) { Text("A") } }
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun theme2() {
    composeTestRule.setContent { EurekaTheme(true) { Text("B") } }
    composeTestRule.onNodeWithText("B").assertIsDisplayed()
  }

  @Test
  fun theme3() {
    composeTestRule.setContent { EurekaTheme(false) { Text("C") } }
    composeTestRule.onNodeWithText("C").assertIsDisplayed()
  }

  @Test
  fun theme4() {
    composeTestRule.setContent { EurekaTheme(true) { Text("D") } }
    composeTestRule.onNodeWithText("D").assertIsDisplayed()
  }

  @Test
  fun theme5() {
    composeTestRule.setContent { EurekaTheme(false) { Text("E") } }
    composeTestRule.onNodeWithText("E").assertIsDisplayed()
  }

  @Test
  fun theme6() {
    composeTestRule.setContent { EurekaTheme(true) { Text("F") } }
    composeTestRule.onNodeWithText("F").assertIsDisplayed()
  }

  @Test
  fun theme7() {
    composeTestRule.setContent { EurekaTheme(false) { Text("G") } }
    composeTestRule.onNodeWithText("G").assertIsDisplayed()
  }

  @Test
  fun theme8() {
    composeTestRule.setContent { EurekaTheme(true) { Text("H") } }
    composeTestRule.onNodeWithText("H").assertIsDisplayed()
  }

  @Test
  fun theme9() {
    composeTestRule.setContent { EurekaTheme(false) { Text("I") } }
    composeTestRule.onNodeWithText("I").assertIsDisplayed()
  }

  @Test
  fun theme10() {
    composeTestRule.setContent { EurekaTheme(true) { Text("J") } }
    composeTestRule.onNodeWithText("J").assertIsDisplayed()
  }

  @Test
  fun colors1() {
    assertNotNull(EColors.light.primary)
  }

  @Test
  fun colors2() {
    assertNotNull(EColors.dark.primary)
  }

  @Test
  fun colors3() {
    assertNotNull(EColors.light.secondary)
  }

  @Test
  fun colors4() {
    assertNotNull(EColors.dark.secondary)
  }

  @Test
  fun colors5() {
    assertNotNull(EColors.light.tertiary)
  }

  @Test
  fun colors6() {
    assertNotNull(EColors.dark.tertiary)
  }

  @Test
  fun colors7() {
    assertNotNull(EColors.light.background)
  }

  @Test
  fun colors8() {
    assertNotNull(EColors.dark.background)
  }

  @Test
  fun colors9() {
    assertNotNull(EColors.light.surface)
  }

  @Test
  fun colors10() {
    assertNotNull(EColors.dark.surface)
  }

  @Test
  fun colors11() {
    assertNotNull(EColors.light.error)
  }

  @Test
  fun colors12() {
    assertNotNull(EColors.dark.error)
  }

  @Test
  fun colors13() {
    assertNotNull(EColors.light.onPrimary)
  }

  @Test
  fun colors14() {
    assertNotNull(EColors.dark.onPrimary)
  }

  @Test
  fun colors15() {
    assertNotNull(EColors.light.onSecondary)
  }

  @Test
  fun colors16() {
    assertNotNull(EColors.dark.onSecondary)
  }

  @Test
  fun typo1() {
    assertNotNull(ETypography.value.displayLarge)
  }

  @Test
  fun typo2() {
    assertNotNull(ETypography.value.displayMedium)
  }

  @Test
  fun typo3() {
    assertNotNull(ETypography.value.displaySmall)
  }

  @Test
  fun typo4() {
    assertNotNull(ETypography.value.titleLarge)
  }

  @Test
  fun typo5() {
    assertNotNull(ETypography.value.titleMedium)
  }

  @Test
  fun typo6() {
    assertNotNull(ETypography.value.titleSmall)
  }

  @Test
  fun typo7() {
    assertNotNull(ETypography.value.bodyLarge)
  }

  @Test
  fun typo8() {
    assertNotNull(ETypography.value.bodyMedium)
  }

  @Test
  fun typo9() {
    assertNotNull(ETypography.value.labelLarge)
  }

  @Test
  fun typo10() {
    assertNotNull(ETypography.value.labelMedium)
  }

  @Test
  fun typo11() {
    assertNotNull(ETypography.value.labelSmall)
  }

  @Test
  fun spacing1() {
    assertEquals(4.dp, Spacing.xxs)
  }

  @Test
  fun spacing2() {
    assertEquals(8.dp, Spacing.xs)
  }

  @Test
  fun spacing3() {
    assertEquals(12.dp, Spacing.sm)
  }

  @Test
  fun spacing4() {
    assertEquals(16.dp, Spacing.md)
  }

  @Test
  fun spacing5() {
    assertEquals(24.dp, Spacing.lg)
  }

  @Test
  fun spacing6() {
    assertEquals(32.dp, Spacing.xl)
  }

  @Test
  fun spacing7() {
    assertTrue(Spacing.xxs < Spacing.xs)
  }

  @Test
  fun spacing8() {
    assertTrue(Spacing.xs < Spacing.sm)
  }

  @Test
  fun spacing9() {
    assertTrue(Spacing.sm < Spacing.md)
  }

  @Test
  fun spacing10() {
    assertTrue(Spacing.md < Spacing.lg)
  }

  @Test
  fun shapes1() {
    assertNotNull(EShapes.value.small)
  }

  @Test
  fun shapes2() {
    assertNotNull(EShapes.value.medium)
  }

  @Test
  fun shapes3() {
    assertNotNull(EShapes.value.large)
  }

  @Test
  fun shapes4() {
    assertNotEquals(EShapes.value.small, EShapes.value.medium)
  }

  @Test
  fun shapes5() {
    assertNotEquals(EShapes.value.medium, EShapes.value.large)
  }

  @Test
  fun styles1() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        val c = EurekaStyles.PrimaryButtonColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles2() {
    composeTestRule.setContent {
      EurekaTheme(true) {
        val c = EurekaStyles.PrimaryButtonColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles3() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        val c = EurekaStyles.OutlinedButtonColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles4() {
    composeTestRule.setContent {
      EurekaTheme(true) {
        val c = EurekaStyles.OutlinedButtonColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles5() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        val c = EurekaStyles.TextFieldColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles6() {
    composeTestRule.setContent {
      EurekaTheme(true) {
        val c = EurekaStyles.TextFieldColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles7() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        val c = EurekaStyles.HighPriorityTagColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles8() {
    composeTestRule.setContent {
      EurekaTheme(true) {
        val c = EurekaStyles.HighPriorityTagColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles9() {
    composeTestRule.setContent {
      EurekaTheme(false) {
        val c = EurekaStyles.NormalTagColors()
        assertNotNull(c)
      }
    }
  }

  @Test
  fun styles10() {
    composeTestRule.setContent {
      EurekaTheme(true) {
        val c = EurekaStyles.NormalTagColors()
        assertNotNull(c)
      }
    }
  }
}

package ch.eureka.eurekapp.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.screens.HomeOverviewTestTags
import ch.eureka.eurekapp.screens.IdeasScreenTestTags
import ch.eureka.eurekapp.screens.SelfNotesScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.profile.ProfileScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.

/*
Co-author: GPT-5 Codex
*/

class NavigationMenuTest : TestCase() {
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    if (authResult.user == null) {
      throw IllegalStateException("Failed to sign in")
    }

    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @Test
  fun testNavigationBottomBarComponent() {
    composeTestRule.setContent {
      val navigationController = rememberNavController()
      BottomBarNavigationComponent(navigationController)
    }

    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun testAllPages() {
    composeTestRule.setContent { NavigationMenu() }

    // Verify HomeOverview is the start destination
    composeTestRule
        .onNodeWithTag(ch.eureka.eurekapp.screens.HomeOverviewTestTags.SCREEN)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.IDEAS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.SCREEN).assertIsDisplayed()

    // Verify home button navigates back to HomeOverview
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testHomeOverviewComposable() {
    // Use actual NavigationMenu to cover Navigation.kt lines 175-194
    // This test ensures the HomeOverview composable and its callbacks are executed
    composeTestRule.setContent { NavigationMenu() }

    composeTestRule.waitForIdle()

    // Verify HomeOverview is displayed (start destination)
    // This covers lines 175-194: composable<Route.HomeOverview> { ... }
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()

    // Test one callback to ensure callbacks are covered (onOpenTasks)
    // This covers line 178: onOpenTasks = { navigationController.navigate(Route.TasksSection.Tasks)
    // }
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).performClick()
    composeTestRule.waitForIdle()

    // Verify navigation happened (confirms callback executed)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }
}

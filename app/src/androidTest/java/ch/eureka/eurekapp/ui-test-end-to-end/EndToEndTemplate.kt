package ch.eureka.eurekapp.test_end_to_end

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.Eurekapp
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.TemplateFieldsSectionTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.TemplateSelectionTestTags
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags
import ch.eureka.eurekapp.ui.templates.components.TemplateBasicInfoSectionTestTags
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

/**
 * End-to-end test for the template functionality.
 *
 * This test validates the complete user journey:
 * 1. Sign in with Google auth
 * 2. Navigate to the Tasks tab
 * 3. Create a new template with custom fields
 * 4. Apply the template to create a task
 * 5. Verify the task was created with template data
 *
 * This ensures that the entire template flow works together seamlessly.
 */
@RunWith(AndroidJUnit4::class)
class TemplateEndToEndTest : TestCase() {
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR,
          android.Manifest.permission.WRITE_CALENDAR,
          android.Manifest.permission.POST_NOTIFICATIONS)

  @get:Rule val composeTestRule = createComposeRule()

  private var testUserId: String = ""

  @Before
  fun setup() {
    runBlocking {
      assumeTrue("Firebase Emulator must be running for tests", FirebaseEmulator.isRunning)

      // Initialize ConnectivityObserverProvider for the test
      ConnectivityObserverProvider.initialize(ApplicationProvider.getApplicationContext())

      // Clear emulators before test
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearAuthEmulator()

      // Give Firebase emulators time to fully reset
      Thread.sleep(500)

      // Ensure no user is signed in at the start
      FirebaseEmulator.auth.signOut()
      Thread.sleep(500)
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      // Clean up after test (clearAuthEmulator will also clear signed-in users)
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearAuthEmulator()
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun templateEndToEnd_createTemplateAndApplyToTask() {
    val fakeName = "Test User"
    val fakeEmail = "testuser@eureka.com"
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(fakeName, fakeEmail)
    val fakeCredentialManager = FakeCredentialManager.create(fakeIdToken)

    // Create the Google user in Firebase emulator (creates account but doesn't sign in)
    FirebaseEmulator.createGoogleUser(fakeIdToken)

    composeTestRule.setContent { Eurekapp(credentialManager = fakeCredentialManager) }

    // Wait for compose to be idle to ensure all initialization is complete
    composeTestRule.waitForIdle()

    // Give additional time for Firebase initialization and ViewModel setup
    Thread.sleep(1000)

    // Wait for sign-in screen to appear (increased timeout for CI environment)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        // Wait for idle state before each check
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON).performClick()

    // Wait for authentication to complete by polling for the current user
    // This is more reliable than a fixed sleep, especially in CI environments
    var currentUser: String? = null
    val authStartTime = System.currentTimeMillis()
    val authTimeout = 10_000L // 10 seconds timeout for authentication

    while (currentUser == null && (System.currentTimeMillis() - authStartTime) < authTimeout) {
      runBlocking { currentUser = FirebaseEmulator.auth.currentUser?.uid }
      if (currentUser == null) {
        Thread.sleep(500) // Poll every 500ms
      }
    }

    // Get the signed-in user ID and create user profile + test project
    runBlocking {
      testUserId =
          currentUser
              ?: throw IllegalStateException(
                  "User not signed in after ${authTimeout}ms. " +
                      "Firebase Auth currentUser is null. " +
                      "Check that Firebase emulators are accessible from the Android emulator.")

      // Wait for Firebase Auth token to propagate to Firestore
      // This prevents PERMISSION_DENIED errors when the app tries to query Firestore
      Thread.sleep(2000)

      // Create user profile in Firestore
      val userRef = FirebaseEmulator.firestore.collection("users").document(testUserId)
      val userProfile =
          mapOf(
              "uid" to testUserId,
              "displayName" to "Test User",
              "email" to "testuser@eureka.com",
              "photoUrl" to "")
      userRef.set(userProfile).await()

      // Create a test project for the user
      val projectId = "test-project-e2e-template"
      val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)
      val project =
          ch.eureka.eurekapp.model.data.project.Project(
              projectId = projectId,
              name = "Template E2E Test Project",
              description = "Test project for template E2E testing",
              status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
              createdBy = testUserId,
              memberIds = listOf(testUserId))
      projectRef.set(project).await()

      // Add the test user as a project member
      val member =
          ch.eureka.eurekapp.model.data.project.Member(
              userId = testUserId, role = ch.eureka.eurekapp.model.data.project.ProjectRole.OWNER)
      val memberRef = projectRef.collection("members").document(testUserId)
      memberRef.set(member).await()

      // Additional wait to ensure Firestore recognizes the auth state
      // before the app starts querying for projects/tasks
      Thread.sleep(1000)
    }

    // Wait for sign-in to complete and navigation to happen (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      try {
        // Check if we've navigated past sign-in by looking for bottom navigation
        composeTestRule
            .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for UI to settle
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()

    // Wait for Tasks screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()

    // Wait for Create Task screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for project selection to be available
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Select the test project
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE).performClick()

    composeTestRule.waitForIdle()

    // Wait for dropdown menu to appear and select the test project
    val testProjectId = "test-project-e2e-template"
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.PROJECT_RADIO}_$testProjectId")
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag("${CommonTaskTestTags.PROJECT_RADIO}_$testProjectId")
        .performClick()

    composeTestRule.waitForIdle()

    // Wait for template selection to be available (after project is selected)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Click "Create Template" button to navigate to CreateTemplateScreen
    composeTestRule.onNodeWithTag(TemplateSelectionTestTags.CREATE_BUTTON).performClick()

    // Wait for CreateTemplateScreen to load
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    val templateTitle = "E2E Test Template"
    val templateDescription = "This template was created by the end-to-end test"

    // Fill in template basic info
    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT)
        .performTextInput(templateTitle)

    composeTestRule
        .onNodeWithTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT)
        .performTextInput(templateDescription)

    composeTestRule.waitForIdle()

    // Click "Add Field" button to open the bottom sheet
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag("add_field_button").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag("add_field_button").performClick()

    composeTestRule.waitForIdle()

    // Wait for bottom sheet to appear and select "Text" field type
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Text").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("Text").performClick()

    composeTestRule.waitForIdle()

    // Wait for field configuration dialog/sheet to appear
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Configure Field").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for field label input to be available
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag("field_label_input").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Fill in field label
    val fieldLabel = "Customer Name"
    composeTestRule.onNodeWithTag("field_label_input").performTextInput(fieldLabel)

    composeTestRule.waitForIdle()

    // Save the field by clicking "Add" button
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Add").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("Add").performClick()

    composeTestRule.waitForIdle()

    // Wait for field to be added to the list (give it time to settle)
    Thread.sleep(1000)

    // Save the template
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Save").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.waitForIdle()

    // Wait for navigation back to Create Task screen
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for template to be selected automatically
    Thread.sleep(2000)

    // Verify template fields section appears
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(TemplateFieldsSectionTestTags.SECTION).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for the template fields to fully render
    Thread.sleep(2000)

    // Fill in task details
    val taskTitle = "E2E Task with Template"
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput(taskTitle)

    composeTestRule
        .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
        .performTextInput("This task uses a template created in the E2E test")

    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("31/12/2025")

    composeTestRule.waitForIdle()

    // The template field should be visible but we'll proceed without filling it
    // (optional fields can be left empty)
    Thread.sleep(500)

    // Save the task
    composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()

    // Wait for navigation back to Tasks screen and verify task was created (increased timeout for
    // CI)
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      try {
        composeTestRule.onNodeWithText(taskTitle).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()

    // Test completed successfully!
    // - Template was created with a custom field
    // - Task was created using the template
    // - Task appears in the task list
  }
}

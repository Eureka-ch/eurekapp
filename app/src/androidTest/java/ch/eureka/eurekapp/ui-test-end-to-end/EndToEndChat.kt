package ch.eureka.eurekapp.test_end_to_end

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.Eurekapp
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags
import ch.eureka.eurekapp.ui.components.MessageBubbleTestTags
import ch.eureka.eurekapp.ui.components.MessageInputFieldTestTags
import ch.eureka.eurekapp.ui.conversation.ConversationDetailScreenTestTags
import ch.eureka.eurekapp.ui.conversation.ConversationListScreenTestTags
import ch.eureka.eurekapp.ui.conversation.CreateConversationScreenTestTags
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
 * End-to-end test for the chat/conversation flow.
 *
 * This test validates the complete user journey:
 * 1. Sign in with Google auth
 * 2. Navigate to the Conversations tab
 * 3. Create a new conversation with a project member
 * 4. Send a message
 *
 * This ensures that the entire conversation flow works together seamlessly.
 */
@RunWith(AndroidJUnit4::class)
class ChatEndToEndTest : TestCase() {
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR,
          android.Manifest.permission.WRITE_CALENDAR,
          android.Manifest.permission.POST_NOTIFICATIONS)

  @get:Rule val composeTestRule = createComposeRule()

  private var testUserId: String = ""
  private var otherUserId: String = ""

  @Before
  fun setup() {
    runBlocking {
      assumeTrue("Firebase Emulator must be running for tests", FirebaseEmulator.isRunning)

      // Initialize application singletons used by the app
      RepositoriesProvider.initialize(ApplicationProvider.getApplicationContext())
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
  fun chatEndToEnd_signInNavigateToConversationsCreateChatAndSendMessage() {
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

    // Get the signed-in user ID and create user profile + test project + another user
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

      // Create a second test user (the person we'll chat with)
      otherUserId = "other-user-e2e-chat"
      val otherUserRef = FirebaseEmulator.firestore.collection("users").document(otherUserId)
      val otherUserProfile =
          mapOf(
              "uid" to otherUserId,
              "displayName" to "Chat Partner",
              "email" to "chatpartner@eureka.com",
              "photoUrl" to "")
      otherUserRef.set(otherUserProfile).await()

      // Create a test project for both users
      val projectId = "test-project-e2e-chat"
      val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)
      val project =
          ch.eureka.eurekapp.model.data.project.Project(
              projectId = projectId,
              name = "End-to-End Chat Project",
              description = "Test project for E2E chat testing",
              status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
              createdBy = testUserId,
              memberIds = listOf(testUserId, otherUserId))
      projectRef.set(project).await()

      // Add both users as project members
      val member =
          ch.eureka.eurekapp.model.data.project.Member(
              userId = testUserId, role = ch.eureka.eurekapp.model.data.project.ProjectRole.OWNER)
      val memberRef = projectRef.collection("members").document(testUserId)
      memberRef.set(member).await()

      val otherMember =
          ch.eureka.eurekapp.model.data.project.Member(
              userId = otherUserId, role = ch.eureka.eurekapp.model.data.project.ProjectRole.MEMBER)
      val otherMemberRef = projectRef.collection("members").document(otherUserId)
      otherMemberRef.set(otherMember).await()

      // Additional wait to ensure Firestore recognizes the auth state
      // before the app starts querying for projects/conversations
      Thread.sleep(1000)
    }

    // Wait for sign-in to complete and navigation to happen (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      try {
        // Check if we've navigated past sign-in by looking for bottom navigation
        composeTestRule
            .onNodeWithTag(BottomBarNavigationTestTags.CONVERSATIONS_SCREEN_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for UI to settle
    composeTestRule.waitForIdle()

    // Navigate to Conversations screen
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.CONVERSATIONS_SCREEN_BUTTON)
        .performClick()

    // Wait for Conversations screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithTag(ConversationListScreenTestTags.CREATE_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Click the create conversation button
    composeTestRule.onNodeWithTag(ConversationListScreenTestTags.CREATE_BUTTON).performClick()

    // Wait for Create Conversation screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule
            .onNodeWithTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Select the test project from the dropdown
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN).performClick()

    composeTestRule.waitForIdle()

    // Click on the project dropdown item (the test project)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("End-to-End Chat Project").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("End-to-End Chat Project").performClick()

    composeTestRule.waitForIdle()

    // Wait for the member dropdown to be ready (it loads after project selection)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule
            .onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Select the chat partner from the member dropdown
    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN).performClick()

    composeTestRule.waitForIdle()

    // Click on the member dropdown item (Chat Partner)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Chat Partner").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("Chat Partner").performClick()

    composeTestRule.waitForIdle()

    // Wait for member selection to complete and state to update
    Thread.sleep(1000)

    composeTestRule.waitForIdle()

    // Click the create conversation button
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(CreateConversationScreenTestTags.CREATE_BUTTON).performClick()

    // Give time for the conversation creation and navigation
    Thread.sleep(2000)

    composeTestRule.waitForIdle()

    // Wait for navigation to the conversation detail screen (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      try {
        composeTestRule.onNodeWithTag(ConversationDetailScreenTestTags.SCREEN).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Wait for the message input field to be ready
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Type a message in the input field
    val testMessage = "Hello! This is an end-to-end test message."
    composeTestRule
        .onNodeWithTag(MessageInputFieldTestTags.INPUT_FIELD)
        .performTextInput(testMessage)

    composeTestRule.waitForIdle()

    // Click the send button
    composeTestRule.onNodeWithTag(MessageInputFieldTestTags.SEND_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Wait for message to be sent and appear
    Thread.sleep(3000)

    composeTestRule.waitForIdle()

    // First, wait for at least one message bubble to appear (indicating messages are loading)
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
      try {
        composeTestRule
            .onAllNodesWithTag(MessageBubbleTestTags.BUBBLE)
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.waitForIdle()

    // Now wait for our specific message text to appear
    composeTestRule.waitUntilAtLeastOneExists(hasText(testMessage), timeoutMillis = 10_000)

    composeTestRule.waitForIdle()

    // Verify the message exists (waitUntilAtLeastOneExists already confirms it's there)
    // Use assertExists instead of assertIsDisplayed since the message might need scrolling
    composeTestRule.onNodeWithText(testMessage).assertExists()
  }
}

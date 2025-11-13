package ch.eureka.eurekapp.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeetingE2EFlowTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val uiDevice: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val testProjectId = "e2e_test_project_${UUID.randomUUID()}"
    private val userEmail = "testuser.e2e@test.com"
    private val userName = "E2E Test User"
    private val meetingTitle = "E2E Meeting ${System.currentTimeMillis()}"

    private lateinit var userIdToken: String
    private lateinit var userId: String

    private lateinit var userRepository: UserRepository
    private lateinit var projectRepository: ProjectRepository

    // Helper function as requested
    private fun findOkButton() = composeTestRule.onNodeWithText("OK")

    @Before
    fun setup() {
        assumeTrue("Firebase Emulator is not running", FirebaseEmulator.isRunning)
        FirebaseEmulator.clearFirestoreEmulator()
        FirebaseEmulator.clearAuthEmulator()

        userRepository = FirestoreUserRepository(FirebaseEmulator.firestore, FirebaseEmulator.auth)
        projectRepository = FirestoreProjectRepository(FirebaseEmulator.firestore, FirebaseEmulator.auth)

        userIdToken = FakeJwtGenerator.createFakeGoogleIdToken(userName, userEmail)

        runBlocking {
            FirebaseEmulator.createGoogleUser(userIdToken)
            val cred = GoogleAuthProvider.getCredential(userIdToken, null)
            val authResult = FirebaseEmulator.auth.signInWithCredential(cred).await()
            userId = authResult.user!!.uid

            userRepository
                .saveUser(User(uid = userId, displayName = userName, email = userEmail))
                .getOrThrow()

            projectRepository
                .createProject(
                    Project(
                        projectId = testProjectId,
                        name = "E2E Test Project",
                        description = "Project for E2E testing",
                        createdBy = userId,
                        memberIds = listOf(userId)),
                    creatorId = userId,
                    creatorRole = ProjectRole.OWNER)
                .getOrThrow()
        }
    }

    @After
    fun tearDown() {
        runBlocking { projectRepository.deleteProject(testProjectId).getOrNull() }

        FirebaseEmulator.clearFirestoreEmulator()
        FirebaseEmulator.clearAuthEmulator()

        uiDevice.executeShellCommand("svc wifi enable")
        uiDevice.executeShellCommand("svc data enable")
    }

    @Test
    fun completeE2EFlow_createEditDeleteMeeting_succeeds() {
        composeTestRule.setContent { NavigationMenu() }

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
            .performTextInput(meetingTitle)
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("30 minutes").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // ---MODIFIED DATE SELECTION---
        composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Don't click a specific day, just click OK
        findOkButton().assertIsDisplayed()
        findOkButton().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        // ---END OF MODIFICATION---

        // ---MODIFIED TIME SELECTION (to use helper)---
        composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        findOkButton().assertIsDisplayed()
        findOkButton().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        // ---END OF MODIFICATION---

        composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_FORMAT).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        composeTestRule.onNodeWithText("Virtual").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText(meetingTitle, useUnmergedTree = true).assertIsDisplayed()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(meetingTitle, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DETAIL_SCREEN)
            .assertIsDisplayed()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule.onNodeWithText(meetingTitle).assertDoesNotExist()
    }
}
package ch.eureka.eurekapp.screen.createprojectscreen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.authentication.AuthRepositoryFirebase
import ch.eureka.eurekapp.model.data.project.CreateProjectViewModel
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateProjectScreenTest : TestCase() {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun testTextInputFieldsInCreateProjectScreenTest() {
    composeRule.setContent { CreateProjectScreen() }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.OPEN))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.ARCHIVED))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.COMPLETED))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(
            CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.IN_PROGRESS))
        .performClick()

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CHECKBOX_LINK_GITHUB_REPOSITORY)
        .performClick()
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CHECKBOX_ENABLE_GOOGLE_DRIVE_FOLDER_TEST_TAG)
        .performClick()
  }

  @Test
  fun testDatePicker() {
    composeRule.setContent { CreateProjectScreen() }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CALENDAR_ICON_BUTTON_START)
        .performClick()
        .assertIsDisplayed()
    composeRule.waitForIdle()
  }

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    // Clear first before signing in
    FirebaseEmulator.clearFirestoreEmulator()
    // Sign in anonymously first to ensure auth is established before clearing data
    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    // Verify auth state is properly set
    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }
  }

  @After open fun tearDown() = runBlocking { FirebaseEmulator.clearFirestoreEmulator() }

  @Test
  fun createProjectWorks() {

    val firestore = FirebaseEmulator.firestore
    val auth = FirebaseEmulator.auth
    val firebaseProjectsRepository = FirestoreProjectRepository(firestore = firestore, auth = auth)

    val authRepository = AuthRepositoryFirebase(auth = auth)

    val createProjectScreenViewModel =
        CreateProjectViewModel(
            projectsRepository = firebaseProjectsRepository,
            authenticationRepository = authRepository)

    val startDateInjectedState = mutableStateOf<String>("24/12/2007")

    var createdProject = false

    composeRule.setContent {
      CreateProjectScreen(
          createProjectViewModel = createProjectScreenViewModel,
          startDate = startDateInjectedState,
          onProjectCreated = { createdProject = true })
    }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .performTextInput("Ilias")
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
        .assertIsDisplayed()
        .assertTextEquals("Ilias")

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG)
        .performClick()
    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.createProjectStatusTestTag(ProjectStatus.OPEN))
        .performClick()

    composeRule.onNodeWithTag(CreateProjectScreenTestTags.CREATE_RPOJECT_BUTTON).performClick()

    Thread.sleep(5000)
    assert(createdProject == true)
  }
}

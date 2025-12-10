/* Portions of this file were written with the help of Gemini and Grok. */
package ch.eureka.eurekapp.screen.createprojectscreen

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.credentials.Credential
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.data.project.CreateProjectViewModel
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.screens.subscreens.projects.creation.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.projects.creation.CreateProjectScreenTestTags
import com.google.firebase.auth.FirebaseUser
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class CreateProjectScreenTest : TestCase() {
  @get:Rule val composeRule = createComposeRule()

  class MockedProjectsRepository : ProjectRepository {
    override fun getProjectById(projectId: String): Flow<Project?> {
      return flow { null }
    }

    override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
      return flow { null }
    }

    override suspend fun createProject(
        project: Project,
        creatorId: String,
        creatorRole: ProjectRole
    ): Result<String> {
      return Result.success(project.projectId)
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getMembers(projectId: String): Flow<List<Member>> {
      return flowOf(listOf())
    }

    override suspend fun addMember(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      return Result.success(Unit)
    }
  }

  class MockedAuthRepositoryFirebase : AuthRepository {
    override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
      TODO("Not yet implemented")
    }

    override fun signOut(): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getUserId(): Result<String?> {
      return Result.success("ilias-id")
    }

    override fun getCurrentUser(): FirebaseUser? {
      return null
    }
  }

  @Test
  fun textInputFieldsInCreateProjectScreen_works() {
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

    composeRule.onNodeWithTag(CreateProjectScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun datePicker_works() {
    composeRule.setContent { CreateProjectScreen() }

    composeRule
        .onNodeWithTag(CreateProjectScreenTestTags.CALENDAR_ICON_BUTTON_START)
        .performClick()
        .assertIsDisplayed()
    composeRule.waitForIdle()
  }

  @Test
  fun createProject_works() {
    runBlocking {
      val auth = MockedAuthRepositoryFirebase()
      val firebaseProjectsRepository = MockedProjectsRepository()

      val createProjectScreenViewModel =
          CreateProjectViewModel(
              projectsRepository = firebaseProjectsRepository, authenticationRepository = auth)

      val startDateInjectedState = mutableStateOf("24/12/2007")

      var createdProject = false

      val scrollState = ScrollState(0)
      composeRule.setContent {
        CreateProjectScreen(
            createProjectViewModel = createProjectScreenViewModel,
            startDate = startDateInjectedState,
            onProjectCreated = { createdProject = true },
            scrollState = scrollState)
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

      composeRule.runOnIdle {
        runBlocking {
          scrollState.scrollTo(scrollState.maxValue)
        } // set scroll position to 300 pixels
      }

      composeRule.onNodeWithTag(CreateProjectScreenTestTags.CREATE_PROJECT_BUTTON).performClick()

      composeRule.waitForIdle()
      assert(createdProject)

      composeRule.onNodeWithTag(CreateProjectScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    }
  }

  @Test
  fun backButton_works() {
    val onBackClickCalled = mutableStateOf(false)
    composeRule.setContent { CreateProjectScreen(onBackClick = { onBackClickCalled.value = true }) }

    composeRule.onNodeWithTag(CreateProjectScreenTestTags.BACK_BUTTON).performClick()

    composeRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled.value }

    assert(onBackClickCalled.value) { "onBackClick should be called" }
  }
}

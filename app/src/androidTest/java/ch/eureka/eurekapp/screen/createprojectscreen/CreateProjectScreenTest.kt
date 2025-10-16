package ch.eureka.eurekapp.screen.createprojectscreen

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
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreenTestTags
import com.google.firebase.auth.FirebaseUser
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.flow.Flow
import org.junit.Rule
import org.junit.Test

class CreateProjectScreenTest : TestCase() {
  @get:Rule val composeRule = createComposeRule()

  class MockedProjectsRepository : ProjectRepository {
    override fun getProjectById(projectId: String): Flow<Project?> {
      TODO("Not yet implemented")
    }

    override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
      TODO("Not yet implemented")
    }

    override suspend fun createProject(
        project: Project,
        creatorId: String,
        creatorRole: ProjectRole
    ): Result<String> {
      return Result.success(project.projectId)
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override fun getMembers(projectId: String): Flow<List<Member>> {
      TODO("Not yet implemented")
    }

    override suspend fun addMember(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  class MockedAuthRepositoryFirebase : AuthRepository {
    override suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser> {
      TODO("Not yet implemented")
    }

    override fun signOut(): Result<Unit> {
      TODO("Not yet implemented")
    }

    override fun getUserId(): Result<String?> {
      return Result.success("ilias-id")
    }
  }

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

  @Test
  fun createProjectWorks() {

    val auth = MockedAuthRepositoryFirebase()
    val firebaseProjectsRepository = MockedProjectsRepository()

    val createProjectScreenViewModel =
        CreateProjectViewModel(
            projectsRepository = firebaseProjectsRepository, authenticationRepository = auth)

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

    composeRule.waitForIdle()
    assert(createdProject)
  }
}

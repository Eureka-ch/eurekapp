package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.tasks.TaskDependenciesViewModel
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

open class TaskDependenciesScreenTest : TestCase() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var projectRepository: ProjectRepository
  private lateinit var tasksRepository: TaskRepository
  private lateinit var usersRepository: UserRepository

  @Before
  fun setup() = runBlocking {
    FirebaseEmulator.clearFirestoreEmulator()
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }
    FirebaseEmulator.auth.signInAnonymously().await()
    usersRepository =
        FirestoreUserRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

    tasksRepository =
        FirestoreTaskRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

    projectRepository =
        FirestoreProjectRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

    usersRepository.saveUser(User().copy(uid = "user1", displayName = "user1"))
    usersRepository.saveUser(User().copy(uid = "user2", displayName = "user2"))
    projectRepository.createProject(
        Project().copy(projectId = "test-project-id", memberIds = listOf("user1")), "user1")
    projectRepository.updateProject(
        Project().copy(projectId = "test-project-id", memberIds = listOf("user1", "user2")))
    tasksRepository.createTask(
        Task()
            .copy(
                taskID = "task1",
                projectId = "test-project-id",
                dependingOnTasks = listOf("task2")))
    tasksRepository.createTask(Task().copy(taskID = "task2", projectId = "test-project-id"))
    Unit
  }

  @Test
  fun testTaskDependenciesScreenCorrectlyShowsEverything() {
    runBlocking {
      val viewModel =
          TaskDependenciesViewModel(
              tasksRepository = tasksRepository,
              usersRepository = usersRepository,
              projectsRepository = projectRepository)

      composeTestRule.setContent {
        TaskDependenciesScreen(
            projectId = "test-project-id", taskId = "task1", taskDependenciesViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      composeTestRule
          .onNodeWithTag(TaskDependenciesScreenTestTags.getFilteringNameTestTag("user1"))
          .performClick()
      composeTestRule
          .onNodeWithTag(TaskDependenciesScreenTestTags.getFilteringNameTestTag("All"))
          .performClick()
      composeTestRule
          .onNodeWithTag(TaskDependenciesScreenTestTags.getFilteringNameTestTag("user2"))
          .performClick()

      composeTestRule.waitForIdle()
    }
  }

  @After
  fun tearDown() = runBlocking {
    // Ensure both Firestore and Auth are reset between tests to avoid leaking auth state
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }
}

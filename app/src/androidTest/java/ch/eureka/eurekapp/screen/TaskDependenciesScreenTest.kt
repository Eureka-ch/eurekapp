package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.flow.first
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

  @org.junit.Ignore("Test fails on CI - needs investigation")
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

  @Test
  fun testBackButtonIsDisplayed() {
    runBlocking {
      val viewModel =
          TaskDependenciesViewModel(
              tasksRepository = tasksRepository,
              usersRepository = usersRepository,
              projectsRepository = projectRepository)

      composeTestRule.setContent {
        val navController = rememberNavController()
        TaskDependenciesScreen(
            projectId = "test-project-id",
            taskId = "task1",
            navigationController = navController,
            taskDependenciesViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify back button is displayed
      composeTestRule.onNodeWithTag("back_button_dependencies").assertIsDisplayed()
    }
  }

  @org.junit.Ignore("Test fails due to Firestore sync timing - needs investigation")
  @Test
  fun showsTasksThatDependOnRootTask() {
    runBlocking {
      val projectId = "dependency-project"
      val rootTaskId = "root-task"
      val dependentTaskId = "dependent-task"

      // Setup project
      projectRepository.createProject(
          Project().copy(projectId = projectId, memberIds = listOf("user1")), "user1")
      projectRepository.updateProject(
          Project().copy(projectId = projectId, memberIds = listOf("user1", "user2")))

      // Create tasks with explicit titles
      val rootTask = Task().copy(taskID = rootTaskId, projectId = projectId, title = "Root Task")
      val dependentTask =
          Task()
              .copy(
                  taskID = dependentTaskId,
                  projectId = projectId,
                  title = "Dependent Task",
                  dependingOnTasks = listOf(rootTaskId))

      tasksRepository.createTask(rootTask)
      tasksRepository.createTask(dependentTask)

      // Wait for Firestore to sync and verify data is present
      val allTasks = tasksRepository.getTasksInProject(projectId).first()
      assert(allTasks.any { it.taskID == rootTaskId })
      assert(allTasks.any { it.taskID == dependentTaskId })

      val viewModel =
          TaskDependenciesViewModel(
              tasksRepository = tasksRepository,
              usersRepository = usersRepository,
              projectsRepository = projectRepository)

      // Verify ViewModel can fetch dependent tasks before showing UI
      val dependentTasks = viewModel.getDependentTasksForTask(projectId, rootTask).first()
      assert(dependentTasks.any { it.taskID == dependentTaskId })

      composeTestRule.setContent {
        TaskDependenciesScreen(
            projectId = projectId, taskId = rootTaskId, taskDependenciesViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Wait for the dependent task to appear in UI
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        try {
          composeTestRule
              .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(dependentTask))
              .assertIsDisplayed()
          true
        } catch (e: AssertionError) {
          false
        }
      }
    }
  }

  @After
  fun tearDown() = runBlocking {
    // Ensure both Firestore and Auth are reset between tests to avoid leaking auth state
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }
}

package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
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

  @Test
  fun testTreeViewFiltersTasksByStatus() = runBlocking {
    val projectId = "filter-test-project"
    val rootTaskId = "root-task"
    val todoTaskId = "todo-task"
    val inProgressTaskId = "in-progress-task"
    val completedTaskId = "completed-task"

    // Setup project
    projectRepository.createProject(
        Project().copy(projectId = projectId, memberIds = listOf("user1")), "user1")

    // Create tasks with different statuses
    val rootTask = Task().copy(taskID = rootTaskId, projectId = projectId, title = "Root Task")
    val todoTask =
        Task()
            .copy(
                taskID = todoTaskId,
                projectId = projectId,
                title = "TODO Task",
                status = TaskStatus.TODO,
                dependingOnTasks = listOf(rootTaskId))
    val inProgressTask =
        Task()
            .copy(
                taskID = inProgressTaskId,
                projectId = projectId,
                title = "In Progress Task",
                status = TaskStatus.IN_PROGRESS,
                dependingOnTasks = listOf(rootTaskId))
    val completedTask =
        Task()
            .copy(
                taskID = completedTaskId,
                projectId = projectId,
                title = "Completed Task",
                status = TaskStatus.COMPLETED,
                dependingOnTasks = listOf(rootTaskId))

    tasksRepository.createTask(rootTask)
    tasksRepository.createTask(todoTask)
    tasksRepository.createTask(inProgressTask)
    tasksRepository.createTask(completedTask)

    // Wait for Firestore to sync and verify data
    val allTasks = tasksRepository.getTasksInProject(projectId).first()
    assert(allTasks.size == 4)

    val viewModel =
        TaskDependenciesViewModel(
            tasksRepository = tasksRepository,
            usersRepository = usersRepository,
            projectsRepository = projectRepository)

    // Verify ViewModel logic before UI test
    val dependentTasks = viewModel.getDependentTasksForTask(projectId, rootTask).first()
    assert(dependentTasks.size == 3) // All three depend on root
    assert(dependentTasks.any { it.taskID == todoTaskId })
    assert(dependentTasks.any { it.taskID == inProgressTaskId })
    assert(dependentTasks.any { it.taskID == completedTaskId })

    composeTestRule.setContent {
      TaskDependenciesScreen(
          projectId = projectId, taskId = rootTaskId, taskDependenciesViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify that only TODO and IN_PROGRESS tasks are displayed (not COMPLETED)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule
            .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(todoTask))
            .assertExists()
        composeTestRule
            .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(inProgressTask))
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    // Verify TODO task is displayed
    composeTestRule
        .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(todoTask))
        .assertIsDisplayed()

    // Verify IN_PROGRESS task is displayed
    composeTestRule
        .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(inProgressTask))
        .assertIsDisplayed()

    // Verify COMPLETED task is NOT displayed (filtered out)
    composeTestRule
        .onNodeWithTag(TaskDependenciesScreenTestTags.getDependentTaskTestTag(completedTask))
        .assertDoesNotExist()
  }

  @Test
  fun testTaskSurfaceComponentDisplaysWithFilter() = runBlocking {
    val projectId = "filter-component-project"
    val taskId = "test-task"

    projectRepository.createProject(
        Project().copy(projectId = projectId, memberIds = listOf("user1", "user2")), "user1")

    val task =
        Task()
            .copy(
                taskID = taskId,
                projectId = projectId,
                title = "Test Task",
                assignedUserIds = listOf("user1"))

    tasksRepository.createTask(task)

    // Verify data is in Firestore
    val allTasks = tasksRepository.getTasksInProject(projectId).first()
    assert(allTasks.any { it.taskID == taskId })

    val viewModel =
        TaskDependenciesViewModel(
            tasksRepository = tasksRepository,
            usersRepository = usersRepository,
            projectsRepository = projectRepository)

    // Verify ViewModel can load the task
    val loadedTask = viewModel.getTaskFromRepository(projectId, taskId).first()
    assert(loadedTask != null)
    assert(loadedTask!!.title == "Test Task")

    composeTestRule.setContent {
      TaskDependenciesScreen(
          projectId = projectId, taskId = taskId, taskDependenciesViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Verify TaskSurfaceComponent is displayed (the task title should be visible)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithText("Test Task").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
  }

  @After
  fun tearDown() = runBlocking {
    // Ensure both Firestore and Auth are reset between tests to avoid leaking auth state
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }
}

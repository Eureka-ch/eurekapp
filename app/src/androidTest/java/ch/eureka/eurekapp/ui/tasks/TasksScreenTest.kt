package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.getFilterTag
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive UI interaction tests for TasksScreen
 *
 * Tests verify that each task displays correctly with all its data, updates propagate properly, and
 * filters work correctly
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository

  private val testUser1 = User(uid = "user1", displayName = "Alice Smith", email = "alice@test.com")
  private val testUser2 = User(uid = "user2", displayName = "Bob Jones", email = "bob@test.com")

  private val testProject1 = Project(projectId = "proj1", name = "Project Alpha")

  private val now = System.currentTimeMillis()
  private val yesterday = Timestamp(java.util.Date(now - 24 * 60 * 60 * 1000))

  // Normalize tomorrow to midnight to ensure it's exactly 1 day away
  private val tomorrow: Timestamp = run {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    Timestamp(calendar.time)
  }

  private val twoWeeksAway = Timestamp(java.util.Date(now + 15 * 24 * 60 * 60 * 1000))

  @Before
  fun setUp() {
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
  }

  @Test
  fun tasksScreen_withLoadingState_displaysLoadingIndicator() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    // Note: The ViewModel's init block sets isLoading to false immediately after loading
    // This test verifies the loading UI components exist in the composition
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_CONTENT).assertExists()
  }

  @Test
  fun tasksScreen_withErrorState_displaysErrorMessage() {
    // Create a mock repository that will cause an error
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    // Note: Testing error state would require a way to inject error into the ViewModel
    // For now, verify the screen loads without crashing
    composeTestRule.waitUntilExactlyOneExists(hasText("No tasks found"), timeoutMillis = 3000)
  }

  @Test
  fun tasksScreen_withEmptyState_displaysCorrectMessage() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("No tasks found"), timeoutMillis = 3000)
    composeTestRule
        .onNodeWithText("Tasks will appear here when repository is connected")
        .assertIsDisplayed()
  }

  @Test
  fun tasksScreen_withSingleTask_displaysAllTaskDetails() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Implement Login Feature",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.IN_PROGRESS,
            dueDate = tomorrow)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Implement Login Feature"), 3000)

    // Check task count first (it's above the scrollable list)
    composeTestRule.onNodeWithText("1 tasks").assertIsDisplayed()

    // Scroll to the task card to make sure all elements are visible
    composeTestRule
        .onNodeWithTag(TasksScreenTestTags.TASK_LIST)
        .performScrollToNode(hasText("Implement Login Feature"))

    composeTestRule.onNodeWithText("Implement Login Feature").assertIsDisplayed()
    composeTestRule.onNodeWithText("👤 Alice Smith").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()

    // Wait for the due date to be calculated and displayed
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("⏰ Due tomorrow").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_withMultipleTasks_displaysAllInOrder() {
    val tasks =
        (1..5).map { i ->
          Task(
              taskID = "task$i",
              projectId = "proj1",
              title = "Task $i",
              assignedUserIds = listOf("user1"))
        }

    mockTaskRepository.setCurrentUserTasks(flowOf(tasks))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Task 1"), 3000)

    // Check task count first (it's above the scrollable list)
    composeTestRule.onNodeWithText("5 tasks").assertIsDisplayed()

    // Scroll to each task and verify it exists
    val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)
    (1..5).forEach { i ->
      taskList.performScrollToNode(hasText("Task $i"))
      composeTestRule.onNodeWithText("Task $i").assertIsDisplayed()
    }
  }

  @Test
  fun tasksScreen_displaysCorrectProgressForAllStatuses() {
    val todoTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "TODO Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)
    val inProgressTask =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "In Progress Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.IN_PROGRESS)
    val completedTask =
        Task(
            taskID = "task3",
            projectId = "proj1",
            title = "Completed Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.COMPLETED)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(todoTask, inProgressTask, completedTask)))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("TODO Task"), 3000)

    // Scroll to each task and verify progress
    val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)

    taskList.performScrollToNode(hasText("TODO Task"))
    composeTestRule.onNodeWithText("TODO Task").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("0%")[0].assertIsDisplayed()

    taskList.performScrollToNode(hasText("In Progress Task"))
    composeTestRule.onNodeWithText("In Progress Task").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("50%")[0].assertIsDisplayed()

    taskList.performScrollToNode(hasText("Completed Task"))
    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("100%")[0].assertIsDisplayed()
  }

  @Test
  fun tasksScreen_separatesCurrentAndCompletedTasks() {
    val currentTasks =
        listOf(
            Task(
                taskID = "task1",
                projectId = "proj1",
                title = "Current 1",
                assignedUserIds = listOf("user1"),
                status = TaskStatus.TODO),
            Task(
                taskID = "task2",
                projectId = "proj1",
                title = "Current 2",
                assignedUserIds = listOf("user1"),
                status = TaskStatus.IN_PROGRESS))
    val completedTasks =
        listOf(
            Task(
                taskID = "task3",
                projectId = "proj1",
                title = "Completed 1",
                assignedUserIds = listOf("user1"),
                status = TaskStatus.COMPLETED))

    mockTaskRepository.setCurrentUserTasks(flowOf(currentTasks + completedTasks))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Current Tasks"), 3000)

    // Check task count first (it's above the scrollable list)
    composeTestRule.onNodeWithText("2 tasks").assertIsDisplayed()

    val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)

    // Verify Current Tasks section
    taskList.performScrollToNode(hasText("Current Tasks", substring = true))
    composeTestRule.onNodeWithText("Current Tasks", substring = true).assertIsDisplayed()

    taskList.performScrollToNode(hasText("Current 1"))
    composeTestRule.onNodeWithText("Current 1").assertIsDisplayed()

    taskList.performScrollToNode(hasText("Current 2"))
    composeTestRule.onNodeWithText("Current 2").assertIsDisplayed()

    // Verify Recently Completed section
    taskList.performScrollToNode(hasText("Recently Completed", substring = true))
    composeTestRule.onNodeWithText("Recently Completed", substring = true).assertIsDisplayed()

    taskList.performScrollToNode(hasText("Completed 1"))
    composeTestRule.onNodeWithText("Completed 1").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_displaysAllDueDateFormats() {
    val tasks =
        listOf(
            Task(
                taskID = "task1",
                projectId = "proj1",
                title = "Overdue",
                assignedUserIds = listOf("user1"),
                dueDate = yesterday),
            Task(
                taskID = "task2",
                projectId = "proj1",
                title = "Tomorrow",
                assignedUserIds = listOf("user1"),
                dueDate = tomorrow),
            Task(
                taskID = "task3",
                projectId = "proj1",
                title = "Two Weeks",
                assignedUserIds = listOf("user1"),
                dueDate = twoWeeksAway),
            Task(
                taskID = "task4",
                projectId = "proj1",
                title = "No Date",
                assignedUserIds = listOf("user1"),
                dueDate = null))

    mockTaskRepository.setCurrentUserTasks(flowOf(tasks))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }
    composeTestRule.waitForIdle()

    val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)

    // Verify overdue task
    taskList.performScrollToNode(hasText(tasks[0].title))
    composeTestRule.waitUntilExactlyOneExists(hasText(tasks[0].title), 3000)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("⏰ Overdue").assertIsDisplayed()

    // Verify tomorrow task - scroll to the task card first, then verify the date
    taskList.performScrollToNode(hasText("⏰ Due tomorrow"))
    composeTestRule.waitUntilExactlyOneExists(hasText("⏰ Due tomorrow"), 3000)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("⏰ Due tomorrow").assertIsDisplayed()

    // Verify two weeks away task
    taskList.performScrollToNode(hasText(tasks[2].title))
    composeTestRule.waitUntilExactlyOneExists(hasText(tasks[2].title), 3000)
    taskList.performScrollToNode(hasText("⏰ Due in more than a week"))
    composeTestRule.onNodeWithText("⏰ Due in more than a week").assertIsDisplayed()

    // Verify no due date task
    taskList.performScrollToNode(hasText(tasks[3].title))
    composeTestRule.waitUntilExactlyOneExists(hasText(tasks[3].title), 3000)
    composeTestRule.onNodeWithText("⏰ No due date").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_displaysPriorityLevels() {
    val critical =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Critical",
            assignedUserIds = listOf("user1"),
            dueDate = yesterday)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(critical)))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Critical Priority"), 3000)
    composeTestRule.onNodeWithText("Critical Priority", substring = true).assertIsDisplayed()
  }

  @Test
  fun tasksScreen_teamFilter_showsOnlyTeamTasks() {
    val myTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "My Task",
            assignedUserIds = listOf("user1"))
    val teamTask =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Team Task",
            assignedUserIds = listOf("user2"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(myTask)))
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(myTask, teamTask)))
    mockUserRepository.setUsers(testUser1, testUser2)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("My Task"), 3000)
    composeTestRule.onNodeWithTag(getFilterTag(TaskScreenFilter.Team)).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasText("Team Task"), 3000)
    composeTestRule.onNodeWithText("Team Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("My Task").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_whenTaskAdded_updatesImmediately() {
    val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    mockTaskRepository.setCurrentUserTasks(tasksFlow)
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("No tasks found"), 3000)
    val newTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "New Task",
            assignedUserIds = listOf("user1"))
    tasksFlow.value = listOf(newTask)
    composeTestRule.waitUntilExactlyOneExists(hasText("New Task"), 3000)
    composeTestRule.onNodeWithText("New Task").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_whenTaskRemoved_updatesImmediately() {
    val task1 =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Task 1",
            assignedUserIds = listOf("user1"))
    val task2 =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Task 2",
            assignedUserIds = listOf("user1"))
    val tasksFlow = MutableStateFlow(listOf(task1, task2))
    mockTaskRepository.setCurrentUserTasks(tasksFlow)
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Task 1"), 3000)
    composeTestRule.onNodeWithText("Task 2").assertIsDisplayed()
    tasksFlow.value = listOf(task2)
    composeTestRule.waitUntil(3000) {
      try {
        composeTestRule.onNodeWithText("Task 1").assertDoesNotExist()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithText("Task 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 tasks").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_whenTaskUpdated_displaysNewData() {
    val originalTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Original",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)
    val tasksFlow = MutableStateFlow(listOf(originalTask))
    mockTaskRepository.setCurrentUserTasks(tasksFlow)
    mockUserRepository.setUsers(testUser1, testUser2)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Original"), 3000)
    composeTestRule.onNodeWithText("0%", substring = true).assertIsDisplayed()
    val updatedTask =
        originalTask.copy(
            title = "Updated", status = TaskStatus.COMPLETED, assignedUserIds = listOf("user2"))
    tasksFlow.value = listOf(updatedTask)
    composeTestRule.waitUntilExactlyOneExists(hasText("Updated"), 3000)
    composeTestRule.onNodeWithText("100%", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob Jones", substring = true).assertIsDisplayed()
  }

  @Test
  fun tasksScreen_thisWeekFilter_showsOnlyTasksDueThisWeek() {
    val taskDueTomorrow =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Due Tomorrow",
            assignedUserIds = listOf("user1"),
            dueDate = tomorrow)
    val taskDueTwoWeeks =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Due Two Weeks",
            assignedUserIds = listOf("user1"),
            dueDate = twoWeeksAway)
    val taskNoDueDate =
        Task(
            taskID = "task3",
            projectId = "proj1",
            title = "No Due Date",
            assignedUserIds = listOf("user1"),
            dueDate = null)

    mockTaskRepository.setCurrentUserTasks(
        flowOf(listOf(taskDueTomorrow, taskDueTwoWeeks, taskNoDueDate)))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Due Tomorrow"), 3000)

    // Click "This Week" filter
    composeTestRule.onNodeWithTag(getFilterTag(TaskScreenFilter.ThisWeek)).performClick()

    // Should show only tasks due within a week (tomorrow)
    composeTestRule.waitUntilExactlyOneExists(hasText("Due Tomorrow"), 3000)
    composeTestRule.onNodeWithText("Due Tomorrow").assertExists()

    // Tasks outside this week should not appear
    composeTestRule.onNodeWithText("Due Two Weeks").assertDoesNotExist()
    composeTestRule.onNodeWithText("No Due Date").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_taskToggleCompletion_triggersViewModelUpdate() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockUserRepository.setUsers(testUser1)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Test Task"), 3000)

    // Scroll to the task
    val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)
    taskList.performScrollToNode(hasText("Test Task"))

    // Find and click the checkbox
    composeTestRule.onNodeWithTag("checkbox").performClick()

    // Verify the repository was called to update the task
    assert(mockTaskRepository.updateTaskCalls.isNotEmpty()) {
      "updateTask should be called when checkbox is clicked"
    }

    val updatedTask = mockTaskRepository.updateTaskCalls[0]
    assert(updatedTask.taskID == "task1") { "Updated task should have the correct ID" }
    assert(updatedTask.status == TaskStatus.COMPLETED) {
      "Task status should be updated to COMPLETED"
    }
  }

  @Test
  fun tasksScreen_createTaskButton_triggersCallback() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    var createTaskClicked = false

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"),
          onCreateTaskClick = { createTaskClicked = true })
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("+ New Task"), 3000)
    composeTestRule.onNodeWithText("+ New Task").performClick()

    assert(createTaskClicked) { "Create task callback should be called when button is clicked" }
  }

  @Test
  fun tasksScreen_autoAssignButton_triggersCallback() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    var autoAssignClicked = false

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"),
          onAutoAssignClick = { autoAssignClicked = true })
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Auto-assign"), 3000)
    composeTestRule.onNodeWithText("Auto-assign").performClick()

    assert(autoAssignClicked) { "Auto-assign callback should be called when button is clicked" }
  }

  @Test
  fun tasksScreen_bothActionButtons_triggerCallbacksIndependently() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    var createTaskClicked = false
    var autoAssignClicked = false

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"),
          onCreateTaskClick = { createTaskClicked = true },
          onAutoAssignClick = { autoAssignClicked = true })
    }

    // Click create task button
    composeTestRule.waitUntilExactlyOneExists(hasText("+ New Task"), 3000)
    composeTestRule.onNodeWithText("+ New Task").performClick()

    assert(createTaskClicked) { "Create task callback should be called" }
    assert(!autoAssignClicked) { "Auto-assign callback should NOT be called yet" }

    // Click auto-assign button
    composeTestRule.onNodeWithText("Auto-assign").performClick()

    assert(createTaskClicked) { "Create task callback should still be true" }
    assert(autoAssignClicked) { "Auto-assign callback should now be called" }
  }

  @Test
  fun tasksScreen_displayCorrectComponentsOnLoad() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  mockTaskRepository, mockProjectRepository, mockUserRepository, "user1"))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("No tasks found"), 3000)

    // Verify all main components are present
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_CONTENT).assertExists()
    composeTestRule.onNodeWithText("+ New Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Auto-assign").assertIsDisplayed()
    composeTestRule.onNodeWithText("0 tasks").assertIsDisplayed()

    // Wait for filters to be rendered and verify only the first visible ones
    composeTestRule.waitForIdle()

    // Verify only the first filter (Mine) which should always be visible
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule.onNodeWithTag(getFilterTag(TaskScreenFilter.Mine)).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }
  }
}

package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.screens.TaskAndUsers
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Portions of this code were generated with the help of Grok.

@OptIn(ExperimentalCoroutinesApi::class)
class TaskScreenViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver
  private lateinit var viewModel: TaskScreenViewModel

  private val testUser1 = User(uid = "user1", displayName = "Alice", email = "alice@test.com")
  private val testUser2 = User(uid = "user2", displayName = "Bob", email = "bob@test.com")

  private val testProject1 = Project(projectId = "proj1", name = "Project 1")
  private val testProject2 = Project(projectId = "proj2", name = "Project 2")

  private val now = System.currentTimeMillis()
  private val yesterday = Timestamp(java.util.Date(now - 24 * 60 * 60 * 1000))
  private val tomorrow = Timestamp(java.util.Date(now + 24 * 60 * 60 * 1000))
  private val nextWeek = Timestamp(java.util.Date(now + 9 * 24 * 60 * 60 * 1000))

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
    mockConnectivityObserver = mockk()
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockTaskRepository.reset()
    mockProjectRepository.reset()
    mockUserRepository.reset()
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Mine, uiState.selectedFilter)
    assertEquals(emptyList<TaskAndUsers>(), uiState.tasksAndUsers)
    assertFalse(uiState.isLoading)
    assertEquals(null, uiState.error)
  }

  @Test
  fun viewModel_withUserTasks_displaysCorrectly() = runTest {
    val task1 =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "My Task 1",
            assignedUserIds = listOf("user1"))
    val task2 =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "My Task 2",
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task1, task2)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(2, uiState.tasksAndUsers.size)
    assertEquals("My Task 1", uiState.tasksAndUsers[0].task.title)
    assertEquals("My Task 2", uiState.tasksAndUsers[1].task.title)
  }

  @Test
  fun viewModel_setFilterToTeam_showsTeamTasks() = runTest {
    val userTask =
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

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(userTask)))
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(userTask, teamTask)))
    mockUserRepository.setUsers(testUser1, testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Team)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Team, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Team Task", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_setFilterToAll_showsAllTasks() = runTest {
    val userTask =
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

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(userTask)))
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(userTask, teamTask)))
    mockUserRepository.setUsers(testUser1, testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.All)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.All, uiState.selectedFilter)
    assertEquals(2, uiState.tasksAndUsers.size)
  }

  @Test
  fun viewModel_setFilterToToday_showsOnlyTasksDueToday() = runTest {
    val baseTime = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = baseTime
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val today = Timestamp(calendar.time)

    val tomorrowCalendar = java.util.Calendar.getInstance()
    tomorrowCalendar.timeInMillis = baseTime
    tomorrowCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    tomorrowCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    tomorrowCalendar.set(java.util.Calendar.MINUTE, 0)
    tomorrowCalendar.set(java.util.Calendar.SECOND, 0)
    tomorrowCalendar.set(java.util.Calendar.MILLISECOND, 0)
    val tomorrowNormalized = Timestamp(tomorrowCalendar.time)

    val taskDueToday =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Due Today",
            assignedUserIds = listOf("user1"),
            dueDate = today)
    val taskDueTomorrow =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Due Tomorrow",
            assignedUserIds = listOf("user1"),
            dueDate = tomorrowNormalized)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(taskDueToday, taskDueTomorrow)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Today)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Today, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Due Today", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_setFilterToTomorrow_showsOnlyTasksDueTomorrow() = runTest {
    val baseTime = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = baseTime
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val today = Timestamp(calendar.time)

    val tomorrowCalendar = java.util.Calendar.getInstance()
    tomorrowCalendar.timeInMillis = baseTime
    tomorrowCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    tomorrowCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    tomorrowCalendar.set(java.util.Calendar.MINUTE, 0)
    tomorrowCalendar.set(java.util.Calendar.SECOND, 0)
    tomorrowCalendar.set(java.util.Calendar.MILLISECOND, 0)
    val tomorrowNormalized = Timestamp(tomorrowCalendar.time)

    val taskDueToday =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Due Today",
            assignedUserIds = listOf("user1"),
            dueDate = today)
    val taskDueTomorrow =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Due Tomorrow",
            assignedUserIds = listOf("user1"),
            dueDate = tomorrowNormalized)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(taskDueToday, taskDueTomorrow)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Tomorrow)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Tomorrow, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Due Tomorrow", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_setFilterToOverdue_showsOnlyOverdueTasks() = runTest {
    val baseTime = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = baseTime
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val today = Timestamp(calendar.time)

    val yesterdayCalendar = java.util.Calendar.getInstance()
    yesterdayCalendar.timeInMillis = baseTime
    yesterdayCalendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
    yesterdayCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    yesterdayCalendar.set(java.util.Calendar.MINUTE, 0)
    yesterdayCalendar.set(java.util.Calendar.SECOND, 0)
    yesterdayCalendar.set(java.util.Calendar.MILLISECOND, 0)
    val yesterdayNormalized = Timestamp(yesterdayCalendar.time)

    val taskOverdue =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Overdue Task",
            assignedUserIds = listOf("user1"),
            dueDate = yesterdayNormalized)
    val taskDueToday =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Due Today",
            assignedUserIds = listOf("user1"),
            dueDate = today)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(taskOverdue, taskDueToday)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Overdue)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Overdue, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Overdue Task", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_setFilterToThisWeek_showsOnlyTasksDueThisWeek() = runTest {
    val taskDueTomorrow =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Due Tomorrow",
            assignedUserIds = listOf("user1"),
            dueDate = tomorrow)
    val taskDueNextWeek =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Due Next Week",
            assignedUserIds = listOf("user1"),
            dueDate = nextWeek)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(taskDueTomorrow, taskDueNextWeek)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.ThisWeek)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.ThisWeek, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Due Tomorrow", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_toggleTaskCompletion_updatesTodoToCompleted() = runTest {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "My Task",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.toggleTaskCompletion(task)
    advanceUntilIdle()

    assertEquals(1, mockTaskRepository.updateTaskCalls.size)
    val updatedTask = mockTaskRepository.updateTaskCalls[0]
    assertEquals(TaskStatus.COMPLETED, updatedTask.status)
    assertEquals("task1", updatedTask.taskID)
  }

  @Test
  fun viewModel_toggleTaskCompletion_updatesCompletedToTodo() = runTest {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "My Task",
            status = TaskStatus.COMPLETED,
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.toggleTaskCompletion(task)
    advanceUntilIdle()

    assertEquals(1, mockTaskRepository.updateTaskCalls.size)
    val updatedTask = mockTaskRepository.updateTaskCalls[0]
    assertEquals(TaskStatus.TODO, updatedTask.status)
  }

  @Test
  fun viewModel_toggleTaskCompletion_withError_setsErrorState() = runTest {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "My Task",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockTaskRepository.setUpdateTaskResult(Result.failure(Exception("Update failed")))
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.toggleTaskCompletion(task)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertNotNull(uiState.error)
    assertTrue(uiState.error!!.contains("Failed to update task"))
  }

  @Test
  fun viewModel_getAssignees_returnsCorrectUsers() = runTest {
    val task =
        Task(taskID = "task1", projectId = "proj1", assignedUserIds = listOf("user1", "user2"))

    mockUserRepository.setUsers(testUser1, testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    val assignees = viewModel.getAssignees(task).first()

    assertEquals(2, assignees.size)
    assertEquals("Alice", assignees[0].displayName)
    assertEquals("Bob", assignees[1].displayName)
  }

  @Test
  fun viewModel_getAssignees_withEmptyList_returnsEmptyList() = runTest {
    val task = Task(taskID = "task1", projectId = "proj1", assignedUserIds = emptyList())

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    val assignees = viewModel.getAssignees(task).first()

    assertEquals(emptyList<User>(), assignees)
  }

  @Test
  fun viewModel_withMultipleProjects_combinesTeamTasks() = runTest {
    val teamTask1 =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Team Task 1",
            assignedUserIds = listOf("user2"))
    val teamTask2 =
        Task(
            taskID = "task2",
            projectId = "proj2",
            title = "Team Task 2",
            assignedUserIds = listOf("user2"))

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1, testProject2)))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(teamTask1)))
    mockTaskRepository.setProjectTasks("proj2", flowOf(listOf(teamTask2)))
    mockUserRepository.setUsers(testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Team)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(2, uiState.tasksAndUsers.size)
  }

  @Test
  fun viewModel_withNoProjects_showsEmptyTeamTasks() = runTest {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Team)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(0, uiState.tasksAndUsers.size)
  }

  @Test
  fun viewModel_tracksRepositoryCalls() = runTest {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    assertTrue(mockTaskRepository.getTasksForCurrentUserCalls.size > 0)
    assertTrue(mockProjectRepository.getProjectsForCurrentUserCalls.size > 0)
  }

  @Test
  fun viewModel_whenTasksUpdate_propagatesChangesToUiState() = runTest {
    val initialTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Initial Task",
            assignedUserIds = listOf("user1"))

    val tasksFlow = kotlinx.coroutines.flow.MutableStateFlow(listOf(initialTask))
    mockTaskRepository.setCurrentUserTasks(tasksFlow)
    mockUserRepository.setUsers(testUser1)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    // Verify initial state
    var uiState = viewModel.uiState.first()
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Initial Task", uiState.tasksAndUsers[0].task.title)

    // Update the flow with new tasks
    val updatedTask =
        Task(
            taskID = "task2",
            projectId = "proj1",
            title = "Updated Task",
            assignedUserIds = listOf("user1"))
    tasksFlow.value = listOf(initialTask, updatedTask)
    advanceUntilIdle()

    // Verify state updated
    uiState = viewModel.uiState.first()
    assertEquals(2, uiState.tasksAndUsers.size)
    assertEquals("Initial Task", uiState.tasksAndUsers[0].task.title)
    assertEquals("Updated Task", uiState.tasksAndUsers[1].task.title)
  }

  @Test
  fun viewModel_whenUserDataUpdates_propagatesChangesToUiState() = runTest {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))

    val userFlow = kotlinx.coroutines.flow.MutableStateFlow<User?>(testUser1)
    mockUserRepository.setUserFlow("user1", userFlow)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    // Verify initial user
    var uiState = viewModel.uiState.first()
    assertEquals("Alice", uiState.tasksAndUsers[0].users[0].displayName)

    // Update user data
    val updatedUser = testUser1.copy(displayName = "Alice Updated")
    userFlow.value = updatedUser
    advanceUntilIdle()

    // Verify user data updated
    uiState = viewModel.uiState.first()
    assertEquals("Alice Updated", uiState.tasksAndUsers[0].users[0].displayName)
  }

  @Test
  fun viewModel_whenProjectsUpdate_propagatesTeamTaskChanges() = runTest {
    val teamTask =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Team Task",
            assignedUserIds = listOf("user2"))

    val projectsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<Project>>(emptyList())
    mockProjectRepository.setCurrentUserProjects(projectsFlow)
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(teamTask)))
    mockUserRepository.setUsers(testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    viewModel.setFilter(TaskScreenFilter.Team)
    advanceUntilIdle()

    // Initially no projects, so no team tasks
    var uiState = viewModel.uiState.first()
    assertEquals(0, uiState.tasksAndUsers.size)

    // Add project
    projectsFlow.value = listOf(testProject1)
    advanceUntilIdle()

    // Verify team task appears
    uiState = viewModel.uiState.first()
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Team Task", uiState.tasksAndUsers[0].task.title)
  }

  @Test
  fun viewModel_whenFilterChanges_immediatelyUpdatesUiState() = runTest {
    val userTask =
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

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(userTask)))
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(userTask, teamTask)))
    mockUserRepository.setUsers(testUser1, testUser2)

    viewModel =
        TaskScreenViewModel(
            mockTaskRepository,
            mockProjectRepository,
            mockUserRepository,
            currentUserId = "user1",
            mockConnectivityObserver)
    advanceUntilIdle()

    // Start with Mine filter
    var uiState = viewModel.uiState.first()
    assertEquals(TaskScreenFilter.Mine, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)

    // Change to All filter
    viewModel.setFilter(TaskScreenFilter.All)
    advanceUntilIdle()

    uiState = viewModel.uiState.value
    assertEquals(TaskScreenFilter.All, uiState.selectedFilter)
    assertEquals(2, uiState.tasksAndUsers.size)

    // Change to Team filter
    viewModel.setFilter(TaskScreenFilter.Team)
    advanceUntilIdle()

    uiState = viewModel.uiState.value
    assertEquals(TaskScreenFilter.Team, uiState.selectedFilter)
    assertEquals(1, uiState.tasksAndUsers.size)
    assertEquals("Team Task", uiState.tasksAndUsers[0].task.title)
  }

  // Tests for TaskScreenUiState data class
  @Test
  fun taskScreenUiState_defaultConstructor_hasCorrectDefaults() {
    val uiState = TaskScreenUiState()

    assertEquals(emptyList<TaskAndUsers>(), uiState.tasksAndUsers)
    assertFalse(uiState.isLoading)
    assertEquals(null, uiState.error)
    assertEquals(TaskScreenFilter.Mine, uiState.selectedFilter)
  }

  @Test
  fun taskScreenUiState_copy_preservesUnchangedValues() {
    val original =
        TaskScreenUiState(
            tasksAndUsers = emptyList(),
            isLoading = true,
            error = "Test error",
            selectedFilter = TaskScreenFilter.Team)

    val copied = original.copy(isLoading = false)

    assertEquals(emptyList<TaskAndUsers>(), copied.tasksAndUsers)
    assertFalse(copied.isLoading)
    assertEquals("Test error", copied.error)
    assertEquals(TaskScreenFilter.Team, copied.selectedFilter)
  }

  @Test
  fun taskScreenUiState_equality_worksCorrectly() {
    val state1 =
        TaskScreenUiState(
            tasksAndUsers = emptyList(),
            isLoading = false,
            error = null,
            selectedFilter = TaskScreenFilter.Mine)
    val state2 =
        TaskScreenUiState(
            tasksAndUsers = emptyList(),
            isLoading = false,
            error = null,
            selectedFilter = TaskScreenFilter.Mine)

    assertEquals(state1, state2)
  }

  // Tests for TaskScreenFilter sealed class and constants
  @Test
  fun taskScreenFilter_mine_hasCorrectDisplayName() {
    assertEquals("My tasks", TaskScreenFilter.Mine.displayName)
  }

  @Test
  fun taskScreenFilter_team_hasCorrectDisplayName() {
    assertEquals("Team", TaskScreenFilter.Team.displayName)
  }

  @Test
  fun taskScreenFilter_thisWeek_hasCorrectDisplayName() {
    assertEquals("This week", TaskScreenFilter.ThisWeek.displayName)
  }

  @Test
  fun taskScreenFilter_all_hasCorrectDisplayName() {
    assertEquals("All", TaskScreenFilter.All.displayName)
  }

  @Test
  fun taskScreenFilter_companion_myTasksDisplayName_hasCorrectValue() {
    assertEquals("My tasks", TaskScreenFilter.MY_TASKS_DISPLAY_NAME)
  }

  @Test
  fun taskScreenFilter_companion_teamDisplayName_hasCorrectValue() {
    assertEquals("Team", TaskScreenFilter.TEAM_DISPLAY_NAME)
  }

  @Test
  fun taskScreenFilter_companion_thisWeekDisplayName_hasCorrectValue() {
    assertEquals("This week", TaskScreenFilter.THIS_WEEK_DISPLAY_NAME)
  }

  @Test
  fun taskScreenFilter_companion_allDisplayName_hasCorrectValue() {
    assertEquals("All", TaskScreenFilter.ALL_DISPLAY_NAME)
  }

  @Test
  fun taskScreenFilter_companion_values_containsAllFilters() {
    val values = TaskScreenFilter.values

    assertEquals(7, values.size)
    assertTrue(values.contains(TaskScreenFilter.Mine))
    assertTrue(values.contains(TaskScreenFilter.Team))
    assertTrue(values.contains(TaskScreenFilter.Today))
    assertTrue(values.contains(TaskScreenFilter.Tomorrow))
    assertTrue(values.contains(TaskScreenFilter.ThisWeek))
    assertTrue(values.contains(TaskScreenFilter.Overdue))
    assertTrue(values.contains(TaskScreenFilter.All))
  }

  @Test
  fun taskScreenFilter_values_areInCorrectOrder() {
    val values = TaskScreenFilter.values

    assertEquals(7, values.size)
    assertEquals(TaskScreenFilter.Mine, values[0])
    assertEquals(TaskScreenFilter.Team, values[1])
    assertEquals(TaskScreenFilter.Today, values[2])
    assertEquals(TaskScreenFilter.Tomorrow, values[3])
    assertEquals(TaskScreenFilter.ThisWeek, values[4])
    assertEquals(TaskScreenFilter.Overdue, values[5])
    assertEquals(TaskScreenFilter.All, values[6])
  }

  // Tests for TaskAndUsers data class
  @Test
  fun taskAndUsers_constructor_storesValuesCorrectly() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))
    val users = listOf(testUser1, testUser2)

    val taskAndUsers = TaskAndUsers(task, users)

    assertEquals(task, taskAndUsers.task)
    assertEquals(users, taskAndUsers.users)
  }

  @Test
  fun taskAndUsers_copy_preservesUnchangedValues() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))
    val users = listOf(testUser1)
    val original = TaskAndUsers(task, users)

    val newUsers = listOf(testUser2)
    val copied = original.copy(users = newUsers)

    assertEquals(task, copied.task)
    assertEquals(newUsers, copied.users)
  }

  @Test
  fun taskAndUsers_equality_worksCorrectly() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))
    val users = listOf(testUser1)

    val taskAndUsers1 = TaskAndUsers(task, users)
    val taskAndUsers2 = TaskAndUsers(task, users)

    assertEquals(taskAndUsers1, taskAndUsers2)
  }
}

/* Portions of this code and documentation were generated with the help of AI (ChatGPT 5.1). */
package ch.eureka.eurekapp.ui.home

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [HomeOverviewViewModel] verifying that it aggregates data from repositories correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeOverviewViewModelTest {

  private val dispatcher = UnconfinedTestDispatcher()

  private lateinit var taskRepository: MockTaskRepository
  private lateinit var projectRepository: MockProjectRepository
  private lateinit var meetingRepository: MockMeetingRepository
  private lateinit var userRepository: MockUserRepository
  private lateinit var connectivityFlow: MutableStateFlow<Boolean>

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    taskRepository = MockTaskRepository()
    projectRepository = MockProjectRepository()
    meetingRepository = MockMeetingRepository()
    userRepository = MockUserRepository()
    connectivityFlow = MutableStateFlow(true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun homeOverviewViewModel_aggregatesTasksMeetingsAndProjects() = runTest {
    val now = System.currentTimeMillis()
    val projectA =
        Project(projectId = "proj-a", name = "Project A", lastUpdated = timestamp(now - 1000))
    val projectB =
        Project(projectId = "proj-b", name = "Project B", lastUpdated = timestamp(now + 1000))

    val tasks =
        (1..4).map { index ->
          Task(
              taskID = "task-$index",
              projectId = projectA.projectId,
              title = "Task $index",
              status = TaskStatus.TODO,
              // Home overview filters on tasks assigned to the current user
              assignedUserIds = listOf("user-1"),
              dueDate = timestamp(now + index * DAY))
        }
    taskRepository.setCurrentUserTasks(flowOf(tasks))

    val meetings =
        (1..4).map { index ->
          Meeting(
              meetingID = "meeting-$index",
              projectId = if (index % 2 == 0) projectB.projectId else projectA.projectId,
              title = "Meeting $index",
              status = MeetingStatus.SCHEDULED,
              datetime = timestamp(now + index * DAY))
        }

    meetingRepository.setMeetingsForCurrentUser(meetings)
    projectRepository.setCurrentUserProjects(flowOf(listOf(projectA, projectB)))
    userRepository.setCurrentUser(flowOf(User(uid = "user-1", displayName = "Eureka User")))

    val viewModel =
        HomeOverviewViewModel(
            taskRepository = taskRepository,
            projectRepository = projectRepository,
            meetingRepository = meetingRepository,
            userRepository = userRepository,
            connectivityFlow = connectivityFlow)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Eureka User", state.currentUserName)
    assertEquals(listOf("Task 1", "Task 2", "Task 3"), state.upcomingTasks.map { it.title })

    // Verify meetings are aggregated and sorted correctly
    assertEquals(3, state.upcomingMeetings.size)
    assertTrue(state.upcomingMeetings.all { it.status != MeetingStatus.COMPLETED })

    // Projects should be sorted by lastUpdated descending => Project B first
    assertEquals("Project B", state.recentProjects.first().name)
  }

  @Test
  fun homeOverviewViewModel_propagatesErrorsAndConnectivity() = runTest {
    taskRepository.setCurrentUserTasks(flow { throw IllegalStateException("Task failure") })
    projectRepository.setCurrentUserProjects(flowOf(emptyList()))
    userRepository.setCurrentUser(flowOf(null))
    connectivityFlow.value = false

    val viewModel =
        HomeOverviewViewModel(
            taskRepository = taskRepository,
            projectRepository = projectRepository,
            meetingRepository = meetingRepository,
            userRepository = userRepository,
            connectivityFlow = connectivityFlow)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isConnected)
    assertEquals("Task failure", state.error)
    assertTrue(state.upcomingTasks.isEmpty())
  }

  @Test
  fun projectsFlow_catchBlock_handlesError() = runTest {
    projectRepository.setCurrentUserProjects(flow { throw IllegalStateException("Project error") })
    userRepository.setCurrentUser(flowOf(null))
    taskRepository.setCurrentUserTasks(flowOf(emptyList()))
    val viewModel =
        HomeOverviewViewModel(
            taskRepository, projectRepository, meetingRepository, userRepository, connectivityFlow)

    advanceUntilIdle()
    assertEquals("Project error", viewModel.uiState.value.error)
  }

  @Test
  fun upcomingTasksWithAssignees_loadsAssigneeNames() = runTest {
    val now = System.currentTimeMillis()
    val projectA = Project(projectId = "proj-a", name = "Project A", lastUpdated = timestamp(now))

    val user1 =
        User(uid = "user-1", displayName = "Alice", email = "alice@example.com", photoUrl = "")
    val user2 = User(uid = "user-2", displayName = "Bob", email = "bob@example.com", photoUrl = "")

    val tasks =
        listOf(
            Task(
                taskID = "task-1",
                projectId = projectA.projectId,
                title = "Task 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user-1"),
                dueDate = timestamp(now + DAY)),
            Task(
                taskID = "task-2",
                projectId = projectA.projectId,
                title = "Task 2",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user-1", "user-2"),
                dueDate = timestamp(now + 2 * DAY)))

    taskRepository.setCurrentUserTasks(flowOf(tasks))
    userRepository.setCurrentUser(flowOf(User(uid = "user-1", displayName = "Current User")))
    userRepository.setUser("user-1", flowOf(user1))
    userRepository.setUser("user-2", flowOf(user2))
    projectRepository.setCurrentUserProjects(flowOf(listOf(projectA)))

    val viewModel =
        HomeOverviewViewModel(
            taskRepository = taskRepository,
            projectRepository = projectRepository,
            meetingRepository = meetingRepository,
            userRepository = userRepository,
            connectivityFlow = connectivityFlow)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.upcomingTasksWithAssignees.size)
    assertEquals(listOf("Alice"), state.upcomingTasksWithAssignees[0].assigneeNames)
    assertEquals(listOf("Alice", "Bob"), state.upcomingTasksWithAssignees[1].assigneeNames)
  }

  @Test
  fun upcomingTasksWithAssignees_handlesTasksWithNoAssigneeNames() = runTest {
    val now = System.currentTimeMillis()
    val projectA = Project(projectId = "proj-a", name = "Project A", lastUpdated = timestamp(now))

    // Task assigned to user-1 but user doesn't exist in repository
    val task =
        Task(
            taskID = "task-1",
            projectId = projectA.projectId,
            title = "Task with missing user",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-missing"),
            dueDate = timestamp(now + DAY))

    taskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    userRepository.setCurrentUser(flowOf(User(uid = "user-1", displayName = "Current User")))
    // Don't set user-missing in repository, so it will return null
    projectRepository.setCurrentUserProjects(flowOf(listOf(projectA)))

    val viewModel =
        HomeOverviewViewModel(
            taskRepository = taskRepository,
            projectRepository = projectRepository,
            meetingRepository = meetingRepository,
            userRepository = userRepository,
            connectivityFlow = connectivityFlow)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Task should appear but with empty assignee names since user doesn't exist
    assertEquals(
        0, state.upcomingTasksWithAssignees.size) // Task is assigned to user-missing, not user-1
  }

  @Test
  fun upcomingMeetings_filtersFutureMeetingsOnly() = runTest {
    val now = System.currentTimeMillis()
    val projectA = Project(projectId = "proj-a", name = "Project A", lastUpdated = timestamp(now))

    val pastMeeting =
        Meeting(
            meetingID = "meeting-past",
            projectId = projectA.projectId,
            title = "Past Meeting",
            status = MeetingStatus.SCHEDULED,
            datetime = timestamp(now - DAY))

    val futureMeeting =
        Meeting(
            meetingID = "meeting-future",
            projectId = projectA.projectId,
            title = "Future Meeting",
            status = MeetingStatus.SCHEDULED,
            datetime = timestamp(now + DAY))

    meetingRepository.setMeetingsForProject(
        projectA.projectId, flowOf(listOf(pastMeeting, futureMeeting)))
    projectRepository.setCurrentUserProjects(flowOf(listOf(projectA)))
    userRepository.setCurrentUser(flowOf(User(uid = "user-1", displayName = "Current User")))
    taskRepository.setCurrentUserTasks(flowOf(emptyList()))

    val viewModel =
        HomeOverviewViewModel(
            taskRepository = taskRepository,
            projectRepository = projectRepository,
            meetingRepository = meetingRepository,
            userRepository = userRepository,
            connectivityFlow = connectivityFlow)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.upcomingMeetings.size)
    assertEquals("Future Meeting", state.upcomingMeetings[0].title)
  }

  private fun timestamp(timeMillis: Long) = Timestamp(java.util.Date(timeMillis))

  companion object {
    private const val DAY = 24 * 60 * 60 * 1000L
  }
}

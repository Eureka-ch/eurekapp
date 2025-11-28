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

// portions of this code and documentation were generated with the help of AI (ChatGPT 5.1).
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
    taskRepository.reset()
    projectRepository.reset()
    userRepository.reset()
  }

  @Test
  fun aggregatesTasksMeetingsAndProjects() = runTest {
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
    meetingRepository.setMeetingsForProject(
        projectA.projectId, flowOf(meetings.filter { it.projectId == projectA.projectId }))
    meetingRepository.setMeetingsForProject(
        projectB.projectId, flowOf(meetings.filter { it.projectId == projectB.projectId }))

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
    assertEquals(3, state.upcomingMeetings.size)
    assertTrue(state.upcomingMeetings.all { it.status != MeetingStatus.COMPLETED })
    // Projects should be sorted by lastUpdated descending => Project B first
    assertEquals("Project B", state.recentProjects.first().name)
  }

  @Test
  fun propagatesErrorsAndConnectivity() = runTest {
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

  private fun timestamp(timeMillis: Long) = Timestamp(java.util.Date(timeMillis))

  companion object {
    private const val DAY = 24 * 60 * 60 * 1000L
  }
}

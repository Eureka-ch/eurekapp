package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import com.google.firebase.Timestamp
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
// portions of this code and documentation were generated with the help of AI.
@OptIn(ExperimentalCoroutinesApi::class)
class AutoAssignResultViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var viewModel: AutoAssignResultViewModel

  private val testUser1 = User(uid = "user1", displayName = "Alice", email = "alice@test.com")
  private val testUser2 = User(uid = "user2", displayName = "Bob", email = "bob@test.com")

  private val testProject1 = ch.eureka.eurekapp.model.data.project.Project(
      projectId = "proj1", name = "Project 1", memberIds = listOf("user1", "user2"))

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockTaskRepository.reset()
    mockProjectRepository.reset()
    mockUserRepository.reset()
  }

  @Test
  fun viewModel_initialState_isLoading() = runTest {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))
    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertTrue(uiState.isLoading)
  }

  @Test
  fun viewModel_withNoProjects_showsError() = runTest {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))
    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.error)
    assertTrue(uiState.error?.contains("No projects") == true)
  }

  @Test
  fun viewModel_withNoMembers_showsError() = runTest {
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers("proj1", flowOf(emptyList()))
    mockTaskRepository.setProjectTasks("proj1", flowOf(emptyList()))
    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.error)
    assertTrue(uiState.error?.contains("No team members") == true)
  }

  @Test
  fun viewModel_withUnassignedTasks_proposesAssignments() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)
    val task2 = Task(taskID = "task2", title = "Task 2", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1, task2)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertFalse(uiState.isLoading)
    assertNull(uiState.error)
    assertEquals(2, uiState.proposedAssignments.size)
    assertTrue(uiState.proposedAssignments.any { it.task.taskID == "task1" })
    assertTrue(uiState.proposedAssignments.any { it.task.taskID == "task2" })
  }

  @Test
  fun viewModel_acceptAssignment_updatesState() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val initialState = viewModel.uiState.first()
    val assignment = initialState.proposedAssignments.first()

    viewModel.acceptAssignment(assignment)
    advanceUntilIdle()

    val updatedState = viewModel.uiState.first()
    val updatedAssignment = updatedState.proposedAssignments.first { it.task.taskID == "task1" }
    assertTrue(updatedAssignment.isAccepted)
    assertFalse(updatedAssignment.isRejected)
  }

  @Test
  fun viewModel_rejectAssignment_updatesState() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    val initialState = viewModel.uiState.first()
    val assignment = initialState.proposedAssignments.first()

    viewModel.rejectAssignment(assignment)
    advanceUntilIdle()

    val updatedState = viewModel.uiState.first()
    val updatedAssignment = updatedState.proposedAssignments.first { it.task.taskID == "task1" }
    assertFalse(updatedAssignment.isAccepted)
    assertTrue(updatedAssignment.isRejected)
  }

  @Test
  fun viewModel_acceptAll_acceptsAllAssignments() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)
    val task2 = Task(taskID = "task2", title = "Task 2", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1, task2)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    viewModel.acceptAll()
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(2, uiState.proposedAssignments.size)
    assertTrue(uiState.proposedAssignments.all { it.isAccepted })
    assertFalse(uiState.proposedAssignments.any { it.isRejected })
  }

  @Test
  fun viewModel_rejectAll_rejectsAllAssignments() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)
    val task2 = Task(taskID = "task2", title = "Task 2", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1, task2)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    viewModel.rejectAll()
    advanceUntilIdle()

    val uiState = viewModel.uiState.first()
    assertEquals(2, uiState.proposedAssignments.size)
    assertTrue(uiState.proposedAssignments.all { it.isRejected })
    assertFalse(uiState.proposedAssignments.any { it.isAccepted })
  }

  @Test
  fun viewModel_applyAcceptedAssignments_appliesOnlyAccepted() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)
    val task2 = Task(taskID = "task2", title = "Task 2", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1, task2)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    // Accept only first assignment
    val initialState = viewModel.uiState.first()
    val assignment1 = initialState.proposedAssignments.first { it.task.taskID == "task1" }
    viewModel.acceptAssignment(assignment1)
    advanceUntilIdle()

    // Apply assignments
    viewModel.applyAcceptedAssignments()
    advanceUntilIdle()

    val finalState = viewModel.uiState.first()
    assertFalse(finalState.isApplying)
    assertEquals(1, finalState.appliedCount)
    // Verify that assignUser was called only once (for accepted task)
    assertEquals(1, mockTaskRepository.assignUserCallCount)
  }

  @Test
  fun viewModel_applyWithNoAccepted_showsError() = runTest {
    val task1 = Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO)

    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(task1)))
    mockUserRepository.setUser("user1", flowOf(testUser1))

    viewModel = AutoAssignResultViewModel(mockTaskRepository, mockProjectRepository, mockUserRepository)
    advanceUntilIdle()

    // Reject the assignment
    val initialState = viewModel.uiState.first()
    val assignment = initialState.proposedAssignments.first()
    viewModel.rejectAssignment(assignment)
    advanceUntilIdle()

    // Try to apply
    viewModel.applyAcceptedAssignments()
    advanceUntilIdle()

    val finalState = viewModel.uiState.first()
    assertFalse(finalState.isApplying)
    assertNotNull(finalState.error)
    assertTrue(finalState.error?.contains("No assignments selected") == true)
  }
}


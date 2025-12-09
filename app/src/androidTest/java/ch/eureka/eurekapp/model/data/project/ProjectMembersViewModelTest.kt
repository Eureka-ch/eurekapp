/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.navigation.HEARTBEAT_DURATION
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * Test suite for [ProjectMembersViewModel].
 *
 * Covers success scenarios, error handling, null safety, user sorting, and the online status logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectMembersViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var projectRepository: FakeProjectRepository
  private lateinit var userRepository: FakeUserRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    projectRepository = FakeProjectRepository()
    userRepository = FakeUserRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialUiStateIsLoading() = runTest {
    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)

    assertEquals(MembersUiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun loadMembersSuccessWithEmptyMembers() = runTest {
    val project = Project(projectId = "p1", name = "Test Project", memberIds = emptyList())
    projectRepository.projectToReturn = project

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val expected = MembersUiState.Success("Test Project", emptyList())
    assertEquals(expected, viewModel.uiState.value)
  }

  @Test
  fun loadMembersSuccessWithSortedMembers() = runTest {
    val project =
        Project(projectId = "p1", name = "Test Project", memberIds = listOf("u1", "u2", "u3"))
    projectRepository.projectToReturn = project

    val user1 = User(uid = "u1", displayName = "Charlie")
    val user2 = User(uid = "u2", displayName = "Alice")
    val user3 = User(uid = "u3", displayName = "Bob")

    userRepository.users["u1"] = user1
    userRepository.users["u2"] = user2
    userRepository.users["u3"] = user3

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value as MembersUiState.Success
    assertEquals("Test Project", state.projectName)
    assertEquals(3, state.members.size)
    assertEquals("Alice", state.members[0].displayName)
    assertEquals("Bob", state.members[1].displayName)
    assertEquals("Charlie", state.members[2].displayName)
  }

  @Test
  fun loadMembersHandlesProjectNull() = runTest {
    projectRepository.projectToReturn = null

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value as MembersUiState.Error
    assertEquals("Failed to load members", state.message)
  }

  @Test
  fun loadMembersHandlesRepoException() = runTest {
    projectRepository.shouldThrow = true

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value as MembersUiState.Error
    assertEquals("Failed to load members", state.message)
  }

  @Test
  fun fetchUsersSkipsNullUsers() = runTest {
    val project = Project(projectId = "p1", name = "Test Project", memberIds = listOf("u1", "u2"))
    projectRepository.projectToReturn = project

    val user1 = User(uid = "u1", displayName = "Alice")
    userRepository.users["u1"] = user1
    userRepository.users["u2"] = null

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value as MembersUiState.Success
    assertEquals(1, state.members.size)
    assertEquals("Alice", state.members[0].displayName)
  }

  @Test
  fun fetchUsersHandlesExceptionForSingleUser() = runTest {
    val project = Project(projectId = "p1", name = "Test Project", memberIds = listOf("u1", "u2"))
    projectRepository.projectToReturn = project

    val user1 = User(uid = "u1", displayName = "Alice")
    userRepository.users["u1"] = user1
    userRepository.users["u2"] = User(uid = "u2")
    userRepository.usersToThrow.add("u2")

    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value as MembersUiState.Success
    assertEquals(1, state.members.size)
    assertEquals("Alice", state.members[0].displayName)
  }

  @Test
  fun isUserOnlineReturnsFalseForZeroTimestamp() {
    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    val timestamp = Timestamp(0, 0)
    assertFalse(viewModel.isUserOnline(timestamp))
  }

  @Test
  fun isUserOnlineReturnsTrueForRecentActivity() {
    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    val now = Date()
    val timestamp = Timestamp(now)
    assertTrue(viewModel.isUserOnline(timestamp))
  }

  @Test
  fun isUserOnlineReturnsFalseForOldActivity() {
    val viewModel = ProjectMembersViewModel("p1", projectRepository, userRepository)
    val oldTime = System.currentTimeMillis() - (HEARTBEAT_DURATION + 10000)
    val timestamp = Timestamp(Date(oldTime))
    assertFalse(viewModel.isUserOnline(timestamp))
  }

  @Test
  fun factoryCreatesViewModelInstance() {
    val factory = ProjectMembersViewModel.Factory("p1")
    val viewModel = factory.create(ProjectMembersViewModel::class.java)
    assertEquals(MembersUiState.Loading, viewModel.uiState.value)
  }

  class FakeProjectRepository : ProjectRepository {
    var projectToReturn: Project? = null
    var shouldThrow = false

    override fun getProjectById(projectId: String): Flow<Project?> = flow {
      if (shouldThrow) throw RuntimeException("Repo Error")
      emit(projectToReturn)
    }

    override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
        flowOf(emptyList())

    override suspend fun createProject(
        project: Project,
        creatorId: String,
        creatorRole: ProjectRole
    ): Result<String> = Result.success("")

    override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

    override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

    override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

    override suspend fun addMember(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> = Result.success(Unit)

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> = Result.success(Unit)
  }

  class FakeUserRepository : UserRepository {
    val users = mutableMapOf<String, User?>()
    val usersToThrow = mutableSetOf<String>()

    override fun getUserById(userId: String): Flow<User?> = flow {
      if (usersToThrow.contains(userId)) throw RuntimeException("User Fetch Error")
      emit(users[userId])
    }

    override fun getCurrentUser(): Flow<User?> = flowOf(null)

    override suspend fun saveUser(user: User): Result<Unit> = Result.success(Unit)

    override suspend fun updateLastActive(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> =
        Result.success(Unit)
  }
}

package ch.eureka.eurekapp.ui.conversation

import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationListViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockConversationRepository: ConversationRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockProjectRepository: ProjectRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver

  private val currentUserId = "currentUser123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockConversationRepository = mockk()
    mockUserRepository = mockk()
    mockProjectRepository = mockk()
    mockConnectivityObserver = mockk()
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loadConversations emits conversations with resolved display data`() = runTest {
    // Arrange: Set up mock data for a conversation with resolved user and project
    val otherUser = User(uid = "otherUser", displayName = "John Doe", photoUrl = "http://photo.url")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"))

    // Configure mocks to return test data
    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    // Act: Create ViewModel which triggers loading
    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    val state = viewModel.uiState.value

    // Assert: Verify display data is correctly resolved
    assertFalse(state.isLoading)
    assertEquals(1, state.conversations.size)
    assertEquals("John Doe", state.conversations[0].otherMemberName)
    assertEquals("Test Project", state.conversations[0].projectName)
    assertEquals("http://photo.url", state.conversations[0].otherMemberPhotoUrl)
  }

  @Test
  fun `loadConversations handles empty list`() = runTest {
    // Arrange: Return empty list from repository
    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(emptyList())

    // Act: Create ViewModel
    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    val state = viewModel.uiState.value

    // Assert: State shows empty list, not loading
    assertFalse(state.isLoading)
    assertTrue(state.conversations.isEmpty())
  }

  @Test
  fun `loadConversations uses fallback for unknown user`() = runTest {
    // Arrange: User repository returns null (user not found)
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "unknownUser"))

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("unknownUser") } returns flowOf(null)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    // Act: Create ViewModel
    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    val state = viewModel.uiState.value

    // Assert: Fallback values are used for unknown user
    assertEquals("Unknown User", state.conversations[0].otherMemberName)
    assertEquals("", state.conversations[0].otherMemberPhotoUrl)
  }

  @Test
  fun `connectivity state updates UI state`() = runTest {
    // Arrange: Connectivity observer reports offline
    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(emptyList())
    every { mockConnectivityObserver.isConnected } returns flowOf(false)

    // Act: Create ViewModel
    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    // Assert: UI state reflects offline status
    assertFalse(viewModel.uiState.value.isConnected)
  }
}

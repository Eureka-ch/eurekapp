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
import kotlinx.coroutines.test.StandardTestDispatcher
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

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationListViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

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

    // Wait for all coroutines to complete
    advanceUntilIdle()

    val state = viewModel.uiState.value

    // Assert: Verify display data is correctly resolved
    assertFalse(state.isLoading)
    assertEquals(1, state.conversations.size)
    assertEquals("John Doe", state.conversations[0].otherMemberName)
    assertEquals("Test Project", state.conversations[0].projectName)
    assertEquals("http://photo.url", state.conversations[0].otherMemberPhotoUrl)
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

    // Wait for all coroutines to complete
    advanceUntilIdle()

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

    // Wait for all coroutines to complete
    advanceUntilIdle()

    // Assert: UI state reflects offline status
    assertFalse(viewModel.uiState.value.isConnected)
  }

  /**
   * Verifies that the last message preview is resolved from the conversation. This text appears on
   * the conversation card to show recent activity.
   */
  @Test
  fun `loadConversations resolves lastMessagePreview`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"),
            lastMessagePreview = "Hello there!")

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Hello there!", state.conversations[0].lastMessagePreview)
  }

  /**
   * Verifies that hasUnread is true when the user has never read the conversation. If lastReadAt
   * map is empty for the current user, any message makes it unread.
   */
  @Test
  fun `loadConversations calculates hasUnread when lastReadAt is null`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"),
            lastMessageAt = com.google.firebase.Timestamp.now(),
            lastReadAt = emptyMap()) // User hasn't read any messages

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.conversations[0].hasUnread)
  }

  /**
   * Verifies that hasUnread is false when the user has read after the last message. Compares
   * lastReadAt timestamp with lastMessageAt to determine read status.
   */
  @Test
  fun `loadConversations calculates hasUnread false when read after last message`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane")
    val project = Project(projectId = "project1", name = "Test Project")
    val pastTime = com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() - 60000))
    val recentTime = com.google.firebase.Timestamp.now()
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"),
            lastMessageAt = pastTime,
            lastReadAt = mapOf(currentUserId to recentTime)) // User read after last message

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.conversations[0].hasUnread)
  }

  /**
   * Verifies that hasUnread is false when the conversation has no messages. New conversations
   * without any messages should not show unread indicators.
   */
  @Test
  fun `loadConversations hasUnread is false when no messages`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"),
            lastMessageAt = null) // No messages yet

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.conversations[0].hasUnread)
  }

  /**
   * Verifies that lastMessageTime is formatted as relative time. Uses Formatters.formatRelativeTime
   * to convert timestamp to "now", "5m", "2h", etc.
   */
  @Test
  fun `loadConversations resolves lastMessageTime`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"),
            lastMessageAt = com.google.firebase.Timestamp.now())

    every { mockConversationRepository.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should return "now" since timestamp is very recent
    assertEquals("now", state.conversations[0].lastMessageTime)
  }
}

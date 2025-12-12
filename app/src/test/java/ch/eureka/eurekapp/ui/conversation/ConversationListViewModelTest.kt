package ch.eureka.eurekapp.ui.conversation

import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
  private lateinit var mockSelfNotesRepository: UnifiedSelfNotesRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver

  private val currentUserId = "currentUser123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockConversationRepository = mockk()
    mockUserRepository = mockk()
    mockProjectRepository = mockk()
    mockSelfNotesRepository = mockk<UnifiedSelfNotesRepository>(relaxed = true)
    mockConnectivityObserver = mockk()
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
    // Default mock for self notes repository - returns empty list
    every { mockSelfNotesRepository.getNotes(any<Int>()) } returns flowOf(emptyList())
    // Default mock for current user
    every { mockUserRepository.getUserById(currentUserId) } returns
        flowOf(
            User(uid = currentUserId, displayName = "Current User", photoUrl = "http://photo.url"))
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    // Wait for all coroutines to complete
    advanceUntilIdle()

    val state = viewModel.uiState.value

    // Assert: Verify display data is correctly resolved
    // Should have "to self" conversation first, then the regular conversation
    assertFalse(state.isLoading)
    assertEquals(2, state.conversations.size)
    // First conversation should be "to self"
    assertEquals(TO_SELF_CONVERSATION_ID, state.conversations[0].conversation.conversationId)
    assertEquals(listOf("To Self"), state.conversations[0].otherMembers)
    assertEquals("Personal", state.conversations[0].projectName)
    // Second conversation should be the regular one
    assertEquals(listOf("John Doe"), state.conversations[1].otherMembers)
    assertEquals("Test Project", state.conversations[1].projectName)
    assertEquals(listOf("http://photo.url"), state.conversations[1].otherMembersPhotoUrl)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    // Wait for all coroutines to complete
    advanceUntilIdle()

    val state = viewModel.uiState.value

    // Assert: Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    // First is "to self"
    assertEquals(TO_SELF_CONVERSATION_ID, state.conversations[0].conversation.conversationId)
    // Second conversation has fallback values for unknown user
    assertEquals(listOf<String>(), state.conversations[1].otherMembers)
    assertEquals(listOf<String>(), state.conversations[1].otherMembersPhotoUrl)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    // Wait for all coroutines to complete
    advanceUntilIdle()

    // Assert: UI state reflects offline status
    assertFalse(viewModel.uiState.value.isConnected)
    // Should still have "to self" conversation even when offline
    assertTrue(viewModel.uiState.value.conversations.isNotEmpty())
    assertEquals(
        TO_SELF_CONVERSATION_ID,
        viewModel.uiState.value.conversations[0].conversation.conversationId)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    assertEquals("Hello there!", state.conversations[1].lastMessagePreview)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should have "to self" first (no unread), then regular conversation
    assertEquals(2, state.conversations.size)
    assertFalse(state.conversations[0].hasUnread) // "to self" never has unread
    assertTrue(state.conversations[1].hasUnread)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    assertFalse(state.conversations[0].hasUnread) // "to self" never has unread
    assertFalse(state.conversations[1].hasUnread)
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
            selfNotesRepository = mockSelfNotesRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    // Should return "now" since timestamp is very recent
    assertEquals("now", state.conversations[1].lastMessageTime)
  }

  @Test
  fun resolveConversationDisplayData_withMultipleOtherMembersReturnsAllNames() = runTest {
    val mockConversationRepo =
        mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockProjectRepo = mockk<ch.eureka.eurekapp.model.data.project.ProjectRepository>()

    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "proj1",
            memberIds = listOf("currentUser", "user1", "user2"))

    every { mockConversationRepo.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockUserRepo.getUserById("user1") } returns
        flowOf(User("user1", "Alice", "alice@example.com", "https://alice.com/photo.jpg"))
    every { mockUserRepo.getUserById("user2") } returns
        flowOf(User("user2", "Bob", "bob@example.com", "https://bob.com/photo.jpg"))
    every { mockProjectRepo.getProjectById("proj1") } returns
        flowOf(Project(projectId = "proj1", name = "Test Project"))

    val mockSelfNotesRepo = mockk<UnifiedSelfNotesRepository>()
    every { mockSelfNotesRepo.getNotes(any()) } returns flowOf(emptyList())
    every { mockUserRepo.getUserById("currentUser") } returns
        flowOf(User("currentUser", "Current User", "", ""))

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepo,
            userRepository = mockUserRepo,
            projectRepository = mockProjectRepo,
            selfNotesRepository = mockSelfNotesRepo,
            getCurrentUserId = { "currentUser" },
            connectivityObserver = mockk { every { isConnected } returns flowOf(true) })

    advanceUntilIdle()
    val state = viewModel.uiState.first { !it.isLoading }

    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    assertEquals(TO_SELF_CONVERSATION_ID, state.conversations[0].conversation.conversationId)
    assertEquals(listOf("Alice", "Bob"), state.conversations[1].otherMembers)
    assertEquals(
        listOf("https://alice.com/photo.jpg", "https://bob.com/photo.jpg"),
        state.conversations[1].otherMembersPhotoUrl)
  }

  @Test
  fun resolveConversationDisplayData_withEmptyOtherUserIdsReturnsEmptyLists() = runTest {
    val mockConversationRepo =
        mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockProjectRepo = mockk<ch.eureka.eurekapp.model.data.project.ProjectRepository>()

    val conversation =
        Conversation(
            conversationId = "conv1", projectId = "proj1", memberIds = listOf("currentUser"))

    every { mockConversationRepo.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))
    every { mockProjectRepo.getProjectById("proj1") } returns
        flowOf(Project(projectId = "proj1", name = "Test Project"))

    val mockSelfNotesRepo = mockk<UnifiedSelfNotesRepository>()
    every { mockSelfNotesRepo.getNotes(any()) } returns flowOf(emptyList())
    every { mockUserRepo.getUserById("currentUser") } returns
        flowOf(User("currentUser", "Current User", "", ""))

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepo,
            userRepository = mockUserRepo,
            projectRepository = mockProjectRepo,
            selfNotesRepository = mockSelfNotesRepo,
            getCurrentUserId = { "currentUser" },
            connectivityObserver = mockk { every { isConnected } returns flowOf(true) })

    advanceUntilIdle()
    val state = viewModel.uiState.first { !it.isLoading }

    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    assertEquals(TO_SELF_CONVERSATION_ID, state.conversations[0].conversation.conversationId)
    assertEquals(emptyList<String>(), state.conversations[1].otherMembers)
    assertEquals(emptyList<String>(), state.conversations[1].otherMembersPhotoUrl)
  }

  @Test
  fun getUserIds_combinesMultipleUserFlows() = runTest {
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()

    every { mockUserRepo.getUserById("user1") } returns
        flowOf(User("user1", "Alice", "alice@example.com", ""))
    every { mockUserRepo.getUserById("user2") } returns
        flowOf(User("user2", "Bob", "bob@example.com", ""))
    every { mockUserRepo.getUserById("user3") } returns flowOf(null)

    val mockConversationRepo =
        mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val conversation =
        Conversation(
            conversationId = "conv1",
            projectId = "proj1",
            memberIds = listOf("currentUser", "user1", "user2", "user3"))

    every { mockConversationRepo.getConversationsForCurrentUser() } returns
        flowOf(listOf(conversation))

    val mockSelfNotesRepo = mockk<UnifiedSelfNotesRepository>()
    every { mockSelfNotesRepo.getNotes(any()) } returns flowOf(emptyList())
    every { mockUserRepo.getUserById("currentUser") } returns
        flowOf(User("currentUser", "Current User", "", ""))

    val viewModel =
        ConversationListViewModel(
            conversationRepository = mockConversationRepo,
            userRepository = mockUserRepo,
            projectRepository =
                mockk {
                  every { getProjectById("proj1") } returns
                      flowOf(Project(projectId = "proj1", name = "Test"))
                },
            selfNotesRepository = mockSelfNotesRepo,
            getCurrentUserId = { "currentUser" },
            connectivityObserver = mockk { every { isConnected } returns flowOf(true) })

    advanceUntilIdle()
    val state = viewModel.uiState.first { !it.isLoading }

    // Should have "to self" first, then regular conversation
    assertEquals(2, state.conversations.size)
    assertEquals(TO_SELF_CONVERSATION_ID, state.conversations[0].conversation.conversationId)
    assertEquals(2, state.conversations[1].otherMembers.size)
  }
}

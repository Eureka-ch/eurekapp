package ch.eureka.eurekapp.ui.conversation

import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationDetailViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockConversationRepository: ConversationRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockProjectRepository: ProjectRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver
  private val currentUserId = "currentUser123"
  private val conversationId = "conv123"

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

  private fun createViewModel(): ConversationDetailViewModel {
    return ConversationDetailViewModel(
        conversationId = conversationId,
        conversationRepository = mockConversationRepository,
        userRepository = mockUserRepository,
        projectRepository = mockProjectRepository,
        getCurrentUserId = { currentUserId },
        connectivityObserver = mockConnectivityObserver)
  }

  @Test
  fun `initial state is loading`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    assertTrue(viewModel.uiState.value.isLoading)
  }

  @Test
  fun `loadConversation resolves display data correctly`() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane Doe")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = conversationId,
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"))

    every { mockConversationRepository.getConversationById(conversationId) } returns
        flowOf(conversation)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { it.otherMemberName.isNotEmpty() }

    assertEquals("Jane Doe", state.otherMemberName)
    assertEquals("Test Project", state.projectName)
  }

  @Test
  fun `loadMessages populates messages list`() = runTest {
    val messages =
        listOf(
            ConversationMessage(messageId = "msg1", text = "Hello"),
            ConversationMessage(messageId = "msg2", text = "Hi"))

    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(messages)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { it.messages.isNotEmpty() }

    assertEquals(2, state.messages.size)
  }

  @Test
  fun `sendMessage sends and clears input`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.sendMessage(conversationId, "Test") } returns
        Result.success(ConversationMessage(messageId = "new", text = "Test"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("Test")
    viewModel.sendMessage()
    advanceUntilIdle()

    assertEquals("", viewModel.uiState.value.currentMessage)
    coVerify { mockConversationRepository.sendMessage(conversationId, "Test") }
  }

  @Test
  fun `sendMessage validates empty and long messages`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("   ")
    viewModel.sendMessage()
    advanceUntilIdle()
    coVerify(exactly = 0) { mockConversationRepository.sendMessage(any(), any()) }

    viewModel.updateMessage("a".repeat(5001))
    viewModel.sendMessage()
    advanceUntilIdle()
    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("too long") == true)
  }

  @Test
  fun `markAsRead calls repository`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.markMessagesAsRead(conversationId) } returns
        Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.markAsRead()
    advanceUntilIdle()

    coVerify { mockConversationRepository.markMessagesAsRead(conversationId) }
  }

  @Test
  fun `connectivity state updates UI`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    every { mockConnectivityObserver.isConnected } returns flowOf(false)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { !it.isConnected }

    assertFalse(state.isConnected)
  }
}

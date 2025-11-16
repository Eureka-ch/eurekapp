package ch.eureka.eurekapp.model.notes

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.note.SelfNotesRepository
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/*
Co-author: GPT-5 Codex
*/

@OptIn(ExperimentalCoroutinesApi::class)
class SelfNotesViewModelTest {

  private lateinit var repository: SelfNotesRepository
  private lateinit var viewModel: SelfNotesViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val testUserId = "test-user-id"

  private val testMessage =
      Message(
          messageID = "msg-1",
          text = "Test note",
          senderId = testUserId,
          createdAt = Timestamp.now(),
          references = emptyList())

  private val testMessages =
      listOf(
          testMessage,
          Message(
              messageID = "msg-2",
              text = "Another note",
              senderId = testUserId,
              createdAt = Timestamp.now(),
              references = emptyList()))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repository = mockk()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `loadNotes loads notes successfully`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(testMessages)

    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(testMessages, state.notes)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
  }

  @Test
  fun `loadNotes sets error when user not authenticated`() = runTest {
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { null }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("User not authenticated", state.errorMsg)
  }

  @Test
  fun `loadNotes handles repository exception`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns
        flow { throw Exception("Network error") }

    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Failed to load notes"))
  }

  @Test
  fun `updateMessage handles special characters`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    val specialText = "Test @#\$%^&*() 测试 🎉"
    viewModel.updateMessage(specialText)

    assertEquals(specialText, viewModel.uiState.value.currentMessage)
  }

  @Test
  fun `sendNote sends message successfully`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(testUserId, any()) } returns Result.success("new-note-id")
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test note to send")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("", state.currentMessage)
    assertFalse(state.isSending)
    assertNull(state.errorMsg)
    coVerify { repository.createNote(testUserId, match { it.text == "Test note to send" }) }
  }

  @Test
  fun `sendNote trims whitespace from message`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(testUserId, any()) } returns Result.success("new-note-id")
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("  Trimmed message  ")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.createNote(testUserId, match { it.text == "Trimmed message" }) }
  }

  @Test
  fun `sendNote sets error when user not authenticated`() = runTest {
    every { repository.getNotesForUser(any(), any()) } returns flowOf(emptyList())
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test message")

    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { null }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test message")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("User not authenticated", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `sendNote handles repository failure`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(testUserId, any()) } returns
        Result.failure(Exception("Network error"))
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test message")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isSending)
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Failed to send note"))
    assertEquals("Test message", state.currentMessage)
  }

  @Test
  fun `sendNote sets isSending to true while sending`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(testUserId, any()) } coAnswers
        {
          delay(100)
          Result.success("new-note-id")
        }
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test message")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceTimeBy(50)

    assertTrue(viewModel.uiState.value.isSending)

    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isSending)
  }

  @Test
  fun `clearError clears error message`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns
        flow { throw Exception("Error") }
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearError()

    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `notes are updated when repository emits new values`() = runTest {
    val updatedMessages = testMessages + testMessage.copy(messageID = "msg-3", text = "New note")
    every { repository.getNotesForUser(testUserId, any()) } returns
        flowOf(testMessages, updatedMessages)

    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(updatedMessages, viewModel.uiState.value.notes)
  }

  @Test
  fun `multiple sendNote calls work correctly`() = runTest {
    every { repository.getNotesForUser(testUserId, any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(testUserId, any()) } returns Result.success("new-note-id")
    viewModel =
        SelfNotesViewModel(
            repository = repository, getCurrentUserId = { testUserId }, dispatcher = testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Note 1")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Note 2")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify(exactly = 2) { repository.createNote(testUserId, any()) }
    assertEquals("", viewModel.uiState.value.currentMessage)
  }
}

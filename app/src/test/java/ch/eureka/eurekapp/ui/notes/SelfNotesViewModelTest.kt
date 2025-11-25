/* Portions of this code were written with the help of GPT-5 Codex. */
package ch.eureka.eurekapp.ui.notes

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.notes.SelfNotesRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

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
          createdAt = Timestamp.Companion.now(),
          references = emptyList())

  private val testMessages =
      listOf(
          testMessage,
          Message(
              messageID = "msg-2",
              text = "Another note",
              senderId = testUserId,
              createdAt = Timestamp.Companion.now(),
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
    every { repository.getNotes(any()) } returns flowOf(testMessages)

    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    // Subscribe to the StateFlow to trigger collection
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertEquals(testMessages, state.notes)
    Assert.assertFalse(state.isLoading)
    Assert.assertNull(state.errorMsg)

    job.cancel()
  }

  @Test
  fun `loadNotes handles repository exception`() = runTest {
    every { repository.getNotes(any()) } returns flow { throw Exception("Network error") }

    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertNotNull(state.errorMsg)
    Assert.assertTrue(state.errorMsg!!.contains("Failed to load notes"))

    job.cancel()
  }

  @Test
  fun `loadNotes handles authentication error from repository`() = runTest {
    every { repository.getNotes(any()) } returns
        flow { throw IllegalStateException("User must be authenticated to access notes") }

    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertNotNull(state.errorMsg)
    Assert.assertTrue(state.errorMsg!!.contains("User must be authenticated"))

    job.cancel()
  }

  @Test
  fun `updateMessage handles special characters`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val specialText = "Test @#\$%^&*() 测试 🎉"
    viewModel.updateMessage(specialText)
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertEquals(specialText, viewModel.uiState.value.currentMessage)

    job.cancel()
  }

  @Test
  fun `sendNote sends message successfully`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Test note to send")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertEquals("", state.currentMessage)
    Assert.assertFalse(state.isSending)
    Assert.assertNull(state.errorMsg)
    coVerify { repository.createNote(match { it.text == "Test note to send" }) }

    job.cancel()
  }

  @Test
  fun `sendNote trims whitespace from message`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("  Trimmed message  ")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.createNote(match { it.text == "Trimmed message" }) }

    job.cancel()
  }

  @Test
  fun `sendNote handles repository failure`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Network error"))
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Test message")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isSending)
    Assert.assertNotNull(state.errorMsg)
    Assert.assertTrue(state.errorMsg!!.contains("Failed to send note"))
    Assert.assertEquals("Test message", state.currentMessage)

    job.cancel()
  }

  @Test
  fun `sendNote sets isSending to true while sending`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(any()) } coAnswers
        {
          delay(100)
          Result.success("new-note-id")
        }
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Test message")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceTimeBy(50)

    Assert.assertTrue(viewModel.uiState.value.isSending)

    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertFalse(viewModel.uiState.value.isSending)

    job.cancel()
  }

  @Test
  fun `clearError clears error message`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    // Manually trigger an error by sending with failure
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Error"))
    viewModel.updateMessage("Test")
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearError()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertNull(viewModel.uiState.value.errorMsg)

    job.cancel()
  }

  @Test
  fun `notes are updated when repository emits new values`() = runTest {
    val updatedMessages = testMessages + testMessage.copy(messageID = "msg-3", text = "New note")
    every { repository.getNotes(any()) } returns flowOf(testMessages, updatedMessages)

    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertEquals(updatedMessages, viewModel.uiState.value.notes)

    job.cancel()
  }

  @Test
  fun `multiple sendNote calls work correctly`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Note 1")
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Note 2")
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify(exactly = 2) { repository.createNote(any()) }
    Assert.assertEquals("", viewModel.uiState.value.currentMessage)

    job.cancel()
  }

  @Test
  fun `sendNote does nothing when message is empty`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify(exactly = 0) { repository.createNote(any()) }

    job.cancel()
  }

  @Test
  fun `sendNote does nothing when message is only whitespace`() = runTest {
    every { repository.getNotes(any()) } returns flowOf(emptyList())
    viewModel = SelfNotesViewModel(repository = repository, dispatcher = testDispatcher)

    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("   ")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify(exactly = 0) { repository.createNote(any()) }

    job.cancel()
  }
}

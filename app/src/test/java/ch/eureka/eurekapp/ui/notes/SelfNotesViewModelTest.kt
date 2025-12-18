/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.notes

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelfNotesViewModelTest {

  private lateinit var repository: UnifiedSelfNotesRepository
  private lateinit var userPrefs: UserPreferencesRepository
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
    repository = mockk(relaxed = true)
    userPrefs = mockk(relaxed = true)

    every { userPrefs.isCloudStorageEnabled } returns flowOf(false) // Default Local
    every { repository.getNotes(any()) } returns flowOf(testMessages)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `loadNotes loads notes and preferences successfully`() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(testMessages, state.notes)
    assertTrue(state.isCloudStorageEnabled)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
    job.cancel()
  }

  @Test
  fun `loadNotes handles repository exception`() = runTest {
    every { repository.getNotes(any()) } returns flow { throw Exception("Database error") }
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Database error", state.errorMsg)
    job.cancel()
  }

  @Test
  fun `toggleStorageMode(true) calls repository and updates status message`() = runTest {
    coEvery { repository.setStorageMode(true) } returns Unit
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleStorageMode(true)
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.setStorageMode(true) }
    assertEquals("Viewing Cloud Notes", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun `toggleStorageMode(false) calls repository and updates status message`() = runTest {
    coEvery { repository.setStorageMode(false) } returns Unit
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleStorageMode(false)
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.setStorageMode(false) }
    assertEquals("Viewing Local Notes", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  // --- CREATE ---

  @Test
  fun `sendNote(Create) sends message successfully`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Test note")
    viewModel.sendNote()

    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("", state.currentMessage)
    assertFalse(state.isSending)
    assertNull(state.errorMsg)
    coVerify { repository.createNote(match { it.text == "Test note" }) }
    job.cancel()
  }

  @Test
  fun `sendNote(Create) handles failure`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Offline"))
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.updateMessage("Fail note")
    viewModel.sendNote()

    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertFalse(state.isSending)
    // The VM sets the error message on failure
    assertEquals("Offline", state.errorMsg)
    job.cancel()
  }

  // --- EDIT ---

  @Test
  fun `startEditing populates fields and clears selection`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("msg-2")
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.selectedNoteIds.contains("msg-2"))

    viewModel.startEditing(testMessage) // msg-1
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("msg-1", state.editingMessageId)
    assertEquals("Test note", state.currentMessage)
    assertTrue(state.selectedNoteIds.isEmpty())
    job.cancel()
  }

  @Test
  fun `cancelEditing clears editing state`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.startEditing(testMessage)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.cancelEditing()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.editingMessageId)
    assertEquals("", state.currentMessage)
    job.cancel()
  }

  @Test
  fun `sendNote(Update) calls updateNote on repository`() = runTest {
    coEvery { repository.updateNote(any(), any()) } returns Result.success(Unit)

    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.startEditing(testMessage)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Updated Text")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.updateNote("msg-1", "Updated Text") }
    assertNull(viewModel.uiState.value.editingMessageId)
    assertEquals("", viewModel.uiState.value.currentMessage)

    job.cancel()
  }

  @Test
  fun `sendNote(Update) handles failure`() = runTest {
    coEvery { repository.updateNote(any(), any()) } returns Result.failure(Exception("Edit failed"))

    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.startEditing(testMessage)
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Updated Text")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.updateNote("msg-1", "Updated Text") }
    assertEquals("Edit failed", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.isSending)

    job.cancel()
  }

  // --- SELECTION & DELETE ---

  @Test
  fun `toggleSelection adds and removes ids`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.selectedNoteIds.contains("1"))

    viewModel.toggleSelection("2")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(setOf("1", "2"), viewModel.uiState.value.selectedNoteIds)

    viewModel.toggleSelection("1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(setOf("2"), viewModel.uiState.value.selectedNoteIds)

    job.cancel()
  }

  @Test
  fun `clearSelection empties the set`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("1")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.clearSelection()
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.selectedNoteIds.isEmpty())

    job.cancel()
  }

  @Test
  fun `deleteSelectedNotes calls repository for each id and clears selection`() = runTest {
    coEvery { repository.deleteNote(any()) } returns Result.success(Unit)

    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("msg-1")
    viewModel.toggleSelection("msg-2")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.deleteSelectedNotes()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.deleteNote("msg-1") }
    coVerify { repository.deleteNote("msg-2") }
    assertTrue(viewModel.uiState.value.selectedNoteIds.isEmpty())

    job.cancel()
  }

  @Test
  fun `deleteSelectedNotes reports error on failure`() = runTest {
    coEvery { repository.deleteNote("msg-1") } returns Result.failure(Exception("Delete failed"))

    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("msg-1")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.deleteSelectedNotes()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.deleteNote("msg-1") }
    // Ensure error message is set
    assertTrue(viewModel.uiState.value.errorMsg!!.contains("Failed to delete"))
    assertTrue(viewModel.uiState.value.selectedNoteIds.isEmpty())

    job.cancel()
  }

  @Test
  fun `sendNote ignores empty or whitespace`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    viewModel.updateMessage("   ")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { repository.createNote(any()) }
  }

  @Test
  fun `clearError clears error message`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Error"))
    viewModel = SelfNotesViewModel(repository, userPrefs, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.updateMessage("Msg")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    org.junit.Assert.assertNotNull(viewModel.uiState.value.errorMsg)
    viewModel.clearError()
    testDispatcher.scheduler.advanceUntilIdle()
    assertNull(viewModel.uiState.value.errorMsg)
    job.cancel()
  }
}

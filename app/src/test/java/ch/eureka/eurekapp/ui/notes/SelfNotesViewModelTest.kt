/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.notes.SyncStats
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
  private lateinit var workManager: WorkManager
  private lateinit var viewModel: SelfNotesViewModel
  private lateinit var connectivityObserver: ConnectivityObserver
  private val isOnlineFlow = MutableStateFlow(true)

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
    workManager = mockk(relaxed = true)
    connectivityObserver = mockk(relaxed = true)
    mockkObject(ConnectivityObserverProvider)
    every { ConnectivityObserverProvider.connectivityObserver } returns connectivityObserver
    every { connectivityObserver.isConnected } returns isOnlineFlow
    every { userPrefs.isCloudStorageEnabled } returns flowOf(false) // Default Local
    every { repository.getNotes(any()) } returns flowOf(testMessages)
    every { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns
        mockk<Operation>()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun selfNotesViewModel_loadsNotesAndPreferencesSuccessfully() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
  fun selfNotesViewModel_handlesRepositoryException() = runTest {
    every { repository.getNotes(any()) } returns flow { throw Exception("Database error") }
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Database error", state.errorMsg)
    job.cancel()
  }

  @Test
  fun selfNotesViewModel_toggleStorageModeToCloudCallsRepositoryAndUpdatesStatusWithUpserts() =
      runTest {
        // Return SyncStats instead of Int
        coEvery { repository.setStorageMode(true) } returns SyncStats(upserts = 5, deletes = 0)
        viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
        val job = launch { viewModel.uiState.collect {} }
        viewModel.toggleStorageMode(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.setStorageMode(true) }
        // Check formatted string
        assertEquals("Switched to Cloud: Synced: 5 sent", viewModel.uiState.value.errorMsg)
        job.cancel()
      }

  @Test
  fun selfNotesViewModel_toggleStorageModeToCloudShowsGenericMessageIfZeroSynced() = runTest {
    coEvery { repository.setStorageMode(true) } returns SyncStats(0, 0)
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.toggleStorageMode(true)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Switched to Cloud Storage", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun selfNotesViewModel_toggleStorageModeToLocalShowsLocalMessage() = runTest {
    coEvery { repository.setStorageMode(false) } returns SyncStats(0, 0)
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.toggleStorageMode(false)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Switched to Local Storage (Private)", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun selfNotesViewModel_connectivityChangeToOnlinetriggersSyncAndReportsMixedStats() = runTest {
    coEvery { repository.syncPendingNotes() } returns SyncStats(upserts = 2, deletes = 1)
    isOnlineFlow.value = false
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    isOnlineFlow.value = true
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.syncPendingNotes() }
    assertEquals("Back online: Synced: 2 sent, 1 deleted", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun selfNotesViewModel_connectivityChangeToOfflineDoesNothing() = runTest {
    isOnlineFlow.value = true
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    io.mockk.clearMocks(repository, answers = false)
    isOnlineFlow.value = false
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { repository.syncPendingNotes() }
  }

  // --- CREATE ---

  @Test
  fun selfNotesViewModel_sendNoteCreateSendsMessageSuccessfullyAndSchedulesWorker() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
    verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
    job.cancel()
  }

  @Test
  fun selfNotesViewModel_sendNoteCreateSendsMessageButDoesNotScheduleWorkerIfCloudIsDisabled() =
      runTest {
        every { userPrefs.isCloudStorageEnabled } returns flowOf(false)
        coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
        viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
        val job = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMessage("Local note")
        viewModel.sendNote()

        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.createNote(match { it.text == "Local note" }) }
        verify(exactly = 0) {
          workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
        job.cancel()
      }

  @Test
  fun selfNotesViewModel_sendNoteCreateHandlesFailure() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("DB Error"))
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.updateMessage("Fail note")
    viewModel.sendNote()

    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    assertFalse(state.isSending)
    assertEquals("Error: DB Error", state.errorMsg)
    verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    job.cancel()
  }

  // --- EDIT ---

  @Test
  fun selfNotesViewModel_startEditingPopulatesFieldsAndClearsSelection() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
  fun selfNotesViewModel_cancelEditingClearsEditingState() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
  fun selfNotesViewModel_sendNoteUpdateCallsUpdateNoteOnRepository() = runTest {
    coEvery { repository.updateNote(any(), any()) } returns Result.success(Unit)
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)

    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.startEditing(testMessage)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateMessage("Updated Text")

    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()

    coVerify { repository.updateNote("msg-1", "Updated Text") }
    assertNull(viewModel.uiState.value.editingMessageId)
    assertEquals("", viewModel.uiState.value.currentMessage)
    verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }

    job.cancel()
  }

  // --- SELECTION & DELETE ---

  @Test
  fun selfNotesViewModel_toggleSelectionAddsAndRemovesIds() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
  fun selfNotesViewModel_clearSelectionEmptiesTheSet() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }

    viewModel.toggleSelection("1")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.clearSelection()
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.selectedNoteIds.isEmpty())

    job.cancel()
  }

  @Test
  fun selfNotesViewModel_deleteSelectedNotesCallsRepositoryForEachIdAndClearsSelection() = runTest {
    coEvery { repository.deleteNote(any()) } returns Result.success(Unit)

    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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
  fun selfNotesViewModel_sendNoteIgnoresEmptyOrWhitespace() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    viewModel.updateMessage("   ")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { repository.createNote(any()) }
  }

  @Test
  fun selfNotesViewModel_clearErrorClearsErrorMessage() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Error"))
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
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

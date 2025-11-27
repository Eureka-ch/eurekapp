/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.chat.Message
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
import org.junit.Assert
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
  fun `loadNotes loads notes and preferences successfully`() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertEquals(testMessages, state.notes)
    Assert.assertTrue(state.isCloudStorageEnabled)
    Assert.assertFalse(state.isLoading)
    Assert.assertNull(state.errorMsg)
    job.cancel()
  }

  @Test
  fun `loadNotes handles repository exception`() = runTest {
    every { repository.getNotes(any()) } returns flow { throw Exception("Database error") }
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertEquals("Database error", state.errorMsg)
    job.cancel()
  }

  @Test
  fun `toggleStorageMode(true) calls repository and updates status`() = runTest {
    coEvery { repository.setStorageMode(true) } returns 5
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.toggleStorageMode(true)
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { repository.setStorageMode(true) }
    Assert.assertEquals("Switched to Cloud: Synced 5 notes", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun `toggleStorageMode(true) shows generic message if 0 synced`() = runTest {
    coEvery { repository.setStorageMode(true) } returns 0
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.toggleStorageMode(true)
    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertEquals("Switched to Cloud Storage", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun `toggleStorageMode(false) shows local message`() = runTest {
    coEvery { repository.setStorageMode(false) } returns 0
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.toggleStorageMode(false)
    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertEquals("Switched to Local Storage (Private)", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun `connectivity change to ONLINE triggers sync`() = runTest {
    coEvery { repository.syncPendingNotes() } returns 3
    isOnlineFlow.value = false
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    isOnlineFlow.value = true
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { repository.syncPendingNotes() }
    Assert.assertEquals("Back online: Uploaded 3 notes", viewModel.uiState.value.errorMsg)
    job.cancel()
  }

  @Test
  fun `connectivity change to OFFLINE does nothing`() = runTest {
    isOnlineFlow.value = true
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    testDispatcher.scheduler.advanceUntilIdle()
    io.mockk.clearMocks(repository, answers = false)
    isOnlineFlow.value = false
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { repository.syncPendingNotes() }
  }

  @Test
  fun `sendNote sends message successfully and schedules worker`() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(true)
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Test note")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertEquals("", state.currentMessage)
    Assert.assertFalse(state.isSending)
    Assert.assertNull(state.errorMsg)
    coVerify { repository.createNote(match { it.text == "Test note" }) }
    verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
    job.cancel()
  }

  @Test
  fun `sendNote sends message but does NOT schedule worker IF cloud is disabled`() = runTest {
    every { userPrefs.isCloudStorageEnabled } returns flowOf(false)
    coEvery { repository.createNote(any()) } returns Result.success("new-note-id")
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()
    viewModel.updateMessage("Local note")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { repository.createNote(match { it.text == "Local note" }) }
    verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    job.cancel()
  }

  @Test
  fun `sendNote handles failure`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("DB Error"))
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.updateMessage("Fail note")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.uiState.value
    Assert.assertFalse(state.isSending)
    Assert.assertEquals("Error: DB Error", state.errorMsg)
    verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    job.cancel()
  }

  @Test
  fun `sendNote trims whitespace`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.success("id")
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    viewModel.updateMessage("  Trimmed  ")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify { repository.createNote(match { it.text == "Trimmed" }) }
  }

  @Test
  fun `sendNote ignores empty message`() = runTest {
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    viewModel.updateMessage("   ")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    coVerify(exactly = 0) { repository.createNote(any()) }
  }

  @Test
  fun `clearError clears error message`() = runTest {
    coEvery { repository.createNote(any()) } returns Result.failure(Exception("Error"))
    viewModel = SelfNotesViewModel(repository, userPrefs, workManager, testDispatcher)
    val job = launch { viewModel.uiState.collect {} }
    viewModel.updateMessage("Msg")
    viewModel.sendNote()
    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertNotNull(viewModel.uiState.value.errorMsg)
    viewModel.clearError()
    testDispatcher.scheduler.advanceUntilIdle()
    Assert.assertNull(viewModel.uiState.value.errorMsg)
    job.cancel()
  }
}

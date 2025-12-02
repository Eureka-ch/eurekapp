/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.database.MessageDao
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [UnifiedSelfNotesRepository]. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UnifiedSelfNotesRepositoryTest {

  @MockK private lateinit var context: Context
  @MockK private lateinit var localDao: MessageDao
  @MockK private lateinit var firestoreRepo: FirestoreSelfNotesRepository
  @MockK private lateinit var userPreferences: UserPreferencesRepository
  @MockK private lateinit var auth: FirebaseAuth
  @MockK private lateinit var firebaseUser: FirebaseUser
  @MockK private lateinit var connectivityManager: ConnectivityManager
  @MockK private lateinit var workManager: WorkManager

  private lateinit var repository: UnifiedSelfNotesRepository
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  // To control the preference flow for testing the init block
  private val isCloudEnabledFlow = MutableStateFlow(false)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0
    every { Log.d(any(), any()) } returns 0
    every { Log.w(any(), any(), any()) } returns 0

    // Mock WorkManager singleton
    mockkStatic(WorkManager::class)
    every { WorkManager.getInstance(any()) } returns workManager
    every { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()

    every { auth.currentUser } returns firebaseUser
    every { firebaseUser.uid } returns "testUser"
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { localDao.insertMessage(any()) } returns 1L
    every { userPreferences.isCloudStorageEnabled } returns isCloudEnabledFlow

    // Stub getNotes() to avoid "no answer found" when init block observes cloud
    coEvery { firestoreRepo.getNotes(any()) } returns flowOf(emptyList())

    repository =
        UnifiedSelfNotesRepository(
            context, localDao, firestoreRepo, userPreferences, auth, testScope, testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  private fun mockNetwork(isOnline: Boolean) {
    val network = mockk<Network>()
    val capabilities = mockk<NetworkCapabilities>()
    every { connectivityManager.activeNetwork } returns if (isOnline) network else null
    if (isOnline) {
      every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
      every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
  }

  @Test
  fun `getNotes maps local entities to domain models`() =
      runTest(testDispatcher) {
        val entity =
            MessageEntity(messageId = "m1", text = "Hi", senderId = "u1", createdAtMillis = 1000)
        every { localDao.getMessagesForUser("testUser") } returns flowOf(listOf(entity))
        val result = repository.getNotes(10).first()
        assertEquals(1, result.size)
        assertEquals("m1", result[0].messageID)
        assertEquals("Hi", result[0].text)
      }

  @Test
  fun `createNote fails if user is not logged in`() =
      runTest(testDispatcher) {
        every { auth.currentUser } returns null
        val result = repository.createNote(Message(messageID = "1"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
      }

  @Test
  fun `createNote saves locally as Private when Cloud is DISABLED`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        isCloudEnabledFlow.value = false
        val msg = Message(messageID = "msg1", text = "Local Note")
        val result = repository.createNote(msg)
        assertTrue(result.isSuccess)
        verify {
          localDao.insertMessage(
              withArg { entity ->
                assertEquals("msg1", entity.messageId)
                assertEquals(false, entity.isPendingSync)
                assertEquals(true, entity.isPrivacyLocalOnly)
              })
        }
        coVerify(exactly = 0) { firestoreRepo.createNote(any()) }
      }

  @Test
  fun `createNote saves locally Pending Sync when Cloud is ENABLED but OFFLINE`() =
      runTest(testDispatcher) {
        mockNetwork(false)
        isCloudEnabledFlow.value = true
        val msg = Message(messageID = "msg1", text = "Offline Cloud Note")
        val result = repository.createNote(msg)
        assertTrue(result.isSuccess)
        verify {
          localDao.insertMessage(
              withArg { entity ->
                assertEquals(true, entity.isPendingSync)
                assertEquals(false, entity.isPrivacyLocalOnly)
              })
        }
        coVerify(exactly = 0) { firestoreRepo.createNote(any()) }
        verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
      }

  @Test
  fun `createNote uploads immediately when Cloud is ENABLED and ONLINE`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        isCloudEnabledFlow.value = true
        coEvery { firestoreRepo.createNote(any()) } returns Result.success("msg1")
        every { localDao.markAsSynced(any(), any()) } returns 1
        val msg = Message(messageID = "msg1")
        val result = repository.createNote(msg)
        assertTrue(result.isSuccess)
        verify { localDao.insertMessage(any()) }
        coVerify { firestoreRepo.createNote(any()) }
        verify { localDao.markAsSynced("msg1", "testUser") }
      }

  @Test
  fun `createNote rolls back cloud upload if markAsSynced fails`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        isCloudEnabledFlow.value = true
        coEvery { firestoreRepo.createNote(any()) } returns Result.success("msg1")
        every { localDao.markAsSynced(any(), any()) } throws RuntimeException("DB update failed")
        coEvery { firestoreRepo.deleteNote("msg1") } returns Result.success(Unit)
        val msg = Message(messageID = "msg1")
        val result = repository.createNote(msg)
        assertTrue(result.isFailure)
        verify { localDao.insertMessage(any()) }
        coVerify { firestoreRepo.createNote(any()) }
        verify { localDao.markAsSynced("msg1", "testUser") }
        coVerify { firestoreRepo.deleteNote("msg1") }
      }

  @Test
  fun `updateNote updates local only if note is Private`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = true)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.updateMessageText(any(), any(), any(), any()) } returns Unit

        val result = repository.updateNote("m1", "New")
        assertTrue(result.isSuccess)

        // Should update local DB with isPendingSync = false (since it's local only)
        verify { localDao.updateMessageText("m1", "testUser", "New", false) }
        // Should NOT touch Firestore
        coVerify(exactly = 0) { firestoreRepo.updateNote(any(), any()) }
      }

  @Test
  fun `updateNote updates local and cloud if note is Cloud and Online`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.updateMessageText(any(), any(), any(), any()) } returns Unit
        coEvery { firestoreRepo.updateNote(any(), any()) } returns Result.success(Unit)
        every { localDao.markAsSynced(any(), any()) } returns 1

        val result = repository.updateNote("m1", "New")
        assertTrue(result.isSuccess)

        // Update local (pending)
        verify { localDao.updateMessageText("m1", "testUser", "New", true) }
        // Update cloud
        coVerify { firestoreRepo.updateNote("m1", "New") }
        // Mark synced
        verify { localDao.markAsSynced("m1", "testUser") }
      }

  @Test
  fun `updateNote schedules worker if Cloud note update fails`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.updateMessageText(any(), any(), any(), any()) } returns Unit
        coEvery { firestoreRepo.updateNote(any(), any()) } returns Result.failure(Exception("Fail"))

        val result = repository.updateNote("m1", "New")
        assertTrue(result.isSuccess) // Success because local update worked

        verify { localDao.updateMessageText("m1", "testUser", "New", true) }
        coVerify { firestoreRepo.updateNote("m1", "New") }
        verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
      }

  @Test
  fun `updateNote updates local and schedules worker if OFFLINE`() =
      runTest(testDispatcher) {
        mockNetwork(false)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.updateMessageText(any(), any(), any(), any()) } returns Unit

        val result = repository.updateNote("m1", "New")
        assertTrue(result.isSuccess)

        // Update local (pending)
        verify { localDao.updateMessageText("m1", "testUser", "New", true) }
        // Ensure NO cloud call
        coVerify(exactly = 0) { firestoreRepo.updateNote(any(), any()) }
        // Worker scheduled
        verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
      }

  @Test
  fun `deleteNote performs immediate hard delete if note is Private`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = true)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.deleteMessage(any(), any()) } returns 1

        val result = repository.deleteNote("m1")
        assertTrue(result.isSuccess)

        verify { localDao.deleteMessage("m1", "testUser") }
        coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
      }

  @Test
  fun `deleteNote performs Soft Delete then Cloud Delete if Online`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.markAsDeleted(any(), any()) } returns Unit
        coEvery { firestoreRepo.deleteNote("m1") } returns Result.success(Unit)
        every { localDao.deleteMessage(any(), any()) } returns 1

        val result = repository.deleteNote("m1")
        assertTrue(result.isSuccess)

        // Soft Delete locally
        verify { localDao.markAsDeleted("m1", "testUser") }
        // Cloud Delete
        coVerify { firestoreRepo.deleteNote("m1") }
        // Hard Delete locally (cleanup)
        verify { localDao.deleteMessage("m1", "testUser") }
      }

  @Test
  fun `deleteNote schedules worker if Cloud Delete fails`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.markAsDeleted(any(), any()) } returns Unit
        coEvery { firestoreRepo.deleteNote("m1") } returns Result.failure(Exception("Fail"))

        val result = repository.deleteNote("m1")
        assertTrue(result.isSuccess)

        verify { localDao.markAsDeleted("m1", "testUser") }
        coVerify { firestoreRepo.deleteNote("m1") }
        // NOT hard deleted yet
        verify(exactly = 0) { localDao.deleteMessage("m1", "testUser") }
        // Worker scheduled
        verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
      }

  @Test
  fun `deleteNote performs Soft Delete and schedules worker if OFFLINE`() =
      runTest(testDispatcher) {
        mockNetwork(false)
        val entity =
            MessageEntity(
                messageId = "m1",
                text = "Old",
                senderId = "testUser",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessageById("m1", "testUser") } returns entity
        every { localDao.markAsDeleted(any(), any()) } returns Unit

        val result = repository.deleteNote("m1")
        assertTrue(result.isSuccess)

        verify { localDao.markAsDeleted("m1", "testUser") }
        coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
        verify { workManager.enqueueUniqueWork("SyncNotes", any(), any<OneTimeWorkRequest>()) }
      }

  @Test
  fun `setStorageMode(false) disables cloud, wipes cloud notes, and makes local private`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        coEvery { userPreferences.setCloudStorageEnabled(false) } returns Unit

        // Mock cloud notes fetch
        val cloudMsg = Message(messageID = "cloud1")
        coEvery { firestoreRepo.getNotes() } returns flowOf(listOf(cloudMsg))
        coEvery { firestoreRepo.deleteNote("cloud1") } returns Result.success(Unit)

        // Mock local update
        val localEntity =
            MessageEntity(
                messageId = "m1",
                text = "t",
                senderId = "u",
                createdAtMillis = 0,
                isPrivacyLocalOnly = false)
        every { localDao.getMessagesForUser("testUser") } returns flowOf(listOf(localEntity))
        every { localDao.insertMessages(any()) } returns Unit

        val stats = repository.setStorageMode(false)
        assertEquals(0, stats.total)

        coVerify { userPreferences.setCloudStorageEnabled(false) }
        // Check cloud wipe
        coVerify { firestoreRepo.deleteNote("cloud1") }
        // Check local update to private
        verify {
          localDao.insertMessages(
              withArg { list ->
                assertEquals(true, list[0].isPrivacyLocalOnly)
                assertEquals(false, list[0].isPendingSync)
              })
        }
      }

  @Test
  fun `setStorageMode(true) enables cloud, makes notes public, and syncs if online`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        coEvery { userPreferences.setCloudStorageEnabled(true) } returns Unit
        every { localDao.makeAllMessagesPublicForUser("testUser") } returns 5
        // Sync logic inside setStorageMode calls syncPendingNotes
        every { localDao.getPendingSyncMessages("testUser") } returns emptyList()

        repository.setStorageMode(true)

        coVerify { userPreferences.setCloudStorageEnabled(true) }
        verify { localDao.makeAllMessagesPublicForUser("testUser") }
        // Verifies that sync was attempted
        verify { localDao.getPendingSyncMessages("testUser") }
      }

  @Test
  fun `syncPendingNotes returns empty stats if offline`() =
      runTest(testDispatcher) {
        mockNetwork(false)
        val stats = repository.syncPendingNotes()
        assertEquals(0, stats.total)
        verify(exactly = 0) { localDao.getPendingSyncMessages(any()) }
      }

  @Test
  fun `syncPendingNotes processes Deletes and Upserts correctly`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        val deleteEntity =
            MessageEntity(
                messageId = "del1",
                text = "d",
                senderId = "u",
                createdAtMillis = 0,
                isPendingSync = true,
                isDeleted = true)
        val upsertEntity =
            MessageEntity(
                messageId = "up1",
                text = "u",
                senderId = "u",
                createdAtMillis = 0,
                isPendingSync = true,
                isDeleted = false)

        every { localDao.getPendingSyncMessages("testUser") } returns
            listOf(deleteEntity, upsertEntity)

        // Mock delete success
        coEvery { firestoreRepo.deleteNote("del1") } returns Result.success(Unit)
        every { localDao.deleteMessage("del1", "testUser") } returns 1

        // Mock upsert success
        coEvery { firestoreRepo.createNote(any()) } returns Result.success("up1")
        every { localDao.markAsSynced("up1", "testUser") } returns 1

        val stats = repository.syncPendingNotes()

        assertEquals(1, stats.upserts)
        assertEquals(1, stats.deletes)

        coVerify { firestoreRepo.deleteNote("del1") }
        verify { localDao.deleteMessage("del1", "testUser") }

        coVerify { firestoreRepo.createNote(match { it.messageID == "up1" }) }
        verify { localDao.markAsSynced("up1", "testUser") }
      }

  @Test
  fun `syncPendingNotes handles partial failures`() =
      runTest(testDispatcher) {
        mockNetwork(true)
        // p1: Create Note (Success)
        val p1 =
            MessageEntity(
                messageId = "p1",
                text = "1",
                senderId = "u",
                createdAtMillis = 0,
                isPendingSync = true,
                isDeleted = false)
        // p2: Delete Note (Failure)
        val p2 =
            MessageEntity(
                messageId = "p2",
                text = "2",
                senderId = "u",
                createdAtMillis = 0,
                isPendingSync = true,
                isDeleted = true)

        every { localDao.getPendingSyncMessages("testUser") } returns listOf(p1, p2)

        // p1 setup
        coEvery { firestoreRepo.createNote(match { it.messageID == "p1" }) } returns
            Result.success("p1")
        every { localDao.markAsSynced("p1", "testUser") } returns 1

        // p2 setup
        coEvery { firestoreRepo.deleteNote("p2") } returns Result.failure(Exception("Fail"))

        val stats = repository.syncPendingNotes()

        assertEquals(1, stats.upserts)
        assertEquals(0, stats.deletes)

        verify { localDao.markAsSynced("p1", "testUser") }
        // Ensure p2 was NOT hard deleted locally because cloud delete failed
        verify(exactly = 0) { localDao.deleteMessage("p2", any()) }
      }

  @Test
  fun `observing remote notes saves them locally, ignoring pending edits`() =
      runTest(testDispatcher) {
        mockNetwork(true)

        // Setup pending ID to verify "Local Wins" conflict resolution
        every { localDao.getPendingSyncMessageIds("testUser") } returns listOf("pending_id")
        every { localDao.insertMessages(any()) } returns Unit
        every { localDao.getMessageById(any(), any()) } returns
            null // Assume new notes for simplicity

        // Mock incoming cloud notes
        val cloudNoteNormal = Message(messageID = "normal_id", text = "Cloud Text")
        val cloudNoteConflict = Message(messageID = "pending_id", text = "Conflict Text")

        // Override the setUp stub specifically for this test
        coEvery { firestoreRepo.getNotes(any()) } returns
            flowOf(listOf(cloudNoteNormal, cloudNoteConflict))

        // Trigger the flow (init block is already running)
        isCloudEnabledFlow.value = true

        advanceUntilIdle() // Wait for flow collection

        // Verify insertMessages was called with ONLY the normal note
        verify {
          localDao.insertMessages(
              withArg { list ->
                assertEquals(1, list.size)
                assertEquals("normal_id", list[0].messageId)
                // Ensure conflict note was filtered out
              })
        }
      }
}

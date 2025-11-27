/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [UnifiedSelfNotesRepository].
 *
 * Uses Robolectric to handle Android logging and threading. Uses MockK to mock database, firestore,
 * and connectivity dependencies.
 */
@RunWith(RobolectricTestRunner::class)
class UnifiedSelfNotesRepositoryTest {

  @MockK private lateinit var context: Context
  @MockK private lateinit var localDao: MessageDao
  @MockK private lateinit var firestoreRepo: FirestoreSelfNotesRepository
  @MockK private lateinit var userPreferences: UserPreferencesRepository
  @MockK private lateinit var auth: FirebaseAuth
  @MockK private lateinit var firebaseUser: FirebaseUser
  @MockK private lateinit var connectivityManager: ConnectivityManager
  private lateinit var repository: UnifiedSelfNotesRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0
    every { Log.d(any(), any()) } returns 0
    every { Log.w(any(), any(), any()) } returns 0
    every { auth.currentUser } returns firebaseUser
    every { firebaseUser.uid } returns "testUser"
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    every { localDao.insertMessage(any()) } returns 1L
    repository = UnifiedSelfNotesRepository(context, localDao, firestoreRepo, userPreferences, auth)
  }

  @After
  fun tearDown() {
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
  fun `getNotes maps local entities to domain models`() = runTest {
    val entity =
        MessageEntity(messageId = "m1", text = "Hi", senderId = "u1", createdAtMillis = 1000)
    every { localDao.getMessagesForUser("testUser") } returns flowOf(listOf(entity))
    val result = repository.getNotes(10).first()
    assertEquals(1, result.size)
    assertEquals("m1", result[0].messageID)
    assertEquals("Hi", result[0].text)
  }

  @Test
  fun `createNote fails if user is not logged in`() = runTest {
    every { auth.currentUser } returns null
    val result = repository.createNote(Message(messageID = "1"))
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun `createNote saves locally as Private when Cloud is DISABLED`() = runTest {
    mockNetwork(true)
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(false)
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
  fun `createNote saves locally Pending Sync when Cloud is ENABLED but OFFLINE`() = runTest {
    mockNetwork(false)
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(true)
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
  }

  @Test
  fun `createNote uploads immediately when Cloud is ENABLED and ONLINE`() = runTest {
    mockNetwork(true)
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(true)
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
  fun `createNote rolls back cloud upload if markAsSynced fails`() = runTest {
    mockNetwork(true)
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(true)
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
  fun `createNote handles Firestore failure gracefully (Offline First)`() = runTest {
    mockNetwork(true)
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(true)
    coEvery { firestoreRepo.createNote(any()) } throws RuntimeException("Upload failed")
    val msg = Message(messageID = "msg1")
    val result = repository.createNote(msg)
    assertTrue(result.isFailure)
    verify { localDao.insertMessage(any()) }
    coVerify { firestoreRepo.createNote(any()) }
    verify(exactly = 0) { localDao.markAsSynced(any(), any()) }
  }

  @Test
  fun `createNote returns failure if Local DB fails`() = runTest {
    coEvery { userPreferences.isCloudStorageEnabled } returns flowOf(false)
    every { localDao.insertMessage(any()) } throws RuntimeException("Disk full")
    val result = repository.createNote(Message())
    assertTrue(result.isFailure)
  }

  @Test
  fun `deleteNote succeeds if both local and cloud delete succeed`() = runTest {
    mockNetwork(true)
    every { localDao.deleteMessage(any(), any()) } returns 1
    coEvery { firestoreRepo.deleteNote(any()) } returns Result.success(Unit)
    val result = repository.deleteNote("msg1")
    assertTrue(result.isSuccess)
    verify { localDao.deleteMessage("msg1", "testUser") }
    coVerify { firestoreRepo.deleteNote("msg1") }
  }

  @Test
  fun `deleteNote fails if cloud delete fails`() = runTest {
    mockNetwork(true)
    every { localDao.deleteMessage(any(), any()) } returns 1
    coEvery { firestoreRepo.deleteNote(any()) } returns Result.failure(Exception("Cloud Error"))
    val result = repository.deleteNote("msg1")
    assertTrue(result.isFailure)
    verify { localDao.deleteMessage("msg1", "testUser") }
    coVerify { firestoreRepo.deleteNote("msg1") }
  }

  @Test
  fun `deleteNote deletes only locally if offline`() = runTest {
    mockNetwork(false)
    every { localDao.deleteMessage(any(), any()) } returns 1
    val result = repository.deleteNote("msg1")
    assertTrue(result.isSuccess)
    verify { localDao.deleteMessage("msg1", "testUser") }
    coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
  }

  @Test
  fun `setStorageMode(false) disables cloud preference only`() = runTest {
    coEvery { userPreferences.setCloudStorageEnabled(false) } returns Unit
    val count = repository.setStorageMode(false)
    assertEquals(0, count)
    coVerify { userPreferences.setCloudStorageEnabled(false) }
    verify(exactly = 0) { localDao.makeAllMessagesPublicForUser(any()) }
  }

  @Test
  fun `setStorageMode(true) enables cloud, makes notes public, and syncs if online`() = runTest {
    mockNetwork(true)
    coEvery { userPreferences.setCloudStorageEnabled(true) } returns Unit
    every { localDao.makeAllMessagesPublicForUser("testUser") } returns 5
    every { localDao.getPendingSyncMessages("testUser") } returns emptyList()
    repository.setStorageMode(true)
    coVerify { userPreferences.setCloudStorageEnabled(true) }
    verify { localDao.makeAllMessagesPublicForUser("testUser") }
    verify { localDao.getPendingSyncMessages("testUser") }
  }

  @Test
  fun `syncPendingNotes returns 0 if offline`() = runTest {
    mockNetwork(false)
    val count = repository.syncPendingNotes()
    assertEquals(0, count)
    verify(exactly = 0) { localDao.getPendingSyncMessages(any()) }
  }

  @Test
  fun `syncPendingNotes returns 0 if user not logged in`() = runTest {
    mockNetwork(true)
    every { auth.currentUser } returns null
    val count = repository.syncPendingNotes()
    assertEquals(0, count)
  }

  @Test
  fun `syncPendingNotes uploads pending notes and returns count`() = runTest {
    mockNetwork(true)
    val pendingEntity =
        MessageEntity(
            messageId = "pending1",
            text = "Wait",
            senderId = "testUser",
            createdAtMillis = 0,
            isPendingSync = true)
    every { localDao.getPendingSyncMessages("testUser") } returns listOf(pendingEntity)
    coEvery { firestoreRepo.createNote(any()) } returns Result.success("pending1")
    every { localDao.markAsSynced("pending1", "testUser") } returns 1
    val count = repository.syncPendingNotes()
    assertEquals(1, count)
    coVerify { firestoreRepo.createNote(any()) }
    verify { localDao.markAsSynced("pending1", "testUser") }
  }

  @Test
  fun `syncPendingNotes handles partial failures`() = runTest {
    mockNetwork(true)
    val p1 =
        MessageEntity(
            messageId = "p1", text = "1", senderId = "u", createdAtMillis = 0, isPendingSync = true)
    val p2 =
        MessageEntity(
            messageId = "p2", text = "2", senderId = "u", createdAtMillis = 0, isPendingSync = true)
    every { localDao.getPendingSyncMessages("testUser") } returns listOf(p1, p2)
    coEvery { firestoreRepo.createNote(match { it.messageID == "p1" }) } returns
        Result.success("p1")
    every { localDao.markAsSynced("p1", "testUser") } returns 1
    coEvery { firestoreRepo.createNote(match { it.messageID == "p2" }) } returns
        Result.failure(Exception())
    val count = repository.syncPendingNotes()
    assertEquals(1, count)
    verify { localDao.markAsSynced("p1", "testUser") }
    verify(exactly = 0) { localDao.markAsSynced("p2", any()) }
  }
}

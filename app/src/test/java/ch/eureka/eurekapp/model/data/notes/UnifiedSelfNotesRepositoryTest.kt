/* Portions of this file were generated with the help of Claude (Sonnet 4.5). */
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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

  private lateinit var repository: UnifiedSelfNotesRepository
  private val testDispatcher = StandardTestDispatcher()

  // To control the preference flow
  private val isCloudEnabledFlow = MutableStateFlow(false)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0
    every { Log.d(any(), any()) } returns 0
    every { Log.w(any(), any(), any()) } returns 0

    every { auth.currentUser } returns firebaseUser
    every { firebaseUser.uid } returns "testUser"
    every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

    // Default mocks
    every { localDao.insertMessage(any()) } returns 1L
    every { userPreferences.isCloudStorageEnabled } returns isCloudEnabledFlow

    // Default storage mode is Local
    coEvery { userPreferences.setCloudStorageEnabled(any()) } returns Unit

    repository =
        UnifiedSelfNotesRepository(
            context, localDao, firestoreRepo, userPreferences, auth, testDispatcher)
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

  // --- getNotes Tests ---

  @Test
  fun getNotes_fetchesFromLocalDaoWhenCloudIsDisabled() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = false
        val entity =
            MessageEntity(
                messageId = "m1", text = "Local", senderId = "testUser", createdAtMillis = 1000)
        every { localDao.getMessagesForUser("testUser") } returns flowOf(listOf(entity))

        val result = repository.getNotes(10).first()

        assertEquals(1, result.size)
        assertEquals("m1", result[0].messageID)
        assertEquals("Local", result[0].text)
        // Ensure Firestore was NOT called
        coVerify(exactly = 0) { firestoreRepo.getNotes(any()) }
      }

  @Test
  fun getNotes_fetchesFromFirestoreWhenCloudIsEnabled() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        val msg = Message(messageID = "c1", text = "Cloud")
        coEvery { firestoreRepo.getNotes(any()) } returns flowOf(listOf(msg))

        val result = repository.getNotes(10).first()

        assertEquals(1, result.size)
        assertEquals("c1", result[0].messageID)
        assertEquals("Cloud", result[0].text)
        // Ensure Local DAO was NOT called
        verify(exactly = 0) { localDao.getMessagesForUser(any()) }
      }

  // --- createNote Tests ---

  @Test
  fun createNote_failsIfUserIsNotLoggedIn() =
      runTest(testDispatcher) {
        every { auth.currentUser } returns null
        val result = repository.createNote(Message(messageID = "1"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
      }

  @Test
  fun createNote_savesToLocalDaoWhenCloudIsDisabled() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = false
        val msg = Message(messageID = "msg1", text = "Local Note")

        val result = repository.createNote(msg)

        assertTrue(result.isSuccess)
        verify {
          localDao.insertMessage(
              withArg { entity ->
                assertEquals("msg1", entity.messageId)
                assertEquals("Local Note", entity.text)
                assertEquals("testUser", entity.senderId)
              })
        }
        coVerify(exactly = 0) { firestoreRepo.createNote(any()) }
      }

  @Test
  fun createNote_savesToFirestoreWhenCloudIsEnabledAndOnline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(true)
        val msg = Message(messageID = "msg1", text = "Cloud Note")
        coEvery { firestoreRepo.createNote(any()) } returns Result.success("msg1")

        val result = repository.createNote(msg)

        assertTrue(result.isSuccess)
        coVerify {
          firestoreRepo.createNote(match { it.messageID == "msg1" && it.senderId == "testUser" })
        }
        // Ensure NO local DB interaction
        verify(exactly = 0) { localDao.insertMessage(any()) }
      }

  @Test
  fun createNote_failsWhenCloudIsEnabledAndOffline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(false) // Offline
        val msg = Message(messageID = "msg1")

        val result = repository.createNote(msg)

        assertTrue(result.isFailure)
        assertEquals(
            "You are offline. Switch to Local mode to save notes.",
            result.exceptionOrNull()?.message)

        // Nothing should be saved
        coVerify(exactly = 0) { firestoreRepo.createNote(any()) }
        verify(exactly = 0) { localDao.insertMessage(any()) }
      }

  // --- updateNote Tests ---

  @Test
  fun updateNote_updatesLocalDaoWhenCloudIsDisabled() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = false
        every { localDao.updateMessageText(any(), any(), any()) } returns Unit

        val result = repository.updateNote("m1", "New Text")

        assertTrue(result.isSuccess)
        verify { localDao.updateMessageText("m1", "testUser", "New Text") }
        coVerify(exactly = 0) { firestoreRepo.updateNote(any(), any()) }
      }

  @Test
  fun updateNote_updatesFirestoreWhenCloudIsEnabledAndOnline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(true)
        coEvery { firestoreRepo.updateNote(any(), any()) } returns Result.success(Unit)

        val result = repository.updateNote("m1", "New Text")

        assertTrue(result.isSuccess)
        coVerify { firestoreRepo.updateNote("m1", "New Text") }
        verify(exactly = 0) { localDao.updateMessageText(any(), any(), any()) }
      }

  @Test
  fun updateNote_failsWhenCloudIsEnabledAndOffline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(false)

        val result = repository.updateNote("m1", "New Text")

        assertTrue(result.isFailure)
        assertEquals("Cannot edit cloud notes while offline.", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { firestoreRepo.updateNote(any(), any()) }
        verify(exactly = 0) { localDao.updateMessageText(any(), any(), any()) }
      }

  // --- deleteNote Tests ---

  @Test
  fun deleteNote_deletesFromLocalDaoWhenCloudIsDisabled() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = false
        every { localDao.deleteMessage(any(), any()) } returns 1

        val result = repository.deleteNote("m1")

        assertTrue(result.isSuccess)
        verify { localDao.deleteMessage("m1", "testUser") }
        coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
      }

  @Test
  fun deleteNote_deletesFromFirestoreWhenCloudIsEnabledAndOnline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(true)
        coEvery { firestoreRepo.deleteNote(any()) } returns Result.success(Unit)

        val result = repository.deleteNote("m1")

        assertTrue(result.isSuccess)
        coVerify { firestoreRepo.deleteNote("m1") }
        verify(exactly = 0) { localDao.deleteMessage(any(), any()) }
      }

  @Test
  fun deleteNote_failsWhenCloudIsEnabledAndOffline() =
      runTest(testDispatcher) {
        isCloudEnabledFlow.value = true
        mockNetwork(false)

        val result = repository.deleteNote("m1")

        assertTrue(result.isFailure)
        assertEquals("Cannot delete cloud notes while offline.", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
        verify(exactly = 0) { localDao.deleteMessage(any(), any()) }
      }

  // --- setStorageMode Tests ---

  @Test
  fun setStorageMode_simplyUpdatesUserPreference() =
      runTest(testDispatcher) {
        repository.setStorageMode(true)
        coVerify { userPreferences.setCloudStorageEnabled(true) }

        repository.setStorageMode(false)
        coVerify { userPreferences.setCloudStorageEnabled(false) }

        // Ensure no complex logic is triggered (no sync, no mass delete)
        coVerify(exactly = 0) { firestoreRepo.deleteNote(any()) }
        coVerify(exactly = 0) { firestoreRepo.createNote(any()) }

        // Use coVerify because deleteMessages is a suspend function in the DAO
        coVerify(exactly = 0) { localDao.deleteMessages(any(), any()) }
      }
}

/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.database.AppDatabase
import ch.eureka.eurekapp.model.database.MessageDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.lang.reflect.Field
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [RepositoriesProvider].
 *
 * This suite verifies the Singleton behavior of all provided repositories. It mocks static
 * Android/Firebase dependencies to test the provider logic in isolation. It ensures that:
 * 1. Cloud repositories are lazy-loaded and return the same instance.
 * 2. Local/Hybrid repositories (Room, DataStore, WorkManager) throw exceptions if accessed before
 *    initialization.
 * 3. Local/Hybrid repositories are instantiated correctly after [RepositoriesProvider.initialize]
 *    is called.
 */
class RepositoriesProviderTest {

  @Before
  fun setup() {
    // Mock Android Log to prevent "Method not mocked" exceptions during repository initialization
    // error handling
    mockkStatic(Log::class)
    every { Log.e(any(), any(), any()) } returns 0
    every { Log.d(any(), any()) } returns 0
    every { Log.w(any(), any(), any()) } returns 0

    mockkStatic(FirebaseFirestore::class)
    mockkStatic(FirebaseAuth::class)
    mockkStatic(FirebaseStorage::class)
    mockkStatic(Room::class)
    mockkStatic(WorkManager::class)

    val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockStorage = mockk<FirebaseStorage>(relaxed = true)
    val mockWorkManager = mockk<WorkManager>(relaxed = true)

    val mockRoomBuilder = mockk<RoomDatabase.Builder<AppDatabase>>(relaxed = true)
    val mockDatabase = mockk<AppDatabase>(relaxed = true)
    val mockDao = mockk<MessageDao>(relaxed = true)

    every { mockDatabase.messageDao() } returns mockDao

    // Ensure method chaining works for Room builder
    every { mockRoomBuilder.fallbackToDestructiveMigration(false) } returns mockRoomBuilder
    every { mockRoomBuilder.build() } returns mockDatabase

    every { Room.databaseBuilder(any<Context>(), AppDatabase::class.java, any()) } returns
        mockRoomBuilder

    every { FirebaseFirestore.getInstance() } returns mockFirestore
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { FirebaseStorage.getInstance() } returns mockStorage
    every { WorkManager.getInstance(any()) } returns mockWorkManager

    resetLazyRepositories()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  private fun resetLazyRepositories() {
    val providerClass = RepositoriesProvider::class.java

    val contextField: Field = providerClass.getDeclaredField("applicationContext")
    contextField.isAccessible = true
    contextField.set(RepositoriesProvider, null)

    val dummyLazy = lazy {}
    val valueField = dummyLazy.javaClass.getDeclaredField("_value")
    valueField.isAccessible = true
    val uninitializedValue = valueField.get(dummyLazy)

    val lazyFields =
        listOf(
            "_taskRepository",
            "_projectRepository",
            "_fileRepository",
            "_chatRepository",
            "_invitationRepository",
            "_meetingRepository",
            "_taskTemplateRepository",
            "_userRepository",
            "_speechToTextRepository",
            "_userPreferencesRepository",
            "_unifiedSelfNotesRepository")

    lazyFields.forEach { fieldName ->
      try {
        val field = providerClass.getDeclaredField(fieldName)
        field.isAccessible = true
        val lazyDelegate = field.get(RepositoriesProvider)
        if (lazyDelegate is Lazy<*>) {
          val delegateValueField = lazyDelegate.javaClass.getDeclaredField("_value")
          delegateValueField.isAccessible = true
          delegateValueField.set(lazyDelegate, uninitializedValue)
        }
      } catch (_: Exception) {}
    }
  }

  @Test
  fun taskRepository_shouldNotBeNull() {
    assertNotNull("Task repository should not be null", RepositoriesProvider.taskRepository)
  }

  @Test
  fun projectRepository_shouldNotBeNull() {
    assertNotNull("Project repository should not be null", RepositoriesProvider.projectRepository)
  }

  @Test
  fun fileRepository_shouldNotBeNull() {
    assertNotNull("File repository should not be null", RepositoriesProvider.fileRepository)
  }

  @Test
  fun fileRepository_shouldBeInstanceOfFirebaseFileStorageRepository() {
    assertTrue(
        "File repository should be instance of FirebaseFileStorageRepository",
        RepositoriesProvider.fileRepository is FirebaseFileStorageRepository)
  }

  @Test
  fun chatRepository_shouldNotBeNull() {
    assertNotNull("Chat repository should not be null", RepositoriesProvider.chatRepository)
  }

  @Test
  fun invitationRepository_shouldNotBeNull() {
    assertNotNull(
        "Invitation repository should not be null", RepositoriesProvider.invitationRepository)
  }

  @Test
  fun meetingRepository_shouldNotBeNull() {
    assertNotNull("Meeting repository should not be null", RepositoriesProvider.meetingRepository)
  }

  @Test
  fun taskTemplateRepository_shouldNotBeNull() {
    assertNotNull(
        "Task template repository should not be null", RepositoriesProvider.taskTemplateRepository)
  }

  @Test
  fun userRepository_shouldNotBeNull() {
    assertNotNull("User repository should not be null", RepositoriesProvider.userRepository)
  }

  @Test
  fun repositoryInstances_shouldBeSingleton() {
    val firstTaskRepo = RepositoriesProvider.taskRepository
    val secondTaskRepo = RepositoriesProvider.taskRepository
    assertSame(
        "Multiple accesses should return same task repository instance",
        firstTaskRepo,
        secondTaskRepo)

    val firstProjectRepo = RepositoriesProvider.projectRepository
    val secondProjectRepo = RepositoriesProvider.projectRepository
    assertSame(
        "Multiple accesses should return same project repository instance",
        firstProjectRepo,
        secondProjectRepo)

    val firstFileRepo = RepositoriesProvider.fileRepository
    val secondFileRepo = RepositoriesProvider.fileRepository
    assertSame(
        "Multiple accesses should return same file repository instance",
        firstFileRepo,
        secondFileRepo)

    val firstChatRepo = RepositoriesProvider.chatRepository
    val secondChatRepo = RepositoriesProvider.chatRepository
    assertSame(
        "Multiple accesses should return same chat repository instance",
        firstChatRepo,
        secondChatRepo)

    val firstInvitationRepo = RepositoriesProvider.invitationRepository
    val secondInvitationRepo = RepositoriesProvider.invitationRepository
    assertSame(
        "Multiple accesses should return same invitation repository instance",
        firstInvitationRepo,
        secondInvitationRepo)

    val firstMeetingRepo = RepositoriesProvider.meetingRepository
    val secondMeetingRepo = RepositoriesProvider.meetingRepository
    assertSame(
        "Multiple accesses should return same meeting repository instance",
        firstMeetingRepo,
        secondMeetingRepo)

    val firstTemplateRepo = RepositoriesProvider.taskTemplateRepository
    val secondTemplateRepo = RepositoriesProvider.taskTemplateRepository
    assertSame(
        "Multiple accesses should return same task template repository instance",
        firstTemplateRepo,
        secondTemplateRepo)

    val firstUserRepo = RepositoriesProvider.userRepository
    val secondUserRepo = RepositoriesProvider.userRepository
    assertSame(
        "Multiple accesses should return same user repository instance",
        firstUserRepo,
        secondUserRepo)
  }

  @Test
  fun unifiedSelfNotesRepository_shouldBeInstanceOfUnifiedSelfNotesRepository_whenInitialized() {
    val mockContext = mockk<Context>(relaxed = true)
    RepositoriesProvider.initialize(mockContext)

    val repo = RepositoriesProvider.unifiedSelfNotesRepository

    assertNotNull(repo)
  }

  @Test
  fun userPreferencesRepository_shouldBeInstanceOfUserPreferencesRepository_whenInitialized() {
    val mockContext = mockk<Context>(relaxed = true)
    RepositoriesProvider.initialize(mockContext)

    val repo = RepositoriesProvider.userPreferencesRepository

    assertNotNull(repo)
  }

  @Test
  fun workManager_shouldThrowIfNotInitialized() {
    assertThrows(IllegalStateException::class.java) { RepositoriesProvider.workManager }
  }

  @Test
  fun workManager_shouldNotBeNull_whenInitialized() {
    val mockContext = mockk<Context>(relaxed = true)
    RepositoriesProvider.initialize(mockContext)

    val wm = RepositoriesProvider.workManager

    assertNotNull(wm)
  }
}

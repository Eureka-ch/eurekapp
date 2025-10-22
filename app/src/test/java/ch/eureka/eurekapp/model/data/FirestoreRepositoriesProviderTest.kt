package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.data.chat.FirestoreChatRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.invitation.FirestoreInvitationRepository
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.template.FirestoreTaskTemplateRepository
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirestoreRepositoriesProviderTest {

  @Before
  fun setup() {
    mockkStatic(FirebaseFirestore::class)
    mockkStatic(FirebaseAuth::class)
    mockkStatic(FirebaseStorage::class)

    val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    val mockStorage = mockk<FirebaseStorage>(relaxed = true)

    every { FirebaseFirestore.getInstance() } returns mockFirestore
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { FirebaseStorage.getInstance() } returns mockStorage

    resetLazyRepositories()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  private fun resetLazyRepositories() {
    val providerClass = FirestoreRepositoriesProvider::class.java
    val lazyFields =
        listOf(
            "_taskRepository",
            "_projectRepository",
            "_fileRepository",
            "_chatRepository",
            "_invitationRepository",
            "_meetingRepository",
            "_taskTemplateRepository",
            "_userRepository")

    lazyFields.forEach { fieldName ->
      try {
        val field = providerClass.getDeclaredField(fieldName)
        field.isAccessible = true
        val lazyDelegate = field.get(FirestoreRepositoriesProvider)
        if (lazyDelegate is Lazy<*>) {
          val valueField = lazyDelegate.javaClass.getDeclaredField("_value")
          valueField.isAccessible = true
          valueField.set(lazyDelegate, null)
        }
      } catch (e: Exception) {
        // Field might not be lazy or might not exist
      }
    }
  }

  @Test
  fun taskRepository_shouldNotBeNull() {
    assertNotNull(
        "Task repository should not be null", FirestoreRepositoriesProvider.taskRepository)
  }

  @Test
  fun taskRepository_shouldBeInstanceOfFirestoreTaskRepository() {
    assertTrue(
        "Task repository should be instance of FirestoreTaskRepository",
        FirestoreRepositoriesProvider.taskRepository is FirestoreTaskRepository)
  }

  @Test
  fun projectRepository_shouldNotBeNull() {
    assertNotNull(
        "Project repository should not be null", FirestoreRepositoriesProvider.projectRepository)
  }

  @Test
  fun projectRepository_shouldBeInstanceOfFirestoreProjectRepository() {
    assertTrue(
        "Project repository should be instance of FirestoreProjectRepository",
        FirestoreRepositoriesProvider.projectRepository is FirestoreProjectRepository)
  }

  @Test
  fun fileRepository_shouldNotBeNull() {
    assertNotNull(
        "File repository should not be null", FirestoreRepositoriesProvider.fileRepository)
  }

  @Test
  fun fileRepository_shouldBeInstanceOfFirebaseFileStorageRepository() {
    assertTrue(
        "File repository should be instance of FirebaseFileStorageRepository",
        FirestoreRepositoriesProvider.fileRepository is FirebaseFileStorageRepository)
  }

  @Test
  fun chatRepository_shouldNotBeNull() {
    assertNotNull(
        "Chat repository should not be null", FirestoreRepositoriesProvider.chatRepository)
  }

  @Test
  fun chatRepository_shouldBeInstanceOfFirestoreChatRepository() {
    assertTrue(
        "Chat repository should be instance of FirestoreChatRepository",
        FirestoreRepositoriesProvider.chatRepository is FirestoreChatRepository)
  }

  @Test
  fun invitationRepository_shouldNotBeNull() {
    assertNotNull(
        "Invitation repository should not be null",
        FirestoreRepositoriesProvider.invitationRepository)
  }

  @Test
  fun invitationRepository_shouldBeInstanceOfFirestoreInvitationRepository() {
    assertTrue(
        "Invitation repository should be instance of FirestoreInvitationRepository",
        FirestoreRepositoriesProvider.invitationRepository is FirestoreInvitationRepository)
  }

  @Test
  fun meetingRepository_shouldNotBeNull() {
    assertNotNull(
        "Meeting repository should not be null", FirestoreRepositoriesProvider.meetingRepository)
  }

  @Test
  fun meetingRepository_shouldBeInstanceOfFirestoreMeetingRepository() {
    assertTrue(
        "Meeting repository should be instance of FirestoreMeetingRepository",
        FirestoreRepositoriesProvider.meetingRepository is FirestoreMeetingRepository)
  }

  @Test
  fun taskTemplateRepository_shouldNotBeNull() {
    assertNotNull(
        "Task template repository should not be null",
        FirestoreRepositoriesProvider.taskTemplateRepository)
  }

  @Test
  fun taskTemplateRepository_shouldBeInstanceOfFirestoreTaskTemplateRepository() {
    assertTrue(
        "Task template repository should be instance of FirestoreTaskTemplateRepository",
        FirestoreRepositoriesProvider.taskTemplateRepository is FirestoreTaskTemplateRepository)
  }

  @Test
  fun userRepository_shouldNotBeNull() {
    assertNotNull(
        "User repository should not be null", FirestoreRepositoriesProvider.userRepository)
  }

  @Test
  fun userRepository_shouldBeInstanceOfFirestoreUserRepository() {
    assertTrue(
        "User repository should be instance of FirestoreUserRepository",
        FirestoreRepositoriesProvider.userRepository is FirestoreUserRepository)
  }

  @Test
  fun repositoryInstances_shouldBeSingleton() {
    val firstTaskRepo = FirestoreRepositoriesProvider.taskRepository
    val secondTaskRepo = FirestoreRepositoriesProvider.taskRepository
    assertSame(
        "Multiple accesses should return same task repository instance",
        firstTaskRepo,
        secondTaskRepo)

    val firstProjectRepo = FirestoreRepositoriesProvider.projectRepository
    val secondProjectRepo = FirestoreRepositoriesProvider.projectRepository
    assertSame(
        "Multiple accesses should return same project repository instance",
        firstProjectRepo,
        secondProjectRepo)

    val firstFileRepo = FirestoreRepositoriesProvider.fileRepository
    val secondFileRepo = FirestoreRepositoriesProvider.fileRepository
    assertSame(
        "Multiple accesses should return same file repository instance",
        firstFileRepo,
        secondFileRepo)

    val firstChatRepo = FirestoreRepositoriesProvider.chatRepository
    val secondChatRepo = FirestoreRepositoriesProvider.chatRepository
    assertSame(
        "Multiple accesses should return same chat repository instance",
        firstChatRepo,
        secondChatRepo)

    val firstInvitationRepo = FirestoreRepositoriesProvider.invitationRepository
    val secondInvitationRepo = FirestoreRepositoriesProvider.invitationRepository
    assertSame(
        "Multiple accesses should return same invitation repository instance",
        firstInvitationRepo,
        secondInvitationRepo)

    val firstMeetingRepo = FirestoreRepositoriesProvider.meetingRepository
    val secondMeetingRepo = FirestoreRepositoriesProvider.meetingRepository
    assertSame(
        "Multiple accesses should return same meeting repository instance",
        firstMeetingRepo,
        secondMeetingRepo)

    val firstTemplateRepo = FirestoreRepositoriesProvider.taskTemplateRepository
    val secondTemplateRepo = FirestoreRepositoriesProvider.taskTemplateRepository
    assertSame(
        "Multiple accesses should return same task template repository instance",
        firstTemplateRepo,
        secondTemplateRepo)

    val firstUserRepo = FirestoreRepositoriesProvider.userRepository
    val secondUserRepo = FirestoreRepositoriesProvider.userRepository
    assertSame(
        "Multiple accesses should return same user repository instance",
        firstUserRepo,
        secondUserRepo)
  }
}

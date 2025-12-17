package ch.eureka.eurekapp.model.data.user

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

/**
 * Test suite for UserRepository implementation.
 *
 * Note: Some of these tests were co-authored by Claude Code, and Grok.
 */
class UserRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: FirestoreUserRepository

  override fun getCollectionPaths(): List<String> {
    return listOf("users")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreUserRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun userRepository_shouldSaveUserToFirestore() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            lastActive = Timestamp.now())

    val result = repository.saveUser(user)

    assertTrue(result.isSuccess)

    val savedUser =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .get()
            .await()
            .toObject(User::class.java)

    assertNotNull(savedUser)
    assertEquals(user.uid, savedUser?.uid)
    assertEquals(user.displayName, savedUser?.displayName)
    assertEquals(user.email, savedUser?.email)
    assertEquals(user.photoUrl, savedUser?.photoUrl)
  }

  @Test
  fun userRepository_shouldReturnUserWhenExists() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "John Doe",
            email = "john@example.com",
            photoUrl = "https://example.com/john.jpg",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    val flow = repository.getUserById(testUserId)
    val retrievedUser = flow.first()

    assertNotNull(retrievedUser)
    assertEquals(user.uid, retrievedUser?.uid)
    assertEquals(user.displayName, retrievedUser?.displayName)
    assertEquals(user.email, retrievedUser?.email)
    assertEquals(user.photoUrl, retrievedUser?.photoUrl)
  }

  @Test
  fun userRepository_shouldReturnNullWhenUserDoesNotExist() = runBlocking {
    val flow = repository.getUserById("non_existent_user")
    val retrievedUser = flow.first()

    assertNull(retrievedUser)
  }

  @Test
  fun userRepository_shouldReturnCurrentUserWhenSignedIn() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Current User",
            email = "current@example.com",
            photoUrl = "https://example.com/current.jpg",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    val flow = repository.getCurrentUser()
    val currentUser = flow.first()

    assertNotNull(currentUser)
    assertEquals(testUserId, currentUser?.uid)
    assertEquals(user.displayName, currentUser?.displayName)
    assertEquals(user.email, currentUser?.email)
  }

  @Test
  fun userRepository_shouldUpdateTimestamp() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            lastActive = Timestamp(0, 0))
    repository.saveUser(user)

    // Get the initial lastActive timestamp
    val initialUser =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .get()
            .await()
            .toObject(User::class.java)
    assertNotNull(initialUser)
    assertEquals(0L, initialUser?.lastActive?.seconds)

    // Update lastActive
    val result = repository.updateLastActive(testUserId)
    assertTrue(result.isSuccess)

    // Verify the timestamp was updated
    val updatedUser =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .get()
            .await()
            .toObject(User::class.java)
    assertNotNull(updatedUser)
    assertTrue(updatedUser!!.lastActive.seconds > 0)
  }

  @Test
  fun userRepository_shouldUpdateExistingUser() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Original Name",
            email = "original@example.com",
            photoUrl = "https://example.com/original.jpg",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    val updatedUser = user.copy(displayName = "Updated Name", email = "updated@example.com")
    val result = repository.saveUser(updatedUser)

    assertTrue(result.isSuccess)

    val savedUser =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .get()
            .await()
            .toObject(User::class.java)

    assertNotNull(savedUser)
    assertEquals("Updated Name", savedUser?.displayName)
    assertEquals("updated@example.com", savedUser?.email)
  }

  @Test
  fun userRepository_shouldReactToChangesByUserId() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Initial Name",
            email = "initial@example.com",
            photoUrl = "https://example.com/initial.jpg",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    val flow = repository.getUserById(testUserId)
    val initialUser = flow.first()

    assertNotNull(initialUser)
    assertEquals("Initial Name", initialUser?.displayName)

    // Update the user
    val updatedUser = user.copy(displayName = "Changed Name")
    repository.saveUser(updatedUser)

    // The flow should emit the updated user
    val changedUser = flow.first()
    assertNotNull(changedUser)
    assertEquals("Changed Name", changedUser?.displayName)
  }

  @Test
  fun userRepository_shouldReactToChangesForCurrentUser() = runBlocking {
    val user =
        User(
            uid = testUserId,
            displayName = "Initial Name",
            email = "initial@example.com",
            photoUrl = "https://example.com/initial.jpg",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    val flow = repository.getCurrentUser()
    val initialUser = flow.first()

    assertNotNull(initialUser)
    assertEquals("Initial Name", initialUser?.displayName)

    // Update the current user
    val updatedUser = user.copy(displayName = "Changed Name")
    repository.saveUser(updatedUser)

    // The flow should emit the updated user
    val changedUser = flow.first()
    assertNotNull(changedUser)
    assertEquals("Changed Name", changedUser?.displayName)
  }
}

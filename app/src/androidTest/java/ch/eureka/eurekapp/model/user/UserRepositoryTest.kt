package ch.eureka.eurekapp.model.user

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

class UserRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: UserRepository

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
  fun saveUser_shouldSaveUserToFirestore() = runBlocking {
    // Given
    val user =
        User(
            uid = testUserId,
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            lastActive = Timestamp.now())

    // When
    val result = repository.saveUser(user)

    // Then
    assertTrue(result.isSuccess)

    // Verify user was saved
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
  }

  @Test
  fun getUserById_shouldReturnUserWhenExists() = runBlocking {
    // Given
    val user =
        User(
            uid = testUserId,
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    // When
    val flow = repository.getUserById(testUserId)
    val retrievedUser = flow.first()

    // Then
    assertNotNull(retrievedUser)
    assertEquals(user.uid, retrievedUser?.uid)
    assertEquals(user.displayName, retrievedUser?.displayName)
  }

  @Test
  fun getUserById_shouldReturnNullWhenUserDoesNotExist() = runBlocking {
    // Given
    val nonExistentUserId = "non_existent_user_id"

    // When
    val flow = repository.getUserById(nonExistentUserId)
    val retrievedUser = flow.first()

    // Then
    assertNull(retrievedUser)
  }

  @Test
  fun getCurrentUser_shouldReturnCurrentAuthenticatedUser() = runBlocking {
    // Given
    val user =
        User(
            uid = testUserId,
            displayName = "Current User",
            email = "current@example.com",
            photoUrl = "",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    // When
    val flow = repository.getCurrentUser()
    val currentUser = flow.first()

    // Then
    assertNotNull(currentUser)
    assertEquals(testUserId, currentUser?.uid)
    assertEquals(user.displayName, currentUser?.displayName)
  }

  @Test
  fun getCurrentUser_shouldReturnNullWhenNotAuthenticated() = runBlocking {
    // Given
    FirebaseEmulator.auth.signOut()

    // When
    val flow = repository.getCurrentUser()
    val currentUser = flow.first()

    // Then
    assertNull(currentUser)
  }

  @Test
  fun updateLastActive_shouldUpdateTimestamp() = runBlocking {
    // Given
    val user =
        User(
            uid = testUserId,
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = "",
            lastActive = Timestamp.now())
    repository.saveUser(user)

    // Wait a bit to ensure timestamp difference
    Thread.sleep(100)

    // When
    val result = repository.updateLastActive(testUserId)

    // Then
    assertTrue(result.isSuccess)

    // Verify timestamp was updated
    val updatedUser =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .get()
            .await()
            .toObject(User::class.java)

    assertNotNull(updatedUser)
    assertNotNull(updatedUser?.lastActive)
  }
}

package ch.eureka.eurekapp.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before

/**
 * Base class for Firestore repository tests that provides common setup and cleanup functionality.
 *
 * Subclasses should:
 * 1. Override [getCollectionPaths] to specify which collections to clear before each test
 * 2. Call [super.setup()] in their @Before method if they override it
 */
abstract class FirestoreRepositoryTest {

  protected lateinit var testUserId: String

  /**
   * Returns the collection paths that should be cleared before each test.
   *
   * Examples:
   * - ["workspaces"] - clears all workspaces
   * - ["workspaces/ws1/groups"] - clears groups in workspace ws1
   * - ["users", "workspaces"] - clears both users and workspaces
   */
  protected abstract fun getCollectionPaths(): List<String>

  @Before
  open fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    // Clear test data before each test
    clearTestData()

    // Sign in anonymously to get a test user ID
    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")
  }

  private suspend fun clearTestData() {
    val collectionPaths = getCollectionPaths()

    for (path in collectionPaths) {
      val pathParts = path.split("/")
      var collectionRef = FirebaseEmulator.firestore.collection(pathParts[0])

      // Navigate through the path (collection/doc/collection/doc/...)
      for (i in 1 until pathParts.size) {
        if (i % 2 == 1) {
          // Odd index = document
          collectionRef = collectionRef.document(pathParts[i]).collection(pathParts[i + 1])
        }
      }

      // Get all documents and delete them in a batch
      val documents = collectionRef.get().await()
      if (documents.isEmpty) continue

      val batch = FirebaseEmulator.firestore.batch()
      documents.documents.forEach { batch.delete(it.reference) }
      batch.commit().await()
    }
  }
}

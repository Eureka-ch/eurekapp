package ch.eureka.eurekapp.utils

/*
 * This test file takes inspiration from the tests provided for the EPFL swent course
 * bootcamp.
 * */

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before

abstract class FirestoreRepositoryTest {

  protected lateinit var testUserId: String

  protected abstract fun getCollectionPaths(): List<String>

  @Before
  open fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    // Clear first before signing in
    FirebaseEmulator.clearFirestoreEmulator()
    // Sign in anonymously first to ensure auth is established before clearing data
    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

    // Verify auth state is properly set
    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }
  }

  @After
  open fun tearDown() = runBlocking {
    // Give time for any pending Firestore operations to complete

    // Clear server data
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  protected suspend fun setupTestProject(projectId: String, role: String = "owner") {
    // Create project and member sequentially (security rules require project to exist first)
    // Note: Data is already cleared in setup() via clearFirestoreEmulator()
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    // First create the project document with createdBy field
    val project =
        mapOf(
            "projectId" to projectId,
            "name" to "Test Project",
            "description" to "Test project for integration tests",
            "status" to "open",
            "createdBy" to testUserId)
    projectRef.set(project).await()

    // Then add the test user as a member
    val member = mapOf("userId" to testUserId, "role" to role)
    val memberRef = projectRef.collection("members").document(testUserId)
    memberRef.set(member).await()
  }
}

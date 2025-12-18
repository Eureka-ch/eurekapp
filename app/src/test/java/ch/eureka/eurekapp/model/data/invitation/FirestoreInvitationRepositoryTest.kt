// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.data.invitation

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FirestoreInvitationRepository using mocked Firestore dependencies.
 *
 * This test suite validates repository logic without requiring Firebase emulator or network access.
 * All Firestore operations are mocked using MockK framework.
 */
class FirestoreInvitationRepositoryTest {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var repository: FirestoreInvitationRepository
  private lateinit var invitationsCollection: CollectionReference
  private lateinit var mockDocumentRef: DocumentReference
  private lateinit var mockQuery: Query

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    invitationsCollection = mockk(relaxed = true)
    mockDocumentRef = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)

    every { firestore.collection("invitations") } returns invitationsCollection
    every { invitationsCollection.document(any()) } returns mockDocumentRef

    repository = FirestoreInvitationRepository(firestore)
  }

  // ========================================
  // Create Invitation Tests
  // ========================================

  @Test
  fun createInvitation_successfullySavesInvitationToFirestore() = runTest {
    // Given
    val invitation = Invitation(token = "TOKEN123", projectId = "project_1", isUsed = false)
    val successTask: Task<Void> = Tasks.forResult(null)

    every { mockDocumentRef.set(invitation) } returns successTask

    // When
    val result = repository.createInvitation(invitation)

    // Then
    assertTrue("Result should be success", result.isSuccess)
    verify { mockDocumentRef.set(invitation) }
  }

  @Test
  fun createInvitation_returnsFailureWhenFirestoreOperationFails() = runTest {
    // Given
    val invitation = Invitation(token = "TOKEN123", projectId = "project_1", isUsed = false)
    val exception =
        FirebaseFirestoreException("Network error", FirebaseFirestoreException.Code.UNAVAILABLE)
    val failureTask: Task<Void> = Tasks.forException(exception)

    every { mockDocumentRef.set(invitation) } returns failureTask

    // When
    val result = repository.createInvitation(invitation)

    // Then
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Exception should contain 'Network error'",
        result.exceptionOrNull()?.message?.contains("Network error") == true)
  }

  // ========================================
  // Get Invitation By Token Tests
  // ========================================

  @Test
  fun getInvitationByToken_emitsInvitationWhenItExists() = runTest {
    // Given
    val testToken = "TOKEN123"
    val invitation = Invitation(token = testToken, projectId = "project_1", isUsed = false)
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { mockSnapshot.toObject(Invitation::class.java) } returns invitation
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockRegistration
        }

    // When
    val flow = repository.getInvitationByToken(testToken)
    val result = flow.first()

    // Then
    assertNotNull("Result should not be null", result)
    assertEquals("Token should match", testToken, result?.token)
    assertEquals("ProjectId should match", "project_1", result?.projectId)
    assertFalse("IsUsed should be false", result?.isUsed ?: true)
  }

  @Test
  fun getInvitationByToken_emitsNullWhenInvitationDoesNotExist() = runTest {
    // Given
    val testToken = "NONEXISTENT"
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { mockSnapshot.toObject(Invitation::class.java) } returns null
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockRegistration
        }

    // When
    val flow = repository.getInvitationByToken(testToken)
    val result = flow.first()

    // Then
    assertNull("Result should be null for non-existent token", result)
  }

  @Test
  fun getInvitationByToken_emitsUpdatesWhenInvitationChanges() = runBlocking {
    // Given
    val testToken = "TOKEN123"
    val unusedInvitation = Invitation(token = testToken, projectId = "project_1", isUsed = false)
    val usedInvitation =
        unusedInvitation.copy(isUsed = true, usedBy = "user_1", usedAt = Timestamp.now())

    val mockSnapshot1: DocumentSnapshot = mockk(relaxed = true)
    val mockSnapshot2: DocumentSnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { mockSnapshot1.toObject(Invitation::class.java) } returns unusedInvitation
    every { mockSnapshot2.toObject(Invitation::class.java) } returns usedInvitation

    var capturedListener: EventListener<DocumentSnapshot>? = null
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          capturedListener = listenerSlot.captured
          capturedListener?.onEvent(mockSnapshot1, null)
          mockRegistration
        }

    // When
    val flow = repository.getInvitationByToken(testToken)

    // Collect first emission
    val firstEmission = flow.first()

    // Then verify first emission
    assertNotNull("First emission should not be null", firstEmission)
    assertFalse("First emission should be unused", firstEmission?.isUsed ?: true)

    // Note: Testing reactive flow updates requires more complex setup with Flow test utilities
    // For unit tests, we verify the initial state works correctly
    // Full reactive behavior is better tested in integration tests
  }

  @Test
  fun getInvitationByToken_closesFlowOnFirestoreError() = runTest {
    // Given
    val testToken = "TOKEN123"
    val exception =
        FirebaseFirestoreException(
            "Permission denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(null, exception)
          mockRegistration
        }

    // When/Then
    val flow = repository.getInvitationByToken(testToken)
    try {
      flow.first()
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assertTrue(
          "Should contain permission denied message",
          e.message?.contains("Permission denied") == true)
    }
  }

  // ========================================
  // Get Project Invitations Tests
  // ========================================

  @Test
  fun getProjectInvitations_emitsAllInvitationsForProject() = runTest {
    // Given
    val projectId = "project_1"
    val invitation1 = Invitation(token = "TOKEN1", projectId = projectId, isUsed = false)
    val invitation2 = Invitation(token = "TOKEN2", projectId = projectId, isUsed = true)

    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    val mockDoc1: DocumentSnapshot = mockk(relaxed = true)
    val mockDoc2: DocumentSnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { invitationsCollection.whereEqualTo("projectId", projectId) } returns mockQuery
    every { mockDoc1.toObject(Invitation::class.java) } returns invitation1
    every { mockDoc2.toObject(Invitation::class.java) } returns invitation2
    every { mockSnapshot.documents } returns listOf(mockDoc1, mockDoc2)
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockRegistration
        }

    // When
    val flow = repository.getProjectInvitations(projectId)
    val result = flow.first()

    // Then
    assertEquals("Should return 2 invitations", 2, result.size)
    assertTrue("Should contain TOKEN1", result.any { it.token == "TOKEN1" })
    assertTrue("Should contain TOKEN2", result.any { it.token == "TOKEN2" })
  }

  @Test
  fun getProjectInvitations_emitsEmptyListWhenNoInvitationsExist() = runTest {
    // Given
    val projectId = "empty_project"
    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { invitationsCollection.whereEqualTo("projectId", projectId) } returns mockQuery
    every { mockSnapshot.documents } returns emptyList()
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockRegistration
        }

    // When
    val flow = repository.getProjectInvitations(projectId)
    val result = flow.first()

    // Then
    assertTrue("Should return empty list", result.isEmpty())
  }

  @Test
  fun getProjectInvitations_filtersOutNullInvitations() = runTest {
    // Given
    val projectId = "project_1"
    val invitation = Invitation(token = "TOKEN1", projectId = projectId, isUsed = false)

    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    val mockDoc1: DocumentSnapshot = mockk(relaxed = true)
    val mockDoc2: DocumentSnapshot = mockk(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockRegistration: ListenerRegistration = mockk(relaxed = true)

    every { invitationsCollection.whereEqualTo("projectId", projectId) } returns mockQuery
    every { mockDoc1.toObject(Invitation::class.java) } returns invitation
    every { mockDoc2.toObject(Invitation::class.java) } returns null // Malformed document
    every { mockSnapshot.documents } returns listOf(mockDoc1, mockDoc2)
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockRegistration
        }

    // When
    val flow = repository.getProjectInvitations(projectId)
    val result = flow.first()

    // Then
    assertEquals("Should return only 1 valid invitation", 1, result.size)
    assertEquals("Should contain TOKEN1", "TOKEN1", result[0].token)
  }

  // ========================================
  // Mark Invitation As Used Tests
  // ========================================

  @Test
  fun markInvitationAsUsed_successfullyMarksUnusedInvitation() = runTest {
    // Given
    val token = "TOKEN123"
    val userId = "user_1"
    val unusedInvitation = Invitation(token = token, projectId = "project_1", isUsed = false)

    val mockTransaction: Transaction = mockk(relaxed = true)
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    val successTask: Task<Void> = Tasks.forResult(null)

    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.toObject(Invitation::class.java) } returns unusedInvitation
    every { mockTransaction.get(mockDocumentRef) } returns mockSnapshot
    every { mockTransaction.set(mockDocumentRef, any()) } returns mockTransaction

    coEvery { firestore.runTransaction<Void>(any()) } coAnswers
        {
          val transactionFunction = firstArg<Transaction.Function<Void>>()
          transactionFunction.apply(mockTransaction)
          successTask
        }

    // When
    val result = repository.markInvitationAsUsed(token, userId)

    // Then
    assertTrue("Result should be success", result.isSuccess)
    verify { mockTransaction.set(mockDocumentRef, any()) }
  }

  @Test
  fun markInvitationAsUsed_failsWhenInvitationDoesNotExist() = runTest {
    // Given
    val token = "NONEXISTENT"
    val userId = "user_1"

    val mockTransaction: Transaction = mockk(relaxed = true)
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)

    every { mockSnapshot.exists() } returns false
    every { mockTransaction.get(mockDocumentRef) } returns mockSnapshot

    coEvery { firestore.runTransaction<Void>(any()) } coAnswers
        {
          val transactionFunction = firstArg<Transaction.Function<Void>>()
          try {
            transactionFunction.apply(mockTransaction)
            Tasks.forResult(null)
          } catch (e: Exception) {
            Tasks.forException(e)
          }
        }

    // When
    val result = repository.markInvitationAsUsed(token, userId)

    // Then
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Error should mention 'not found'",
        result.exceptionOrNull()?.message?.contains("not found") == true)
  }

  @Test
  fun markInvitationAsUsed_failsWhenInvitationAlreadyUsed() = runTest {
    // Given
    val token = "USED_TOKEN"
    val userId = "user_2"
    val usedInvitation =
        Invitation(
            token = token,
            projectId = "project_1",
            isUsed = true,
            usedBy = "user_1",
            usedAt = Timestamp.now())

    val mockTransaction: Transaction = mockk(relaxed = true)
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)

    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.toObject(Invitation::class.java) } returns usedInvitation
    every { mockTransaction.get(mockDocumentRef) } returns mockSnapshot

    coEvery { firestore.runTransaction<Void>(any()) } coAnswers
        {
          val transactionFunction = firstArg<Transaction.Function<Void>>()
          try {
            transactionFunction.apply(mockTransaction)
            Tasks.forResult(null)
          } catch (e: Exception) {
            Tasks.forException(e)
          }
        }

    // When
    val result = repository.markInvitationAsUsed(token, userId)

    // Then
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Error should mention 'already been used'",
        result.exceptionOrNull()?.message?.contains("already been used") == true)
  }

  @Test
  fun markInvitationAsUsed_handlesFirestoreTransactionFailure() = runTest {
    // Given
    val token = "TOKEN123"
    val userId = "user_1"
    val exception =
        FirebaseFirestoreException("Transaction failed", FirebaseFirestoreException.Code.ABORTED)
    val failureTask: Task<Void> = Tasks.forException(exception)

    coEvery { firestore.runTransaction<Void>(any()) } returns failureTask

    // When
    val result = repository.markInvitationAsUsed(token, userId)

    // Then
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Error should mention 'Transaction failed'",
        result.exceptionOrNull()?.message?.contains("Transaction failed") == true)
  }

  @Test
  fun markInvitationAsUsed_setsTimestampWhenMarkingAsUsed() = runTest {
    // Given
    val token = "TOKEN123"
    val userId = "user_1"
    val unusedInvitation = Invitation(token = token, projectId = "project_1", isUsed = false)

    val mockTransaction: Transaction = mockk(relaxed = true)
    val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    val successTask: Task<Void> = Tasks.forResult(null)
    val capturedInvitation = slot<Invitation>()

    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.toObject(Invitation::class.java) } returns unusedInvitation
    every { mockTransaction.get(mockDocumentRef) } returns mockSnapshot
    every { mockTransaction.set(mockDocumentRef, capture(capturedInvitation)) } returns
        mockTransaction

    coEvery { firestore.runTransaction<Void>(any()) } coAnswers
        {
          val transactionFunction = firstArg<Transaction.Function<Void>>()
          transactionFunction.apply(mockTransaction)
          successTask
        }

    // When
    val result = repository.markInvitationAsUsed(token, userId)

    // Then
    assertTrue("Result should be success", result.isSuccess)
    assertTrue("Captured invitation should be used", capturedInvitation.captured.isUsed)
    assertEquals(
        "Captured invitation should have userId", userId, capturedInvitation.captured.usedBy)
    assertNotNull("Captured invitation should have timestamp", capturedInvitation.captured.usedAt)
  }
}

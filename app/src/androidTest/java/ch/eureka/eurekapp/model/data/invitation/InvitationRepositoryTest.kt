package ch.eureka.eurekapp.model.data.invitation

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test

/**
 * Test suite for InvitationRepository implementation.
 *
 * Tests cover CRUD operations, token validation, and concurrent access scenarios to ensure
 * invitation system integrity.
 *
 * Note: This file was co-authored by Claude Code.
 */
class InvitationRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: FirestoreInvitationRepository
  private lateinit var testProjectId: String

  override fun getCollectionPaths(): List<String> {
    return listOf("invitations")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    testProjectId = "test_project_123"
    repository = FirestoreInvitationRepository(firestore = FirebaseEmulator.firestore)

    // Explicitly clear invitations collection to prevent test interference
    val invitations = FirebaseEmulator.firestore.collection("invitations").get().await()
    invitations.documents.forEach { it.reference.delete().await() }
  }

  @Test
  fun createInvitation_shouldSaveInvitationToFirestore() = runBlocking {
    val invitation = Invitation(token = "TOKEN123", projectId = testProjectId, isUsed = false)

    val result = repository.createInvitation(invitation)

    assertTrue(result.isSuccess)

    val savedInvitation =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("TOKEN123")
            .get()
            .await()
            .toObject(Invitation::class.java)

    assertNotNull(savedInvitation)
    assertEquals(invitation.token, savedInvitation?.token)
    assertEquals(invitation.projectId, savedInvitation?.projectId)
    assertFalse(savedInvitation?.isUsed ?: true)
    assertNull(savedInvitation?.usedBy)
  }

  @Test
  fun getInvitationByToken_shouldReturnInvitationWhenExists() = runBlocking {
    val invitation =
        Invitation(token = "UNIQUE_TOKEN_456", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)

    val flow = repository.getInvitationByToken("UNIQUE_TOKEN_456")
    val retrievedInvitation = flow.first()

    assertNotNull(retrievedInvitation)
    assertEquals(invitation.token, retrievedInvitation?.token)
    assertEquals(invitation.projectId, retrievedInvitation?.projectId)
    assertFalse(retrievedInvitation?.isUsed ?: true)
  }

  @Test
  fun getInvitationByToken_shouldReturnNullWhenTokenDoesNotExist() = runBlocking {
    val flow = repository.getInvitationByToken("NON_EXISTENT_TOKEN")
    val retrievedInvitation = flow.first()

    assertNull(retrievedInvitation)
  }

  @Test
  fun getProjectInvitations_shouldReturnAllInvitationsForProject() = runBlocking {
    val invitation1 = Invitation(token = "TOKEN_1", projectId = testProjectId, isUsed = false)

    val invitation2 =
        Invitation(token = "TOKEN_2", projectId = testProjectId, isUsed = true, usedBy = "user_456")

    val invitation3 = Invitation(token = "TOKEN_3", projectId = "other_project", isUsed = false)

    repository.createInvitation(invitation1)
    repository.createInvitation(invitation2)
    repository.createInvitation(invitation3)

    val flow = repository.getProjectInvitations(testProjectId)
    val invitations = flow.first()

    assertEquals(2, invitations.size)
    assertTrue(invitations.any { it.token == "TOKEN_1" })
    assertTrue(invitations.any { it.token == "TOKEN_2" })
    assertFalse(invitations.any { it.token == "TOKEN_3" })
  }

  @Test
  fun getProjectInvitations_shouldReturnEmptyListWhenNoInvitations() = runBlocking {
    val flow = repository.getProjectInvitations("non_existent_project")
    val invitations = flow.first()

    assertTrue(invitations.isEmpty())
  }

  @Test
  fun markInvitationAsUsed_shouldUpdateInvitationSuccessfully() = runBlocking {
    val invitation =
        Invitation(token = "MARK_USED_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)

    // Give Firestore time to persist
    kotlinx.coroutines.delay(500)

    val result = repository.markInvitationAsUsed("MARK_USED_TOKEN", "new_user_123")

    assertTrue(
        "markInvitationAsUsed should return success, but got: ${result.exceptionOrNull()?.message}",
        result.isSuccess)

    // Give Firestore time to propagate update
    kotlinx.coroutines.delay(500)

    val updatedInvitation =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("MARK_USED_TOKEN")
            .get()
            .await()
            .toObject(Invitation::class.java)

    assertNotNull("Updated invitation should not be null after marking as used", updatedInvitation)
    assertTrue(
        "Invitation isUsed flag should be true, but was: ${updatedInvitation?.isUsed}",
        updatedInvitation?.isUsed ?: false)
    assertEquals(
        "UsedBy should be 'new_user_123', but was: ${updatedInvitation?.usedBy}",
        "new_user_123",
        updatedInvitation?.usedBy)
    assertNotNull(
        "UsedAt timestamp should not be null after marking as used", updatedInvitation?.usedAt)
  }

  @Test
  fun markInvitationAsUsed_shouldFailWhenInvitationAlreadyUsed() = runBlocking {
    val invitation =
        Invitation(
            token = "ALREADY_USED_TOKEN",
            projectId = testProjectId,
            isUsed = true,
            usedBy = "first_user",
            usedAt = Timestamp.now())
    repository.createInvitation(invitation)

    // Give Firestore time to persist
    kotlinx.coroutines.delay(100)

    val result = repository.markInvitationAsUsed("ALREADY_USED_TOKEN", "second_user")

    assertTrue(
        "Result should be failure when invitation already used, but was success", result.isFailure)
    assertTrue(
        "Error message should contain 'already been used', but was: ${result.exceptionOrNull()?.message}",
        result.exceptionOrNull()?.message?.contains("already been used") ?: false)
  }

  @Test
  fun markInvitationAsUsed_shouldFailWhenInvitationNotFound() = runBlocking {
    val result = repository.markInvitationAsUsed("non_existent_invitation", "user_123")

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("not found") ?: false)
  }

  @Test
  fun getInvitationByToken_shouldReactToChanges() = runBlocking {
    val invitation = Invitation(token = "REACTIVE_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)

    // Small delay to ensure creation completes
    kotlinx.coroutines.delay(200)

    val flow = repository.getInvitationByToken("REACTIVE_TOKEN")

    // Start collecting in background to capture both emissions
    val emissionsJob = async { flow.take(2).toList() }

    // Wait for first emission
    kotlinx.coroutines.delay(200)

    // Mark as used - this should trigger second emission
    repository.markInvitationAsUsed("REACTIVE_TOKEN", "reactive_user")

    // Wait for both emissions with timeout
    val emissions = withTimeout(3000) { emissionsJob.await() }

    // Verify 2 emissions received
    assertTrue("Should have received 2 emissions, but got: ${emissions.size}", emissions.size == 2)

    val initialInvitation = emissions[0]
    assertNotNull("Initial invitation should not be null", initialInvitation)
    assertFalse(
        "Initial invitation isUsed should be false, but was: ${initialInvitation?.isUsed}",
        initialInvitation?.isUsed ?: true)

    val updatedInvitation = emissions[1]
    assertNotNull("Updated invitation should not be null", updatedInvitation)
    assertTrue(
        "Updated invitation isUsed should be true after marking as used, but was: ${updatedInvitation?.isUsed}",
        updatedInvitation?.isUsed ?: false)
    assertEquals(
        "Updated invitation usedBy should be 'reactive_user', but was: ${updatedInvitation?.usedBy}",
        "reactive_user",
        updatedInvitation?.usedBy)
  }

  @Test
  fun getProjectInvitations_shouldReactToChanges() = runBlocking {
    val invitation1 = Invitation(token = "REACTIVE_1", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation1)

    val flow = repository.getProjectInvitations(testProjectId)
    val initialInvitations = flow.first()

    assertEquals(1, initialInvitations.size)

    val invitation2 = Invitation(token = "REACTIVE_2", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation2)

    // The flow should emit the updated list
    val updatedInvitations = flow.first()
    assertEquals(2, updatedInvitations.size)
  }

  // ========================================
  // Edge Cases & Input Validation Tests
  // ========================================

  @Test
  fun createInvitation_shouldHandleDuplicateToken() = runBlocking {
    val invitation1 =
        Invitation(token = "DUPLICATE_TOKEN", projectId = testProjectId, isUsed = false)
    val invitation2 =
        Invitation(token = "DUPLICATE_TOKEN", projectId = "other_project", isUsed = false)

    val result1 = repository.createInvitation(invitation1)
    assertTrue("First creation should succeed", result1.isSuccess)

    val result2 = repository.createInvitation(invitation2)
    assertTrue("Duplicate token creation should succeed (overwrites)", result2.isSuccess)

    // Verify the second invitation overwrote the first
    val retrieved = repository.getInvitationByToken("DUPLICATE_TOKEN").first()
    assertNotNull("Invitation should exist", retrieved)
    assertEquals("Should have second project ID", "other_project", retrieved?.projectId)
  }

  @Test
  fun markInvitationAsUsed_shouldValidateEmptyUserId() = runBlocking {
    val invitation =
        Invitation(token = "EMPTY_USER_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(500)

    val result = repository.markInvitationAsUsed("EMPTY_USER_TOKEN", "")

    assertTrue("Should succeed even with empty userId", result.isSuccess)

    kotlinx.coroutines.delay(500)

    val updated =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("EMPTY_USER_TOKEN")
            .get()
            .await()
            .toObject(Invitation::class.java)
    assertTrue("Should be marked as used", updated?.isUsed ?: false)
    assertEquals("Should store empty userId", "", updated?.usedBy)
  }

  @Test
  fun getInvitationByToken_shouldHandleSpecialCharacters() = runBlocking {
    val specialToken = "TOKEN_WITH_!@#$%^&*()_+-=[]{}|;:',.<>?"
    val invitation = Invitation(token = specialToken, projectId = testProjectId, isUsed = false)

    val createResult = repository.createInvitation(invitation)
    assertTrue("Should handle special characters in token", createResult.isSuccess)

    val retrieved = repository.getInvitationByToken(specialToken).first()
    assertNotNull("Should retrieve invitation with special characters", retrieved)
    assertEquals("Token should match exactly", specialToken, retrieved?.token)
  }

  @Test
  fun createInvitation_shouldHandleVeryLongToken() = runBlocking {
    val longToken = "A".repeat(1000) // 1000 character token
    val invitation = Invitation(token = longToken, projectId = testProjectId, isUsed = false)

    val result = repository.createInvitation(invitation)
    assertTrue("Should handle long tokens", result.isSuccess)

    val retrieved = repository.getInvitationByToken(longToken).first()
    assertNotNull("Should retrieve long token invitation", retrieved)
    assertEquals("Long token should match", longToken, retrieved?.token)
  }

  // ========================================
  // Concurrency & Race Condition Tests
  // ========================================

  @Test
  fun markInvitationAsUsed_shouldHandleConcurrentAttempts() = runBlocking {
    val invitation =
        Invitation(token = "CONCURRENT_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(500)

    // Launch 3 concurrent attempts to mark as used
    val results =
        listOf(
                async { repository.markInvitationAsUsed("CONCURRENT_TOKEN", "user_1") },
                async { repository.markInvitationAsUsed("CONCURRENT_TOKEN", "user_2") },
                async { repository.markInvitationAsUsed("CONCURRENT_TOKEN", "user_3") })
            .map { it.await() }

    // Exactly one should succeed, others should fail
    val successCount = results.count { it.isSuccess }
    val failureCount = results.count { it.isFailure }

    assertEquals("Exactly one concurrent attempt should succeed", 1, successCount)
    assertEquals("Exactly two concurrent attempts should fail", 2, failureCount)

    kotlinx.coroutines.delay(500)

    // Verify the invitation is marked as used by one of the users
    val updated =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("CONCURRENT_TOKEN")
            .get()
            .await()
            .toObject(Invitation::class.java)
    assertTrue("Invitation should be marked as used", updated?.isUsed ?: false)
    assertNotNull("UsedBy should be set", updated?.usedBy)
    assertTrue(
        "UsedBy should be one of the concurrent users",
        updated?.usedBy in listOf("user_1", "user_2", "user_3"))
  }

  @Test
  fun createInvitation_shouldHandleConcurrentCreation() = runBlocking {
    val token = "CONCURRENT_CREATE_TOKEN"

    // Launch 3 concurrent creation attempts
    val results =
        listOf(
                async { repository.createInvitation(Invitation(token, "project_1", false)) },
                async { repository.createInvitation(Invitation(token, "project_2", false)) },
                async { repository.createInvitation(Invitation(token, "project_3", false)) })
            .map { it.await() }

    // All should succeed (last write wins in Firestore)
    assertTrue("All concurrent creations should succeed", results.all { it.isSuccess })

    // Verify one of the invitations exists
    val retrieved = repository.getInvitationByToken(token).first()
    assertNotNull("Invitation should exist after concurrent creation", retrieved)
    assertTrue(
        "ProjectId should be one of the concurrent values",
        retrieved?.projectId in listOf("project_1", "project_2", "project_3"))
  }

  // ========================================
  // Data Integrity Tests
  // ========================================

  @Test
  fun markInvitationAsUsed_shouldSetTimestampCorrectly() = runBlocking {
    val invitation =
        Invitation(token = "TIMESTAMP_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(500)

    val beforeTimestamp = Timestamp.now()
    kotlinx.coroutines.delay(100)

    repository.markInvitationAsUsed("TIMESTAMP_TOKEN", "timestamp_user")
    kotlinx.coroutines.delay(500)

    val afterTimestamp = Timestamp.now()

    val updated =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("TIMESTAMP_TOKEN")
            .get()
            .await()
            .toObject(Invitation::class.java)
    assertNotNull("UsedAt timestamp should be set", updated?.usedAt)

    val usedAt = updated?.usedAt
    assertTrue(
        "UsedAt should be after beforeTimestamp",
        usedAt != null && usedAt.seconds >= beforeTimestamp.seconds)
    assertTrue(
        "UsedAt should be before afterTimestamp",
        usedAt != null && usedAt.seconds <= afterTimestamp.seconds)
  }

  @Test
  fun markInvitationAsUsed_shouldNotModifyOtherFields() = runBlocking {
    val originalToken = "IMMUTABLE_TOKEN"
    val originalProjectId = "original_project_123"
    val invitation =
        Invitation(token = originalToken, projectId = originalProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(500)

    repository.markInvitationAsUsed(originalToken, "modifier_user")
    kotlinx.coroutines.delay(500)

    val updated =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document(originalToken)
            .get()
            .await()
            .toObject(Invitation::class.java)
    assertNotNull("Invitation should still exist", updated)
    assertEquals("Token should not be modified", originalToken, updated?.token)
    assertEquals("ProjectId should not be modified", originalProjectId, updated?.projectId)
    assertTrue("IsUsed should be true", updated?.isUsed ?: false)
    assertEquals("UsedBy should be set", "modifier_user", updated?.usedBy)
  }

  @Test
  fun getProjectInvitations_shouldHandleMultipleUsedStates() = runBlocking {
    // Create mix of used and unused invitations
    repository.createInvitation(Invitation("MIXED_1", testProjectId, false))
    repository.createInvitation(
        Invitation("MIXED_2", testProjectId, true, "user_a", Timestamp.now()))
    repository.createInvitation(Invitation("MIXED_3", testProjectId, false))
    repository.createInvitation(
        Invitation("MIXED_4", testProjectId, true, "user_b", Timestamp.now()))
    kotlinx.coroutines.delay(300)

    val invitations = repository.getProjectInvitations(testProjectId).first()

    assertEquals("Should return all 4 invitations", 4, invitations.size)
    assertEquals("Should have 2 unused invitations", 2, invitations.count { !it.isUsed })
    assertEquals("Should have 2 used invitations", 2, invitations.count { it.isUsed })
  }

  // ========================================
  // Performance & Scalability Tests
  // ========================================

  @Test
  fun getProjectInvitations_shouldHandleLargeNumberOfInvitations() = runBlocking {
    val largeProjectId = "large_project_123"
    val invitationCount = 50

    // Create 50 invitations
    repeat(invitationCount) { index ->
      repository.createInvitation(
          Invitation(
              token = "LARGE_TOKEN_$index",
              projectId = largeProjectId,
              isUsed = index % 3 == 0 // Every 3rd invitation is used
              ))
    }
    kotlinx.coroutines.delay(1000) // Allow Firestore to settle

    val invitations = repository.getProjectInvitations(largeProjectId).first()

    assertEquals(
        "Should retrieve all $invitationCount invitations", invitationCount, invitations.size)

    val usedCount = invitations.count { it.isUsed }
    assertTrue(
        "Should have correct proportion of used invitations",
        usedCount >= 15 && usedCount <= 20 // Approximately 1/3
        )
  }

  @Test
  fun getInvitationByToken_shouldHandleQuickSuccessiveReads() = runBlocking {
    val invitation =
        Invitation(token = "QUICK_READ_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(200)

    // Perform 10 quick successive reads
    val results = (1..10).map { repository.getInvitationByToken("QUICK_READ_TOKEN").first() }

    assertEquals("All reads should return results", 10, results.size)
    assertTrue("All reads should return non-null", results.all { it != null })
    assertTrue(
        "All reads should have consistent data", results.all { it?.token == "QUICK_READ_TOKEN" })
  }

  // ========================================
  // Flow Behavior Tests
  // ========================================

  @Test
  fun getInvitationByToken_shouldHandleMultipleSubscribers() = runBlocking {
    val invitation =
        Invitation(token = "MULTI_SUB_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)
    kotlinx.coroutines.delay(200)

    val flow = repository.getInvitationByToken("MULTI_SUB_TOKEN")

    // Create 3 concurrent subscribers
    val subscriber1 = async { flow.take(2).toList() }
    val subscriber2 = async { flow.take(2).toList() }
    val subscriber3 = async { flow.take(2).toList() }

    kotlinx.coroutines.delay(200)

    // Trigger update
    repository.markInvitationAsUsed("MULTI_SUB_TOKEN", "multi_sub_user")

    // All subscribers should receive both emissions
    val emissions1 = withTimeout(3000) { subscriber1.await() }
    val emissions2 = withTimeout(3000) { subscriber2.await() }
    val emissions3 = withTimeout(3000) { subscriber3.await() }

    assertEquals("Subscriber 1 should receive 2 emissions", 2, emissions1.size)
    assertEquals("Subscriber 2 should receive 2 emissions", 2, emissions2.size)
    assertEquals("Subscriber 3 should receive 2 emissions", 2, emissions3.size)

    // All should see the update
    assertTrue("Subscriber 1 should see used state", emissions1[1]?.isUsed ?: false)
    assertTrue("Subscriber 2 should see used state", emissions2[1]?.isUsed ?: false)
    assertTrue("Subscriber 3 should see used state", emissions3[1]?.isUsed ?: false)
  }

  @Test
  fun getProjectInvitations_shouldEmitOnDelete() = runBlocking {
    val token = "DELETE_TOKEN"
    repository.createInvitation(Invitation(token, testProjectId, false))
    kotlinx.coroutines.delay(200)

    val flow = repository.getProjectInvitations(testProjectId)

    val emissionsJob = async { flow.take(2).toList() }

    kotlinx.coroutines.delay(200)

    // Delete the invitation
    FirebaseEmulator.firestore.collection("invitations").document(token).delete().await()

    val emissions = withTimeout(3000) { emissionsJob.await() }

    assertEquals("Should receive 2 emissions", 2, emissions.size)
    assertEquals("First emission should have 1 invitation", 1, emissions[0].size)
    assertEquals("Second emission should have 0 invitations", 0, emissions[1].size)
  }
}

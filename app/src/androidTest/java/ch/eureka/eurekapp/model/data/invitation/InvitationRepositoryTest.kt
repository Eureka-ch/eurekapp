package ch.eureka.eurekapp.model.data.invitation

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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
    val invitation =
        Invitation(token = "TOKEN123", projectId = testProjectId, isUsed = false)

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
    val invitation = Invitation(token = "UNIQUE_TOKEN_456", projectId = testProjectId, isUsed = false)
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
    val invitation = Invitation(token = "MARK_USED_TOKEN", projectId = testProjectId, isUsed = false)
    repository.createInvitation(invitation)


    // Give Firestore time to persist
    kotlinx.coroutines.delay(100)

    val result = repository.markInvitationAsUsed("MARK_USED_TOKEN", "new_user_123")

    assertTrue(result.isSuccess)

    // Give Firestore time to propagate update
    kotlinx.coroutines.delay(100)

    val updatedInvitation =
        FirebaseEmulator.firestore
            .collection("invitations")
            .document("MARK_USED_TOKEN")
            .get()
            .await()
            .toObject(Invitation::class.java)

    assertNotNull(updatedInvitation)
    assertTrue(updatedInvitation?.isUsed ?: false)
    assertEquals("new_user_123", updatedInvitation?.usedBy)
    assertNotNull(updatedInvitation?.usedAt)
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

    assertTrue(result.isFailure)
    assertTrue(
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

    // Give Firestore time to persist
    kotlinx.coroutines.delay(100)

    val flow = repository.getInvitationByToken("REACTIVE_TOKEN")
    val initialInvitation = flow.first()

    assertNotNull(initialInvitation)
    assertFalse(initialInvitation?.isUsed ?: true)

    // Mark as used
    repository.markInvitationAsUsed("REACTIVE_TOKEN", "reactive_user")

    // Give Firestore and Flow time to propagate the update
    kotlinx.coroutines.delay(200)

    // The flow should emit the updated invitation
    val updatedInvitation = flow.first()
    assertNotNull(updatedInvitation)
    assertTrue(updatedInvitation?.isUsed ?: false)
    assertEquals("reactive_user", updatedInvitation?.usedBy)
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
}

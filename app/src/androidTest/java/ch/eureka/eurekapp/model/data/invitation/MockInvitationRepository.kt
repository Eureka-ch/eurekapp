package ch.eureka.eurekapp.model.data.invitation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Mock implementation of InvitationRepository for testing.
 *
 * Provides controllable behavior for testing various scenarios including success, failure, and edge
 * cases. Thread-safe and tracks all method calls for verification.
 */
class MockInvitationRepository : InvitationRepository {

  // Storage for invitations
  private val invitations = mutableMapOf<String, Invitation>()

  // Track method calls for verification
  val getInvitationByTokenCalls = mutableListOf<String>()
  val getProjectInvitationsCalls = mutableListOf<String>()
  val createInvitationCalls = mutableListOf<Invitation>()
  val markInvitationAsUsedCalls = mutableListOf<Pair<String, String>>()

  // Control behavior
  var shouldThrowException = false
  var exceptionToThrow: Exception = Exception("Mock exception")
  var shouldFailMarkAsUsed = false
  var markAsUsedFailureMessage: String? = "Failed to mark invitation as used"

  /** Add an invitation to the mock repository */
  fun addInvitation(invitation: Invitation) {
    invitations[invitation.token] = invitation
  }

  /** Clear all invitations */
  fun clearInvitations() {
    invitations.clear()
  }

  /** Clear all call tracking */
  fun clearCallTracking() {
    getInvitationByTokenCalls.clear()
    getProjectInvitationsCalls.clear()
    createInvitationCalls.clear()
    markInvitationAsUsedCalls.clear()
  }

  /** Reset all state */
  fun reset() {
    clearInvitations()
    clearCallTracking()
    shouldThrowException = false
    shouldFailMarkAsUsed = false
    markAsUsedFailureMessage = "Failed to mark invitation as used"
  }

  override fun getInvitationByToken(token: String): Flow<Invitation?> {
    getInvitationByTokenCalls.add(token)

    if (shouldThrowException) {
      throw exceptionToThrow
    }

    return flow { emit(invitations[token]) }
  }

  override fun getProjectInvitations(projectId: String): Flow<List<Invitation>> {
    getProjectInvitationsCalls.add(projectId)

    if (shouldThrowException) {
      throw exceptionToThrow
    }

    val projectInvitations = invitations.values.filter { it.projectId == projectId }
    return MutableStateFlow(projectInvitations)
  }

  override suspend fun createInvitation(invitation: Invitation): Result<Unit> {
    createInvitationCalls.add(invitation)

    if (shouldThrowException) {
      throw exceptionToThrow
    }

    invitations[invitation.token] = invitation
    return Result.success(Unit)
  }

  override suspend fun markInvitationAsUsed(token: String, userId: String): Result<Unit> {
    markInvitationAsUsedCalls.add(Pair(token, userId))

    if (shouldThrowException) {
      return Result.failure(exceptionToThrow)
    }

    if (shouldFailMarkAsUsed) {
      return Result.failure(Exception(markAsUsedFailureMessage))
    }

    val invitation = invitations[token]
    if (invitation != null) {
      invitation.isUsed = true
      invitation.usedBy = userId
      invitation.usedAt = com.google.firebase.Timestamp.now()
      invitations[token] = invitation
    }

    return Result.success(Unit)
  }
}

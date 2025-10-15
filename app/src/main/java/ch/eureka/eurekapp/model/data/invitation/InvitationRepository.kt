package ch.eureka.eurekapp.model.data.invitation

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing project invitations.
 *
 * This interface defines operations for creating, retrieving, and managing project invitations.
 * Implementations should ensure token uniqueness and handle concurrent access properly.
 *
 * Note: This file was co-authored by Claude Code.
 */
interface InvitationRepository {
  /**
   * Get an invitation by its token with real-time updates.
   *
   * @param token The unique invitation token.
   * @return Flow emitting the invitation if found, null otherwise.
   */
  fun getInvitationByToken(token: String): Flow<Invitation?>

  /**
   * Get all invitations for a specific project with real-time updates.
   *
   * @param projectId The ID of the project.
   * @return Flow emitting list of invitations for the project.
   */
  fun getProjectInvitations(projectId: String): Flow<List<Invitation>>

  /**
   * Create a new invitation for a project.
   *
   * @param invitation The invitation to create.
   * @return Result indicating success or failure.
   */
  suspend fun createInvitation(invitation: Invitation): Result<Unit>

  /**
   * Mark an invitation as used by a specific user.
   *
   * This operation should be atomic to prevent race conditions. Once marked as used, the
   * invitation cannot be used again.
   *
   * @param token The token of the invitation to mark as used.
   * @param userId The ID of the user who is using the invitation.
   * @return Result indicating success or failure.
   */
  suspend fun markInvitationAsUsed(token: String, userId: String): Result<Unit>
}

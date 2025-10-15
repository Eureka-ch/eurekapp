package ch.eureka.eurekapp.model.data.invitation

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provides a single [InvitationRepository] instance for the whole codebase.
 *
 * This follows the singleton provider pattern used throughout the application for repository
 * dependency injection.
 *
 * Note: This file was co-authored by Claude Code.
 */
object InvitationRepositoryProvider {
  private val _repository: InvitationRepository by lazy {
    FirestoreInvitationRepository(FirebaseFirestore.getInstance())
  }

  val repository: InvitationRepository = _repository
}

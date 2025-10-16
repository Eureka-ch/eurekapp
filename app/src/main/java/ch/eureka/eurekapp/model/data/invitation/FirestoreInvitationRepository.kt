package ch.eureka.eurekapp.model.data.invitation

import android.util.Log
import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of [InvitationRepository].
 *
 * This implementation provides real-time updates for invitation data and ensures atomic operations
 * for marking invitations as used to prevent race conditions.
 *
 * @property firestore The Firestore instance to use for database operations.
 */
class FirestoreInvitationRepository(private val firestore: FirebaseFirestore) :
    InvitationRepository {

  override fun getInvitationByToken(token: String): Flow<Invitation?> = callbackFlow {
    val listener =
        firestore.collection(FirestorePaths.INVITATIONS).document(token).addSnapshotListener {
            snapshot,
            error ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          val invitation = snapshot?.toObject(Invitation::class.java)
          trySend(invitation)
        }
    awaitClose { listener.remove() }
  }

  override fun getProjectInvitations(projectId: String): Flow<List<Invitation>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.INVITATIONS)
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val invitations =
                  snapshot?.documents?.mapNotNull { it.toObject(Invitation::class.java) }
                      ?: emptyList()
              trySend(invitations)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createInvitation(invitation: Invitation): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.INVITATIONS)
        .document(invitation.token)
        .set(invitation)
        .await()
  }

  override suspend fun markInvitationAsUsed(token: String, userId: String): Result<Unit> =
      runCatching {
        val invitationRef = firestore.collection(FirestorePaths.INVITATIONS).document(token)

        firestore
            .runTransaction { transaction ->
              val snapshot = transaction.get(invitationRef)

              if (!snapshot.exists()) {
                throw IllegalStateException("Invitation not found")
              }

              val invitation =
                  snapshot.toObject(Invitation::class.java)
                      ?: throw IllegalStateException("Invitation not found")

              if (invitation.isUsed) {
                throw IllegalStateException("Invitation has already been used")
              }

              // mark as used -> update the object and write it back
              invitation.isUsed = true
              invitation.usedBy = userId
              invitation.usedAt = Timestamp.now()

              // use set with the updated object to ensure all fields are properly written
              transaction.set(invitationRef, invitation)
            }
            .await()
      }
}

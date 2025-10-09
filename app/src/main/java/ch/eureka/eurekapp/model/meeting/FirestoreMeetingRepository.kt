package ch.eureka.eurekapp.model.meeting

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMeetingRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MeetingRepository {

  override fun getMeetingById(workspaceId: String, meetingId: String): Flow<Meeting?> =
      callbackFlow {
        val listener =
            firestore
                .collection("workspaces")
                .document(workspaceId)
                .collection("meetings")
                .document(meetingId)
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  trySend(snapshot?.toObject(Meeting::class.java))
                }
        awaitClose { listener.remove() }
      }

  override fun getMeetingsInWorkspace(workspaceId: String): Flow<List<Meeting>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("meetings")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val meetings =
                  snapshot?.documents?.mapNotNull { it.toObject(Meeting::class.java) }
                      ?: emptyList()
              trySend(meetings)
            }
    awaitClose { listener.remove() }
  }

  override fun getMeetingsForContext(
      workspaceId: String,
      contextId: String,
      contextType: ContextType
  ): Flow<List<Meeting>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("meetings")
            .whereEqualTo("contextId", contextId)
            .whereEqualTo("contextType", contextType.name)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val meetings =
                  snapshot?.documents?.mapNotNull { it.toObject(Meeting::class.java) }
                      ?: emptyList()
              trySend(meetings)
            }
    awaitClose { listener.remove() }
  }

  override fun getMeetingsForCurrentUser(workspaceId: String): Flow<List<Meeting>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("meetings")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val meetings =
                  snapshot
                      ?.documents
                      ?.mapNotNull { it.toObject(Meeting::class.java) }
                      ?.filter { it.participants.containsKey(currentUserId) } ?: emptyList()
              trySend(meetings)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createMeeting(meeting: Meeting): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(meeting.workspaceId)
        .collection("meetings")
        .document(meeting.meetingID)
        .set(meeting)
        .await()
    meeting.meetingID
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(meeting.workspaceId)
        .collection("meetings")
        .document(meeting.meetingID)
        .set(meeting)
        .await()
  }

  override suspend fun deleteMeeting(workspaceId: String, meetingId: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("meetings")
            .document(meetingId)
            .delete()
            .await()
      }

  override suspend fun addParticipant(
      workspaceId: String,
      meetingId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("meetings")
        .document(meetingId)
        .update("participants.$userId", role)
        .await()
  }

  override suspend fun removeParticipant(
      workspaceId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("meetings")
        .document(meetingId)
        .update("participants.$userId", FieldValue.delete())
        .await()
  }
}

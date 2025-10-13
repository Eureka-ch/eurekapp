package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.enumFromString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMeetingRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MeetingRepository {

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
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

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
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

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.MEETINGS)
                .whereEqualTo("taskId", taskId)
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

  override fun getMeetingsForCurrentUser(projectId: String): Flow<List<Meeting>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collectionGroup("participants")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }

              val meetingIds =
                  snapshot?.documents?.mapNotNull { doc -> doc.reference.parent.parent?.id }
                      ?: emptyList()

              if (meetingIds.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
              }

              firestore
                  .collection(FirestorePaths.PROJECTS)
                  .document(projectId)
                  .collection(FirestorePaths.MEETINGS)
                  .whereIn("meetingID", meetingIds)
                  .get()
                  .addOnSuccessListener { meetingSnapshot ->
                    val meetings =
                        meetingSnapshot.documents.mapNotNull { it.toObject(Meeting::class.java) }
                    trySend(meetings)
                  }
                  .addOnFailureListener { close(it) }
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: String
  ): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .set(meeting)
        .await()

    val participant =
        Participant(userId = creatorId, role = enumFromString<MeetingRole>(creatorRole))
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .collection("participants")
        .document(creatorId)
        .set(participant)
        .await()

    meeting.meetingID
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .set(meeting)
        .await()
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      runCatching {
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
            .document(meetingId)
            .delete()
            .await()
      }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.MEETINGS)
                .document(meetingId)
                .collection("participants")
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  val participants =
                      snapshot?.documents?.mapNotNull { it.toObject(Participant::class.java) }
                          ?: emptyList()
                  trySend(participants)
                }
        awaitClose { listener.remove() }
      }

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    val participant = Participant(userId = userId, role = enumFromString<MeetingRole>(role))
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meetingId)
        .collection("participants")
        .document(userId)
        .set(participant)
        .await()
  }

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meetingId)
        .collection("participants")
        .document(userId)
        .delete()
        .await()
  }

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meetingId)
        .collection("participants")
        .document(userId)
        .update("role", enumFromString<MeetingRole>(role))
        .await()
  }
}

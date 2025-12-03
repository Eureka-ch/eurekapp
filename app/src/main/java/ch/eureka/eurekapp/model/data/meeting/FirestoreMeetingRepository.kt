/*
 * This file was co-authored by Claude Code and Gemini.
 */
package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.ActivityLogger
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class FirestoreMeetingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
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

  override fun getMeetingsForCurrentUser(
      projectId: String,
      skipCache: Boolean
  ): Flow<List<Meeting>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              // Skip cached data if requested to avoid stale results
              if (skipCache && snapshot?.metadata?.isFromCache == true) {
                return@addSnapshotListener
              }
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

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> = runCatching {
    // Add creator to participantIds
    val meetingWithParticipant = meeting.copy(participantIds = listOf(creatorId))

    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .set(meetingWithParticipant)
        .await()

    val participant = Participant(userId = creatorId, role = creatorRole)
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .collection("participants")
        .document(creatorId)
        .set(participant)
        .await()

    // Log activity to global feed so everyone sees all meetings
    ActivityLogger.logActivity(
        projectId = meeting.projectId,
        activityType = ActivityType.CREATED,
        entityType = EntityType.MEETING,
        entityId = meeting.meetingID,
        userId = creatorId,
        metadata = mapOf("title" to meeting.title))

    meeting.meetingID
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = runCatching {
    // Fetch old meeting to detect status changes using getMeetingById
    val oldMeeting = getMeetingById(meeting.projectId, meeting.meetingID).first()

    // Perform the update
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(meeting.projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meeting.meetingID)
        .set(meeting)
        .await()

    // Log activity to global feed after successful update
    val currentUserId = auth.currentUser?.uid

    // Log if oldMeeting is null
    if (oldMeeting == null) {
      android.util.Log.e(
          "FirestoreMeetingRepository",
          "Failed to fetch old meeting for status change detection: projectId=${meeting.projectId}, meetingId=${meeting.meetingID}")
    }

    // Log if currentUserId is null
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreMeetingRepository",
          "Cannot log activity for meeting update: currentUser is null")
    }

    if (currentUserId != null && oldMeeting != null) {
      if (oldMeeting.status != meeting.status) {
        // Status changed - use STATUS_CHANGED
        ActivityLogger.logActivity(
            projectId = meeting.projectId,
            activityType = ActivityType.STATUS_CHANGED,
            entityType = EntityType.MEETING,
            entityId = meeting.meetingID,
            userId = currentUserId,
            metadata =
                mapOf(
                    "title" to meeting.title,
                    "oldStatus" to oldMeeting.status.name,
                    "newStatus" to meeting.status.name))
      } else {
        // Other fields changed - use UPDATED
        ActivityLogger.logActivity(
            projectId = meeting.projectId,
            activityType = ActivityType.UPDATED,
            entityType = EntityType.MEETING,
            entityId = meeting.meetingID,
            userId = currentUserId,
            metadata = mapOf("title" to meeting.title))
      }
    }
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      runCatching {
        // Get meeting title before deletion for activity log
        val meetingSnapshot =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.MEETINGS)
                .document(meetingId)
                .get()
                .await()
        val meetingTitle = meetingSnapshot.toObject(Meeting::class.java)?.title

        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
            .document(meetingId)
            .delete()
            .await()

        // Log activity to global feed after successful deletion
        val currentUserId = requireNotNull(auth.currentUser?.uid) { "User must be logged in." }
        requireNotNull(meetingTitle) { "Title should not be null." }
        ActivityLogger.logActivity(
            projectId = projectId,
            activityType = ActivityType.DELETED,
            entityType = EntityType.MEETING,
            entityId = meetingId,
            userId = currentUserId,
            metadata = mapOf("title" to meetingTitle))
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
      role: MeetingRole
  ): Result<Unit> = runCatching {
    val meetingRef =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
            .document(meetingId)

    firestore
        .runBatch { batch ->
          // Update participantIds array
          batch.update(meetingRef, "participantIds", FieldValue.arrayUnion(userId))

          // Create participant document
          val participant = Participant(userId = userId, role = role)
          batch[meetingRef.collection("participants").document(userId)] = participant
        }
        .await()
  }

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = runCatching {
    val meetingRef =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEETINGS)
            .document(meetingId)

    firestore
        .runBatch { batch ->
          // Remove from participantIds array
          batch.update(meetingRef, "participantIds", FieldValue.arrayRemove(userId))

          // Delete participant document
          batch.delete(meetingRef.collection("participants").document(userId))
        }
        .await()
  }

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meetingId)
        .collection("participants")
        .document(userId)
        .update("role", role.name)
        .await()
  }
}

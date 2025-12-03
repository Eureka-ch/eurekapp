package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.ActivityLogger
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of ProjectRepository.
 *
 * Provides project management operations including CRUD for projects and member management using
 * Firebase Firestore with real-time updates through Flow-based APIs.
 */
class FirestoreProjectRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ProjectRepository {

  override fun getProjectById(projectId: String): Flow<Project?> = callbackFlow {
    val listener =
        firestore.collection(FirestorePaths.PROJECTS).document(projectId).addSnapshotListener {
            snapshot,
            error ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          trySend(snapshot?.toObject(Project::class.java))
        }
    awaitClose { listener.remove() }
  }

  override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .whereArrayContains("memberIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              // Skip cached data if requested to avoid stale results
              if (skipCache && snapshot?.metadata?.isFromCache == true) {
                return@addSnapshotListener
              }
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val projects =
                  snapshot?.documents?.mapNotNull { it.toObject(Project::class.java) }
                      ?: emptyList()
              trySend(projects)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createProject(
      project: Project,
      creatorId: String,
      creatorRole: ProjectRole
  ): Result<String> = runCatching {
    val projectRef = firestore.collection(FirestorePaths.PROJECTS).document(project.projectId)

    // Add creator to memberIds
    val projectWithMember =
        project.copy(memberIds = listOf(creatorId), lastUpdated = Timestamp.now())
    projectRef.set(projectWithMember).await()

    // Then create initial member document (security rules require project to exist first)
    val member = Member(userId = creatorId, role = creatorRole)
    val memberRef = projectRef.collection("members").document(creatorId)
    memberRef.set(member).await()

    // Log project creation activity
    ActivityLogger.logActivity(
        projectId = project.projectId,
        activityType = ActivityType.CREATED,
        entityType = EntityType.PROJECT,
        entityId = project.projectId,
        userId = creatorId,
        metadata = mapOf("name" to project.name))

    project.projectId
  }

  override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
    val updatedProject = project.copy(lastUpdated = Timestamp.now())
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(project.projectId)
        .set(updatedProject)
        .await()

    // Log activity to global feed after successful update
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreProjectRepository",
          "Cannot log activity for project update: currentUser is null")
    } else {
      ActivityLogger.logActivity(
          projectId = project.projectId,
          activityType = ActivityType.UPDATED,
          entityType = EntityType.PROJECT,
          entityId = project.projectId,
          userId = currentUserId,
          metadata = mapOf("name" to project.name))
    }
  }

  override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
    // Get project name before deletion for activity log
    val projectSnapshot =
        firestore.collection(FirestorePaths.PROJECTS).document(projectId).get().await()
    val projectName = projectSnapshot.toObject(Project::class.java)?.name

    // Perform deletion
    firestore.collection(FirestorePaths.PROJECTS).document(projectId).delete().await()

    // Log activity to global feed after successful deletion
    val currentUserId = auth.currentUser?.uid

    // Log if currentUserId is null
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreProjectRepository",
          "Cannot log activity for project deletion: currentUser is null")
    }

    // Log if projectName is null
    if (projectName == null) {
      android.util.Log.e(
          "FirestoreProjectRepository",
          "Cannot log activity for project deletion: project name is null for projectId=$projectId")
    }

    if (currentUserId != null && projectName != null) {
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.DELETED,
          entityType = EntityType.PROJECT,
          entityId = projectId,
          userId = currentUserId,
          metadata = mapOf("name" to projectName))
    }
  }

  override fun getMembers(projectId: String): Flow<List<Member>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val members =
                  snapshot?.documents?.mapNotNull { it.toObject(Member::class.java) } ?: emptyList()
              trySend(members)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun addMember(
      projectId: String,
      userId: String,
      role: ProjectRole
  ): Result<Unit> = runCatching {
    val projectRef = firestore.collection(FirestorePaths.PROJECTS).document(projectId)

    firestore
        .runBatch { batch ->
          // Update memberIds array
          batch.update(projectRef, "memberIds", FieldValue.arrayUnion(userId))

          // Create member document
          val member = Member(userId = userId, role = role)
          batch[projectRef.collection("members").document(userId)] = member
        }
        .await()

    // Log member joining activity
    ActivityLogger.logActivity(
        projectId = projectId,
        activityType = ActivityType.JOINED,
        entityType = EntityType.MEMBER,
        entityId = userId,
        userId = userId,
        metadata = mapOf("role" to role.name))
  }

  override suspend fun removeMember(projectId: String, userId: String): Result<Unit> = runCatching {
    val projectRef = firestore.collection(FirestorePaths.PROJECTS).document(projectId)

    firestore
        .runBatch { batch ->
          // Remove from memberIds array
          batch.update(projectRef, "memberIds", FieldValue.arrayRemove(userId))

          // Delete member document
          batch.delete(projectRef.collection("members").document(userId))
        }
        .await()
  }

  override suspend fun updateMemberRole(
      projectId: String,
      userId: String,
      role: ProjectRole
  ): Result<Unit> = runCatching {
    // Fetch old member to get previous role
    val oldMemberDoc =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection("members")
            .document(userId)
            .get()
            .await()
    val oldMember = oldMemberDoc.toObject(Member::class.java)
    val oldRole = oldMember?.role

    // Perform role update
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection("members")
        .document(userId)
        .update("role", role.name)
        .await()

    // Log role change activity
    val currentUserId = auth.currentUser?.uid ?: userId

    // Log if oldRole is null
    if (oldRole == null) {
      android.util.Log.e(
          "FirestoreProjectRepository",
          "Cannot log role change activity: old role is null for projectId=$projectId, userId=$userId")
    } else {
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.ROLE_CHANGED,
          entityType = EntityType.MEMBER,
          entityId = userId,
          userId = currentUserId,
          metadata =
              mapOf("targetUserId" to userId, "oldRole" to oldRole.name, "newRole" to role.name))
    }
  }
}

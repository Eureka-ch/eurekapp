package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.FirestorePaths
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

    project.projectId
  }

  override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
    val updatedProject = project.copy(lastUpdated = Timestamp.now())
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(project.projectId)
        .set(updatedProject)
        .await()
  }

  override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
    firestore.collection(FirestorePaths.PROJECTS).document(projectId).delete().await()
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
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection("members")
        .document(userId)
        .update("role", role.name)
        .await()
  }
}

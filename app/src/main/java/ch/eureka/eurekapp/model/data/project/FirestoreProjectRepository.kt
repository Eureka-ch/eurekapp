package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.auth.FirebaseAuth
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

  override fun getProjectsForCurrentUser(): Flow<List<Project>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collectionGroup("members")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }

              val projectIds =
                  snapshot?.documents?.mapNotNull { doc -> doc.reference.parent.parent?.id }
                      ?: emptyList()

              if (projectIds.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
              }

              firestore
                  .collection(FirestorePaths.PROJECTS)
                  .whereIn("projectId", projectIds)
                  .get()
                  .addOnSuccessListener { projectSnapshot ->
                    val projects =
                        projectSnapshot.documents.mapNotNull { it.toObject(Project::class.java) }
                    trySend(projects)
                  }
                  .addOnFailureListener { close(it) }
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createProject(
      project: Project,
      creatorId: String,
      creatorRole: ProjectRole
  ): Result<String> = runCatching {
    val projectRef = firestore.collection(FirestorePaths.PROJECTS).document(project.projectId)

    projectRef.set(project).await()

    // Then create initial member document (security rules require project to exist first)
    val member = Member(userId = creatorId, role = creatorRole)
    val memberRef = projectRef.collection("members").document(creatorId)
    memberRef.set(member).await()

    project.projectId
  }

  override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
    firestore.collection(FirestorePaths.PROJECTS).document(project.projectId).set(project).await()
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
    val member = Member(userId = userId, role = role)
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection("members")
        .document(userId)
        .set(member)
        .await()
  }

  override suspend fun removeMember(projectId: String, userId: String): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection("members")
        .document(userId)
        .delete()
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

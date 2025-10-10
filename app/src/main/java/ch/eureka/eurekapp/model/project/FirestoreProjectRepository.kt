package ch.eureka.eurekapp.model.project

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProjectRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ProjectRepository {

  override fun getProjectById(
      workspaceId: String,
      groupId: String,
      projectId: String
  ): Flow<Project?> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .document(groupId)
            .collection("projects")
            .document(projectId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              trySend(snapshot?.toObject(Project::class.java))
            }
    awaitClose { listener.remove() }
  }

  override fun getProjectsInGroup(workspaceId: String, groupId: String): Flow<List<Project>> =
      callbackFlow {
        val listener =
            firestore
                .collection("workspaces")
                .document(workspaceId)
                .collection("groups")
                .document(groupId)
                .collection("projects")
                .addSnapshotListener { snapshot, error ->
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

  override suspend fun createProject(project: Project): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(project.workspaceId)
        .collection("groups")
        .document(project.groupId)
        .collection("projects")
        .document(project.projectId)
        .set(project)
        .await()
    project.projectId
  }

  override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(project.workspaceId)
        .collection("groups")
        .document(project.groupId)
        .collection("projects")
        .document(project.projectId)
        .set(project)
        .await()
  }

  override suspend fun deleteProject(
      workspaceId: String,
      groupId: String,
      projectId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .collection("projects")
        .document(projectId)
        .delete()
        .await()
  }
}

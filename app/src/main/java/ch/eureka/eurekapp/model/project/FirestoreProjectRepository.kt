package ch.eureka.eurekapp.model.project

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProjectRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth
) : ProjectRepository {

  override fun getProjectById(
      workspaceId: String,
      groupId: String,
      projectId: String
  ): Flow<Project?> = callbackFlow {
    val listener =
        firestore
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

  override suspend fun createProject(project: Project): Result<String> = runCatching {
    firestore
        .collection("projects")
        .document(project.projectId)
        .set(project)
        .await()
    project.projectId
  }

  override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
    firestore
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
        .collection("projects")
        .document(projectId)
        .delete()
        .await()
  }

    override suspend fun getNewProjectId(): Result<String> = runCatching {
        firestore.collection("projects").document().id
    }
}

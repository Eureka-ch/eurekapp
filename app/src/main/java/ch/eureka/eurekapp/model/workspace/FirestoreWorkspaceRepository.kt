package ch.eureka.eurekapp.model.workspace

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreWorkspaceRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WorkspaceRepository {

  override fun getWorkspaceById(workspaceId: String): Flow<Workspace?> = callbackFlow {
    val listener =
        firestore.collection("workspaces").document(workspaceId).addSnapshotListener {
            snapshot,
            error ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          trySend(snapshot?.toObject(Workspace::class.java))
        }
    awaitClose { listener.remove() }
  }

  override fun getWorkspacesForCurrentUser(): Flow<List<Workspace>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore.collection("workspaces").addSnapshotListener { snapshot, error ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          val workspaces =
              snapshot
                  ?.documents
                  ?.mapNotNull { it.toObject(Workspace::class.java) }
                  ?.filter { it.members.containsKey(currentUserId) } ?: emptyList()
          trySend(workspaces)
        }
    awaitClose { listener.remove() }
  }

  override suspend fun createWorkspace(workspace: Workspace): Result<String> = runCatching {
    firestore.collection("workspaces").document(workspace.workspaceId).set(workspace).await()
    workspace.workspaceId
  }

  override suspend fun updateWorkspace(workspace: Workspace): Result<Unit> = runCatching {
    firestore.collection("workspaces").document(workspace.workspaceId).set(workspace).await()
  }

  override suspend fun deleteWorkspace(workspaceId: String): Result<Unit> = runCatching {
    firestore.collection("workspaces").document(workspaceId).delete().await()
  }

  override suspend fun addMember(workspaceId: String, userId: String, role: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .update("members.$userId", role)
            .await()
      }

  override suspend fun removeMember(workspaceId: String, userId: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .update("members.$userId", FieldValue.delete())
            .await()
      }

  override suspend fun updateMemberRole(
      workspaceId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    firestore.collection("workspaces").document(workspaceId).update("members.$userId", role).await()
  }
}

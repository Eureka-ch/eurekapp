package ch.eureka.eurekapp.model.group

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreGroupRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : GroupRepository {

  override fun getGroupById(workspaceId: String, groupId: String): Flow<Group?> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              trySend(snapshot?.toObject(Group::class.java))
            }
    awaitClose { listener.remove() }
  }

  override fun getGroupsInWorkspace(workspaceId: String): Flow<List<Group>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val groups =
                  snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) } ?: emptyList()
              trySend(groups)
            }
    awaitClose { listener.remove() }
  }

  override fun getGroupsForCurrentUser(workspaceId: String): Flow<List<Group>> = callbackFlow {
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
            .collection("groups")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val groups =
                  snapshot
                      ?.documents
                      ?.mapNotNull { it.toObject(Group::class.java) }
                      ?.filter { it.members.containsKey(currentUserId) } ?: emptyList()
              trySend(groups)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createGroup(group: Group): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(group.workspaceId)
        .collection("groups")
        .document(group.groupID)
        .set(group)
        .await()
    group.groupID
  }

  override suspend fun updateGroup(group: Group): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(group.workspaceId)
        .collection("groups")
        .document(group.groupID)
        .set(group)
        .await()
  }

  override suspend fun deleteGroup(workspaceId: String, groupId: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .document(groupId)
            .delete()
            .await()
      }

  override suspend fun addMember(
      workspaceId: String,
      groupId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .update("members.$userId", role)
        .await()
  }

  override suspend fun removeMember(
      workspaceId: String,
      groupId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .update("members.$userId", FieldValue.delete())
        .await()
  }

  override suspend fun updateMemberRole(
      workspaceId: String,
      groupId: String,
      userId: String,
      role: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .update("members.$userId", role)
        .await()
  }
}

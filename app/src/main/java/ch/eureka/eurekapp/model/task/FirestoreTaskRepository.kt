package ch.eureka.eurekapp.model.task

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTaskRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskRepository {

  override fun getTaskById(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String
  ): Flow<Task?> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .document(groupId)
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              trySend(snapshot?.toObject(Task::class.java))
            }
    awaitClose { listener.remove() }
  }

  override fun getTasksInProject(
      workspaceId: String,
      groupId: String,
      projectId: String
  ): Flow<List<Task>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("groups")
            .document(groupId)
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val tasks =
                  snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList()
              trySend(tasks)
            }
    awaitClose { listener.remove() }
  }

  override fun getTasksForCurrentUser(): Flow<List<Task>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collectionGroup("tasks")
            .whereArrayContains("assignedUserIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val tasks =
                  snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList()
              trySend(tasks)
            }
    awaitClose { listener.remove() }
  }

  override fun getTasksForCurrentUserInWorkspace(workspaceId: String): Flow<List<Task>> =
      callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
          trySend(emptyList())
          close()
          return@callbackFlow
        }

        val listener =
            firestore
                .collectionGroup("tasks")
                .whereEqualTo("workspaceId", workspaceId)
                .whereArrayContains("assignedUserIds", currentUserId)
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  val tasks =
                      snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) }
                          ?: emptyList()
                  trySend(tasks)
                }
        awaitClose { listener.remove() }
      }

  override suspend fun createTask(task: Task): Result<String> = runCatching {
    firestore
        .collection("workspaces")
        .document(task.workspaceId)
        .collection("groups")
        .document(task.groupId)
        .collection("projects")
        .document(task.projectId)
        .collection("tasks")
        .document(task.taskID)
        .set(task)
        .await()
    task.taskID
  }

  override suspend fun updateTask(task: Task): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(task.workspaceId)
        .collection("groups")
        .document(task.groupId)
        .collection("projects")
        .document(task.projectId)
        .collection("tasks")
        .document(task.taskID)
        .set(task)
        .await()
  }

  override suspend fun deleteTask(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .collection("projects")
        .document(projectId)
        .collection("tasks")
        .document(taskId)
        .delete()
        .await()
  }

  override suspend fun assignUser(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .collection("projects")
        .document(projectId)
        .collection("tasks")
        .document(taskId)
        .update("assignedUserIds", FieldValue.arrayUnion(userId))
        .await()
  }

  override suspend fun unassignUser(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(workspaceId)
        .collection("groups")
        .document(groupId)
        .collection("projects")
        .document(projectId)
        .collection("tasks")
        .document(taskId)
        .update("assignedUserIds", FieldValue.arrayRemove(userId))
        .await()
  }
}

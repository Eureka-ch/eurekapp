package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.template.field.serialization.FirestoreConverters
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

  override fun getTaskById(projectId: String, taskId: String): Flow<Task?> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .document(taskId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val task = snapshot?.data?.let { FirestoreConverters.mapToTask(it) }
              trySend(task)
            }
    awaitClose { listener.remove() }
  }

  override fun getTasksInProject(projectId: String): Flow<List<Task>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val tasks =
                  snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { FirestoreConverters.mapToTask(it) }
                  } ?: emptyList()
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
            .collectionGroup(FirestorePaths.TASKS)
            .whereArrayContains("assignedUserIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                trySend(emptyList())
                return@addSnapshotListener
              }
              val tasks =
                  snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { FirestoreConverters.mapToTask(it) }
                  } ?: emptyList()
              trySend(tasks)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createTask(task: Task): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(task.projectId)
        .collection(FirestorePaths.TASKS)
        .document(task.taskID)
        .set(FirestoreConverters.taskToMap(task))
        .await()
    task.taskID
  }

  override suspend fun updateTask(task: Task): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(task.projectId)
        .collection(FirestorePaths.TASKS)
        .document(task.taskID)
        .set(FirestoreConverters.taskToMap(task))
        .await()
  }

  override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.TASKS)
        .document(taskId)
        .delete()
        .await()
  }

  override suspend fun assignUser(projectId: String, taskId: String, userId: String): Result<Unit> =
      runCatching {
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .document(taskId)
            .update("assignedUserIds", FieldValue.arrayUnion(userId))
            .await()
      }

  override suspend fun unassignUser(
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.TASKS)
        .document(taskId)
        .update("assignedUserIds", FieldValue.arrayRemove(userId))
        .await()
  }
}

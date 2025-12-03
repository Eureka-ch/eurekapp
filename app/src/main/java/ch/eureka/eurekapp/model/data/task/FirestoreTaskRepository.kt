package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.ActivityLogger
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
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
              val task = parseSnapshot(snapshot?.data)
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
                  snapshot?.documents?.mapNotNull { doc -> parseSnapshot(doc.data) } ?: emptyList()
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
                  snapshot?.documents?.mapNotNull { doc -> parseSnapshot(doc.data) } ?: emptyList()
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

    // Log creation activity
    ActivityLogger.logActivity(
        projectId = task.projectId,
        activityType = ActivityType.CREATED,
        entityType = EntityType.TASK,
        entityId = task.taskID,
        userId = task.createdBy,
        metadata = mapOf("title" to task.title, "status" to task.status.name))

    task.taskID
  }

  override suspend fun updateTask(task: Task): Result<Unit> = runCatching {
    // Fetch old task to detect status changes
    val oldTaskDoc =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(task.projectId)
            .collection(FirestorePaths.TASKS)
            .document(task.taskID)
            .get()
            .await()
    val oldTask = parseSnapshot(oldTaskDoc.data)

    // Perform the update
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(task.projectId)
        .collection(FirestorePaths.TASKS)
        .document(task.taskID)
        .set(FirestoreConverters.taskToMap(task))
        .await()

    // Determine activity type and metadata based on what changed
    val currentUserId = auth.currentUser?.uid

    // Log if currentUserId is null
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreTaskRepository", "Cannot log activity for task update: currentUser is null")
    }

    // Log if oldTask is null
    if (oldTask == null) {
      android.util.Log.e(
          "FirestoreTaskRepository",
          "Failed to fetch old task for status change detection: projectId=${task.projectId}, taskId=${task.taskID}")
    }

    if (currentUserId != null && oldTask != null) {
      if (oldTask.status != task.status) {
        // Status changed - use STATUS_CHANGED
        ActivityLogger.logActivity(
            projectId = task.projectId,
            activityType = ActivityType.STATUS_CHANGED,
            entityType = EntityType.TASK,
            entityId = task.taskID,
            userId = currentUserId,
            metadata =
                mapOf(
                    "title" to task.title,
                    "oldStatus" to oldTask.status.name,
                    "newStatus" to task.status.name))
      } else {
        // Other fields changed - use UPDATED
        ActivityLogger.logActivity(
            projectId = task.projectId,
            activityType = ActivityType.UPDATED,
            entityType = EntityType.TASK,
            entityId = task.taskID,
            userId = currentUserId,
            metadata = mapOf("title" to task.title))
      }
    }
  }

  override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> = runCatching {
    // Fetch task for metadata before deletion
    val taskDoc =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .document(taskId)
            .get()
            .await()
    val task = parseSnapshot(taskDoc.data)

    // Validate task data
    if (task == null) {
      throw IllegalArgumentException(
          "Task is malformed or not found: projectId=$projectId, taskId=$taskId")
    }

    // Perform deletion
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.TASKS)
        .document(taskId)
        .delete()
        .await()

    // Log deletion activity
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreTaskRepository", "Cannot log activity for task deletion: currentUser is null")
    } else {
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.DELETED,
          entityType = EntityType.TASK,
          entityId = taskId,
          userId = currentUserId,
          metadata = mapOf("title" to task.title))
    }
  }

  override suspend fun assignUser(projectId: String, taskId: String, userId: String): Result<Unit> =
      runCatching {
        // Fetch task for metadata
        val taskDoc =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.TASKS)
                .document(taskId)
                .get()
                .await()
        val task = parseSnapshot(taskDoc.data)

        // Validate task data
        if (task == null) {
          throw IllegalArgumentException(
              "Task is malformed or not found: projectId=$projectId, taskId=$taskId")
        }

        // Perform assignment
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .document(taskId)
            .update("assignedUserIds", FieldValue.arrayUnion(userId))
            .await()

        // Log assignment activity
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
          android.util.Log.e(
              "FirestoreTaskRepository",
              "Cannot log activity for task assignment: currentUser is null")
        } else {
          ActivityLogger.logActivity(
              projectId = projectId,
              activityType = ActivityType.ASSIGNED,
              entityType = EntityType.TASK,
              entityId = taskId,
              userId = currentUserId,
              metadata = mapOf("title" to task.title, "assigneeId" to userId))
        }
      }

  override suspend fun unassignUser(
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = runCatching {
    // Fetch task for metadata
    val taskDoc =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .document(taskId)
            .get()
            .await()
    val task = parseSnapshot(taskDoc.data)

    // Validate task data
    if (task == null) {
      throw IllegalArgumentException(
          "Task is malformed or not found: projectId=$projectId, taskId=$taskId")
    }

    // Perform unassignment
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.TASKS)
        .document(taskId)
        .update("assignedUserIds", FieldValue.arrayRemove(userId))
        .await()

    // Log unassignment activity
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      android.util.Log.e(
          "FirestoreTaskRepository",
          "Cannot log activity for task unassignment: currentUser is null")
    } else {
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.UNASSIGNED,
          entityType = EntityType.TASK,
          entityId = taskId,
          userId = currentUserId,
          metadata = mapOf("title" to task.title, "assigneeId" to userId))
    }
  }

  private fun parseSnapshot(data: Map<String, Any>?): Task? {
    return try {
      data?.let { FirestoreConverters.mapToTask(it) }
    } catch (e: Exception) {
      null
    }
  }
}

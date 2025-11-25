/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.data.activity

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/** Firestore implementation with one-time activity fetching. */
class FirestoreActivityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ActivityRepository {

  private fun Query.asActivityFlow(): Flow<List<Activity>> = flow {
    val snapshot = get().await()
    val activities = snapshot.documents.mapNotNull { doc ->
      doc.toObject(Activity::class.java)?.copy(activityId = doc.id)
    }
    emit(activities)
  }

  override fun getActivitiesInProject(projectId: String, limit: Int): Flow<List<Activity>> =
      firestore.collection(FirestorePaths.activitiesPath(projectId))
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(limit.toLong())
          .asActivityFlow()

  override fun getActivitiesByEntityType(projectId: String, entityType: EntityType, limit: Int): Flow<List<Activity>> =
      firestore.collection(FirestorePaths.activitiesPath(projectId))
          .whereEqualTo("entityType", entityType.name)
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(limit.toLong())
          .asActivityFlow()

  override fun getActivitiesByUser(projectId: String, userId: String, limit: Int): Flow<List<Activity>> =
      firestore.collection(FirestorePaths.activitiesPath(projectId))
          .whereEqualTo("userId", userId)
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(limit.toLong())
          .asActivityFlow()

  override fun getActivitiesByActivityType(projectId: String, activityType: ActivityType, limit: Int): Flow<List<Activity>> =
      firestore.collection(FirestorePaths.activitiesPath(projectId))
          .whereEqualTo("activityType", activityType.name)
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(limit.toLong())
          .asActivityFlow()

  override fun getActivitiesForEntity(projectId: String, entityId: String): Flow<List<Activity>> =
      firestore.collection(FirestorePaths.activitiesPath(projectId))
          .whereEqualTo("entityId", entityId)
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .asActivityFlow()

  override suspend fun createActivity(activity: Activity): Result<String> = runCatching {
    val activityRef = firestore.collection(FirestorePaths.activitiesPath(activity.projectId)).document()
    val activityWithId = activity.copy(activityId = activityRef.id)
    activityRef.set(activityWithId).await()
    activityRef.id
  }

  override suspend fun deleteActivity(projectId: String, activityId: String): Result<Unit> =
      runCatching {
        firestore
            .collection(FirestorePaths.activitiesPath(projectId))
            .document(activityId)
            .delete()
            .await()
      }
}

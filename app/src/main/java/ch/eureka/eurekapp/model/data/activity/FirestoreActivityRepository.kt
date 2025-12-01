/*
 * This file was co-authored by Claude Code and Gemini.
 */
package ch.eureka.eurekapp.model.data.activity

import android.util.Log
import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of the [ActivityRepository].
 *
 * This class handles the interaction with the Firestore database to retrieve, create, and delete
 * activities. It implements a two-step query process to fetch the "federated" activity feed for a
 * user.
 *
 * @property firestore The [FirebaseFirestore] instance to use for database operations.
 */
class FirestoreActivityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ActivityRepository {

  /**
   * Helper function to get the reference to the top-level activities collection.
   *
   * @return The [com.google.firebase.firestore.CollectionReference] for the activities collection.
   */
  private fun activitiesCollection() = firestore.collection(FirestorePaths.activitiesPath())

  /**
   * Fetches all activities relevant to the user by finding their project memberships.
   *
   * The process is:
   * 1. Query the `members` collection group to find all projects where [userId] is a member.
   * 2. Add the `test-project-id` to this list to ensure visibility of legacy meeting data.
   * 3. Query the top-level `activities` collection where `projectId` is in the user's project list.
   *
   * @param userId The unique identifier of the user.
   * @return A [Flow] emitting the list of relevant activities.
   */
  override fun getActivities(userId: String): Flow<List<Activity>> = flow {
    try {
      // find all projects the user is a member of
      val memberDocs =
          firestore.collectionGroup("members").whereEqualTo("userId", userId).get().await()

      val userProjectIds =
          memberDocs.documents.mapNotNull { doc -> doc.reference.parent.parent?.id }

      val targetIds = mutableSetOf<String>()
      targetIds.add("test-project-id") // for fetching all meetings activity
      targetIds.addAll(userProjectIds)

      if (targetIds.isEmpty()) {
        emit(emptyList())
      } else {
        val safeIds = targetIds.take(30).toList()

        // 3. Query the Top-Level Collection
        val snapshot =
            activitiesCollection()
                .whereIn("projectId", safeIds)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

        val activities =
            snapshot.documents.mapNotNull { doc ->
              doc.toObject(Activity::class.java)?.copy(activityId = doc.id)
            }
        emit(activities)
      }
    } catch (e: Exception) {
      Log.e("FirestoreActivityRepository", e.message ?: "No message in error.")
      emit(emptyList())
    }
  }

  /**
   * Fetches a specific activity by its ID.
   *
   * @param activityId The unique identifier of the activity to fetch.
   * @return The [Activity] object if found, or null otherwise.
   */
  override suspend fun getActivityById(activityId: String): Activity? = try {
    val doc = activitiesCollection().document(activityId).get().await()
    doc.toObject(Activity::class.java)?.copy(activityId = doc.id)
  } catch (e: Exception) {
    Log.e("FirestoreActivityRepository", "Error fetching activity: ${e.message}")
    null
  }

  /**
   * Creates a new activity in the top-level collection.
   *
   * @param activity The activity object to store.
   * @return A [Result] with the new document ID.
   */
  override suspend fun createActivity(activity: Activity): Result<String> = runCatching {
    val activityRef = activitiesCollection().document()
    val activityWithId = activity.copy(activityId = activityRef.id)
    activityRef.set(activityWithId).await()
    activityRef.id
  }

  /**
   * Deletes an activity from the top-level collection.
   *
   * @param activityId The ID of the activity to delete.
   * @return A [Result] indicating success or failure.
   */
  override suspend fun deleteActivity(activityId: String): Result<Unit> = runCatching {
    activitiesCollection().document(activityId).delete().await()
  }
}

package ch.eureka.eurekapp.model.data.activity

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

class FirestoreActivityRepositoryTest : FirestoreRepositoryTest() {
  private lateinit var repository: FirestoreActivityRepository

  override fun getCollectionPaths(): List<String> {
    return listOf("activities")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository = FirestoreActivityRepository(FirebaseEmulator.firestore)
  }

  @Test
  fun getActivityById_found_returnsActivity() = runBlocking {
    val activity =
        Activity(
            activityId = "activity-1",
            userId = testUserId,
            projectId = "test-project-id",
            entityType = EntityType.TASK,
            entityId = "task-1",
            activityType = ActivityType.CREATED,
            timestamp = Timestamp.now())

    FirebaseEmulator.firestore.collection("activities").document("activity-1").set(activity).await()

    val result = repository.getActivityById("activity-1")
    assertNotNull(result)
    assertEquals("activity-1", result?.activityId)
  }

  @Test
  fun getActivityById_notFound_returnsNull() = runBlocking {
    val result = repository.getActivityById("missing")
    assertNull(result)
  }

  @Test
  fun createActivity_returnsId() = runBlocking {
    val activity =
        Activity(
            userId = testUserId,
            projectId = "test-project-id",
            entityType = EntityType.TASK,
            entityId = "task-1",
            activityType = ActivityType.CREATED,
            timestamp = Timestamp.now())
    val result = repository.createActivity(activity)
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull())
  }

  @Test
  fun deleteActivity_succeeds() = runBlocking {
    val activity =
        Activity(
            activityId = "activity-2",
            userId = testUserId,
            projectId = "test-project-id",
            entityType = EntityType.TASK,
            entityId = "task-1",
            activityType = ActivityType.CREATED,
            timestamp = Timestamp.now())

    FirebaseEmulator.firestore.collection("activities").document("activity-2").set(activity).await()

    val result = repository.deleteActivity("activity-2")
    assertTrue(result.isSuccess)

    val deleted = repository.getActivityById("activity-2")
    assertNull(deleted)
  }
}

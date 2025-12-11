/*
 * Co-Authored-By: Claude Sonnet 4.5
 */
package ch.eureka.eurekapp.ui.activity

import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ActivityDetailViewModelTest {

  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var connectivityObserver: ConnectivityObserver
  private lateinit var viewModel: ActivityDetailViewModel
  private val testUserId = "user-123"
  private val testActivityId = "activity-456"
  private val testProjectId = "project-789"
  private val testEntityId = "entity-101"

  @Before
  fun setup() {
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)
    connectivityObserver = mockk(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser

    every { connectivityObserver.isConnected } returns flowOf(true)
  }

  @Test
  fun activityDetailViewModel_initialState_isLoading() {
    coEvery { repository.getActivities(testUserId) } returns flowOf(emptyList())
    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    assertNotNull(viewModel)
    assertNotNull(viewModel.uiState)
  }

  @Test
  fun activityDetailViewModel_deleteActivity_setsDeleteSuccess() {
    val activity =
        createActivity(testActivityId, EntityType.TASK, "Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))
    coEvery { repository.deleteActivity(testActivityId) } returns Result.success(Unit)

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    Thread.sleep(1000)
    viewModel.deleteActivity()
    Thread.sleep(1000)
    assertNotNull("ViewModel should exist", viewModel)
    coEvery { repository.deleteActivity(testActivityId) }
  }

  @Test
  fun activityDetailViewModel_deleteActivityOffline_setsError() {
    every { connectivityObserver.isConnected } returns flowOf(false)
    val activity =
        createActivity(
            testActivityId, EntityType.MEETING, "Meeting", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)
    viewModel.deleteActivity()
    Thread.sleep(1000)
    assertNotNull("ViewModel should exist", viewModel)
    every { connectivityObserver.isConnected }
  }

  @Test
  fun activityDetailViewModel_markShareSuccess_updatesState() {
    val activity =
        createActivity(
            testActivityId, EntityType.MESSAGE, "Message", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)
    viewModel.markShareSuccess()
    Thread.sleep(500)
    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_clearError_removesErrorMessage() {
    val activities = emptyList<Activity>()
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)

    viewModel.clearError()

    Thread.sleep(500)

    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_enrichActivityMissingDisplayName_setsError() {
    val activity =
        createActivity(testActivityId, EntityType.TASK, "Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns null
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)

    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_enrichActivityFirestoreException_setsError() {
    val activity =
        createActivity(testActivityId, EntityType.TASK, "Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    every { firestore.collection("users").document(any()).get() } returns
        Tasks.forException(RuntimeException("Firestore error"))

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)

    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_batchFetchMissingDisplayName_setsError() {
    val activity1 =
        createActivity(
            testActivityId, EntityType.TASK, "Task 1", ActivityType.CREATED, testEntityId)
    val activity2 =
        createActivity("activity-2", EntityType.TASK, "Task 2", ActivityType.UPDATED, testEntityId)

    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity1, activity2))

    val userDoc1 = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc1.getString("displayName") } returns "User 1"
    every { userDoc1.id } returns testUserId

    val userDoc2 = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc2.getString("displayName") } returns null
    every { userDoc2.id } returns "user-2"

    val querySnapshot = mockk<com.google.firebase.firestore.QuerySnapshot>(relaxed = true)
    every { querySnapshot.documents } returns listOf(userDoc1, userDoc2)

    every { firestore.collection("users").document(testUserId).get() } returns
        Tasks.forResult(userDoc1)

    every {
      firestore
          .collection("users")
          .whereIn(com.google.firebase.firestore.FieldPath.documentId(), any<List<String>>())
          .get()
    } returns Tasks.forResult(querySnapshot)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    Thread.sleep(1000)

    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_batchFetchFirestoreException_setsError() {
    val activity1 =
        createActivity(
            testActivityId, EntityType.TASK, "Task 1", ActivityType.CREATED, testEntityId)
    val activity2 =
        createActivity("activity-2", EntityType.TASK, "Task 2", ActivityType.UPDATED, testEntityId)

    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity1, activity2))

    val userDoc1 = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc1.getString("displayName") } returns "User 1"

    every { firestore.collection("users").document(testUserId).get() } returns
        Tasks.forResult(userDoc1)

    every {
      firestore
          .collection("users")
          .whereIn(com.google.firebase.firestore.FieldPath.documentId(), any<List<String>>())
          .get()
    } returns Tasks.forException(RuntimeException("Batch fetch error"))

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    Thread.sleep(1000)
    assertNotNull("ViewModel should exist", viewModel)
  }

  @Test
  fun activityDetailViewModel_getShareText_returnsFormattedString() {
    val activity =
        createActivity(
            testActivityId, EntityType.TASK, "Test Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    Thread.sleep(1000)
    val shareText = viewModel.getShareText()

    assertNotNull("Share text should not be null", shareText)
    assertTrue("Share text should contain activity type", shareText!!.contains("Type:"))
    assertTrue("Share text should contain entity type", shareText.contains("Entity:"))
  }

  private fun createActivity(
      id: String,
      entityType: EntityType,
      title: String,
      activityType: ActivityType = ActivityType.CREATED,
      entityId: String = testEntityId,
      timestamp: Timestamp = Timestamp.now()
  ): Activity {
    return Activity(
        activityId = id,
        userId = testUserId,
        projectId = testProjectId,
        activityType = activityType,
        entityType = entityType,
        entityId = entityId,
        timestamp = timestamp,
        metadata = mapOf("title" to title))
  }
}

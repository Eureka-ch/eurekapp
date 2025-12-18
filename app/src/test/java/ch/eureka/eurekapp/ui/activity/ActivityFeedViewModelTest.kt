/*
 * This file was co-authored by Claude Code.
 * This code was written with help of Claude.
 */
package ch.eureka.eurekapp.ui.activity

import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityFeedViewModelTest {

  private lateinit var viewModel: ActivityFeedViewModel
  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var testDispatcher: TestDispatcher

  private val testUserId = "test-user-123"

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)

    // Mock Firebase auth
    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser

    // Mock Firestore user fetches using Tasks.forResult for immediate completion
    val userDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { firestore.collection("users") } returns usersCollection
    every { usersCollection.document(any()) } returns userDocRef
    every { userDocRef.get() } returns Tasks.forResult(userDoc)

    // Mock Firestore conversation access (MESSAGE activity filtering)
    val conversationDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    every { conversationDoc.get("memberIds") } returns listOf(testUserId)
    val conversationsCollection =
        mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val conversationDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { firestore.collection("conversations") } returns conversationsCollection
    every { conversationsCollection.document(any()) } returns conversationDocRef
    every { conversationDocRef.get() } returns Tasks.forResult(conversationDoc)

    // Mock Firestore meeting access (MEETING activity filtering)
    val meetingDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    every { meetingDoc.get("participantIds") } returns listOf(testUserId)
    val meetingsCollection =
        mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val meetingDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { firestore.collection("meetings") } returns meetingsCollection
    every { meetingsCollection.document(any()) } returns meetingDocRef
    every { meetingDocRef.get() } returns Tasks.forResult(meetingDoc)

    viewModel = ActivityFeedViewModel(repository = repository, firestore = firestore, auth = auth)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun initialState_hasCorrectDefaults() {
    val state = viewModel.uiState.value

    assertTrue(state.activities.isEmpty())
    assertTrue(state.allActivities.isEmpty())
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
    assertNull(state.filterEntityType)
    assertNull(state.filterActivityType)
    assertFalse(state.isCompactMode)
  }

  @Test
  fun loadActivities_fetchesAndEnrichesActivitiesWithUserNames() = runTest {
    val activities =
        listOf(createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.MEETING))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    viewModel.loadActivities()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.activities.size)
    assertEquals(2, state.allActivities.size)
    assertEquals("Test User", state.activities[0].metadata["userName"])
    assertEquals("Test User", state.activities[1].metadata["userName"])
  }

  @Test
  fun loadActivities_handlesRepositoryErrors() = runTest {
    coEvery { repository.getActivities(testUserId) } returns
        kotlinx.coroutines.flow.flow { throw Exception("Network error") }

    viewModel.loadActivities()
    advanceUntilIdle()

    assertEquals("Network error", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun loadActivities_handlesEnrichmentErrorsGracefully() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    // Mock Firestore to throw error
    every { firestore.collection("users").document(any()).get() } returns
        Tasks.forException(Exception("Firestore error"))

    viewModel.loadActivities()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Someone", state.activities[0].metadata["userName"])
  }

  @Test
  fun applyFilter_includesBothProjectAndMemberActivitiesWithProject() = runTest {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT),
            createActivity("2", EntityType.MEMBER),
            createActivity("3", EntityType.MEETING),
            createActivity("4", EntityType.FILE))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(EntityType.PROJECT, state.filterEntityType)
    assertEquals(2, state.activities.size) // Only PROJECT + MEMBER
    assertTrue(
        state.activities.all {
          it.entityType == EntityType.PROJECT || it.entityType == EntityType.MEMBER
        })
  }

  @Test
  fun applyFilter_includesOnlyMeetingActivitiesWithMeeting() = runTest {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT),
            createActivity("2", EntityType.MEETING),
            createActivity("3", EntityType.MEETING))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    viewModel.applyEntityTypeFilter(EntityType.MEETING)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(EntityType.MEETING, state.filterEntityType)
    assertEquals(2, state.activities.size)
    assertTrue(state.activities.all { it.entityType == EntityType.MEETING })
  }

  @Test
  fun applyFilter_includesOnlyFileActivitiesWithFile() = runTest {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT),
            createActivity("2", EntityType.FILE),
            createActivity("3", EntityType.MEETING))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    viewModel.applyEntityTypeFilter(EntityType.FILE)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(EntityType.FILE, state.filterEntityType)
    assertEquals(1, state.activities.size)
    assertEquals(EntityType.FILE, state.activities[0].entityType)
  }

  @Test
  fun clearFilters_showsEmptyState() = runTest {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT),
            createActivity("2", EntityType.MEETING),
            createActivity("3", EntityType.FILE))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    // First apply a filter
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()
    assertEquals(1, viewModel.uiState.value.activities.size)

    // Then clear filters
    viewModel.clearFilters()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.filterEntityType)
    assertEquals(0, state.activities.size) // Empty state when no filter selected
    assertEquals(3, state.allActivities.size) // Cache still has all activities
  }

  @Test
  fun clearFilters_resultsInEmptyDisplayedActivities() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    // Load activities first
    viewModel.loadActivities()
    advanceUntilIdle()

    // Apply a filter to show activities
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()
    assertEquals(1, viewModel.uiState.value.activities.size)

    // Clear filters - should show empty state
    viewModel.clearFilters()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.filterEntityType)
    assertEquals(0, state.activities.size) // No activities displayed when no filter
    assertEquals(1, state.allActivities.size) // Cache still has loaded data
  }

  @Test
  fun setCompactMode_updatesStateCorrectly() {
    assertFalse(viewModel.uiState.value.isCompactMode)

    viewModel.setCompactMode(true)
    assertTrue(viewModel.uiState.value.isCompactMode)

    viewModel.setCompactMode(false)
    assertFalse(viewModel.uiState.value.isCompactMode)
  }

  @Test
  fun clearErrorMsg_clearsErrorState() {
    // Manually set error through failed load
    coEvery { repository.getActivities(testUserId) } returns
        kotlinx.coroutines.flow.flow { throw Exception("Test error") }

    runTest {
      viewModel.loadActivities()
      advanceUntilIdle()
      assertEquals("Test error", viewModel.uiState.value.errorMsg)

      viewModel.clearErrorMsg()
      assertNull(viewModel.uiState.value.errorMsg)
    }
  }

  @Test
  fun deleteActivity_removesActivityFromList() = runTest {
    val activitiesFlow =
        MutableStateFlow(
            listOf(
                createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.MEETING)))
    coEvery { repository.getActivities(testUserId) } returns activitiesFlow
    coEvery { repository.deleteActivity("1") } coAnswers
        {
          activitiesFlow.value =
              activitiesFlow.value.filter { activity -> activity.activityId != "1" }
          Result.success(Unit)
        }

    viewModel.loadActivities()
    advanceUntilIdle()
    assertEquals(2, viewModel.uiState.value.activities.size)

    viewModel.deleteActivity("1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.activities.size)
    assertEquals(1, state.allActivities.size)
    assertEquals("2", state.activities[0].activityId)
  }

  @Test
  fun deleteActivity_rollsBackOnFailure() = runTest {
    val activities =
        listOf(createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.MEETING))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    coEvery { repository.deleteActivity("1") } returns Result.failure(Exception("Delete failed"))

    viewModel.loadActivities()
    advanceUntilIdle()
    assertEquals(2, viewModel.uiState.value.activities.size)

    viewModel.deleteActivity("1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.activities.size) // Rolled back
    assertEquals(2, state.allActivities.size)
    assertEquals("Delete failed", state.errorMsg)
  }

  @Test
  fun deleteActivity_removesFromFilteredViewCorrectly() = runTest {
    val activitiesFlow =
        MutableStateFlow(
            listOf(
                createActivity("1", EntityType.PROJECT),
                createActivity("2", EntityType.PROJECT),
                createActivity("3", EntityType.MEETING)))
    coEvery { repository.getActivities(testUserId) } returns activitiesFlow
    coEvery { repository.deleteActivity("1") } coAnswers
        {
          activitiesFlow.value =
              activitiesFlow.value.filter { activity -> activity.activityId != "1" }
          Result.success(Unit)
        }

    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()
    assertEquals(2, viewModel.uiState.value.activities.size)

    viewModel.deleteActivity("1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.activities.size) // Filtered view updated
    assertEquals(2, state.allActivities.size) // Cache updated
  }

  @Test
  fun loadActivities_returnsEarlyIfNoCurrentUser() = runTest {
    every { auth.currentUser } returns null

    viewModel.loadActivities()
    advanceUntilIdle()

    coVerify(exactly = 0) { repository.getActivities(any()) }
  }

  @Test
  fun applyingFilter_loadsActivitiesOnEmptyCache() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    coVerify { repository.getActivities(testUserId) }
    assertEquals(1, viewModel.uiState.value.activities.size)
  }

  private fun createActivity(
      id: String,
      entityType: EntityType,
      activityType: ActivityType = ActivityType.CREATED
  ): Activity {
    val baseMetadata = mapOf("title" to "Test $entityType $id")
    val metadata =
        when (entityType) {
          EntityType.MESSAGE -> baseMetadata + ("conversationId" to "test-conversation-$id")
          else -> baseMetadata
        }

    return Activity(
        activityId = id,
        projectId = "test-project",
        activityType = activityType,
        entityType = entityType,
        entityId = "entity-$id",
        userId = testUserId,
        timestamp = Timestamp.now(),
        metadata = metadata)
  }
}

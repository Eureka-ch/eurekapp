/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  private lateinit var testDispatcher: TestDispatcher

  private val projectId = "global-activities"

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)

    // Mock Firestore user fetches using Tasks.forResult for immediate completion
    val userDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { firestore.collection("users") } returns usersCollection
    every { usersCollection.document(any()) } returns userDocRef
    every { userDocRef.get() } returns Tasks.forResult(userDoc)

    viewModel = ActivityFeedViewModel(repository = repository, firestore = firestore)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun `PROJECT filter includes both PROJECT and MEMBER activities`() = runTest {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT),
            createActivity("2", EntityType.MEMBER),
            createActivity("3", EntityType.MEETING))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    viewModel.loadActivitiesByEntityType(projectId, EntityType.PROJECT)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.activities.size) // Only PROJECT + MEMBER
    assertTrue(
        state.activities.all {
          it.entityType == EntityType.PROJECT || it.entityType == EntityType.MEMBER
        })
  }

  @Test
  fun `MEETING filter includes only MEETING activities`() = runTest {
    val activities =
        listOf(createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.MEETING))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    viewModel.loadActivitiesByEntityType(projectId, EntityType.MEETING)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.activities.size)
    assertEquals(EntityType.MEETING, state.activities[0].entityType)
  }

  @Test
  fun `deleteActivity removes activity and rolls back on failure`() = runTest {
    val activities =
        listOf(createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.MEETING))
    coEvery { repository.getActivitiesInProject(projectId, 20) } returns flowOf(activities)
    coEvery { repository.deleteActivity(projectId, "1") } returns
        Result.failure(Exception("Failed"))

    viewModel.loadActivities(projectId)
    advanceUntilIdle()
    viewModel.deleteActivity(projectId, "1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.activities.size) // Rolled back
  }

  @Test
  fun `enrichActivitiesWithUserNames adds userName to metadata`() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivitiesInProject(projectId, 20) } returns flowOf(activities)

    viewModel.loadActivities(projectId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Test User", state.activities[0].metadata["userName"])
  }

  @Test
  fun `clearFilters resets filter state and clears activities`() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivitiesInProject(projectId, any()) } returns flowOf(activities)

    viewModel.loadActivitiesByEntityType(projectId, EntityType.PROJECT)
    advanceUntilIdle()
    viewModel.clearFilters(projectId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(null, state.filterEntityType)
    assertEquals(null, state.filterActivityType)
    assertEquals(0, state.activities.size)
  }

  @Test
  fun `setCompactMode changes limit from 20 to 10`() = runTest {
    var state = viewModel.uiState.value
    assertEquals(20, state.limit)

    viewModel.setCompactMode(true)
    advanceUntilIdle()

    state = viewModel.uiState.value
    assertEquals(10, state.limit)
    assertTrue(state.isCompactMode)
  }

  @Test
  fun `refresh maintains active filters`() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)
    coEvery { repository.getActivitiesByActivityType(projectId, ActivityType.CREATED, 20) } returns
        flowOf(activities)

    viewModel.loadActivitiesByEntityType(projectId, EntityType.PROJECT)
    advanceUntilIdle()
    viewModel.refresh(projectId)
    advanceUntilIdle()
    assertEquals(EntityType.PROJECT, viewModel.uiState.value.filterEntityType)

    viewModel.loadActivitiesByActivityType(projectId, ActivityType.CREATED)
    advanceUntilIdle()
    viewModel.refresh(projectId)
    advanceUntilIdle()
    assertEquals(ActivityType.CREATED, viewModel.uiState.value.filterActivityType)
  }

  @Test
  fun `loadActivities handles repository errors`() = runTest {
    coEvery { repository.getActivitiesInProject(projectId, 20) } returns
        kotlinx.coroutines.flow.flow { throw Exception("Network error") }

    viewModel.loadActivities(projectId)
    advanceUntilIdle()
    assertEquals("Network error", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `deleteActivity succeeds and removes activity from list`() = runTest {
    val activities =
        listOf(createActivity("1", EntityType.PROJECT), createActivity("2", EntityType.PROJECT))
    coEvery { repository.getActivitiesInProject(projectId, 20) } returns flowOf(activities)
    coEvery { repository.deleteActivity(projectId, "1") } returns Result.success(Unit)

    viewModel.loadActivities(projectId)
    advanceUntilIdle()
    assertEquals(2, viewModel.uiState.value.activities.size)

    viewModel.deleteActivity(projectId, "1")
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(1, state.activities.size)
    assertEquals("2", state.activities[0].activityId)
  }

  @Test
  fun `error message state management works correctly`() = runTest {
    viewModel.setErrorMsg("Test error")
    advanceUntilIdle()
    assertEquals("Test error", viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    advanceUntilIdle()
    assertEquals(null, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `enrichActivitiesWithUserNames handles Firestore fetch failure`() = runTest {
    val activities = listOf(createActivity("1", EntityType.PROJECT))
    coEvery { repository.getActivitiesInProject(projectId, 20) } returns flowOf(activities)

    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    val newFirestore = mockk<com.google.firebase.firestore.FirebaseFirestore>(relaxed = true)
    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    every { newFirestore.collection("users") } returns usersCollection
    every { usersCollection.document(any()) } returns userDocRef
    every { userDocRef.get() } returns Tasks.forException(Exception("Firestore error"))

    val newViewModel = ActivityFeedViewModel(repository = repository, firestore = newFirestore)
    newViewModel.loadActivities(projectId)
    advanceUntilIdle()

    val state = newViewModel.uiState.value
    assertEquals("Someone", state.activities[0].metadata["userName"])
  }

  @Test
  fun `loadActivitiesByEntityType respects compact mode limit`() = runTest {
    val activities = (1..15).map { createActivity("$it", EntityType.PROJECT) }
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    viewModel.setCompactMode(true)
    advanceUntilIdle()
    viewModel.loadActivitiesByEntityType(projectId, EntityType.PROJECT)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(10, state.activities.size)
  }

  private fun createActivity(id: String, entityType: EntityType) =
      Activity(
          activityId = id,
          projectId = projectId,
          activityType = ActivityType.CREATED,
          entityType = entityType,
          entityId = "entity-$id",
          userId = "user-1",
          timestamp = Timestamp.now(),
          metadata = emptyMap())
}

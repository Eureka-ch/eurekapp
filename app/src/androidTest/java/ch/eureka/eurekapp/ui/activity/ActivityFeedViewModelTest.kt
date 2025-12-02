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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityFeedViewModelTest {

  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var viewModel: ActivityFeedViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val testUserId = "user-123"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    viewModel = ActivityFeedViewModel(repository, firestore, auth)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadActivities_success_updatesUIState() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A"),
            createActivity("2", EntityType.MEETING, "Meeting B"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    // Act
    viewModel.loadActivities()
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertEquals(2, state.allActivities.size)
    assertNull(state.errorMsg)
  }

  @Test
  fun applyEntityTypeFilter_projects_filtersCorrectly() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A"),
            createActivity("2", EntityType.MEETING, "Meeting B"),
            createActivity("3", EntityType.MEMBER, "Member C"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()

    // Act
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(2, state.activities.size) // PROJECT + MEMBER
    assertTrue(state.activities.any { it.entityType == EntityType.PROJECT })
    assertTrue(state.activities.any { it.entityType == EntityType.MEMBER })
    assertFalse(state.activities.any { it.entityType == EntityType.MEETING })
  }

  @Test
  fun applyEntityTypeFilter_meetings_filtersCorrectly() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A"),
            createActivity("2", EntityType.MEETING, "Meeting B"),
            createActivity("3", EntityType.MEETING, "Meeting C"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()

    // Act
    viewModel.applyEntityTypeFilter(EntityType.MEETING)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(2, state.activities.size)
    assertTrue(state.activities.all { it.entityType == EntityType.MEETING })
  }

  @Test
  fun applyActivityTypeFilter_created_filtersCorrectly() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A", ActivityType.CREATED),
            createActivity("2", EntityType.PROJECT, "Project B", ActivityType.UPDATED),
            createActivity("3", EntityType.PROJECT, "Project C", ActivityType.CREATED))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Act
    viewModel.applyActivityTypeFilter(ActivityType.CREATED)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(2, state.activities.size)
    assertTrue(state.activities.all { it.activityType == ActivityType.CREATED })
  }

  @Test
  fun applySearch_filtersByTitle() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Alpha Project"),
            createActivity("2", EntityType.PROJECT, "Beta Project"),
            createActivity("3", EntityType.PROJECT, "Gamma Project"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Act
    viewModel.applySearch("Alpha")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(1, state.activities.size)
    assertEquals("Alpha Project", state.activities[0].metadata["title"])
  }

  @Test
  fun applySearch_caseInsensitive() = runTest {
    // Arrange
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Important Project"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Act
    viewModel.applySearch("important")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(1, state.activities.size)
  }

  @Test
  fun clearFilters_resetsAllFilters() = runTest {
    // Arrange
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Project A"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    viewModel.applySearch("Project")
    advanceUntilIdle()

    // Act
    viewModel.clearFilters()
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertNull(state.filterEntityType)
    assertNull(state.filterActivityType)
    assertEquals("", state.searchQuery)
    assertTrue(state.activities.isEmpty())
  }

  @Test
  fun markAsRead_addsToReadSet() = runTest {
    // Act
    viewModel.markAsRead("activity-1")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertTrue(state.readActivityIds.contains("activity-1"))
  }

  @Test
  fun markAllAsRead_marksAllVisibleActivities() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A"),
            createActivity("2", EntityType.PROJECT, "Project B"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Act
    viewModel.markAllAsRead()
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(2, state.readActivityIds.size)
    assertTrue(state.readActivityIds.contains("1"))
    assertTrue(state.readActivityIds.contains("2"))
  }

  @Test
  fun toggleGroupByProject_togglesState() = runTest {
    // Act
    viewModel.toggleGroupByProject()
    advanceUntilIdle()

    // Assert
    var state = viewModel.uiState.first()
    assertTrue(state.groupByProject)

    // Act again
    viewModel.toggleGroupByProject()
    advanceUntilIdle()

    // Assert
    state = viewModel.uiState.first()
    assertFalse(state.groupByProject)
  }

  @Test
  fun deleteActivity_success_removesFromList() = runTest {
    // Arrange
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project A"),
            createActivity("2", EntityType.PROJECT, "Project B"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    coEvery { repository.deleteActivity("1") } returns Result.success(Unit)
    viewModel.loadActivities()
    advanceUntilIdle()
    viewModel.applyEntityTypeFilter(EntityType.PROJECT)
    advanceUntilIdle()

    // Act
    viewModel.deleteActivity("1")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertEquals(1, state.activities.size)
    assertEquals("2", state.activities[0].activityId)
  }

  @Test
  fun setCompactMode_updatesState() = runTest {
    // Act
    viewModel.setCompactMode(true)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.first()
    assertTrue(state.isCompactMode)
  }

  private fun createActivity(
      id: String,
      entityType: EntityType,
      title: String,
      activityType: ActivityType = ActivityType.CREATED
  ) =
      Activity(
          activityId = id,
          projectId = "project-1",
          activityType = activityType,
          entityType = entityType,
          entityId = "entity-$id",
          userId = testUserId,
          timestamp = Timestamp.now(),
          metadata = mapOf("title" to title))
}

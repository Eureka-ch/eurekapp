/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityFeedScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: ActivityFeedViewModel
  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth

  private val testUserId = "test-user-123"

  @Before
  fun setup() {
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)

    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser

    val userDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { userDoc.get("readActivityIds") } returns emptyList<String>()
    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { firestore.collection("users") } returns usersCollection
    every { usersCollection.document(any()) } returns userDocRef
    every { userDocRef.get() } returns Tasks.forResult(userDoc)

    viewModel = ActivityFeedViewModel(repository, firestore, auth)
  }

  @Test
  fun displaysFilterChipsAndEmptyState() {
    coEvery { repository.getActivities(testUserId) } returns flowOf(emptyList())

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MeetingsFilterChip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
    composeTestRule.onNodeWithText("Select a filter to view activities").assertIsDisplayed()
  }

  @Test
  fun clickingProjectsFilter_displaysActivities() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ActivitiesList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Project", substring = true).assertIsDisplayed()
  }

  @Test
  fun clickingMeetingsFilter_displaysActivities() {
    val activities = listOf(createActivity("1", EntityType.MEETING, "Sprint Meeting"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("MeetingsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ActivitiesList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sprint Meeting", substring = true).assertIsDisplayed()
  }

  @Test
  fun deselectingFilter_showsEmptyStateAgain() {
    coEvery { repository.getActivities(testUserId) } returns
        flowOf(listOf(createActivity("1", EntityType.PROJECT, "Test")))

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ActivitiesList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
    composeTestRule.onNodeWithText("Select a filter to view activities").assertIsDisplayed()
  }

  @Test
  fun filterSelected_noActivities_showsNoActivitiesMessage() {
    coEvery { repository.getActivities(testUserId) } returns flowOf(emptyList())

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
    composeTestRule.onNodeWithText("No activities match your filters").assertIsDisplayed()
  }

  @Test
  fun switchingFilters_updatesDisplayedActivities() {
    val projectActivities = listOf(createActivity("1", EntityType.PROJECT, "Project Alpha"))
    val meetingActivities = listOf(createActivity("2", EntityType.MEETING, "Sprint Planning"))
    val allActivities = projectActivities + meetingActivities

    coEvery { repository.getActivities(testUserId) } returns flowOf(allActivities)

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project Alpha", substring = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("MeetingsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Sprint Planning", substring = true).assertIsDisplayed()
  }

  @Test
  fun deleteButton_removesActivityFromList() {
    val activities =
        listOf(
            createActivity("1", EntityType.PROJECT, "Project Alpha"),
            createActivity("2", EntityType.PROJECT, "Project Beta"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)
    coEvery { repository.deleteActivity("1") } returns Result.success(Unit)

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Project Alpha", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Project Beta", substring = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("DeleteButton_1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Project Alpha", substring = true).assertDoesNotExist()
    composeTestRule.onNodeWithText("Project Beta", substring = true).assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorMessage() {
    coEvery { repository.getActivities(testUserId) } returns
        kotlinx.coroutines.flow.flow { throw Exception("Network error") }

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Error: Network error", substring = true).assertIsDisplayed()
  }

  @Test
  fun dateHeader_displaysForActivityGroups() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Today").assertIsDisplayed()
  }

  @Test
  fun loadingState_showsLoadingIndicator() {
    coEvery { repository.getActivities(testUserId) } returns
        kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(Long.MAX_VALUE) }

    composeTestRule.setContent { ActivityFeedScreen(viewModel = viewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading activities...").assertIsDisplayed()
  }

  @Test
  fun activityClick_triggersCallback() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(activities)

    var clickedActivityId: String? = null
    composeTestRule.setContent {
      ActivityFeedScreen(
          viewModel = viewModel,
          onActivityClick = { activityId, _ -> clickedActivityId = activityId })
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test Project", substring = true).performClick()
    composeTestRule.waitForIdle()

    assert(clickedActivityId == "1")
  }

  private fun createActivity(id: String, entityType: EntityType, title: String) =
      Activity(
          activityId = id,
          projectId = "test-project",
          activityType = ActivityType.CREATED,
          entityType = entityType,
          entityId = "entity-$id",
          userId = testUserId,
          timestamp = Timestamp.now(),
          metadata = mapOf("title" to title, "userName" to "Test User"))
}

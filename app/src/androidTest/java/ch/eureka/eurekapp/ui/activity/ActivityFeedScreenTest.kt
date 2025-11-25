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
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityFeedScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: ActivityFeedViewModel
  private lateinit var repository: ActivityRepository

  private val projectId = "global-activities"

  @Before
  fun setup() {
    repository = mockk(relaxed = true)
    val firestore = mockk<FirebaseFirestore>(relaxed = true)
    val userDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
    io.mockk.every { userDoc.getString("displayName") } returns "Test User"
    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    io.mockk.every { firestore.collection("users") } returns usersCollection
    io.mockk.every { usersCollection.document(any()) } returns userDocRef
    io.mockk.every { userDocRef.get() } returns Tasks.forResult(userDoc)

    viewModel = ActivityFeedViewModel(repository = repository, firestore = firestore)
  }

  @Test
  fun displaysFilterChipsAndEmptyState() {
    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MeetingsFilterChip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
    composeTestRule.onNodeWithText("Select a filter").assertIsDisplayed()
  }

  @Test
  fun clickingProjectsFilter_displaysActivities() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ActivitiesList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Project", substring = true).assertIsDisplayed()
  }

  @Test
  fun clickingMeetingsFilter_displaysActivities() {
    val activities = listOf(createActivity("1", EntityType.MEETING, "Sprint Meeting"))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("MeetingsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ActivitiesList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sprint Meeting", substring = true).assertIsDisplayed()
  }

  @Test
  fun deselectingFilter_showsEmptyStateAgain() {
    coEvery { repository.getActivitiesInProject(projectId, any()) } returns flowOf(listOf(createActivity("1", EntityType.PROJECT, "Test")))

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
  }

  @Test
  fun filterSelected_noActivities_showsNoActivitiesMessage() {
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(emptyList())

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EmptyState").assertIsDisplayed()
    composeTestRule.onNodeWithText("No activities yet").assertIsDisplayed()
  }

  @Test
  fun switchingFilters_updatesDisplayedActivities() {
    val projectActivities = listOf(createActivity("1", EntityType.PROJECT, "Project Alpha"))
    val meetingActivities = listOf(createActivity("2", EntityType.MEETING, "Sprint Planning"))
    val allActivities = projectActivities + meetingActivities

    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(allActivities)

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project Alpha", substring = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("MeetingsFilterChip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Sprint Planning", substring = true).assertIsDisplayed()
  }

  @Test
  fun deleteButton_removesActivityFromList() {
    val activities = listOf(
        createActivity("1", EntityType.PROJECT, "Project Alpha"),
        createActivity("2", EntityType.PROJECT, "Project Beta")
    )
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)
    coEvery { repository.deleteActivity(projectId, "1") } returns Result.success(Unit)

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

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
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns
        kotlinx.coroutines.flow.flow { throw Exception("Network error") }

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithText("Error: Network error", substring = true).assertIsDisplayed()
  }

  @Test
  fun dateHeader_displaysForActivityGroups() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Today").assertIsDisplayed()
  }

  @Test
  fun loadingState_showsLoadingIndicator() {
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns
        kotlinx.coroutines.flow.flow {
          kotlinx.coroutines.delay(Long.MAX_VALUE)
        }

    composeTestRule.setContent {
      ActivityFeedScreen(projectId = projectId, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading activities...").assertIsDisplayed()
  }

  @Test
  fun activityClick_triggersCallback() {
    val activities = listOf(createActivity("1", EntityType.PROJECT, "Test Project"))
    coEvery { repository.getActivitiesInProject(projectId, 100) } returns flowOf(activities)

    var clickedEntityId: String? = null
    composeTestRule.setContent {
      ActivityFeedScreen(
          projectId = projectId,
          viewModel = viewModel,
          onActivityClick = { entityId -> clickedEntityId = entityId }
      )
    }

    composeTestRule.onNodeWithTag("ProjectsFilterChip").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test Project", substring = true).performClick()
    composeTestRule.waitForIdle()

    assert(clickedEntityId == "entity-1")
  }

  private fun createActivity(id: String, entityType: EntityType, title: String) = Activity(
      activityId = id,
      projectId = projectId,
      activityType = ActivityType.CREATED,
      entityType = entityType,
      entityId = "entity-$id",
      userId = "user-1",
      timestamp = Timestamp.now(),
      metadata = mapOf("title" to title, "userName" to "Test User")
  )
}

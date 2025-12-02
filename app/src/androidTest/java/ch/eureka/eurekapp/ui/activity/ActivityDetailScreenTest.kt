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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: ActivityDetailViewModel
  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore

  @Before
  fun setup() {
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)

    val projectDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { projectDoc.exists() } returns true
    every { projectDoc.getString("name") } returns "Test Project"
    every { firestore.collection("projects").document(any()).get() } returns
        Tasks.forResult(projectDoc)

    viewModel = ActivityDetailViewModel(repository, firestore)
  }

  @Test
  fun activityDetailScreen_loading_showsLoadingIndicator() {
    coEvery { repository.getActivityById(any()) } coAnswers
        {
          kotlinx.coroutines.delay(Long.MAX_VALUE)
          null
        }

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = "test-id",
          onNavigateBack = {},
          onNavigateToEntity = { _, _ -> },
          viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_activityLoaded_displaysDetails() {
    val activity = createTestActivity("act-1", EntityType.PROJECT, "Project Alpha")
    coEvery { repository.getActivityById("act-1") } returns activity

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = "act-1",
          onNavigateBack = {},
          onNavigateToEntity = { _, _ -> },
          viewModel = viewModel)
    }

    // Wait for loading to complete and content to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("ActivityDetailContent").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify activity type is displayed (CREATED)
    composeTestRule.onNodeWithText("Created", substring = true).assertIsDisplayed()
    // Verify user name is displayed
    composeTestRule.onNodeWithText("Test User", substring = true).assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_backButton_triggersCallback() {
    val activity = createTestActivity("act-1", EntityType.PROJECT, "Project Alpha")
    coEvery { repository.getActivityById("act-1") } returns activity

    var backPressed = false
    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = "act-1",
          onNavigateBack = { backPressed = true },
          onNavigateToEntity = { _, _ -> },
          viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("BackButton").performClick()

    assert(backPressed)
  }

  @Test
  fun activityDetailScreen_deleteButton_visible() {
    val activity = createTestActivity("act-1", EntityType.PROJECT, "Project Alpha")
    coEvery { repository.getActivityById("act-1") } returns activity
    coEvery { repository.deleteActivity("act-1") } returns Result.success(Unit)

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = "act-1",
          onNavigateBack = {},
          onNavigateToEntity = { _, _ -> },
          viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("DeleteButton").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_error_displaysErrorMessage() {
    coEvery { repository.getActivityById("invalid") } returns null

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = "invalid",
          onNavigateBack = {},
          onNavigateToEntity = { _, _ -> },
          viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Activity not found", substring = true).assertIsDisplayed()
  }

  private fun createTestActivity(id: String, entityType: EntityType, title: String) =
      Activity(
          activityId = id,
          projectId = "project-1",
          activityType = ActivityType.CREATED,
          entityType = entityType,
          entityId = "entity-1",
          userId = "user-1",
          timestamp = Timestamp.now(),
          metadata = mapOf("title" to title))
}

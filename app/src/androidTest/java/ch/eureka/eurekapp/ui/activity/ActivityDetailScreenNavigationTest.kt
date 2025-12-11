package ch.eureka.eurekapp.ui.activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
import org.junit.Rule
import org.junit.Test

class ActivityDetailScreenNavigationTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var connectivityObserver: ConnectivityObserver
  private lateinit var viewModel: ActivityDetailViewModel
  private val testUserId = "user-123"
  private val testActivityId = "activity-456"
  private val testProjectId = "project-789"
  private val testEntityId = "entity-101"
  private var navigateBackCalled = false
  private var navigateToEntityCalled = false
  private var lastEntityType: EntityType? = null
  private var lastEntityId: String? = null
  private var lastProjectId: String? = null

  @Before
  fun setup() {
    repository = mockk(relaxed = true)
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)
    connectivityObserver = mockk(relaxed = true)
    navigateBackCalled = false
    navigateToEntityCalled = false
    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser
    every { connectivityObserver.isConnected } returns flowOf(true)
  }

  private fun setupActivity(entityType: EntityType, projectId: String = testProjectId): Activity {
    val activity =
        Activity(
            testActivityId,
            projectId,
            ActivityType.CREATED,
            entityType,
            testEntityId,
            testUserId,
            Timestamp.now(),
            mapOf("title" to "Test"))
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))
    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns "Test User"
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)
    return activity
  }

  private fun setScreen() {
    composeTestRule.setContent {
      ActivityDetailScreen(testActivityId, viewModel, { navigateBackCalled = true }) {
          type,
          id,
          projId ->
        navigateToEntityCalled = true
        lastEntityType = type
        lastEntityId = id
        lastProjectId = projId
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun loadsAndNavigates() {
    setupActivity(EntityType.MEETING)
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    setScreen()
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_DETAIL_SCREEN)
        .assertExists()
    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
    assertTrue(navigateBackCalled)
  }

  @Test
  fun entityButtonNavigatesCorrectly() {
    setupActivity(EntityType.MEETING)
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    setScreen()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).performClick()
    assertEquals(EntityType.MEETING, lastEntityType)
    assertEquals(testProjectId, lastProjectId)
  }

  @Test
  fun nonNavigableEntitiesHideButton() {
    setupActivity(EntityType.FILE)
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)
    setScreen()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).assertDoesNotExist()
  }

  @Test
  fun invalidActivityShowsError() {
    coEvery { repository.getActivities(testUserId) } returns flowOf(emptyList())
    viewModel =
        ActivityDetailViewModel("invalid", repository, connectivityObserver, firestore, auth)
    composeTestRule.setContent { ActivityDetailScreen("invalid", viewModel, {}) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ERROR_MESSAGE).assertExists()
  }
}

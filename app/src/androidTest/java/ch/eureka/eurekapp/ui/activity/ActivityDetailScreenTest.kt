/*
 * Co-Authored-By: Claude Sonnet 4.5
 */
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: ActivityRepository
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var connectivityObserver: ConnectivityObserver
  private lateinit var viewModel: ActivityDetailViewModel
  private val testUserId = "test-user-123"
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
    lastEntityType = null
    lastEntityId = null
    lastProjectId = null

    val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    every { firebaseUser.uid } returns testUserId
    every { auth.currentUser } returns firebaseUser

    every { connectivityObserver.isConnected } returns flowOf(true)
  }

  private fun setupSimpleUserMock(displayName: String = "Test User") {
    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.getString("displayName") } returns displayName
    every { firestore.collection("users").document(any()).get() } returns Tasks.forResult(userDoc)
  }

  @Test
  fun activityDetailScreen_errorState_displaysErrorMessage() {
    coEvery { repository.getActivities(testUserId) } returns flowOf(emptyList())
    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Activity not found").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_activityDetails_displayedCorrectly() {
    val activity =
        createActivity(
            testActivityId, EntityType.MEETING, "Team Meeting", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock("John Doe")
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_HEADER).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_INFO_CARD)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_TYPE)
        .assertTextEquals("CREATED")
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_TYPE)
        .assertTextEquals("MEETING")
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.USER_NAME)
        .assertTextEquals("John Doe")
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_TITLE)
        .assertTextEquals("Team Meeting")
  }

  @Test
  fun activityDetailScreen_entityButton_displayedForSupportedEntities() {
    val activity =
        createActivity(
            testActivityId,
            EntityType.MEETING,
            "Sprint Planning",
            ActivityType.CREATED,
            testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("View MEETING").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_entityButton_notDisplayedForFileEntity() {
    val activity =
        createActivity(
            testActivityId, EntityType.FILE, "document.pdf", ActivityType.UPLOADED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).assertDoesNotExist()
  }

  @Test
  fun activityDetailScreen_entityButton_navigatesToCorrectScreen() {
    val activity =
        createActivity(
            testActivityId, EntityType.TASK, "Fix bug", ActivityType.UPDATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = testActivityId,
          viewModel = viewModel,
          onNavigateToEntity = { type, id, projectId ->
            navigateToEntityCalled = true
            lastEntityType = type
            lastEntityId = id
            lastProjectId = projectId
          })
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).performClick()
    composeTestRule.waitForIdle()

    assert(navigateToEntityCalled)
    assert(lastEntityType == EntityType.TASK)
    assert(lastEntityId == testEntityId)
    assert(lastProjectId == testProjectId)
  }

  @Test
  fun activityDetailScreen_relatedActivities_displayedCorrectly() {
    val mainActivity =
        createActivity(
            testActivityId,
            EntityType.PROJECT,
            "Project Alpha",
            ActivityType.CREATED,
            testEntityId,
            Timestamp.now())
    val relatedActivity1 =
        createActivity(
            "activity-2",
            EntityType.PROJECT,
            "Project Alpha",
            ActivityType.UPDATED,
            testEntityId,
            Timestamp(Timestamp.now().seconds - 100, 0))
    val relatedActivity2 =
        createActivity(
            "activity-3",
            EntityType.PROJECT,
            "Project Alpha",
            ActivityType.SHARED,
            testEntityId,
            Timestamp(Timestamp.now().seconds - 200, 0))

    coEvery { repository.getActivities(testUserId) } returns
        flowOf(listOf(mainActivity, relatedActivity1, relatedActivity2))

    val userDoc = mockk<DocumentSnapshot>(relaxed = true)
    every { userDoc.id } returns testUserId
    every { userDoc.getString("displayName") } returns "Test User"

    val userDocRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
    every { userDocRef.get() } returns Tasks.forResult(userDoc)

    val querySnapshot = mockk<com.google.firebase.firestore.QuerySnapshot>(relaxed = true)
    every { querySnapshot.documents } returns listOf(userDoc)
    val usersQuery = mockk<com.google.firebase.firestore.Query>(relaxed = true)
    every { usersQuery.get() } returns Tasks.forResult(querySnapshot)

    val usersCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
    every { usersCollection.document(any()) } returns userDocRef
    every { usersCollection.whereIn(any<com.google.firebase.firestore.FieldPath>(), any()) } returns
        usersQuery
    every { firestore.collection("users") } returns usersCollection

    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.RELATED_ACTIVITIES_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Related Activities (2)").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${ActivityDetailScreenTestTags.RELATED_ACTIVITY_ITEM}_activity-2")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${ActivityDetailScreenTestTags.RELATED_ACTIVITY_ITEM}_activity-3")
        .assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_noRelatedActivities_displaysMessage() {
    val activity =
        createActivity(
            testActivityId, EntityType.MESSAGE, "Hello", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.RELATED_ACTIVITIES_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Related Activities (0)").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.NO_RELATED_ACTIVITIES)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("No other activities for this entity").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_shareButton_clickable() {
    val activity =
        createActivity(
            testActivityId, EntityType.TASK, "Task 1", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.SHARE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.SHARE_BUTTON).assertIsEnabled()
    composeTestRule.onNodeWithText("Share Activity").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_deleteButton_opensConfirmationDialog() {
    val activity =
        createActivity(
            testActivityId, EntityType.PROJECT, "Project", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete Activity?").assertIsDisplayed()
    composeTestRule.onNodeWithText("This action cannot be undone.").assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_deleteDialog_cancelButton_closesDialog() {
    val activity =
        createActivity(
            testActivityId, EntityType.MEETING, "Meeting", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_DIALOG).assertDoesNotExist()
  }

  @Test
  fun activityDetailScreen_deleteDialog_confirmButton_deletesActivity() {
    val activity =
        createActivity(testActivityId, EntityType.TASK, "Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))
    coEvery { repository.deleteActivity(testActivityId) } returns Result.success(Unit)

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = testActivityId,
          viewModel = viewModel,
          onNavigateBack = { navigateBackCalled = true })
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_DIALOG).assertDoesNotExist()
  }

  @Test
  fun activityDetailScreen_offlineMode_displaysOfflineMessage() {
    every { connectivityObserver.isConnected } returns flowOf(false)
    val activity =
        createActivity(
            testActivityId, EntityType.PROJECT, "Project", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
    composeTestRule
        .onNodeWithText("You are offline. Some actions are unavailable.")
        .assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_offlineMode_disablesActionButtons() {
    every { connectivityObserver.isConnected } returns flowOf(false)
    val activity =
        createActivity(
            testActivityId, EntityType.MEETING, "Meeting", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.SHARE_BUTTON).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.DELETE_BUTTON).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun activityDetailScreen_backButton_navigatesBack() {
    val activity =
        createActivity(testActivityId, EntityType.TASK, "Task", ActivityType.CREATED, testEntityId)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))

    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(
          activityId = testActivityId,
          viewModel = viewModel,
          onNavigateBack = { navigateBackCalled = true })
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
    composeTestRule.waitForIdle()

    assert(navigateBackCalled)
  }

  @Test
  fun activityDetailScreen_updatedActivityType_displaysCorrectColor() {
    val activity = createActivity(testActivityId, EntityType.TASK, "Test", ActivityType.UPDATED)
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activity))
    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_HEADER).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ActivityDetailScreenTestTags.ACTIVITY_INFO_CARD)
        .assertIsDisplayed()
  }

  @Test
  fun activityDetailScreen_activityWithoutTitle_doesNotDisplayEntityTitle() {
    val activityNoTitle =
        Activity(
            activityId = testActivityId,
            userId = testUserId,
            projectId = testProjectId,
            activityType = ActivityType.CREATED,
            entityType = EntityType.TASK,
            entityId = testEntityId,
            timestamp = Timestamp.now(),
            metadata = emptyMap())
    coEvery { repository.getActivities(testUserId) } returns flowOf(listOf(activityNoTitle))
    setupSimpleUserMock()
    viewModel =
        ActivityDetailViewModel(testActivityId, repository, connectivityObserver, firestore, auth)

    composeTestRule.setContent {
      ActivityDetailScreen(activityId = testActivityId, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(ActivityDetailScreenTestTags.ENTITY_TITLE).assertDoesNotExist()
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

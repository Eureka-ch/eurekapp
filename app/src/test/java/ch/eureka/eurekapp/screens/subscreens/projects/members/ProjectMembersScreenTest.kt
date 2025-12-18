/* Portions of this file were written with the help of Gemini. */
/* This code was written with help of Claude. */
package ch.eureka.eurekapp.screens.subscreens.projects.members

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.project.MembersUiState
import ch.eureka.eurekapp.model.data.project.ProjectMembersViewModel
import ch.eureka.eurekapp.model.data.user.User
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test suite for [ProjectMembersScreen] and its related components.
 *
 * This suite verifies:
 * - UI states (Loading, Error, Success with/without members).
 * - Navigation and Refresh actions.
 * - Member item rendering (Profile pictures, Online indicators).
 * - Status text generation logic (including edge cases and error handling).
 *
 * It uses Mockk to mock the ViewModel and static Android methods (DateUtils) to ensure 100%
 * coverage.
 */
@RunWith(RobolectricTestRunner::class)
class ProjectMembersScreenTest {

  @get:Rule val composeRule = createComposeRule()

  private lateinit var mockViewModel: ProjectMembersViewModel
  private lateinit var uiStateFlow: MutableStateFlow<MembersUiState>

  @Before
  fun setUp() {
    mockViewModel = mockk(relaxed = true)
    uiStateFlow = MutableStateFlow(MembersUiState.Loading)
    every { mockViewModel.uiState } returns uiStateFlow
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun projectMembersScreen_displaysLoadingState() {
    uiStateFlow.value = MembersUiState.Loading

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project", onBackClick = {}, projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.LOADING).assertIsDisplayed()
  }

  @Test
  fun projectMembersScreen_displaysErrorState() {
    val errorMessage = "Network error"
    uiStateFlow.value = MembersUiState.Error(errorMessage)

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project", onBackClick = {}, projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.ERROR).assertIsDisplayed()
    composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
  }

  @Test
  fun projectMembersScreen_displaysEmptySuccessState() {
    uiStateFlow.value = MembersUiState.Success("My Project", emptyList())

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project", onBackClick = {}, projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithText("My Project").assertIsDisplayed()
    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.EMPTY_STATE).assertIsDisplayed()
  }

  @Test
  fun projectMembersScreen_displaysMembersList() {
    val user1 = User(uid = "1", displayName = "Alice", photoUrl = "http://site.com/pic.jpg")
    val user2 = User(uid = "2", displayName = "Bob", photoUrl = "")

    uiStateFlow.value = MembersUiState.Success("My Project", listOf(user1, user2))

    every { mockViewModel.isUserOnline(any()) } returns false

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project", onBackClick = {}, projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.MEMBERS_LIST).assertIsDisplayed()
    composeRule.onNodeWithText("Members — 2").assertIsDisplayed()
    composeRule.onNodeWithText("Alice").assertIsDisplayed()
    composeRule.onNodeWithText("Bob").assertIsDisplayed()
  }

  @Test
  fun onBackClick_triggersCallback() {
    var backClicked = false
    uiStateFlow.value = MembersUiState.Loading

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project",
          onBackClick = { backClicked = true },
          projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun onRefreshClick_triggersLoadMembers() {
    uiStateFlow.value = MembersUiState.Loading

    composeRule.setContent {
      ProjectMembersScreen(
          projectId = "test-project", onBackClick = {}, projectMembersViewModel = mockViewModel)
    }

    composeRule.onNodeWithTag(ProjectMembersScreenTestTags.REFRESH_BUTTON).performClick()
    verify { mockViewModel.loadMembers() }
  }

  @Test
  fun memberItem_displaysProfilePictureWhenUrlPresent() {
    val user = User(uid = "1", displayName = "Alice", photoUrl = "http://example.com/image.png")

    composeRule.setContent { MemberItem(user = user, isUserOnline = { false }) }

    composeRule.onNodeWithContentDescription("Profile picture of Alice").assertIsDisplayed()
  }

  @Test
  fun memberItem_displaysFallbackIconWhenUrlEmpty() {
    val user = User(uid = "1", displayName = "Alice", photoUrl = "")

    composeRule.setContent { MemberItem(user = user, isUserOnline = { false }) }

    composeRule.onNodeWithContentDescription("Profile picture of Alice").assertDoesNotExist()
    composeRule.onNodeWithText("Alice").assertIsDisplayed()
  }

  @Test
  fun memberItem_showsOnlineStatusCorrectly() {
    val user = User(uid = "1", displayName = "Alice")

    composeRule.setContent { MemberItem(user = user, isUserOnline = { true }) }

    composeRule.onNodeWithText("Online").assertIsDisplayed()
  }

  @Test
  fun memberItem_throwsExceptionWhenDisplayNameIsBlank() {
    val invalidUser = User(uid = "1", displayName = "   ")

    assertThrows(IllegalArgumentException::class.java) {
      composeRule.setContent { MemberItem(user = invalidUser, isUserOnline = { false }) }
    }
  }

  @Test
  fun getStatusText_returnsOnlineWhenIsOnlineTrue() {
    val result = getStatusText(Timestamp(Date()), isOnline = true)
    assertEquals("Online", result)
  }

  @Test
  fun getStatusText_returnsNeverActiveWhenTimestampIsZero() {
    val result = getStatusText(Timestamp(0, 0), isOnline = false)
    assertEquals("Never active", result)
  }

  @Test
  fun getStatusText_returnsRelativeTime() {
    val now = System.currentTimeMillis()
    val tenMinutesAgo = now - (10 * 60 * 1000)
    val timestamp = Timestamp(Date(tenMinutesAgo))

    val result = getStatusText(timestamp, isOnline = false)

    assert(result.isNotBlank())
    assert(result != "Online")
    assert(result != "Never active")
  }

  @Test
  fun getStatusText_logsAndRethrowsOnError() {
    mockkStatic(DateUtils::class)
    mockkStatic(Log::class)

    val exception = RuntimeException("Date format error")

    every { DateUtils.getRelativeTimeSpanString(any<Long>(), any<Long>(), any<Long>()) } throws
        exception

    every { Log.e(any(), any()) } returns 0

    val timestamp = Timestamp(Date())

    val thrown =
        assertThrows(RuntimeException::class.java) { getStatusText(timestamp, isOnline = false) }

    assertEquals("Date format error", thrown.message)

    verify { Log.e("ProjectMemberScreen", "Fail to construct create status text.") }
  }
}

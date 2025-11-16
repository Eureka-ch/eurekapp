package ch.eureka.eurekapp.ui.tasks.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.UserAssignmentField
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

/**
 * Tests for UserAssignmentField component
 *
 * These tests verify the correct display and interaction behavior of the user assignment dropdown
 * including user selection, checkbox states, and display text.
 */
@RunWith(AndroidJUnit4::class)
class UserAssignmentFieldTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  private val testUser1 =
      User(
          uid = "user1",
          displayName = "John Doe",
          email = "john@example.com",
          photoUrl = "",
          lastActive = Timestamp.now())

  private val testUser2 =
      User(
          uid = "user2",
          displayName = "Jane Smith",
          email = "jane@example.com",
          photoUrl = "",
          lastActive = Timestamp.now())

  private val testUser3 =
      User(
          uid = "user3",
          displayName = "",
          email = "noemail@example.com",
          photoUrl = "",
          lastActive = Timestamp.now())

  @Test
  fun userAssignmentFieldDisplaysNoUsersMessageWhenEmptyList() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = emptyList(),
            selectedUserIds = emptyList(),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.NO_USERS_AVAILABLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No users available in this project").assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldDisplaysNoUsersAssignedWhenSelectionEmpty() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1, testUser2),
            selectedUserIds = emptyList(),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No users assigned").assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldDisplaysSingleUserNameWhenOneSelected() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1, testUser2),
            selectedUserIds = listOf("user1"),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldDisplaysUserEmailWhenDisplayNameBlank() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser3),
            selectedUserIds = listOf("user3"),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithText("noemail@example.com").assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldDisplaysUserCountWhenMultipleSelected() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1, testUser2),
            selectedUserIds = listOf("user1", "user2"),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithText("2 users assigned").assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldOpensDropdownMenuWhenClicked() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1, testUser2),
            selectedUserIds = emptyList(),
            onUserToggled = {},
            enabled = true)
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_TITLE).performClick()

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_MENU).assertIsDisplayed()
  }

  @Test
  fun userAssignmentFieldCallsOnUserToggledWhenUserItemClicked() {
    var toggledUserId: String? = null
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1, testUser2),
            selectedUserIds = emptyList(),
            onUserToggled = { userId -> toggledUserId = userId },
            enabled = true)
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_TITLE).performClick()
    composeTestRule.onNodeWithTag("user_item_user1").performClick()

    assert(toggledUserId == "user1")
  }

  @Test
  fun userAssignmentFieldDoesNotOpenDropdownWhenDisabled() {
    composeTestRule.setContent {
      EurekappTheme {
        UserAssignmentField(
            availableUsers = listOf(testUser1),
            selectedUserIds = emptyList(),
            onUserToggled = {},
            enabled = false)
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_TITLE).performClick()

    composeTestRule.onNodeWithTag(CommonTaskTestTags.USER_ASSIGNMENT_MENU).assertIsNotDisplayed()
  }
}

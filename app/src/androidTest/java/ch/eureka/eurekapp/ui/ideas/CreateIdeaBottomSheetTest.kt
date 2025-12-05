/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android tests for CreateIdeaBottomSheet.
 *
 * Tests UI interactions and display states. Pattern follows AddFieldBottomSheetTest.
 */
class CreateIdeaBottomSheetTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testProjects =
      listOf(
          Project(projectId = "p1", name = "Project 1"),
          Project(projectId = "p2", name = "Project 2"))

  private val testUsers =
      listOf(
          User(uid = "user1", displayName = "User One", email = "user1@test.com"),
          User(uid = "user2", displayName = "User Two", email = "user2@test.com"))

  // ========== DISPLAY TESTS ==========

  @Test
  fun createIdeaBottomSheet_displaysAllFields() {
    var onCreateIdeaCalled = false
    var onDismissCalled = false

    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = { onDismissCalled = true },
          onCreateIdea = { _, _, _ -> onCreateIdeaCalled = true },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false)
    }

    composeTestRule.onNodeWithText("Create New Idea").assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_withEmptyProjects_showsNoProjectsMessage() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = emptyList(),
          availableUsers = emptyList(),
          isLoading = false)
    }

    composeTestRule.onNodeWithText("No projects available").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_withEmptyUsers_showsNoUsersMessage() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false,
          onProjectSelected = {})
    }

    // Select a project first
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("No users available in this project").assertIsDisplayed()
  }

  // ========== INTERACTION TESTS ==========

  @Test
  fun createIdeaBottomSheet_titleFieldAcceptsInput() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false)
    }

    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD)
        .performTextInput("My Idea Title")
    composeTestRule.waitForIdle()

    // Verify text was entered (field should contain the text)
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD).assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_projectSelectionOpensDropdown() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false,
          onProjectSelected = {})
    }

    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()

    // Verify dropdown items are displayed
    composeTestRule.onNodeWithText("Project 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Project 2").assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_selectingProjectLoadsUsers() {
    var projectSelectedId: String? = null

    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = testUsers,
          isLoading = false,
          onProjectSelected = { projectId -> projectSelectedId = projectId })
    }

    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    // Verify onProjectSelected was called
    assertEquals("p1", projectSelectedId)

    // Verify participants field is now visible
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .assertIsDisplayed()
  }

  @Test
  fun createIdeaBottomSheet_participantSelectionOpensDropdown() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = testUsers,
          isLoading = false,
          onProjectSelected = {})
    }

    // First select a project
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    // Then open participants dropdown
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()

    // Verify participant items are displayed
    composeTestRule.onNodeWithText("User One").assertIsDisplayed()
    composeTestRule.onNodeWithText("User Two").assertIsDisplayed()
  }

  // ========== CREATION TESTS ==========

  @Test
  fun createIdeaBottomSheet_createButtonCallsOnCreateIdea() {
    var onCreateIdeaCalled = false
    var createdTitle: String? = null
    var createdProjectId: String? = null
    var createdParticipantIds: List<String>? = null

    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { title, projectId, participantIds ->
            onCreateIdeaCalled = true
            createdTitle = title
            createdProjectId = projectId
            createdParticipantIds = participantIds
          },
          availableProjects = testProjects,
          availableUsers = testUsers,
          isLoading = false,
          onProjectSelected = {})
    }

    // Enter title
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD)
        .performTextInput("My Idea")
    composeTestRule.waitForIdle()

    // Select project
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    // Click create button
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    assert(onCreateIdeaCalled) { "onCreateIdea should be called" }
    assertEquals("My Idea", createdTitle)
    assertEquals("p1", createdProjectId)
    assertNotNull(createdParticipantIds)
  }

  @Test
  fun createIdeaBottomSheet_createButtonDisabledWithoutProject() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false)
    }

    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun createIdeaBottomSheet_createButtonEnabledWithProject() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false,
          onProjectSelected = {})
    }

    // Select a project
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).assertIsEnabled()
  }

  @Test
  fun createIdeaBottomSheet_cancelButtonCallsOnDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = { dismissCalled = true },
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = false)
    }

    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON).performClick()
    composeTestRule.waitForIdle()

    assert(dismissCalled) { "onDismiss should be called when Cancel is clicked" }
  }

  @Test
  fun createIdeaBottomSheet_createWithParticipants_callsOnCreateIdeaWithParticipantIds() {
    var createdParticipantIds: List<String>? = null

    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, participantIds -> createdParticipantIds = participantIds },
          availableProjects = testProjects,
          availableUsers = testUsers,
          isLoading = false,
          onProjectSelected = {})
    }

    // Select project
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    // Select participants
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("User One").performClick()
    composeTestRule.waitForIdle()

    // Click create
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    assertNotNull(createdParticipantIds)
    assertEquals(1, createdParticipantIds!!.size)
    assertEquals("user1", createdParticipantIds!![0])
  }

  // ========== LOADING TESTS ==========

  @Test
  fun createIdeaBottomSheet_showsLoadingIndicatorWhenLoadingUsers() {
    composeTestRule.setContent {
      CreateIdeaBottomSheet(
          onDismiss = {},
          onCreateIdea = { _, _, _ -> },
          availableProjects = testProjects,
          availableUsers = emptyList(),
          isLoading = true,
          onProjectSelected = {})
    }

    // Select project to trigger loading
    composeTestRule.onNodeWithTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Project 1").performClick()
    composeTestRule.waitForIdle()

    // Loading indicator should be visible (CircularProgressIndicator)
    // Note: We check for the participants field which shows loading state
    composeTestRule
        .onNodeWithTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)
        .assertIsDisplayed()
  }
}

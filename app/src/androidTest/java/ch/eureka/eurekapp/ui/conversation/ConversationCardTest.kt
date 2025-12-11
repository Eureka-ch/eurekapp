package ch.eureka.eurekapp.ui.conversation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.conversation.Conversation
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

@RunWith(AndroidJUnit4::class)
class ConversationCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createDisplayData(
      otherMemberNames: List<String> = listOf("Jane Doe"),
      projectName: String = "Test Project",
      lastMessagePreview: String? = null,
      lastMessageTime: String? = null,
      hasUnread: Boolean = false
  ) =
      ConversationDisplayData(
          conversation = Conversation(conversationId = "conv1", projectId = "p1"),
          otherMembers = otherMemberNames,
          otherMembersPhotoUrl = listOf(""),
          projectName = projectName,
          lastMessagePreview = lastMessagePreview,
          lastMessageTime = lastMessageTime,
          hasUnread = hasUnread)

  @Test
  fun conversationCard_displaysMemberAndProjectName() {
    composeTestRule.setContent {
      ConversationCard(
          displayData = createDisplayData(otherMemberNames = listOf("John"), projectName = "Alpha"),
          onClick = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.MEMBER_NAME, useUnmergedTree = true)
        .assertTextEquals("John")
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.PROJECT_NAME, useUnmergedTree = true)
        .assertTextEquals("Alpha")
  }

  @Test
  fun conversationCard_displaysLastMessageAndTime() {
    composeTestRule.setContent {
      ConversationCard(
          displayData = createDisplayData(lastMessagePreview = "Hello!", lastMessageTime = "2h"),
          onClick = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.LAST_MESSAGE, useUnmergedTree = true)
        .assertTextEquals("Hello!")
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.LAST_MESSAGE_TIME, useUnmergedTree = true)
        .assertTextEquals("2h")
  }

  @Test
  fun conversationCard_showsUnreadIndicatorWhenHasUnread() {
    composeTestRule.setContent {
      ConversationCard(
          displayData = createDisplayData(hasUnread = true, lastMessagePreview = "New"),
          onClick = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.UNREAD_INDICATOR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun conversationCard_hidesUnreadIndicatorWhenNoUnread() {
    composeTestRule.setContent {
      ConversationCard(
          displayData = createDisplayData(hasUnread = false, lastMessagePreview = "Old"),
          onClick = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.UNREAD_INDICATOR, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun conversationCard_clickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      ConversationCard(displayData = createDisplayData(), onClick = { clicked = true })
    }
    composeTestRule.onNodeWithTag(ConversationCardTestTags.CONVERSATION_CARD).performClick()
    assertTrue(clicked)
  }

  @Test
  fun conversationCard_hidesOptionalFieldsWhenNull() {
    composeTestRule.setContent {
      ConversationCard(
          displayData = createDisplayData(lastMessagePreview = null, lastMessageTime = null),
          onClick = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.LAST_MESSAGE, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(ConversationCardTestTags.LAST_MESSAGE_TIME, useUnmergedTree = true)
        .assertDoesNotExist()
  }

    @Test
    fun conversationCard_withSingleMemberDisplaysSingleAvatar() {
        val displayData = ConversationDisplayData(
            conversation = Conversation("conv1", "proj1"),
            otherMembers = listOf("Alice"),
            otherMembersPhotoUrl = listOf("https://example.com/alice.jpg"),
            projectName = "Project"
        )

        composeTestRule.setContent {
                ConversationCard(displayData = displayData, onClick = {})
        }

        composeTestRule.onNodeWithContentDescription("Profile picture of Alice").assertExists()
    }

    @Test
    fun conversationCard_withMultipleMembersDisplaysTwoAvatars() {
        val displayData = ConversationDisplayData(
            conversation = Conversation("conv1", "proj1"),
            otherMembers = listOf("Alice", "Bob", "Charlie"),
            otherMembersPhotoUrl = listOf("https://example.com/alice.jpg", "https://example.com/bob.jpg", "https://example.com/charlie.jpg"),
            projectName = "Project"
        )

        composeTestRule.setContent {
                ConversationCard(displayData = displayData, onClick = {})
        }

        composeTestRule.onNodeWithContentDescription("Profile picture of Alice").assertExists()
        composeTestRule.onNodeWithContentDescription("Profile picture of Bob").assertExists()
    }

    @Test
    fun conversationCard_withEmptyMembersHandlesGracefully() {
        val displayData = ConversationDisplayData(
            conversation = Conversation("conv1", "proj1"),
            otherMembers = listOf(),
            otherMembersPhotoUrl = listOf(),
            projectName = "Project"
        )

        composeTestRule.setContent {
                ConversationCard(displayData = displayData, onClick = {})
        }
    }

    @Test
    fun conversationCard_withNullPhotoUrlUsesFallbackAvatar() {
        val displayData = ConversationDisplayData(
            conversation = Conversation("conv1", "proj1"),
            otherMembers = listOf("Alice"),
            otherMembersPhotoUrl = listOf(""),
            projectName = "Project"
        )

        composeTestRule.setContent {
                ConversationCard(displayData = displayData, onClick = {})
        }

        composeTestRule.onNodeWithContentDescription("Member icon").assertExists()
    }

    @Test
    fun conversationCard_memberNamesJoinedWithComma() {
        val displayData = ConversationDisplayData(
            conversation = Conversation("conv1", "proj1"),
            otherMembers = listOf("Alice", "Bob"),
            otherMembersPhotoUrl = listOf("", ""),
            projectName = "Project"
        )

        composeTestRule.setContent {
                ConversationCard(displayData = displayData, onClick = {})
        }

        composeTestRule.onNodeWithText("Alice, Bob").assertExists()
    }
}

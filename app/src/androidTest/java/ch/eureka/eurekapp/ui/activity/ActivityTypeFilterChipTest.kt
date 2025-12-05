/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import org.junit.Rule
import org.junit.Test

class ActivityTypeFilterChipTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun filterChip_notSelected_displaysCorrectly() {
    composeTestRule.setContent {
      ActivityTypeFilterChip(label = "Projects", selected = false, onClick = {})
    }

    composeTestRule.onNodeWithText("Projects").assertIsDisplayed()
    composeTestRule.onNodeWithText("Projects").assertIsNotSelected()
  }

  @Test
  fun filterChip_selected_displaysCorrectly() {
    composeTestRule.setContent {
      ActivityTypeFilterChip(label = "Meetings", selected = true, onClick = {})
    }

    composeTestRule.onNodeWithText("Meetings").assertIsDisplayed()
    composeTestRule.onNodeWithText("Meetings").assertIsSelected()
  }

  @Test
  fun filterChip_click_triggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      ActivityTypeFilterChip(label = "Tasks", selected = false, onClick = { clicked = true })
    }

    composeTestRule.onNodeWithText("Tasks").performClick()

    assert(clicked)
  }

  @Test
  fun entityType_toDisplayString_convertsCorrectly() {
    assert(EntityType.MEETING.toDisplayString() == "Meetings")
    assert(EntityType.MESSAGE.toDisplayString() == "Messages")
    assert(EntityType.FILE.toDisplayString() == "Files")
    assert(EntityType.TASK.toDisplayString() == "Tasks")
    assert(EntityType.PROJECT.toDisplayString() == "Projects")
    assert(EntityType.MEMBER.toDisplayString() == "Members")
  }

  @Test
  fun activityType_toDisplayString_convertsCorrectly() {
    assert(ActivityType.CREATED.toDisplayString() == "Created")
    assert(ActivityType.UPDATED.toDisplayString() == "Updated")
    assert(ActivityType.DELETED.toDisplayString() == "Deleted")
    assert(ActivityType.UPLOADED.toDisplayString() == "Uploaded")
    assert(ActivityType.SHARED.toDisplayString() == "Shared")
    assert(ActivityType.COMMENTED.toDisplayString() == "Commented")
    assert(ActivityType.STATUS_CHANGED.toDisplayString() == "Status Changed")
    assert(ActivityType.JOINED.toDisplayString() == "Joined")
    assert(ActivityType.LEFT.toDisplayString() == "Left")
    assert(ActivityType.ASSIGNED.toDisplayString() == "Assigned")
    assert(ActivityType.UNASSIGNED.toDisplayString() == "Unassigned")
    assert(ActivityType.ROLE_CHANGED.toDisplayString() == "Role Changed")
    assert(ActivityType.DOWNLOADED.toDisplayString() == "Downloaded")
  }
}

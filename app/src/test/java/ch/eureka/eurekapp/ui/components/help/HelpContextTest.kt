package ch.eureka.eurekapp.ui.components.help

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Portions of this code were generated with the help of AI(chatGPT) and Claude Sonnet 4.5
// This code was written with help of Claude.
class HelpContextTest {

  @Test
  fun homeOverviewContext_generatesCorrectHelpContent() {
    val userName = "John"
    val content = HelpContext.HOME_OVERVIEW.toHelpContent(userName)

    assertEquals("Welcome $userName 👋", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("Summary cards", content.steps[0].highlight)
    assertEquals("Quick actions", content.steps[1].highlight)
    assertEquals("Interactive sections", content.steps[2].highlight)
  }

  @Test
  fun tasksContext_generatesCorrectHelpContent() {
    val userName = "Alice"
    val content = HelpContext.TASKS.toHelpContent(userName)

    assertEquals("Task management", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("Filter bar", content.steps[0].highlight)
    assertEquals("Action buttons", content.steps[1].highlight)
    assertEquals("Interactive cards", content.steps[2].highlight)
  }

  @Test
  fun meetingsContext_generatesCorrectHelpContent() {
    val userName = "Bob"
    val content = HelpContext.MEETINGS.toHelpContent(userName)

    assertEquals("Mastering meetings", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(4, content.steps.size)
    assertEquals("Upcoming/Past tabs", content.steps[0].highlight)
    assertEquals("Meeting card", content.steps[1].highlight)
    assertEquals("+ Button", content.steps[2].highlight)
    assertEquals("File attachments", content.steps[3].highlight)
  }

  @Test
  fun projectsContext_generatesCorrectHelpContent() {
    val userName = "Charlie"
    val content = HelpContext.PROJECTS.toHelpContent(userName)

    assertEquals("Project view", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("Project context", content.steps[0].highlight)
    assertEquals("Quick navigation", content.steps[1].highlight)
    assertEquals("Back to home", content.steps[2].highlight)
  }

  @Test
  fun createTaskContext_generatesCorrectHelpContentWithDependencies() {
    val userName = "Diana"
    val content = HelpContext.CREATE_TASK.toHelpContent(userName)

    assertEquals("Guided creation", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(4, content.steps.size)
    assertEquals("Essential fields", content.steps[0].highlight)
    assertEquals("Project & team", content.steps[1].highlight)
    assertEquals("Task templates", content.steps[2].highlight)
    assertEquals("Task dependencies", content.steps[3].highlight)

    // Verify templates explanation is present
    val templatesStep = content.steps[2]
    assertTrue(templatesStep.detail.contains("templates"))
    assertTrue(templatesStep.detail.contains("custom fields"))

    // Verify dependencies explanation is present
    val dependenciesStep = content.steps[3]
    assertTrue(dependenciesStep.detail.contains("execution order"))
    assertTrue(dependenciesStep.detail.contains("cannot start"))
    assertTrue(dependenciesStep.detail.contains("cycles"))
  }

  @Test
  fun allContexts_generateNonEmptyContent() {
    val userName = "TestUser"
    HelpContext.entries.forEach { context ->
      val content = context.toHelpContent(userName)
      assertFalse("Title should not be empty for $context", content.title.isBlank())
      assertFalse("Intro should not be empty for $context", content.intro.isBlank())
      assertTrue("Should have at least one step for $context", content.steps.isNotEmpty())
      content.steps.forEach { step ->
        assertFalse("Step highlight should not be empty", step.highlight.isBlank())
        assertFalse("Step detail should not be empty", step.detail.isBlank())
      }
    }
  }

  @Test
  fun helpContent_usesProvidedUserName() {
    val customName = "CustomName"
    val content = HelpContext.HOME_OVERVIEW.toHelpContent(customName)

    assertTrue(content.title.contains(customName))
    assertTrue(content.intro.contains(customName))
  }

  @Test
  fun helpContent_handlesEmptyUserNameWithDefault() {
    // When empty string is passed, it should still generate valid content
    val content = HelpContext.HOME_OVERVIEW.toHelpContent("")

    // Content should still be valid even with empty name
    assertFalse(content.title.isBlank())
    assertFalse(content.intro.isBlank())
    assertTrue(content.steps.isNotEmpty())
  }

  @Test
  fun filesManagementContext_generatesCorrectHelpContent() {
    val userName = "Emma"
    val content = HelpContext.FILES_MANAGEMENT.toHelpContent(userName)

    assertEquals("File management", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("View files", content.steps[0].highlight)
    assertEquals("Open files", content.steps[1].highlight)
    assertEquals("Delete files", content.steps[2].highlight)
  }

  @Test
  fun meetingVotesContext_generatesCorrectHelpContent() {
    val userName = "Frank"
    val content = HelpContext.MEETING_VOTES.toHelpContent(userName)

    assertEquals("Meeting proposals voting", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(4, content.steps.size)
    assertEquals("View proposals", content.steps[0].highlight)
    assertEquals("Select preferences", content.steps[1].highlight)
    assertEquals("Add new proposal", content.steps[2].highlight)
    assertEquals("Confirm votes", content.steps[3].highlight)
  }

  @Test
  fun tokenEntryContext_generatesCorrectHelpContent() {
    val userName = "Grace"
    val content = HelpContext.TOKEN_ENTRY.toHelpContent(userName)

    assertEquals("Join with token", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("Get your token", content.steps[0].highlight)
    assertEquals("Enter token", content.steps[1].highlight)
    assertEquals("Validate", content.steps[2].highlight)
  }

  @Test
  fun viewTaskContext_generatesCorrectHelpContent() {
    val userName = "Henry"
    val content = HelpContext.VIEW_TASK.toHelpContent(userName)

    assertEquals("Viewing task details", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(4, content.steps.size)
    assertEquals("Task information", content.steps[0].highlight)
    assertEquals("Edit task", content.steps[1].highlight)
    assertEquals("View dependencies", content.steps[2].highlight)
    assertEquals("Attachments", content.steps[3].highlight)
  }

  @Test
  fun notesContext_generatesCorrectHelpContent() {
    val userName = "Iris"
    val content = HelpContext.NOTES.toHelpContent(userName)

    assertEquals("Personal notes", content.title)
    assertTrue(content.intro.contains(userName))
    assertEquals(3, content.steps.size)
    assertEquals("Cloud vs Local", content.steps[0].highlight)
    assertEquals("Add notes", content.steps[1].highlight)
    assertEquals("View history", content.steps[2].highlight)

    // Verify cloud sync explanation is present
    val cloudStep = content.steps[0]
    assertTrue(cloudStep.detail.contains("automatically synchronized"))
    assertTrue(cloudStep.detail.contains("safe"))
  }
}

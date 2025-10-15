package ch.eureka.eurekapp.model.data

import junit.framework.TestCase
import org.junit.Test

/**
 * Test suite for IdGenerator utility.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class IdGeneratorTest {

  @Test
  fun generateUserId_shouldStartWithUserPrefix() {
    val userId = IdGenerator.generateUserId()
    TestCase.assertTrue(userId.startsWith("user_"))
  }

  @Test
  fun generateUserId_shouldBeUnique() {
    val userId1 = IdGenerator.generateUserId()
    val userId2 = IdGenerator.generateUserId()
    TestCase.assertFalse(userId1 == userId2)
  }

  @Test
  fun generateProjectId_shouldStartWithProjectPrefix() {
    val projectId = IdGenerator.generateProjectId()
    TestCase.assertTrue(projectId.startsWith("project_"))
  }

  @Test
  fun generateProjectId_shouldBeUnique() {
    val projectId1 = IdGenerator.generateProjectId()
    val projectId2 = IdGenerator.generateProjectId()
    TestCase.assertFalse(projectId1 == projectId2)
  }

  @Test
  fun generateTaskId_shouldStartWithTaskPrefix() {
    val taskId = IdGenerator.generateTaskId()
    TestCase.assertTrue(taskId.startsWith("task_"))
  }

  @Test
  fun generateTaskId_shouldBeUnique() {
    val taskId1 = IdGenerator.generateTaskId()
    val taskId2 = IdGenerator.generateTaskId()
    TestCase.assertFalse(taskId1 == taskId2)
  }

  @Test
  fun generateTaskTemplateId_shouldStartWithTemplatePrefix() {
    val templateId = IdGenerator.generateTaskTemplateId()
    TestCase.assertTrue(templateId.startsWith("template_"))
  }

  @Test
  fun generateTaskTemplateId_shouldBeUnique() {
    val templateId1 = IdGenerator.generateTaskTemplateId()
    val templateId2 = IdGenerator.generateTaskTemplateId()
    TestCase.assertFalse(templateId1 == templateId2)
  }

  @Test
  fun generateMeetingId_shouldStartWithMeetingPrefix() {
    val meetingId = IdGenerator.generateMeetingId()
    TestCase.assertTrue(meetingId.startsWith("meeting_"))
  }

  @Test
  fun generateMeetingId_shouldBeUnique() {
    val meetingId1 = IdGenerator.generateMeetingId()
    val meetingId2 = IdGenerator.generateMeetingId()
    TestCase.assertFalse(meetingId1 == meetingId2)
  }

  @Test
  fun generateChatChannelId_shouldStartWithChannelPrefix() {
    val channelId = IdGenerator.generateChatChannelId()
    TestCase.assertTrue(channelId.startsWith("channel_"))
  }

  @Test
  fun generateChatChannelId_shouldBeUnique() {
    val channelId1 = IdGenerator.generateChatChannelId()
    val channelId2 = IdGenerator.generateChatChannelId()
    TestCase.assertFalse(channelId1 == channelId2)
  }

  @Test
  fun generateMessageId_shouldStartWithMsgPrefix() {
    val messageId = IdGenerator.generateMessageId()
    TestCase.assertTrue(messageId.startsWith("msg_"))
  }

  @Test
  fun generateMessageId_shouldBeUnique() {
    val messageId1 = IdGenerator.generateMessageId()
    val messageId2 = IdGenerator.generateMessageId()
    TestCase.assertFalse(messageId1 == messageId2)
  }
}

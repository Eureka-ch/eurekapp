package ch.eureka.eurekapp.model.data

import junit.framework.TestCase
import org.junit.Test

/**
 * Test suite for FirestorePaths utility.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */

/*
Co-author: GPT-5 Codex
*/

class FirestorePathsTest {

  @Test
  fun constants_shouldHaveCorrectValues() {
    TestCase.assertEquals("users", FirestorePaths.USERS)
    TestCase.assertEquals("projects", FirestorePaths.PROJECTS)
    TestCase.assertEquals("chatChannels", FirestorePaths.CHAT_CHANNELS)
    TestCase.assertEquals("messages", FirestorePaths.MESSAGES)
    TestCase.assertEquals("meetings", FirestorePaths.MEETINGS)
    TestCase.assertEquals("taskTemplates", FirestorePaths.TASK_TEMPLATES)
    TestCase.assertEquals("tasks", FirestorePaths.TASKS)
    TestCase.assertEquals("transcriptions", FirestorePaths.TRANSCRIPTIONS)
    TestCase.assertEquals("selfNotes", FirestorePaths.SELF_NOTES)
  }

  @Test
  fun userPath_shouldReturnCorrectPath() {
    val userId = "user123"
    val expected = "users/user123"
    TestCase.assertEquals(expected, FirestorePaths.userPath(userId))
  }

  @Test
  fun projectPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123"
    TestCase.assertEquals(expected, FirestorePaths.projectPath(projectId))
  }

  @Test
  fun chatChannelsPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123/chatChannels"
    TestCase.assertEquals(expected, FirestorePaths.chatChannelsPath(projectId))
  }

  @Test
  fun chatChannelPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val channelId = "channel456"
    val expected = "projects/project123/chatChannels/channel456"
    TestCase.assertEquals(expected, FirestorePaths.chatChannelPath(projectId, channelId))
  }

  @Test
  fun messagesPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val channelId = "channel456"
    val expected = "projects/project123/chatChannels/channel456/messages"
    TestCase.assertEquals(expected, FirestorePaths.messagesPath(projectId, channelId))
  }

  @Test
  fun messagePath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val channelId = "channel456"
    val messageId = "msg789"
    val expected = "projects/project123/chatChannels/channel456/messages/msg789"
    TestCase.assertEquals(expected, FirestorePaths.messagePath(projectId, channelId, messageId))
  }

  @Test
  fun meetingsPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123/meetings"
    TestCase.assertEquals(expected, FirestorePaths.meetingsPath(projectId))
  }

  @Test
  fun meetingPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val meetingId = "meeting456"
    val expected = "projects/project123/meetings/meeting456"
    TestCase.assertEquals(expected, FirestorePaths.meetingPath(projectId, meetingId))
  }

  @Test
  fun taskTemplatesPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123/taskTemplates"
    TestCase.assertEquals(expected, FirestorePaths.taskTemplatesPath(projectId))
  }

  @Test
  fun taskTemplatePath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val templateId = "template456"
    val expected = "projects/project123/taskTemplates/template456"
    TestCase.assertEquals(expected, FirestorePaths.taskTemplatePath(projectId, templateId))
  }

  @Test
  fun tasksPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123/tasks"
    TestCase.assertEquals(expected, FirestorePaths.tasksPath(projectId))
  }

  @Test
  fun taskPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val taskId = "task456"
    val expected = "projects/project123/tasks/task456"
    TestCase.assertEquals(expected, FirestorePaths.taskPath(projectId, taskId))
  }

  @Test
  fun membersPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val expected = "projects/project123/members"
    TestCase.assertEquals(expected, FirestorePaths.membersPath(projectId))
  }

  @Test
  fun memberPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val userId = "user456"
    val expected = "projects/project123/members/user456"
    TestCase.assertEquals(expected, FirestorePaths.memberPath(projectId, userId))
  }

  @Test
  fun participantsPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val meetingId = "meeting456"
    val expected = "projects/project123/meetings/meeting456/participants"
    TestCase.assertEquals(expected, FirestorePaths.participantsPath(projectId, meetingId))
  }

  @Test
  fun participantPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val meetingId = "meeting456"
    val userId = "user789"
    val expected = "projects/project123/meetings/meeting456/participants/user789"
    TestCase.assertEquals(expected, FirestorePaths.participantPath(projectId, meetingId, userId))
  }

  @Test
  fun transcriptionsPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val meetingId = "meeting456"
    val expected = "projects/project123/meetings/meeting456/transcriptions"
    TestCase.assertEquals(expected, FirestorePaths.transcriptionsPath(projectId, meetingId))
  }

  @Test
  fun transcriptionPath_shouldReturnCorrectPath() {
    val projectId = "project123"
    val meetingId = "meeting456"
    val transcriptionId = "transcription789"
    val expected = "projects/project123/meetings/meeting456/transcriptions/transcription789"
    TestCase.assertEquals(
        expected, FirestorePaths.transcriptionPath(projectId, meetingId, transcriptionId))
  }

  @Test
  fun selfNotesPath_shouldReturnCorrectPath() {
    val userId = "user123"
    val expected = "users/user123/selfNotes"
    TestCase.assertEquals(expected, FirestorePaths.selfNotesPath(userId))
  }

  @Test
  fun selfNotePath_shouldReturnCorrectPath() {
    val userId = "user123"
    val noteId = "note456"
    val expected = "users/user123/selfNotes/note456"
    TestCase.assertEquals(expected, FirestorePaths.selfNotePath(userId, noteId))
  }

  // ==================== Conversations Tests ====================

  @Test
  fun constants_shouldHaveConversationsValue() {
    // Verify the CONVERSATIONS constant is correctly defined
    TestCase.assertEquals("conversations", FirestorePaths.CONVERSATIONS)
  }

  @Test
  fun conversationsPath_shouldReturnCorrectPath() {
    // Conversations is a top-level collection, so path is just the collection name
    val expected = "conversations"
    TestCase.assertEquals(expected, FirestorePaths.conversationsPath())
  }

  @Test
  fun conversationPath_shouldReturnCorrectPath() {
    // Individual conversation document path
    val conversationId = "conv123"
    val expected = "conversations/conv123"
    TestCase.assertEquals(expected, FirestorePaths.conversationPath(conversationId))
  }
}

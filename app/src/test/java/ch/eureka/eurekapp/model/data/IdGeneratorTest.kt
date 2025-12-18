// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code
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
  fun generateUserId_startsWithUserPrefix() {
    val userId = IdGenerator.generateUserId()
    TestCase.assertTrue(userId.startsWith("user_"))
  }

  @Test
  fun generateUserId_isUnique() {
    val userId1 = IdGenerator.generateUserId()
    val userId2 = IdGenerator.generateUserId()
    TestCase.assertFalse(userId1 == userId2)
  }

  @Test
  fun generateProjectId_startsWithProjectPrefix() {
    val projectId = IdGenerator.generateProjectId()
    TestCase.assertTrue(projectId.startsWith("project_"))
  }

  @Test
  fun generateProjectId_isUnique() {
    val projectId1 = IdGenerator.generateProjectId()
    val projectId2 = IdGenerator.generateProjectId()
    TestCase.assertFalse(projectId1 == projectId2)
  }

  @Test
  fun generateTaskId_startsWithTaskPrefix() {
    val taskId = IdGenerator.generateTaskId()
    TestCase.assertTrue(taskId.startsWith("task_"))
  }

  @Test
  fun generateTaskId_isUnique() {
    val taskId1 = IdGenerator.generateTaskId()
    val taskId2 = IdGenerator.generateTaskId()
    TestCase.assertFalse(taskId1 == taskId2)
  }

  @Test
  fun generateTaskTemplateId_startsWithTemplatePrefix() {
    val templateId = IdGenerator.generateTaskTemplateId()
    TestCase.assertTrue(templateId.startsWith("template_"))
  }

  @Test
  fun generateTaskTemplateId_isUnique() {
    val templateId1 = IdGenerator.generateTaskTemplateId()
    val templateId2 = IdGenerator.generateTaskTemplateId()
    TestCase.assertFalse(templateId1 == templateId2)
  }

  @Test
  fun generateMeetingId_startsWithMeetingPrefix() {
    val meetingId = IdGenerator.generateMeetingId()
    TestCase.assertTrue(meetingId.startsWith("meeting_"))
  }

  @Test
  fun generateMeetingId_isUnique() {
    val meetingId1 = IdGenerator.generateMeetingId()
    val meetingId2 = IdGenerator.generateMeetingId()
    TestCase.assertFalse(meetingId1 == meetingId2)
  }

  @Test
  fun generateChatChannelId_startsWithChannelPrefix() {
    val channelId = IdGenerator.generateChatChannelId()
    TestCase.assertTrue(channelId.startsWith("channel_"))
  }

  @Test
  fun generateChatChannelId_isUnique() {
    val channelId1 = IdGenerator.generateChatChannelId()
    val channelId2 = IdGenerator.generateChatChannelId()
    TestCase.assertFalse(channelId1 == channelId2)
  }

  @Test
  fun generateMessageId_startsWithMsgPrefix() {
    val messageId = IdGenerator.generateMessageId()
    TestCase.assertTrue(messageId.startsWith("msg_"))
  }

  @Test
  fun generateMessageId_isUnique() {
    val messageId1 = IdGenerator.generateMessageId()
    val messageId2 = IdGenerator.generateMessageId()
    TestCase.assertFalse(messageId1 == messageId2)
  }

  @Test
  fun generateFieldId_sanitizesLabel() {
    val fieldId = IdGenerator.generateFieldId("My Field Label!")
    TestCase.assertTrue(fieldId.startsWith("my_field_label_"))
  }

  @Test
  fun generateFieldId_isUnique() {
    val fieldId1 = IdGenerator.generateFieldId("Test Field")
    val fieldId2 = IdGenerator.generateFieldId("Test Field")
    TestCase.assertFalse(fieldId1 == fieldId2)
  }

  @Test
  fun generateFieldId_handlesEmptyLabel() {
    val fieldId = IdGenerator.generateFieldId("")
    TestCase.assertTrue(fieldId.startsWith("field_"))
    TestCase.assertTrue(fieldId.length > 6)
  }

  @Test
  fun generateFieldId_handlesSpecialCharacters() {
    val fieldId = IdGenerator.generateFieldId("@#$%^&*()")
    TestCase.assertTrue(fieldId.startsWith("field_"))
  }

  @Test
  fun generateFieldId_truncatesLongLabels() {
    val longLabel = "a".repeat(50)
    val fieldId = IdGenerator.generateFieldId(longLabel)
    val sanitizedPart = fieldId.substringBefore("_")
    TestCase.assertTrue(sanitizedPart.length <= 20)
  }

  @Test
  fun generateFieldId_appendsUuidSuffix() {
    val fieldId = IdGenerator.generateFieldId("test")
    val parts = fieldId.split("_", limit = 2)
    TestCase.assertEquals(2, parts.size)
    TestCase.assertEquals("test", parts[0])
    TestCase.assertEquals(36, parts[1].length) // Full UUID with hyphens
  }

  @Test
  fun generateUniqueToken_startsWithTokenPrefix() {
    val token = IdGenerator.generateUniqueToken()
    TestCase.assertTrue(token.startsWith("token_"))
  }

  @Test
  fun generateUniqueToken_isUnique() {
    val token1 = IdGenerator.generateUniqueToken()
    val token2 = IdGenerator.generateUniqueToken()
    TestCase.assertFalse(token1 == token2)
  }

  @Test
  fun generateUniqueToken_containsValidUuid() {
    val token = IdGenerator.generateUniqueToken()
    val uuidPart = token.removePrefix("token_")
    TestCase.assertEquals(36, uuidPart.length) // UUID format with hyphens
  }

  @Test
  fun generateFieldId_handlesWhitespaceOnlyLabel() {
    val fieldId = IdGenerator.generateFieldId("   ")
    TestCase.assertTrue(fieldId.startsWith("field_"))
  }

  @Test
  fun generateFieldId_handlesUnicodeCharacters() {
    val fieldId = IdGenerator.generateFieldId("Café résumé")
    TestCase.assertTrue(fieldId.contains("caf") || fieldId.startsWith("field_"))
    TestCase.assertTrue(fieldId.length > 6)
  }

  @Test
  fun generateFieldId_handlesSingleCharacterLabel() {
    val fieldId = IdGenerator.generateFieldId("a")
    TestCase.assertTrue(fieldId.startsWith("a_"))
  }

  @Test
  fun generateFieldId_collapsesConsecutiveSpecialCharacters() {
    val fieldId = IdGenerator.generateFieldId("field!!!name")
    TestCase.assertTrue(fieldId.contains("field_name_"))
  }
}

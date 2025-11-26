package ch.eureka.eurekapp.model.data

import java.util.UUID

/**
 * Object providing ID generation functions for all data models.
 *
 * Generates unique IDs with type-specific prefixes to make IDs self-documenting and easier to
 * debug. All IDs use UUID v4 for guaranteed uniqueness.
 *
 * Note: This file was co-authored by Claude Code.
 */
object IdGenerator {
  /** Generates a unique user ID with "user_" prefix. */
  fun generateUserId(): String = "user_${UUID.randomUUID()}"

  /** Generates a unique project ID with "project_" prefix. */
  fun generateProjectId(): String = "project_${UUID.randomUUID()}"

  /** Generates a unique task ID with "task_" prefix. */
  fun generateTaskId(): String = "task_${UUID.randomUUID()}"

  /** Generates a unique task template ID with "template_" prefix. */
  fun generateTaskTemplateId(): String = "template_${UUID.randomUUID()}"

  /** Generates a unique meeting ID with "meeting_" prefix. */
  fun generateMeetingId(): String = "meeting_${UUID.randomUUID()}"

  /** Generates a unique chat channel ID with "channel_" prefix. */
  fun generateChatChannelId(): String = "channel_${UUID.randomUUID()}"

  /** Generates a unique message ID with "msg_" prefix. */
  fun generateMessageId(): String = "msg_${UUID.randomUUID()}"

  fun generateUniqueToken(): String = "token_${UUID.randomUUID()}"

  /** Generates a unique field ID with "field_" prefix. */
  fun generateFieldId(label: String): String = "field_${UUID.randomUUID()}"
}

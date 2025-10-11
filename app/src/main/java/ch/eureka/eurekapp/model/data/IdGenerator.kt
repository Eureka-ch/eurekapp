package ch.eureka.eurekapp.model.data

import java.util.UUID

object IdGenerator {
  fun generateUserId(): String = "user_${UUID.randomUUID()}"

  fun generateProjectId(): String = "project_${UUID.randomUUID()}"

  fun generateTaskId(): String = "task_${UUID.randomUUID()}"

  fun generateTaskTemplateId(): String = "template_${UUID.randomUUID()}"

  fun generateMeetingId(): String = "meeting_${UUID.randomUUID()}"

  fun generateChatChannelId(): String = "channel_${UUID.randomUUID()}"

  fun generateMessageId(): String = "msg_${UUID.randomUUID()}"
}

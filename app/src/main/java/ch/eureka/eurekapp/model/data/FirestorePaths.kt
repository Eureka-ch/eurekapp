package ch.eureka.eurekapp.model.data

object FirestorePaths {
  // Top-level collections
  const val USERS = "users"
  const val PROJECTS = "projects"
  const val CHAT_CHANNELS = "chatChannels"
  const val MESSAGES = "messages"
  const val MEETINGS = "meetings"
  const val TASK_TEMPLATES = "taskTemplates"
  const val TASKS = "tasks"

  fun userPath(userId: String) = "$USERS/$userId"

  fun projectPath(projectId: String) = "$PROJECTS/$projectId"

  fun chatChannelsPath(projectId: String) = "${projectPath(projectId)}/$CHAT_CHANNELS"

  fun chatChannelPath(projectId: String, channelId: String) =
      "${chatChannelsPath(projectId)}/$channelId"

  fun messagesPath(projectId: String, channelId: String) =
      "${chatChannelPath(projectId, channelId)}/messages"

  fun messagePath(projectId: String, channelId: String, messageId: String) =
      "${messagesPath(projectId, channelId)}/$messageId"

  fun meetingsPath(projectId: String) = "${projectPath(projectId)}/meetings"

  fun meetingPath(projectId: String, meetingId: String) = "${meetingsPath(projectId)}/$meetingId"

  fun taskTemplatesPath(projectId: String) = "${projectPath(projectId)}/taskTemplates"

  fun taskTemplatePath(projectId: String, templateId: String) =
      "${taskTemplatesPath(projectId)}/$templateId"

  fun tasksPath(projectId: String) = "${projectPath(projectId)}/tasks"

  fun taskPath(projectId: String, taskId: String) = "${tasksPath(projectId)}/$taskId"

  fun membersPath(projectId: String) = "${projectPath(projectId)}/members"

  fun memberPath(projectId: String, userId: String) = "${membersPath(projectId)}/$userId"

  fun participantsPath(projectId: String, meetingId: String) =
      "${meetingPath(projectId, meetingId)}/participants"

  fun participantPath(projectId: String, meetingId: String, userId: String) =
      "${participantsPath(projectId, meetingId)}/$userId"
}

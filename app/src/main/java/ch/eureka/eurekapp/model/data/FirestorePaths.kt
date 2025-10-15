package ch.eureka.eurekapp.model.data

/**
 * Object containing constants and helper functions for Firestore document paths.
 *
 * This centralizes all Firestore path logic to ensure consistency across the application and make
 * path structure changes easier to manage. The architecture uses a project-centric design with
 * subcollections for tasks, meetings, members, and chat channels.
 *
 * Note: This file was co-authored by Claude Code.
 */
object FirestorePaths {
  /** Top-level users collection */
  const val USERS = "users"

  /** Top-level projects collection */
  const val PROJECTS = "projects"

  /** Chat channels subcollection name */
  const val CHAT_CHANNELS = "chatChannels"

  /** Messages subcollection name */
  const val MESSAGES = "messages"

  /** Meetings subcollection name */
  const val MEETINGS = "meetings"

  /** Task templates subcollection name */
  const val TASK_TEMPLATES = "taskTemplates"

  /** Tasks subcollection name */
  const val TASKS = "tasks"

  /** Top-level invitations collection */
  const val INVITATIONS = "invitations"

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

  fun invitationsPath() = INVITATIONS

  fun invitationPath(invitationId: String) = "$INVITATIONS/$invitationId"
}

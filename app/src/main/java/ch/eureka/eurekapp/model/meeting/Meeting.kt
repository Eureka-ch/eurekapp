package ch.eureka.eurekapp.model.meeting

import ch.eureka.eurekapp.model.annotations.firestore.*

enum class ContextType {
  WORKSPACE,
  GROUP,
  PROJECT,
  TASK
}

@CollectionPath("workspaces/{workspaceId}/meetings")
@Rules(
    read = "request.auth != null && request.auth.uid in resource.data.participants.keys()",
    create =
        "request.auth != null && request.auth.uid in request.resource.data.participants.keys()",
    update = "request.auth != null && request.auth.uid in resource.data.participants.keys()",
    delete = "request.auth != null && resource.data.participants[request.auth.uid] == 'host'")
data class Meeting(
    @Required @Immutable val meetingID: String = "",
    @Required @Immutable val workspaceId: String = "", // For nested path
    @Required @Immutable val contextId: String = "", // workspaceId, groupId, projectId, or taskId
    @Required @Immutable val contextType: ContextType = ContextType.WORKSPACE,
    @Required @Length(min = 1, max = 200) val title: String = "",
    @Required val status: String = "", // scheduled, in_progress, completed, cancelled
    @Required val participants: Map<String, String> = emptyMap(), // userId -> role (host, participant)
    val attachmentUrls: List<String> = emptyList()
)

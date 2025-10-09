package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*

enum class ContextType {
    WORKSPACE,
    GROUP,
    PROJECT,
    TASK
}

@CollectionPath("meetings")
@Rules(
    read = "request.auth != null && request.auth.uid in resource.data.participants.keys()",
    create = "request.auth != null && request.auth.uid in request.resource.data.participants.keys()",
    update = "request.auth != null && request.auth.uid in resource.data.participants.keys()",
    delete = "request.auth != null && request.auth.uid in resource.data.participants.keys()"
)
data class Meeting(
    @Required
    @Immutable
    val meetingID: String,

    @Required
    @Immutable
    val contextId: String,

    @Required
    @Immutable
    val contextType: ContextType,

    @Required
    @Length(min = 1, max = 200)
    val title: String,

    @Required
    val status: String,

    @Required
    val participants: Map<String, String>,

    val attachmentUrls: List<String>,

    val imageUris: List<String>
)

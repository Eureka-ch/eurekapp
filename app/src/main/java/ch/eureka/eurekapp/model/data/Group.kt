package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.annotations.firestore.*

@CollectionPath("workspaces/{workspaceId}/groups")
@Rules(
    read = "request.auth != null && request.auth.uid in resource.data.members.keys()",
    create = "request.auth != null && request.auth.uid in request.resource.data.members.keys()",
    update = "request.auth != null && request.auth.uid in resource.data.members.keys()",
    delete = "request.auth != null && request.auth.uid in resource.data.members.keys() && resource.data.members[request.auth.uid] == 'admin'"
)
data class Group(
    @Required
    @Immutable
    val groupID: String,

    @Required
    @Immutable
    val workspaceId: String,

    @Required
    @Length(min = 1, max = 100)
    val name: String,

    @Required
    val members: Map<String, String>
)

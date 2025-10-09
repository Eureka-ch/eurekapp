package ch.eureka.eurekapp.model.workspace

import ch.eureka.eurekapp.model.annotations.firestore.*

@CollectionPath("workspaces")
@Rules(
    read = "request.auth != null && request.auth.uid in resource.data.members.keys()",
    create = "request.auth != null && request.auth.uid in request.resource.data.members.keys()",
    update = "request.auth != null && request.auth.uid in resource.data.members.keys()",
    delete =
        "request.auth != null && request.auth.uid in resource.data.members.keys() && resource.data.members[request.auth.uid] == 'owner'")
data class Workspace(
    @Required @Immutable val workspaceId: String,
    @Required @Length(min = 1, max = 100) val name: String,
    @Required val isPersonal: Boolean,
    @Required val members: Map<String, String> // userId -> role (owner, admin, member)
)

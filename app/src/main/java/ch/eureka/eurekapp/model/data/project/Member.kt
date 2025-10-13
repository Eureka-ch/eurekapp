package ch.eureka.eurekapp.model.data.project

/**
 * Data class representing a member of a project.
 *
 * Members are stored as a subcollection under each project document, allowing for role-based access
 * control and permissions management.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property userId The ID of the user who is a member of the project.
 * @property role The role of the member (owner, admin, member).
 */
data class Member(val userId: String = "", val role: String = "")

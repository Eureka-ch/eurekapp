package ch.eureka.eurekapp.model.data.invitation

import com.google.firebase.Timestamp

/**
 * Data class representing a project invitation in the application.
 *
 * Invitations allow users to join projects using unique tokens. The token serves as the document
 * ID in Firestore for direct lookup. Each invitation can only be used once.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property token Unique token string (serves as document ID in Firestore).
 * @property projectId ID of the project this invitation is for.
 * @property isUsed Whether this invitation has been used.
 * @property usedBy User ID of the person who used this invitation (null if unused).
 * @property usedAt Timestamp when the invitation was used (null if unused).
 */
data class Invitation(
    val token: String = "",
    val projectId: String = "",
    val isUsed: Boolean = false,
    val usedBy: String? = null,
    val usedAt: Timestamp? = null
)

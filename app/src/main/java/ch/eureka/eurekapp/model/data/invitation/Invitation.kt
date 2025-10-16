package ch.eureka.eurekapp.model.data.invitation

import com.google.firebase.Timestamp

/**
 * Data class representing a project invitation in the application.
 *
 * Invitations allow users to join projects using unique tokens. The token serves as the document ID
 * in Firestore for direct lookup. Each invitation can only be used once.
 *
 * IMPORTANT: This class uses var properties (not val) to enable Firestore deserialization.
 * Firestore requires mutable properties to set values after object construction.
 *
 * @property token Unique token string (serves as document ID in Firestore).
 * @property projectId ID of the project this invitation is for.
 * @property isUsed Whether this invitation has been used.
 * @property usedBy User ID of the person who used this invitation (null if unused).
 * @property usedAt Timestamp when the invitation was used (null if unused).
 */
data class Invitation(
    var token: String = "",
    var projectId: String = "",
    var isUsed: Boolean = false,
    var usedBy: String? = null,
    var usedAt: Timestamp? = null
)

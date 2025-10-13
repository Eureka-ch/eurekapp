package ch.eureka.eurekapp.model.data.user

import com.google.firebase.Timestamp

/**
 * Data class representing a user in the application.
 *
 * User data is synchronized with Firebase Authentication and stored in Firestore for additional
 * profile information and activity tracking.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property uid Unique identifier for the user (matches Firebase Auth UID).
 * @property displayName The user's display name.
 * @property email The user's email address.
 * @property photoUrl URL to the user's profile photo.
 * @property lastActive Timestamp of the user's last activity in the application.
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val lastActive: Timestamp = Timestamp(0, 0)
)

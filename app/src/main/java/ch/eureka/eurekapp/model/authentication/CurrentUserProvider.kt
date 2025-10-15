package ch.eureka.eurekapp.model.authentication

// Portions of this code were generated with the help of ChatGPT.

/** Provides the current user's ID, or null if not authenticated. */
interface CurrentUserProvider {
  val currentUserId: String?
}

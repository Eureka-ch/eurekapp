package ch.eureka.eurekapp.model.authentication

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Portions of this code were generated with the help of ChatGPT.

/** Firebase implementation of CurrentUserProvider. */
class FirebaseCurrentUserProvider : CurrentUserProvider {
  override val currentUserId: String?
    get() = Firebase.auth.currentUser?.uid
}

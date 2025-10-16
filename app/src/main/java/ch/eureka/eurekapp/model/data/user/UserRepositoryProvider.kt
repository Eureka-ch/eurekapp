package ch.eureka.eurekapp.model.data.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Provides a singleton instance of UserRepository */
object UserRepositoryProvider {
  val repository: UserRepository by lazy {
    FirestoreUserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }
}

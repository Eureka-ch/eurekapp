package ch.eureka.eurekapp.model.user

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

  override fun getUserById(userId: String): Flow<User?> = callbackFlow {
    val listener =
        firestore.collection("users").document(userId).addSnapshotListener { snapshot, error ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          trySend(snapshot?.toObject(User::class.java))
        }
    awaitClose { listener.remove() }
  }

  override fun getCurrentUser(): Flow<User?> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(null)
      close()
      return@callbackFlow
    }

    val listener =
        firestore.collection("users").document(currentUserId).addSnapshotListener { snapshot, error
          ->
          if (error != null) {
            close(error)
            return@addSnapshotListener
          }
          trySend(snapshot?.toObject(User::class.java))
        }
    awaitClose { listener.remove() }
  }

  override suspend fun saveUser(user: User): Result<Unit> = runCatching {
    firestore.collection("users").document(user.uid).set(user).await()
  }

  override suspend fun updateLastActive(userId: String): Result<Unit> = runCatching {
    firestore
        .collection("users")
        .document(userId)
        .update("lastActive", Timestamp(Date(System.currentTimeMillis())))
        .await()
  }
}

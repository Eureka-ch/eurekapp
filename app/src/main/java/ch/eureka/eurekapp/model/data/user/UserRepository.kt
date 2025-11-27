package ch.eureka.eurekapp.model.data.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {
  /** Get user by ID with real-time updates */
  fun getUserById(userId: String): Flow<User?>

  /** Get current authenticated user with real-time updates */
  fun getCurrentUser(): Flow<User?>

  /** Create or update user profile */
  suspend fun saveUser(user: User): Result<Unit>

  /** Update last active timestamp for user */
  suspend fun updateLastActive(userId: String): Result<Unit>

  /** Update fcmToken for user */
  suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit>
}

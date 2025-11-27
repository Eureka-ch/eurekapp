package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Configurable mock implementation of UserRepository for testing
 *
 * Allows tests to configure user data, flows, and error scenarios
 */
class MockUserRepository : UserRepository {
  private val users = mutableMapOf<String, User>()
  private val userFlows = mutableMapOf<String, Flow<User?>>()
  private val errors = mutableMapOf<String, Exception>()

  // Track method calls for verification
  val getUserByIdCalls = mutableListOf<String>()

  /** Configure users that exist in the repository */
  fun setUsers(vararg users: User) {
    users.forEach { this.users[it.uid] = it }
  }

  /** Configure a specific flow to return for a user ID */
  fun setUserFlow(userId: String, flow: Flow<User?>) {
    userFlows[userId] = flow
  }

  /** Configure a user to return for a user ID (simplified) */
  fun setUser(userId: String, flow: Flow<User?>) {
    userFlows[userId] = flow
  }

  /** Configure an error to throw for a specific user ID */
  fun setError(userId: String, error: Exception) {
    errors[userId] = error
  }

  /** Clear all configuration */
  fun reset() {
    users.clear()
    userFlows.clear()
    errors.clear()
    getUserByIdCalls.clear()
  }

  override fun getUserById(userId: String): Flow<User?> {
    getUserByIdCalls.add(userId)

    // If error configured, throw it
    errors[userId]?.let { throw it }

    // If custom flow configured, return it
    userFlows[userId]?.let {
      return it
    }

    // Otherwise return user from map or null
    return flowOf(users[userId])
  }

  override fun getCurrentUser(): Flow<User?> = flowOf(null)

  override suspend fun saveUser(user: User): Result<Unit> = Result.success(Unit)

  override suspend fun updateLastActive(userId: String): Result<Unit> = Result.success(Unit)

  override suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> {
    return Result.success(Unit)
  }
}

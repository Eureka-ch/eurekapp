/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// portions of this code and documentation were generated with the help of AI.
/**
 * Configurable mock implementation of UserRepository for testing
 *
 * Allows tests to configure user data, flows, and error scenarios
 */
class MockUserRepository : UserRepository {
  private val users = mutableMapOf<String, User>()
  private val userFlows = mutableMapOf<String, Flow<User?>>()
  private val errors = mutableMapOf<String, Exception>()
  private var currentUserFlow: Flow<User?> = flowOf(null)

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

  /** Configure an error to throw for a specific user ID */
  fun setError(userId: String, error: Exception) {
    errors[userId] = error
  }

  /** Configure the flow returned by getCurrentUser() */
  fun setCurrentUser(flow: Flow<User?>) {
    currentUserFlow = flow
  }

  /** Configure a specific user flow returned by getUserById */
  fun setUser(userId: String, flow: Flow<User?>) {
    userFlows[userId] = flow
  }

  /** Clear all configuration */
  fun reset() {
    users.clear()
    userFlows.clear()
    errors.clear()
    getUserByIdCalls.clear()
    currentUserFlow = flowOf(null)
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

  override fun getCurrentUser(): Flow<User?> = currentUserFlow

  override suspend fun saveUser(user: User): Result<Unit> = Result.success(Unit)

  override suspend fun updateLastActive(userId: String): Result<Unit> = Result.success(Unit)

  override suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> {
    return Result.success(Unit)
  }
}

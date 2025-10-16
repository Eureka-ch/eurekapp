package ch.eureka.eurekapp.model.authentication

import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AuthRepositoryProviderTest {

  @Test
  fun repositoryIsInitializedAndIsAuthRepositoryType() {
    val instance: AuthRepository = AuthRepositoryProvider.repository

    TestCase.assertNotNull(instance)

    val secondInstance = AuthRepositoryProvider.repository
    TestCase.assertEquals(instance, secondInstance)
  }

  @Test
  fun getUserLoggedInId() {
    val instance: AuthRepository = AuthRepositoryProvider.repository
    val userId = instance.getUserId()
    assertTrue(userId.isFailure) // no logged in user
  }
}

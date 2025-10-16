package ch.eureka.eurekapp.model.authentication

import junit.framework.TestCase
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
    assert(userId.isSuccess) // no logged in user
  }
}

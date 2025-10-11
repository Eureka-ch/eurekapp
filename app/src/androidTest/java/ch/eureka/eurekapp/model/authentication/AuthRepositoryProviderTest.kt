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
}

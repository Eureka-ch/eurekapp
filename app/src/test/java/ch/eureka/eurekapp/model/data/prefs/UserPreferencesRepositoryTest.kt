/* Portions of this file were generated with the help of Claude (Sonnet 4.5). */
package ch.eureka.eurekapp.model.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [UserPreferencesRepository].
 *
 * Uses Robolectric to simulate the Android Context and DataStore file operations locally on the
 * JVM.
 */
@RunWith(RobolectricTestRunner::class)
class UserPreferencesRepositoryTest {

  private lateinit var context: Context
  private lateinit var repository: UserPreferencesRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    repository = UserPreferencesRepository(context)

    runBlocking { context.dataStore.edit { it.clear() } }
  }

  @Test
  fun isCloudStorageEnabled_returnsFalseByDefault() = runTest {
    val isEnabled = repository.isCloudStorageEnabled.first()

    assertFalse("Default value should be false (Local only)", isEnabled)
  }

  @Test
  fun setCloudStorageEnabled_toTrueUpdatesFlow() = runTest {
    repository.setCloudStorageEnabled(true)
    val isEnabled = repository.isCloudStorageEnabled.first()

    assertTrue("Should be true after enabling", isEnabled)
  }

  @Test
  fun setCloudStorageEnabled_togglesValueCorrectly() = runTest {
    repository.setCloudStorageEnabled(true)
    assertTrue(repository.isCloudStorageEnabled.first())

    repository.setCloudStorageEnabled(false)
    val isEnabled = repository.isCloudStorageEnabled.first()

    assertFalse("Should be false after disabling", isEnabled)
  }
}

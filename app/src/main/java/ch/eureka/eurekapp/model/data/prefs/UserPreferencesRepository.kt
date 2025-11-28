/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore extension property
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * Repository for managing user preferences (like Local vs Cloud storage).
 *
 * @property context The application context.
 */
class UserPreferencesRepository(private val context: Context) {

  companion object {
    private val IS_CLOUD_STORAGE_ENABLED = booleanPreferencesKey("is_cloud_storage_enabled")
  }

  /**
   * Flow that emits true if the user wants to save to Cloud, false for Local only. Defaults to
   * false (Local) for privacy.
   */
  val isCloudStorageEnabled: Flow<Boolean> =
      context.dataStore.data.map { preferences -> preferences[IS_CLOUD_STORAGE_ENABLED] ?: false }

  /**
   * Toggles the storage mode.
   *
   * @param enabled True if enable cloud storage, false otherwise.
   */
  suspend fun setCloudStorageEnabled(enabled: Boolean) {
    context.dataStore.edit { preferences -> preferences[IS_CLOUD_STORAGE_ENABLED] = enabled }
  }
}

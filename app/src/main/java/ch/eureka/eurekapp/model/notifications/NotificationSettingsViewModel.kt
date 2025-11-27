// Portions of this code were generated with the help of Gemini 3 Pro
package ch.eureka.eurekapp.model.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.data.user.defaultValuesNotificationSettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user notification preferences. Handles interaction between the
 * UI and the UserRepository to read/write settings.
 */
class NotificationSettingsViewModel(
    private val usersRepository: UserRepository = FirestoreRepositoriesProvider.userRepository
) : ViewModel() {

  /**
   * Updates a specific notification setting for the current user.
   *
   * @param userNotificationSetting The specific setting key to update.
   * @param value The new boolean value for the setting.
   * @param onFailure Callback invoked with an error message if the operation fails or user is null.
   * @param onSuccess Callback invoked when the setting is successfully saved to the repository.
   */
  fun saveUserSetting(
      userNotificationSetting: UserNotificationSettingsKeys,
      value: Boolean,
      onFailure: (String) -> Unit,
      onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val currentUser = usersRepository.getCurrentUser().first()

      if (currentUser == null) {
        onFailure("No user was associated with this app")
        return@launch
      }

      val updatedSettings = currentUser.notificationSettings.toMutableMap()
      updatedSettings[userNotificationSetting.name] = value

      val updatedUser = currentUser.copy(notificationSettings = updatedSettings)
      usersRepository
          .saveUser(updatedUser)
          .fold(
              onSuccess = { onSuccess() },
              onFailure = { error ->
                Log.d("NotificationSettingsViewModel", error.message.toString())
                onFailure(error.message ?: "Unknown error while saving settings")
              })
    }
  }

  /**
   * Retrieves a stream of the current value for a specific notification setting. If the user has
   * not explicitly set a preference, a default value is returned.
   *
   * @param userNotificationSetting The setting key to observe.
   * @return A Flow emitting the current boolean state of the setting.
   */
  fun getUserSetting(userNotificationSetting: UserNotificationSettingsKeys): Flow<Boolean> {
    return usersRepository.getCurrentUser().map { user ->
      // Return the user's setting if it exists, otherwise fall back to the app default
      user?.notificationSettings?.get(userNotificationSetting.name)
          ?: defaultValuesNotificationSettingsKeys.getOrDefault(userNotificationSetting.name, true)
    }
  }
}

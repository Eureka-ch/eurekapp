package ch.eureka.eurekapp.model.notifications

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

class NotificationSettingsViewModel(
    private val usersRepository: UserRepository = FirestoreRepositoriesProvider.userRepository
): ViewModel() {
    fun saveUserSetting(userNotificationSetting: UserNotificationSettingsKeys, value: Boolean,
                        onFailure: (String) -> Unit, onSuccess: () -> Unit){
        viewModelScope.launch {
            val currentUser = usersRepository.getCurrentUser().first()
            if(currentUser == null){
                onFailure("No user was associated with this app")
                return@launch
            }
            val currentUserValueCopied = currentUser
                .copy(notificationSettings =
                    currentUser.notificationSettings + (userNotificationSetting.name to value))
            usersRepository.saveUser(currentUserValueCopied).onSuccess {
                onSuccess()
            }.onFailure { error ->
                onFailure(error.message.toString())
            }
        }
    }

    fun getUserSetting(userNotificationSetting: UserNotificationSettingsKeys): Flow<Boolean> {
        return usersRepository.getCurrentUser().map { user ->
            user?.notificationSettings?.get(userNotificationSetting.name) ?:
            defaultValuesNotificationSettingsKeys
                .getOrDefault(userNotificationSetting.name, true)
        }
    }
}
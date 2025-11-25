package ch.eureka.eurekapp.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.defaultValuesNotificationSettingsKeys
import ch.eureka.eurekapp.model.notifications.NotificationSettingsViewModel
import ch.eureka.eurekapp.model.notifications.NotificationType
import ch.eureka.eurekapp.ui.theme.Typography

data class NotificationSettingState(
    val userNotificationSettingsKey: UserNotificationSettingsKeys,
    val value: Boolean,
    val onValueChange: (Boolean) -> Unit
)

@Composable
fun NotificationPreferencesScreen(
    modifier: Modifier = Modifier,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel()
){
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        NotificationOptionsCategory(
            title = "Meeting Notifications:",
            optionsList = UserNotificationSettingsKeys.entries
                .filter { entry -> entry.notificationType == NotificationType.MEETING_NOTIFICATION  }
                .map { entry ->
                    createNotificationStateFromNotificationsSettingsKey(entry, notificationSettingsViewModel) }
        )
        NotificationOptionsCategory(
            title = "Message Notifications:",
            optionsList = UserNotificationSettingsKeys.entries
                .filter { entry -> entry.notificationType == NotificationType.MESSAGE_NOTIFICATION  }
                .map { entry -> createNotificationStateFromNotificationsSettingsKey(entry, notificationSettingsViewModel) }
        )
        NotificationOptionsCategory(
            title = "General Notifications:",
            optionsList = UserNotificationSettingsKeys.entries
                .filter { entry -> entry.notificationType == NotificationType.GENERAL_NOTIFICATION }
                .map { entry -> createNotificationStateFromNotificationsSettingsKey(entry, notificationSettingsViewModel) }
        )
    }
}

@Composable
fun NotificationOptionsCategory(title: String, optionsList: List<NotificationSettingState>){
    Column (
        modifier = Modifier.fillMaxWidth()
    ){
        Text(title, style = Typography.titleLarge)
        Spacer(modifier = Modifier.padding(vertical = 10.dp))
        optionsList.forEach { option ->
            OptionBooleanSwitch(
                value = option.value,
                title = option.userNotificationSettingsKey.displayName,
                onValueChange = option.onValueChange
            )
        }
    }
}

@Composable
fun OptionBooleanSwitch(
    value: Boolean,
    title: String,
    onValueChange: (Boolean) -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(0.8f).padding(horizontal = 20.dp)
        ){
            Text(title, style = Typography.titleMedium)
        }
        Row(
            modifier = Modifier.weight(0.2f)
        ){
            Spacer(Modifier.padding(horizontal = 10.dp))
            Switch(
                checked = value,
                onCheckedChange = onValueChange
            )
        }
    }
}

@Composable
private fun createNotificationStateFromNotificationsSettingsKey(key: UserNotificationSettingsKeys,
                                                                notificationSettingsViewModel: NotificationSettingsViewModel):
        NotificationSettingState{
    return NotificationSettingState(
        userNotificationSettingsKey = key,
        value = remember{notificationSettingsViewModel.getUserSetting(key)}.collectAsState(
            defaultValuesNotificationSettingsKeys.getOrDefault(key.name, true)
        ).value,
        onValueChange = { newValue ->
            notificationSettingsViewModel.saveUserSetting(key, newValue, {}, {})
        }
    )
}
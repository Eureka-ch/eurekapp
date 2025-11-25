package ch.eureka.eurekapp.ui.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.theme.Typography

data class NotificationSettingState(
    val title: String,
    val value: Boolean,
    val mapKey: String,
    val onValueChange: (Boolean) -> Unit
)

@Composable
fun NotificationPreferencesScreen(){
    Column(){
    }
}

@Composable
fun NotificationOptionsCategory(optionsList: List<NotificationSettingState>){
    optionsList.forEach { option -> @androidx.compose.runtime.Composable {
        OptionBooleanSwitch(
            value = option.value,
            title = option.title,
            onValueChange = option.onValueChange
        )
    }   }
}

@Composable
fun OptionBooleanSwitch(
    value: Boolean,
    title: String,
    onValueChange: (Boolean) -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = Typography.titleMedium)
        Spacer(Modifier.padding(horizontal = 10.dp))
        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}
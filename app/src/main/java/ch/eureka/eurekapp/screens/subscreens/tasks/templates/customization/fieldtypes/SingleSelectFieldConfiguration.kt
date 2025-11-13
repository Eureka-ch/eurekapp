package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.eureka.eurekapp.model.data.template.field.FieldType

@Composable
fun SingleSelectFieldConfiguration(
    fieldType: FieldType.SingleSelect,
    onUpdate: (FieldType.SingleSelect) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = fieldType.allowCustom,
          onCheckedChange = { onUpdate(fieldType.copy(allowCustom = it)) },
          enabled = enabled,
          modifier = Modifier.testTag("single_select_allow_custom"))
      Text("Allow Custom Values")
    }
  }
}

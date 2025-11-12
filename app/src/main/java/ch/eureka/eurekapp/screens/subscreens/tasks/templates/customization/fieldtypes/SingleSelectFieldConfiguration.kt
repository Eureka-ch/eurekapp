package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.SelectOptionsEditor

@Composable
fun SingleSelectFieldConfiguration(
    fieldType: FieldType.SingleSelect,
    onUpdate: (FieldType.SingleSelect) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    SelectOptionsEditor(
        options = fieldType.options,
        onOptionsChange = { onUpdate(fieldType.copy(options = it)) },
        enabled = enabled)

    Spacer(modifier = Modifier.height(8.dp))

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

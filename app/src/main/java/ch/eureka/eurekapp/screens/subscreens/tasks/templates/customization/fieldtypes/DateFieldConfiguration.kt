package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

@Composable
fun DateFieldConfiguration(
    fieldType: FieldType.Date,
    onUpdate: (FieldType.Date) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = fieldType.minDate ?: "",
        onValueChange = { onUpdate(fieldType.copy(minDate = it.ifBlank { null })) },
        label = { Text("Min Date (YYYY-MM-DD)") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("date_min"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.maxDate ?: "",
        onValueChange = { onUpdate(fieldType.copy(maxDate = it.ifBlank { null })) },
        label = { Text("Max Date (YYYY-MM-DD)") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("date_max"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = fieldType.includeTime,
          onCheckedChange = { onUpdate(fieldType.copy(includeTime = it)) },
          enabled = enabled,
          modifier = Modifier.testTag("date_include_time"))
      Text("Include Time")
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.format ?: "yyyy-MM-dd",
        onValueChange = { onUpdate(fieldType.copy(format = it.ifBlank { null })) },
        label = { Text("Format") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("date_format"),
        colors = EurekaStyles.TextFieldColors())
  }
}

package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

@Composable
fun SelectOptionsEditor(
    options: List<SelectOption>,
    onOptionsChange: (List<SelectOption>) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text("Options (minimum 2)", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(8.dp))

    options.forEachIndexed { index, option ->
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = option.label,
                onValueChange = { newLabel ->
                  val updated = options.toMutableList()
                  val sanitizedValue = sanitizeLabelToValue(newLabel)
                  updated[index] = option.copy(label = newLabel, value = sanitizedValue)
                  onOptionsChange(updated)
                },
                label = { Text("Label") },
                enabled = enabled,
                modifier = Modifier.weight(1f).testTag("option_label_${option.value}"),
                colors = EurekaStyles.TextFieldColors())

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                  val updated = options.toMutableList()
                  updated.removeAt(index)
                  onOptionsChange(updated)
                },
                enabled = enabled && options.size > 2,
                modifier = Modifier.testTag("option_delete_${option.value}")) {
                  Icon(Icons.Default.Delete, "Delete option")
                }
          }
    }

    Button(
        onClick = {
          val nextId = options.size + 1
          val label = "Option $nextId"
          onOptionsChange(options + SelectOption(sanitizeLabelToValue(label), label, null))
        },
        enabled = enabled,
        modifier = Modifier.testTag("add_option_button")) {
          Text("Add Option")
        }
  }
}

private fun sanitizeLabelToValue(label: String): String {
  return label.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "option" }
}

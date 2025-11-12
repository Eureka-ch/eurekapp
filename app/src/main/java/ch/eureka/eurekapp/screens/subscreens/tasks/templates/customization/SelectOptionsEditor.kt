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
      Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
              value = option.value,
              onValueChange = { newValue ->
                val updated = options.toMutableList()
                updated[index] = option.copy(value = newValue)
                onOptionsChange(updated)
              },
              label = { Text("Value") },
              enabled = enabled,
              modifier = Modifier.weight(1f).testTag("option_value_${option.value}"),
              colors = EurekaStyles.TextFieldColors())

          Spacer(modifier = Modifier.width(8.dp))

          OutlinedTextField(
              value = option.label,
              onValueChange = { newLabel ->
                val updated = options.toMutableList()
                updated[index] = option.copy(label = newLabel)
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

        option.description?.let { desc ->
          OutlinedTextField(
              value = desc,
              onValueChange = { newDesc ->
                val updated = options.toMutableList()
                updated[index] = option.copy(description = newDesc.ifBlank { null })
                onOptionsChange(updated)
              },
              label = { Text("Description (optional)") },
              enabled = enabled,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(top = 4.dp)
                      .testTag("option_desc_${option.value}"),
              colors = EurekaStyles.TextFieldColors())
        }
            ?: run {
              OutlinedTextField(
                  value = "",
                  onValueChange = { newDesc ->
                    val updated = options.toMutableList()
                    updated[index] = option.copy(description = newDesc.ifBlank { null })
                    onOptionsChange(updated)
                  },
                  label = { Text("Description (optional)") },
                  enabled = enabled,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = 4.dp)
                          .testTag("option_desc_${option.value}"),
                  colors = EurekaStyles.TextFieldColors())
            }
      }
    }

    Button(
        onClick = {
          val nextId = options.size + 1
          onOptionsChange(options + SelectOption("option$nextId", "Option $nextId", null))
        },
        enabled = enabled,
        modifier = Modifier.testTag("add_option_button")) {
          Text("Add Option")
        }
  }
}

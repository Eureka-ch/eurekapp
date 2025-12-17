package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object SelectOptionsEditorTestTags {
  fun optionLabel(value: String) = "option_label_$value"

  fun optionDelete(value: String) = "option_delete_$value"

  const val ADD_OPTION_BUTTON = "add_option_button"
}

@Composable
fun SelectOptionsEditor(
    options: List<SelectOption>,
    onOptionsChange: (List<SelectOption>) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(stringResource(R.string.select_options_header), style = MaterialTheme.typography.labelLarge)
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
                  val uniqueValue = ensureUniqueValue(sanitizedValue, index, options)
                  updated[index] = option.copy(label = newLabel, value = uniqueValue)
                  onOptionsChange(updated)
                },
                label = { Text(stringResource(R.string.select_option_label)) },
                enabled = enabled,
                modifier =
                    Modifier.weight(1f)
                        .testTag(SelectOptionsEditorTestTags.optionLabel(option.value)),
                colors = EurekaStyles.textFieldColors())

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                  val updated = options.toMutableList()
                  updated.removeAt(index)
                  onOptionsChange(updated)
                },
                enabled = enabled && options.size > 2,
                modifier =
                    Modifier.testTag(SelectOptionsEditorTestTags.optionDelete(option.value))) {
                  Icon(Icons.Default.Delete, stringResource(R.string.select_option_delete_button))
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
        modifier = Modifier.testTag(SelectOptionsEditorTestTags.ADD_OPTION_BUTTON)) {
          Text(stringResource(R.string.select_option_add_button))
        }
  }
}

private fun sanitizeLabelToValue(label: String): String {
  return label.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_').ifBlank { "option" }
}

private fun ensureUniqueValue(
    newValue: String,
    currentIndex: Int,
    allOptions: List<SelectOption>
): String {
  var uniqueValue = newValue
  var counter = 1
  while (allOptions
      .filterIndexed { index, _ -> index != currentIndex }
      .any { it.value == uniqueValue }) {
    uniqueValue = "${newValue}_${counter++}"
  }
  return uniqueValue
}

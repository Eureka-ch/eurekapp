/*
 * Co-authored-by: Claude Sonnet 4.5
 */
package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object TemplateSelectionTestTags {
  const val DROPDOWN = "template_selection_dropdown"
  const val DROPDOWN_MENU = "template_selection_menu"
  const val NO_TEMPLATE_OPTION = "template_option_none"
  const val TEMPLATE_OPTION_PREFIX = "template_option_"
  const val CREATE_BUTTON = "template_create_button"
}

@Composable
fun TemplateSelectionField(
    templates: List<TaskTemplate>,
    selectedTemplateId: String?,
    onTemplateSelected: (String?) -> Unit,
    onCreateTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedTemplate = templates.find { it.templateID == selectedTemplateId }
  val displayText = selectedTemplate?.title ?: "No template"

  Column(modifier = modifier) {
    Text(text = "Use Template", style = MaterialTheme.typography.titleMedium)

    if (templates.isNotEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))

      Box(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("No template") },
            modifier = Modifier.fillMaxWidth().testTag(TemplateSelectionTestTags.DROPDOWN),
            enabled = false)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(TemplateSelectionTestTags.DROPDOWN_MENU)) {
              DropdownMenuItem(
                  text = { Text("No template") },
                  onClick = {
                    onTemplateSelected(null)
                    expanded = false
                  },
                  modifier = Modifier.testTag(TemplateSelectionTestTags.NO_TEMPLATE_OPTION))

              templates.forEach { template ->
                DropdownMenuItem(
                    text = { Text(template.title) },
                    onClick = {
                      onTemplateSelected(template.templateID)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${TemplateSelectionTestTags.TEMPLATE_OPTION_PREFIX}${template.templateID}"))
              }
            }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = onCreateTemplate,
        colors = EurekaStyles.outlinedButtonColors(),
        modifier = Modifier.testTag(TemplateSelectionTestTags.CREATE_BUTTON)) {
          Text("+ Create Template")
        }
  }
}

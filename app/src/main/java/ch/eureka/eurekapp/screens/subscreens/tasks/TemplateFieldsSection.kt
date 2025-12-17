/*
 * Co-Authored-By: Claude Opus 4.5
 */
package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue

object TemplateFieldsSectionTestTags {
  const val SECTION = "template_fields_section"
  const val SECTION_TITLE = "template_fields_section_title"

  fun field(fieldId: String) = "template_field_$fieldId"
}

@Composable
fun TemplateFieldsSection(
    template: TaskTemplate?,
    customData: TaskCustomData,
    onFieldValueChange: (fieldId: String, value: FieldValue) -> Unit,
    modifier: Modifier = Modifier,
    mode: FieldInteractionMode = FieldInteractionMode.EditOnly
) {
  if (template == null || template.definedFields.fields.isEmpty()) {
    return
  }

  Column(modifier = modifier.fillMaxWidth().testTag(TemplateFieldsSectionTestTags.SECTION)) {
    Text(
        text = stringResource(R.string.template_fields_section_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.testTag(TemplateFieldsSectionTestTags.SECTION_TITLE))

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    template.definedFields.fields.forEach { fieldDefinition ->
      val currentValue = customData.getValue(fieldDefinition.id) ?: fieldDefinition.defaultValue

      Column(modifier = Modifier.testTag(TemplateFieldsSectionTestTags.field(fieldDefinition.id))) {
        when (val fieldType = fieldDefinition.type) {
          is FieldType.Text -> {
            TextFieldComponent(
                fieldDefinition = fieldDefinition,
                value = currentValue as? FieldValue.TextValue,
                onValueChange = { onFieldValueChange(fieldDefinition.id, it) },
                mode = mode)
          }
          is FieldType.Number -> {
            NumberFieldComponent(
                fieldDefinition = fieldDefinition,
                value = currentValue as? FieldValue.NumberValue,
                onValueChange = { onFieldValueChange(fieldDefinition.id, it) },
                mode = mode)
          }
          is FieldType.Date -> {
            DateFieldComponent(
                fieldDefinition = fieldDefinition,
                value = currentValue as? FieldValue.DateValue,
                onValueChange = { onFieldValueChange(fieldDefinition.id, it) },
                mode = mode)
          }
          is FieldType.SingleSelect -> {
            SingleSelectFieldComponent(
                fieldDefinition = fieldDefinition,
                value = currentValue as? FieldValue.SingleSelectValue,
                onValueChange = { onFieldValueChange(fieldDefinition.id, it) },
                mode = mode)
          }
          is FieldType.MultiSelect -> {
            MultiSelectFieldComponent(
                fieldDefinition = fieldDefinition,
                value = currentValue as? FieldValue.MultiSelectValue,
                onValueChange = { onFieldValueChange(fieldDefinition.id, it) },
                mode = mode)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.getConstraintHint

@Composable
fun FieldCustomizationPreview(field: FieldDefinition, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth().testTag("field_preview_${field.id}")) {
    Row {
      Text(
          field.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
      if (field.required) {
        Text(" *", style = MaterialTheme.typography.titleMedium, color = Color.Red)
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        "Type: ${getFieldTypeName(field.type)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)

    field.description?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    getConstraintHint(field.type)?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

private fun getFieldTypeName(fieldType: FieldType) =
    when (fieldType) {
      is FieldType.Text -> "Text"
      is FieldType.Number -> "Number"
      is FieldType.Date -> "Date"
      is FieldType.SingleSelect -> "Single Select"
      is FieldType.MultiSelect -> "Multi Select"
    }

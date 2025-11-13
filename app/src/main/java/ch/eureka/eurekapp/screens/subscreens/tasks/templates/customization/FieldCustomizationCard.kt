package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.FieldInteractionMode
import ch.eureka.eurekapp.screens.subscreens.tasks.canToggle
import ch.eureka.eurekapp.screens.subscreens.tasks.isEditing
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.DateFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.MultiSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.NumberFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.SingleSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.TextFieldConfiguration

@Composable
fun FieldCustomizationCard(
    field: FieldDefinition,
    mode: FieldInteractionMode,
    onFieldUpdate: (FieldDefinition) -> Unit,
    onModeToggle: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    canMoveUp: Boolean = true,
    canMoveDown: Boolean = true,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  var editingField by
      remember(mode) {
        mutableStateOf(
            if (mode is FieldInteractionMode.Toggleable && mode.isEditing) field else null)
      }
  var originalField by remember(mode) { mutableStateOf<FieldDefinition?>(null) }

  var prevIsEditing by remember { mutableStateOf(mode.isEditing) }

  LaunchedEffect(mode.isEditing) {
    if (mode.isEditing && !prevIsEditing && mode is FieldInteractionMode.Toggleable) {
      originalField = field
      editingField = field
    } else if (!mode.isEditing && prevIsEditing) {
      editingField = null
      originalField = null
    }
    prevIsEditing = mode.isEditing
  }

  val currentField =
      if (mode is FieldInteractionMode.Toggleable && mode.isEditing) {
        editingField ?: field
      } else {
        field
      }

  Card(modifier = modifier.fillMaxWidth().testTag("field_customization_card_${field.id}")) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
              if (mode is FieldInteractionMode.Toggleable && mode.isEditing && expanded) {
                // Save, exit edit, collapse
                editingField?.let { onFieldUpdate(it) }
                onModeToggle()
                expanded = false
              } else if (mode is FieldInteractionMode.Toggleable && !mode.isEditing) {
                // Enter edit mode and expand
                onModeToggle()
                expanded = true
              } else {
                // Just toggle expand
                expanded = !expanded
              }
            },
            modifier = Modifier.testTag("field_expand_button")) {
              val icon =
                  when {
                    mode is FieldInteractionMode.Toggleable && mode.isEditing -> Icons.Default.Check
                    expanded -> Icons.Default.ExpandLess
                    mode is FieldInteractionMode.ViewOnly -> Icons.Default.Info
                    else -> Icons.Default.Edit
                  }
              Icon(icon, contentDescription = "Toggle")
            }

        Text(
            text = currentField.label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f))

        if (mode.canToggle && mode.isEditing) {
          IconButton(
              onClick = {
                editingField = originalField
                onModeToggle()
                expanded = false
              },
              modifier = Modifier.testTag("field_cancel_button")) {
                Icon(Icons.Default.Close, "Cancel", tint = MaterialTheme.colorScheme.error)
              }
        }

        if (mode is FieldInteractionMode.Toggleable || mode is FieldInteractionMode.EditOnly) {
          IconButton(
              onClick = onMoveUp,
              enabled = canMoveUp,
              modifier = Modifier.testTag("field_move_up_button")) {
                Icon(Icons.Default.ArrowUpward, "Move Up")
              }
          IconButton(
              onClick = onMoveDown,
              enabled = canMoveDown,
              modifier = Modifier.testTag("field_move_down_button")) {
                Icon(Icons.Default.ArrowDownward, "Move Down")
              }

          IconButton(onClick = onDuplicate, modifier = Modifier.testTag("field_duplicate_button")) {
            Icon(Icons.Default.ContentCopy, "Duplicate")
          }
        }
      }

      if (expanded) {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        if (mode.isEditing) {
          var advancedExpanded by remember { mutableStateOf(false) }

          CommonFieldConfiguration(
              field = currentField,
              onFieldUpdate = { updatedField ->
                if (mode is FieldInteractionMode.Toggleable) {
                  editingField = updatedField
                } else {
                  onFieldUpdate(updatedField)
                }
              },
              enabled = true)

          Spacer(modifier = Modifier.height(16.dp))

          // Show options editor for select fields outside advanced section
          when (val type = currentField.type) {
            is FieldType.SingleSelect,
            is FieldType.MultiSelect -> {
              val options =
                  when (type) {
                    is FieldType.SingleSelect -> type.options
                    is FieldType.MultiSelect -> type.options
                    else -> emptyList()
                  }
              SelectOptionsEditor(
                  options = options,
                  onOptionsChange = { newOptions ->
                    val newType =
                        when (type) {
                          is FieldType.SingleSelect -> type.copy(options = newOptions)
                          is FieldType.MultiSelect -> type.copy(options = newOptions)
                          else -> type
                        }
                    val updated = currentField.copy(type = newType)
                    if (mode is FieldInteractionMode.Toggleable) {
                      editingField = updated
                    } else {
                      onFieldUpdate(updated)
                    }
                  },
                  enabled = true)
              Spacer(modifier = Modifier.height(16.dp))
            }
            else -> {}
          }

          // Advanced Configuration toggle
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable { advancedExpanded = !advancedExpanded }
                      .testTag("advanced_configuration_toggle"),
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Advanced Configuration",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f))
                Icon(
                    if (advancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (advancedExpanded) "Collapse" else "Expand")
              }

          if (advancedExpanded) {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            DefaultValueInput(
                field = currentField,
                onFieldUpdate = { updatedField ->
                  if (mode is FieldInteractionMode.Toggleable) {
                    editingField = updatedField
                  } else {
                    onFieldUpdate(updatedField)
                  }
                },
                enabled = true)

            Spacer(modifier = Modifier.height(16.dp))

            when (val type = currentField.type) {
              is FieldType.Text -> {
                TextFieldConfiguration(
                    fieldType = type,
                    onUpdate = { newType ->
                      val updated = currentField.copy(type = newType)
                      if (mode is FieldInteractionMode.Toggleable) {
                        editingField = updated
                      } else {
                        onFieldUpdate(updated)
                      }
                    },
                    enabled = true)
              }
              is FieldType.Number -> {
                NumberFieldConfiguration(
                    fieldType = type,
                    onUpdate = { newType ->
                      val updated = currentField.copy(type = newType)
                      if (mode is FieldInteractionMode.Toggleable) {
                        editingField = updated
                      } else {
                        onFieldUpdate(updated)
                      }
                    },
                    enabled = true)
              }
              is FieldType.Date -> {
                DateFieldConfiguration(
                    fieldType = type,
                    onUpdate = { newType ->
                      val updated = currentField.copy(type = newType)
                      if (mode is FieldInteractionMode.Toggleable) {
                        editingField = updated
                      } else {
                        onFieldUpdate(updated)
                      }
                    },
                    enabled = true)
              }
              is FieldType.SingleSelect -> {
                SingleSelectFieldConfiguration(
                    fieldType = type,
                    onUpdate = { newType ->
                      val updated = currentField.copy(type = newType)
                      if (mode is FieldInteractionMode.Toggleable) {
                        editingField = updated
                      } else {
                        onFieldUpdate(updated)
                      }
                    },
                    enabled = true)
              }
              is FieldType.MultiSelect -> {
                MultiSelectFieldConfiguration(
                    fieldType = type,
                    onUpdate = { newType ->
                      val updated = currentField.copy(type = newType)
                      if (mode is FieldInteractionMode.Toggleable) {
                        editingField = updated
                      } else {
                        onFieldUpdate(updated)
                      }
                    },
                    enabled = true)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))

          Button(
              onClick = onDelete,
              modifier = Modifier.fillMaxWidth().testTag("field_delete_button"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.errorContainer,
                      contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Delete Field")
              }
        } else {
          FieldCustomizationPreview(currentField, showTitle = false)
        }
      }
    }
  }
}

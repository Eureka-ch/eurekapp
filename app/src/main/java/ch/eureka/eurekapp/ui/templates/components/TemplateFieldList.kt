// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import sh.calvin.reorderable.*

/**
 * Reorderable list of template fields.
 *
 * @param fields List of field definitions
 * @param editingFieldId ID of field being edited
 * @param fieldErrors Map of field errors
 * @param onFieldEdit Callback to start editing a field
 * @param onFieldSave Callback to save field changes
 * @param onFieldCancel Callback to cancel editing
 * @param onFieldDelete Callback to delete a field
 * @param onFieldDuplicate Callback to duplicate a field
 * @param onFieldsReorder Callback when fields are reordered
 * @param title Template title (optional, for scrollable header)
 * @param description Template description (optional, for scrollable header)
 * @param titleError Title validation error (optional)
 * @param onTitleChange Callback when title changes
 * @param onDescriptionChange Callback when description changes
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateFieldList(
    fields: List<FieldDefinition>,
    editingFieldId: String?,
    fieldErrors: Map<String, String>,
    onFieldEdit: (String) -> Unit,
    onFieldSave: (String, FieldDefinition) -> Unit,
    onFieldCancel: (String) -> Unit,
    onFieldDelete: (String) -> Unit,
    onFieldDuplicate: (String) -> Unit,
    onFieldsReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    titleError: String? = null,
    onTitleChange: ((String) -> Unit)? = null,
    onDescriptionChange: ((String) -> Unit)? = null
) {
  val lazyListState = rememberLazyListState()
  // Calculate offset: 1 if basic info section is present, 0 otherwise
  val hasBasicInfo = title != null && onTitleChange != null && onDescriptionChange != null
  val indexOffset = if (hasBasicInfo) 1 else 0

  val reorderableState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        // Adjust indices to account for basic info section
        val fromFieldIndex = from.index - indexOffset
        val toFieldIndex = to.index - indexOffset
        // Only reorder if both indices are valid field indices
        if (fromFieldIndex >= 0 && toFieldIndex >= 0) {
          onFieldsReorder(fromFieldIndex, toFieldIndex)
        }
      }

  LazyColumn(
      state = lazyListState,
      modifier = modifier,
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null && onTitleChange != null && onDescriptionChange != null) {
          item(key = "basic_info") {
            Column {
              TemplateBasicInfoSection(
                  title = title,
                  description = description ?: "",
                  titleError = titleError,
                  onTitleChange = onTitleChange,
                  onDescriptionChange = onDescriptionChange)
              Spacer(Modifier.height(8.dp))
              HorizontalDivider()
              Spacer(Modifier.height(8.dp))
            }
          }
        }
        if (fields.isEmpty()) {
          item(key = "empty_placeholder") {
            Box(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add field",
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No fields yet", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Tap + to add your first field", style = MaterialTheme.typography.bodySmall)
                  }
                }
          }
        } else {
          items(fields, key = { it.id }) { field ->
            ReorderableItem(reorderableState, key = field.id) { isDragging ->
              TemplateFieldListItem(
                  field = field,
                  isExpanded = editingFieldId == field.id,
                  error = fieldErrors[field.id],
                  onExpand = { onFieldEdit(field.id) },
                  onFieldChange = { onFieldSave(field.id, it) },
                  onSave = { onFieldCancel(field.id) },
                  onCancel = { onFieldCancel(field.id) },
                  onDelete = { onFieldDelete(field.id) },
                  onDuplicate = { onFieldDuplicate(field.id) },
                  dragHandle = {
                    IconButton(onClick = {}, modifier = Modifier.draggableHandle()) {
                      Icon(Icons.Default.DragHandle, "Reorder")
                    }
                  },
                  modifier = Modifier.shadow(if (isDragging) 8.dp else 0.dp))
            }
          }
        }
      }
}

@Composable
private fun EmptyFieldsPlaceholder(modifier: Modifier) {
  Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(Icons.Default.Add, contentDescription = "Add field", modifier = Modifier.size(64.dp))
      Spacer(Modifier.height(16.dp))
      Text("No fields yet", style = MaterialTheme.typography.bodyLarge)
      Text("Tap + to add your first field", style = MaterialTheme.typography.bodySmall)
    }
  }
}

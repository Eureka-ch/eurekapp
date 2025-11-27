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
 * Callbacks for template field list operations.
 *
 * @param onFieldEdit Callback to start editing a field
 * @param onFieldSave Callback to save field changes
 * @param onFieldCancel Callback to cancel editing
 * @param onFieldDelete Callback to delete a field
 * @param onFieldDuplicate Callback to duplicate a field
 * @param onFieldsReorder Callback when fields are reordered
 */
data class TemplateFieldListCallbacks(
    val onFieldEdit: (String) -> Unit,
    val onFieldSave: (String, FieldDefinition) -> Unit,
    val onFieldCancel: (String) -> Unit,
    val onFieldDelete: (String) -> Unit,
    val onFieldDuplicate: (String) -> Unit,
    val onFieldsReorder: (Int, Int) -> Unit
)

/**
 * Callbacks for template basic info (title/description).
 *
 * @param onTitleChange Callback when title changes
 * @param onDescriptionChange Callback when description changes
 */
data class TemplateBasicInfoCallbacks(
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit
)

/**
 * Configuration for the optional header section in TemplateFieldList.
 *
 * @param title Template title
 * @param description Template description (optional)
 * @param titleError Title validation error (optional)
 * @param callbacks Callbacks for title/description changes
 */
data class TemplateHeaderConfig(
    val title: String,
    val description: String? = null,
    val titleError: String? = null,
    val callbacks: TemplateBasicInfoCallbacks
)

/**
 * Reorderable list of template fields.
 *
 * @param modifier Modifier to apply to the root lazy column
 * @param fields List of field definitions
 * @param editingFieldId ID of field being edited
 * @param fieldErrors Map of field errors
 * @param callbacks Callbacks for field operations
 * @param headerConfig Optional header configuration for title/description section
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateFieldList(
    modifier: Modifier = Modifier,
    fields: List<FieldDefinition>,
    editingFieldId: String?,
    fieldErrors: Map<String, String>,
    callbacks: TemplateFieldListCallbacks,
    headerConfig: TemplateHeaderConfig? = null
) {
  val lazyListState = rememberLazyListState()
  val indexOffset = if (headerConfig != null) 1 else 0

  val reorderableState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        handleReorder(from.index, to.index, indexOffset, callbacks.onFieldsReorder)
      }

  LazyColumn(
      state = lazyListState,
      modifier = modifier,
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (headerConfig != null) {
          item(key = "basic_info") { TemplateFieldListHeader(headerConfig) }
        }
        if (fields.isEmpty()) {
          item(key = "empty_placeholder") { EmptyFieldsPlaceholderContent() }
        } else {
          items(fields, key = { it.id }) { field ->
            ReorderableItem(reorderableState, key = field.id) { isDragging ->
              TemplateFieldListItem(
                  modifier = Modifier.shadow(if (isDragging) 8.dp else 0.dp),
                  field = field,
                  isExpanded = editingFieldId == field.id,
                  error = fieldErrors[field.id],
                  callbacks =
                      TemplateFieldCallbacks(
                          onExpand = { callbacks.onFieldEdit(field.id) },
                          onFieldChange = { callbacks.onFieldSave(field.id, it) },
                          onSave = { callbacks.onFieldCancel(field.id) },
                          onCancel = { callbacks.onFieldCancel(field.id) },
                          onDelete = { callbacks.onFieldDelete(field.id) },
                          onDuplicate = { callbacks.onFieldDuplicate(field.id) })) {
                    IconButton(onClick = {}, modifier = Modifier.draggableHandle()) {
                      Icon(Icons.Default.DragHandle, "Reorder")
                    }
                  }
            }
          }
        }
      }
}

private fun handleReorder(
    fromIndex: Int,
    toIndex: Int,
    offset: Int,
    onReorder: (Int, Int) -> Unit
) {
  val fromFieldIndex = fromIndex - offset
  val toFieldIndex = toIndex - offset
  if (fromFieldIndex >= 0 && toFieldIndex >= 0) {
    onReorder(fromFieldIndex, toFieldIndex)
  }
}

@Composable
private fun TemplateFieldListHeader(config: TemplateHeaderConfig) {
  Column {
    TemplateBasicInfoSection(
        title = config.title,
        description = config.description ?: "",
        titleError = config.titleError,
        onTitleChange = config.callbacks.onTitleChange,
        onDescriptionChange = config.callbacks.onDescriptionChange)
    Spacer(Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(Modifier.height(8.dp))
  }
}

@Composable
private fun EmptyFieldsPlaceholderContent() {
  Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(Icons.Default.Add, contentDescription = "Add field", modifier = Modifier.size(64.dp))
      Spacer(Modifier.height(16.dp))
      Text("No fields yet", style = MaterialTheme.typography.bodyLarge)
      Text("Tap + to add your first field", style = MaterialTheme.typography.bodySmall)
    }
  }
}

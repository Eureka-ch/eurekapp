# Implementation Plan: Create Template Screen

## Core Principles
✅ **Keep code concise** - No unnecessary verbosity
✅ **KDoc all public methods** - Every public function/composable/class gets documentation
✅ **Reuse existing components** - TaskTemplateRepository, PR 312 field configs, FieldValidator
✅ **Build only what's needed for Create Template Screen**
✅ **Prefix all files** - `// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code`

---

## Phase 1: Data Model Extensions

### 1.1 Add to IdGenerator.kt
**File:** `app/src/main/java/ch/eureka/eurekapp/model/data/IdGenerator.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

/**
 * Generates a unique field ID from a label.
 * Sanitizes the label and appends a short UUID for uniqueness.
 *
 * @param label The field label to generate an ID from
 * @return A unique field ID in format "sanitized_label_uuid"
 */
fun generateFieldId(label: String): String {
    val sanitized = label.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .take(20)
    val uuid = UUID.randomUUID().toString().take(8)
    return if (sanitized.isEmpty()) "field_$uuid" else "${sanitized}_${uuid}"
}
```

**Test:** Add to `IdGeneratorTest.kt`

### 1.2 Add to TaskTemplateSchema.kt
**File:** `app/src/main/java/ch/eureka/eurekapp/model/data/template/TaskTemplateSchema.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

/**
 * Reorders a field from one position to another.
 *
 * @param fromIndex The current index of the field to move
 * @param toIndex The target index to move the field to
 * @return A new TaskTemplateSchema with the field reordered
 */
fun reorderField(fromIndex: Int, toIndex: Int): TaskTemplateSchema {
    if (fromIndex !in fields.indices || toIndex !in fields.indices) return this
    val newFields = fields.toMutableList()
    newFields.add(toIndex, newFields.removeAt(fromIndex))
    return copy(fields = newFields)
}

/**
 * Duplicates a field, appending "(copy)" to its label and generating a new ID.
 *
 * @param fieldId The ID of the field to duplicate
 * @return A new TaskTemplateSchema with the duplicated field inserted after the original
 */
fun duplicateField(fieldId: String): TaskTemplateSchema {
    val original = getField(fieldId) ?: return this
    val newLabel = "${original.label} (copy)"
    val duplicate = original.copy(
        id = IdGenerator.generateFieldId(newLabel),
        label = newLabel
    )
    val index = fields.indexOfFirst { it.id == fieldId }
    return copy(fields = fields.toMutableList().apply { add(index + 1, duplicate) })
}
```

**Test:** Add to `TaskTemplateSchemaTest.kt`

---

## Phase 2: Validation

### 2.1 TemplateValidation.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/model/data/template/validation/TemplateValidation.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType

/**
 * Validation logic for template definitions.
 *
 * This validates field configurations (not runtime values).
 * Use FieldValidator for validating field values at runtime.
 */
object TemplateValidation {

    /** Validates template title is not blank. */
    fun validateTitle(title: String): String? =
        if (title.isBlank()) "Title is required" else null

    /** Validates template has at least one field. */
    fun validateFields(fields: List<FieldDefinition>): String? =
        if (fields.isEmpty()) "At least one field is required" else null

    /** Validates field label is not blank. */
    fun validateFieldLabel(label: String): String? =
        if (label.isBlank()) "Field label is required" else null

    /**
     * Validates a field definition's configuration.
     * Checks constraints are valid and default value is valid if present.
     *
     * @param field The field definition to validate
     * @return Error message if invalid, null if valid
     */
    fun validateFieldDefinition(field: FieldDefinition): String? {
        validateFieldLabel(field.label)?.let { return it }

        when (val type = field.type) {
            is FieldType.Text -> {
                if (type.minLength != null && type.maxLength != null &&
                    type.minLength > type.maxLength) {
                    return "Min length cannot exceed max length"
                }
            }
            is FieldType.Number -> {
                if (type.min != null && type.max != null && type.min > type.max) {
                    return "Min value cannot exceed max value"
                }
                if (type.decimals != null && type.decimals < 0) {
                    return "Decimals must be non-negative"
                }
            }
            is FieldType.MultiSelect -> {
                if (type.options.isEmpty()) return "At least one option required"
                if (type.minSelections != null && type.maxSelections != null &&
                    type.minSelections > type.maxSelections) {
                    return "Min selections cannot exceed max selections"
                }
            }
            is FieldType.SingleSelect -> {
                if (type.options.isEmpty()) return "At least one option required"
            }
            else -> {}
        }

        field.defaultValue?.let { value ->
            val result = FieldValidator.validate(value, field)
            if (result is FieldValidationResult.Invalid) {
                return "Invalid default value: ${result.errors.firstOrNull()}"
            }
        }

        return null
    }
}
```

**Test:** `TemplateValidationTest.kt` (add prefix comment)

---

## Phase 3: State & ViewModel

### 3.1 State model
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/TemplateEditorState.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition

/**
 * UI state for template editor.
 *
 * @property title Template title
 * @property description Template description
 * @property projectId Project ID (null if selectable)
 * @property fields List of field definitions
 * @property editingFieldId ID of field currently being edited
 * @property titleError Title validation error
 * @property fieldErrors Map of field ID to validation error
 * @property isSaving Whether save is in progress
 * @property saveError Error from last save attempt
 */
data class TemplateEditorState(
    val title: String = "",
    val description: String = "",
    val projectId: String? = null,
    val fields: List<FieldDefinition> = emptyList(),
    val editingFieldId: String? = null,
    val titleError: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saveError: String? = null
) {
    val errorCount: Int get() =
        (if (titleError != null) 1 else 0) + fieldErrors.size

    val canSave: Boolean get() =
        title.isNotBlank() && fields.isNotEmpty() && errorCount == 0 && !isSaving
}

// Extension functions for concise immutable updates
fun TemplateEditorState.updateTitle(title: String) = copy(title = title)
fun TemplateEditorState.updateDescription(desc: String) = copy(description = desc)
fun TemplateEditorState.setEditingFieldId(id: String?) = copy(editingFieldId = id)
fun TemplateEditorState.setTitleError(error: String?) = copy(titleError = error)
fun TemplateEditorState.setFieldError(fieldId: String, error: String?) = copy(
    fieldErrors = if (error != null) fieldErrors + (fieldId to error) else fieldErrors - fieldId
)
fun TemplateEditorState.setSaving(saving: Boolean) = copy(isSaving = saving)

fun TemplateEditorState.addField(field: FieldDefinition) = copy(fields = fields + field)

fun TemplateEditorState.updateField(fieldId: String, updater: (FieldDefinition) -> FieldDefinition) =
    copy(fields = fields.map { if (it.id == fieldId) updater(it) else it })

fun TemplateEditorState.removeField(fieldId: String) = copy(
    fields = fields.filterNot { it.id == fieldId },
    fieldErrors = fieldErrors - fieldId
)

fun TemplateEditorState.reorderFields(fromIndex: Int, toIndex: Int) =
    copy(fields = TaskTemplateSchema(fields).reorderField(fromIndex, toIndex).fields)

fun TemplateEditorState.duplicateField(fieldId: String) =
    copy(fields = TaskTemplateSchema(fields).duplicateField(fieldId).fields)
```

### 3.2 CreateTemplateViewModel
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/CreateTemplateViewModel.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.validation.TemplateValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for creating new templates.
 *
 * @param repository Template repository for persistence
 * @param initialProjectId Optional initial project ID from navigation
 */
class CreateTemplateViewModel(
    private val repository: TaskTemplateRepository,
    initialProjectId: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        TemplateEditorState(projectId = initialProjectId)
    )
    val state: StateFlow<TemplateEditorState> = _state.asStateFlow()

    /** Updates template title and validates. */
    fun updateTitle(title: String) {
        _state.update { it.updateTitle(title).setTitleError(TemplateValidation.validateTitle(title)) }
    }

    /** Updates template description. */
    fun updateDescription(description: String) {
        _state.update { it.updateDescription(description) }
    }

    /** Adds a new field and validates it. */
    fun addField(field: FieldDefinition) {
        _state.update { it.addField(field) }
        validateField(field.id)
    }

    /** Updates an existing field and validates it. */
    fun updateField(fieldId: String, field: FieldDefinition) {
        _state.update { it.updateField(fieldId) { field } }
        validateField(fieldId)
    }

    /** Removes a field. */
    fun removeField(fieldId: String) {
        _state.update { it.removeField(fieldId) }
    }

    /** Duplicates a field and sets it as editing. */
    fun duplicateField(fieldId: String) {
        _state.update { it.duplicateField(fieldId) }
        _state.value.fields.find { it.label.endsWith(" (copy)") }?.id?.let {
            setEditingFieldId(it)
        }
    }

    /** Reorders fields. */
    fun reorderFields(fromIndex: Int, toIndex: Int) {
        _state.update { it.reorderFields(fromIndex, toIndex) }
    }

    /** Sets which field is currently being edited. */
    fun setEditingFieldId(fieldId: String?) {
        _state.update { it.setEditingFieldId(fieldId) }
    }

    private fun validateField(fieldId: String) {
        val field = _state.value.fields.find { it.id == fieldId } ?: return
        val error = TemplateValidation.validateFieldDefinition(field)
        _state.update { it.setFieldError(fieldId, error) }
    }

    /** Validates all fields. Returns true if valid. */
    fun validateAll(): Boolean {
        _state.update { it.setTitleError(TemplateValidation.validateTitle(it.title)) }

        TemplateValidation.validateFields(_state.value.fields)?.let {
            return false
        }

        _state.value.fields.forEach { validateField(it.id) }

        return _state.value.canSave
    }

    /**
     * Saves the template.
     * Repository will automatically set createdBy to current user.
     * @return Result with template ID on success
     */
    suspend fun save(): Result<String> {
        if (!validateAll()) return Result.failure(Exception("Validation failed"))

        _state.update { it.setSaving(true) }

        val template = TaskTemplate(
            templateID = IdGenerator.generateTaskTemplateId(),
            projectId = _state.value.projectId ?: "",
            title = _state.value.title,
            description = _state.value.description,
            definedFields = TaskTemplateSchema(_state.value.fields),
            createdBy = ""  // Repository will set this to current user
        )

        return repository.createTemplate(template).also {
            _state.update { it.setSaving(false) }
        }
    }
}
```

**Test:** `CreateTemplateViewModelTest.kt` (add prefix comment)

**Note:** Repository implementation may need updating to automatically set `createdBy` field.

---

## Phase 4: UI Components (Concise)

### 4.1 Add dependency
**File:** `app/build.gradle.kts`
```kotlin
implementation("sh.calvin.reorderable:reorderable:2.3.0")
```

### 4.2 TemplateBasicInfoSection.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/components/TemplateBasicInfoSection.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

/**
 * Section for editing template basic information.
 *
 * @param title Template title
 * @param description Template description
 * @param titleError Validation error for title
 * @param onTitleChange Callback when title changes
 * @param onDescriptionChange Callback when description changes
 */
@Composable
fun TemplateBasicInfoSection(
    title: String,
    description: String,
    titleError: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Template Title") },
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = EurekaStyles.textFieldColors()
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = EurekaStyles.textFieldColors()
        )
    }
}
```

**Test:** `TemplateBasicInfoSectionTest.kt` (add prefix comment)

### 4.3 TemplateFieldListItem.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/components/TemplateFieldListItem.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.*
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.*

/**
 * List item for a template field with expand/collapse editing.
 *
 * @param field The field definition
 * @param isExpanded Whether the field is expanded for editing
 * @param error Validation error if any
 * @param onExpand Callback to expand the field
 * @param onFieldChange Callback when field definition changes
 * @param onSave Callback to save changes (collapses)
 * @param onCancel Callback to cancel changes (collapses)
 * @param onDelete Callback to delete field
 * @param onDuplicate Callback to duplicate field
 * @param dragHandle Composable for drag handle
 */
@Composable
fun TemplateFieldListItem(
    field: FieldDefinition,
    isExpanded: Boolean,
    error: String?,
    onExpand: () -> Unit,
    onFieldChange: (FieldDefinition) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var localField by remember(field, isExpanded) { mutableStateOf(field) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            // Collapsed header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isExpanded) { onExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dragHandle()
                Icon(field.type.icon, null, tint = MaterialTheme.colorScheme.primary)

                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(field.label, style = MaterialTheme.typography.bodyLarge)
                        if (field.required) Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                    Text(field.type.name, style = MaterialTheme.typography.bodySmall)
                }

                if (error != null) Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error)
                if (!isExpanded) Icon(Icons.Default.ExpandMore, "Expand")
            }

            // Expanded content
            if (isExpanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    CommonFieldConfiguration(
                        fieldDefinition = localField,
                        onUpdate = { localField = it },
                        enabled = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Type-specific config (reuse PR 312 components)
                    when (val type = localField.type) {
                        is FieldType.Text -> TextFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
                        is FieldType.Number -> NumberFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
                        is FieldType.Date -> DateFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
                        is FieldType.SingleSelect -> SingleSelectFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
                        is FieldType.MultiSelect -> MultiSelectFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
                    }

                    // Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onFieldChange(localField); onSave() }) {
                            Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onDuplicate) {
                            Icon(Icons.Default.ContentCopy, "Duplicate")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private val FieldType.icon get() = when (this) {
    is FieldType.Text -> Icons.Default.TextFields
    is FieldType.Number -> Icons.Default.Numbers
    is FieldType.Date -> Icons.Default.CalendarToday
    is FieldType.SingleSelect -> Icons.Default.RadioButtonChecked
    is FieldType.MultiSelect -> Icons.Default.CheckBox
}

private val FieldType.name get() = when (this) {
    is FieldType.Text -> "Text"
    is FieldType.Number -> "Number"
    is FieldType.Date -> "Date"
    is FieldType.SingleSelect -> "Single Select"
    is FieldType.MultiSelect -> "Multi Select"
}
```

**Test:** `TemplateFieldListItemTest.kt` (add prefix comment)

### 4.4 TemplateFieldList.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/components/TemplateFieldList.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlaylistAdd
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
 */
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
    modifier: Modifier = Modifier
) {
    if (fields.isEmpty()) {
        EmptyFieldsPlaceholder(modifier)
    } else {
        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            onFieldsReorder(from.index, to.index)
        }

        LazyColumn(
            state = lazyListState,
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        modifier = Modifier.shadow(if (isDragging) 8.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFieldsPlaceholder(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PlaylistAdd, null, Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("No fields yet", style = MaterialTheme.typography.bodyLarge)
            Text("Tap + to add your first field", style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

**Test:** `TemplateFieldListTest.kt` (add prefix comment)

### 4.5 AddFieldBottomSheet.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/components/AddFieldBottomSheet.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.*
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.*

/**
 * Bottom sheet for adding a new field.
 * Shows type selector, then field editor.
 *
 * @param onDismiss Callback to dismiss sheet
 * @param onFieldCreated Callback when field is created
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldBottomSheet(
    onDismiss: () -> Unit,
    onFieldCreated: (FieldDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<FieldType?>(null) }
    var currentField by remember { mutableStateOf<FieldDefinition?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            selectedType?.let { type ->
                currentField?.let { FieldEditor(it, onDismiss, onFieldCreated) }
            } ?: FieldTypeSelector { type ->
                selectedType = type
                currentField = FieldDefinition(
                    id = IdGenerator.generateFieldId("New Field"),
                    label = "New Field",
                    type = type,
                    required = false
                )
            }
        }
    }
}

@Composable
private fun FieldTypeSelector(onTypeSelected: (FieldType) -> Unit) {
    Text("Select Field Type", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

    listOf(
        "Text" to FieldType.Text(),
        "Number" to FieldType.Number(),
        "Date" to FieldType.Date(),
        "Single Select" to FieldType.SingleSelect(listOf(SelectOption("option1", "Option 1"))),
        "Multi Select" to FieldType.MultiSelect(listOf(SelectOption("option1", "Option 1")))
    ).forEach { (name, type) ->
        Card(onClick = { onTypeSelected(type) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(name, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun FieldEditor(
    field: FieldDefinition,
    onDismiss: () -> Unit,
    onFieldCreated: (FieldDefinition) -> Unit
) {
    var editingField by remember { mutableStateOf(field) }

    Text("Configure Field", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

    LazyColumn(modifier = Modifier.weight(1f)) {
        item {
            CommonFieldConfiguration(editingField, { editingField = it }, true)
            Spacer(Modifier.height(8.dp))

            when (val type = editingField.type) {
                is FieldType.Text -> TextFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
                is FieldType.Number -> NumberFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
                is FieldType.Date -> DateFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
                is FieldType.SingleSelect -> SingleSelectFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
                is FieldType.MultiSelect -> MultiSelectFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
            }
        }
    }

    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(onClick = { onFieldCreated(editingField); onDismiss() }, modifier = Modifier.weight(1f)) { Text("Add") }
    }
}
```

**Test:** `AddFieldBottomSheetTest.kt` (add prefix comment)

### 4.6 TemplatePreview.kt
**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/components/TemplatePreview.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.*

/**
 * Preview of how template fields will appear.
 * Shows actual field components or error placeholders.
 *
 * @param fields List of field definitions
 * @param fieldErrors Map of field errors
 * @param onErrorFieldClick Callback when error field is clicked
 */
@Composable
fun TemplatePreview(
    fields: List<FieldDefinition>,
    fieldErrors: Map<String, String>,
    onErrorFieldClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (fields.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text("No fields to preview")
                }
            }
        } else {
            items(fields, key = { it.id }) { field ->
                fieldErrors[field.id]?.let { error ->
                    ErrorFieldPlaceholder(field.label, error) { onErrorFieldClick(field.id) }
                } ?: FieldPreview(field)
            }
        }
    }
}

@Composable
private fun ErrorFieldPlaceholder(label: String, error: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.2f))) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FieldPreview(field: FieldDefinition) {
    when (field.type) {
        is FieldType.Text -> TextFieldComponent(field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
        is FieldType.Number -> NumberFieldComponent(field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
        is FieldType.Date -> DateFieldComponent(field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
        is FieldType.SingleSelect -> SingleSelectFieldComponent(field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
        is FieldType.MultiSelect -> MultiSelectFieldComponent(field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
    }
}
```

**Test:** `TemplatePreviewTest.kt` (add prefix comment)

---

## Phase 5: CreateTemplateScreen

**New file:** `app/src/main/java/ch/eureka/eurekapp/ui/templates/CreateTemplateScreen.kt`

```kotlin
// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.templates.components.*
import kotlinx.coroutines.launch

/**
 * Screen for creating a new template.
 *
 * @param onNavigateBack Callback to navigate back
 * @param onTemplateCreated Callback when template is created (receives template ID)
 * @param viewModel ViewModel for managing state
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateCreated: (String) -> Unit,
    viewModel: CreateTemplateViewModel
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddFieldSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EurekaTopBar(
                title = "Create Template",
                navigationIcon = { BackButton(onClick = onNavigateBack) },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val result = viewModel.save()
                                result.fold(
                                    onSuccess = onTemplateCreated,
                                    onFailure = { snackbarHostState.showSnackbar(it.message ?: "Save failed") }
                                )
                            }
                        },
                        enabled = state.canSave
                    ) {
                        if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp))
                        else Text("Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddFieldSheet = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Field") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(pagerState.currentPage) {
                Tab(
                    pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Configure")
                            if (state.errorCount > 0) Badge { Text("${state.errorCount}") }
                        }
                    }
                )
                Tab(
                    pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Preview") }
                )
            }

            HorizontalPager(pagerState, Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> Column {
                        TemplateBasicInfoSection(
                            state.title,
                            state.description,
                            state.titleError,
                            viewModel::updateTitle,
                            viewModel::updateDescription
                        )
                        HorizontalDivider()
                        TemplateFieldList(
                            state.fields,
                            state.editingFieldId,
                            state.fieldErrors,
                            viewModel::setEditingFieldId,
                            viewModel::updateField,
                            { viewModel.setEditingFieldId(null) },
                            viewModel::removeField,
                            viewModel::duplicateField,
                            viewModel::reorderFields,
                            Modifier.weight(1f)
                        )
                    }
                    1 -> TemplatePreview(
                        state.fields,
                        state.fieldErrors,
                        { fieldId ->
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                                viewModel.setEditingFieldId(fieldId)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddFieldSheet) {
        AddFieldBottomSheet(
            onDismiss = { showAddFieldSheet = false },
            onFieldCreated = { field ->
                viewModel.addField(field)
                viewModel.setEditingFieldId(field.id)
            }
        )
    }
}
```

**Test:** `CreateTemplateScreenTest.kt` (add prefix comment)

---

## Implementation Timeline (Today)

1. **Phase 1**: Data extensions (1h)
2. **Phase 2**: Validation (1h)
3. **Phase 3**: State & ViewModel (1.5h)
4. **Phase 4**: UI Components (3.5h)
5. **Phase 5**: Screen (0.5h)
6. **Polish** (0.5h) - ktfmtFormat, manual testing

**Total: ~8 hours**

---

## Key Requirements

✅ Concise code
✅ KDoc on all public methods
✅ Prefix comment on all files
✅ Reuse existing components
✅ Create screen only (no Edit/View)
✅ No navigation changes
✅ Simple ViewModel (no Hilt, no service locator)
✅ Repository handles createdBy field

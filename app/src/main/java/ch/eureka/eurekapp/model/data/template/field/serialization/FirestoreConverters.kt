/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field.serialization

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldTypeKey
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import com.google.firebase.Timestamp

object FirestoreConverters {

  fun schemaToMap(schema: TaskTemplateSchema): Map<String, Any> {
    return mapOf("fields" to schema.fields.map { fieldDefinitionToMap(it) })
  }

  fun mapToSchema(map: Map<String, Any>): TaskTemplateSchema {
    @Suppress("UNCHECKED_CAST")
    val fieldsList = map["fields"] as? List<Map<String, Any>> ?: emptyList()
    val fields = fieldsList.map { mapToFieldDefinition(it) }
    return TaskTemplateSchema(fields)
  }

  fun customDataToMap(customData: TaskCustomData): Map<String, Any> {
    return customData.data.mapValues { (_, value) -> fieldValueToMap(value) }
  }

  fun mapToCustomData(map: Map<String, Any>): TaskCustomData {
    @Suppress("UNCHECKED_CAST")
    val data = map.mapValues { (_, value) -> mapToFieldValue(value as Map<String, Any>) }
    return TaskCustomData(data)
  }

  private fun fieldDefinitionToMap(field: FieldDefinition): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    map["id"] = field.id
    map["label"] = field.label
    map["type"] = fieldTypeToMap(field.type)
    map["required"] = field.required
    field.description?.let { map["description"] = it }
    field.defaultValue?.let { map["defaultValue"] = fieldValueToMap(it) }
    return map
  }

  private fun mapToFieldDefinition(map: Map<String, Any>): FieldDefinition {
    @Suppress("UNCHECKED_CAST")
    return FieldDefinition(
        id = map["id"] as String,
        label = map["label"] as String,
        type = mapToFieldType(map["type"] as Map<String, Any>),
        required = map["required"] as? Boolean ?: false,
        description = map["description"] as? String,
        defaultValue = (map["defaultValue"] as? Map<String, Any>)?.let { mapToFieldValue(it) })
  }

  private fun fieldTypeToMap(fieldType: FieldType): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    map["typeKey"] = fieldType.typeKey.name

    when (fieldType) {
      is FieldType.Text -> {
        fieldType.maxLength?.let { map["maxLength"] = it }
        fieldType.minLength?.let { map["minLength"] = it }
        fieldType.pattern?.let { map["pattern"] = it }
        fieldType.placeholder?.let { map["placeholder"] = it }
      }
      is FieldType.Number -> {
        fieldType.min?.let { map["min"] = it }
        fieldType.max?.let { map["max"] = it }
        fieldType.step?.let { map["step"] = it }
        fieldType.decimals?.let { map["decimals"] = it }
        fieldType.unit?.let { map["unit"] = it }
      }
      is FieldType.Date -> {
        fieldType.minDate?.let { map["minDate"] = it }
        fieldType.maxDate?.let { map["maxDate"] = it }
        map["includeTime"] = fieldType.includeTime
        fieldType.format?.let { map["format"] = it }
      }
      is FieldType.SingleSelect -> {
        map["options"] = fieldType.options.map { selectOptionToMap(it) }
        map["allowCustom"] = fieldType.allowCustom
      }
      is FieldType.MultiSelect -> {
        map["options"] = fieldType.options.map { selectOptionToMap(it) }
        fieldType.minSelections?.let { map["minSelections"] = it }
        fieldType.maxSelections?.let { map["maxSelections"] = it }
        map["allowCustom"] = fieldType.allowCustom
      }
    }

    return map
  }

  private fun mapToFieldType(map: Map<String, Any>): FieldType {
    val typeKey = FieldTypeKey.valueOf(map["typeKey"] as String)

    return when (typeKey) {
      FieldTypeKey.TEXT ->
          FieldType.Text(
              maxLength = map["maxLength"] as? Int,
              minLength = map["minLength"] as? Int,
              pattern = map["pattern"] as? String,
              placeholder = map["placeholder"] as? String)
      FieldTypeKey.NUMBER ->
          FieldType.Number(
              min = map["min"] as? Double,
              max = map["max"] as? Double,
              step = map["step"] as? Double,
              decimals = map["decimals"] as? Int,
              unit = map["unit"] as? String)
      FieldTypeKey.DATE ->
          FieldType.Date(
              minDate = map["minDate"] as? String,
              maxDate = map["maxDate"] as? String,
              includeTime = map["includeTime"] as? Boolean ?: false,
              format = map["format"] as? String)
      FieldTypeKey.SINGLE_SELECT -> {
        @Suppress("UNCHECKED_CAST") val optionsList = map["options"] as List<Map<String, Any>>
        FieldType.SingleSelect(
            options = optionsList.map { mapToSelectOption(it) },
            allowCustom = map["allowCustom"] as? Boolean ?: false)
      }
      FieldTypeKey.MULTI_SELECT -> {
        @Suppress("UNCHECKED_CAST") val optionsList = map["options"] as List<Map<String, Any>>
        FieldType.MultiSelect(
            options = optionsList.map { mapToSelectOption(it) },
            minSelections = map["minSelections"] as? Int,
            maxSelections = map["maxSelections"] as? Int,
            allowCustom = map["allowCustom"] as? Boolean ?: false)
      }
    }
  }

  private fun selectOptionToMap(option: SelectOption): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    map["value"] = option.value
    map["label"] = option.label
    option.description?.let { map["description"] = it }
    return map
  }

  private fun mapToSelectOption(map: Map<String, Any>): SelectOption {
    return SelectOption(
        value = map["value"] as String,
        label = map["label"] as String,
        description = map["description"] as? String)
  }

  private fun fieldValueToMap(fieldValue: FieldValue): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    map["typeKey"] = fieldValue.typeKey.name

    when (fieldValue) {
      is FieldValue.TextValue -> map["value"] = fieldValue.value
      is FieldValue.NumberValue -> map["value"] = fieldValue.value
      is FieldValue.DateValue -> map["value"] = fieldValue.value
      is FieldValue.SingleSelectValue -> map["value"] = fieldValue.value
      is FieldValue.MultiSelectValue -> map["values"] = fieldValue.values
    }

    return map
  }

  private fun mapToFieldValue(map: Map<String, Any>): FieldValue {
    val typeKey = FieldTypeKey.valueOf(map["typeKey"] as String)

    return when (typeKey) {
      FieldTypeKey.TEXT -> FieldValue.TextValue(map["value"] as String)
      FieldTypeKey.NUMBER -> FieldValue.NumberValue(map["value"] as Double)
      FieldTypeKey.DATE -> FieldValue.DateValue(map["value"] as String)
      FieldTypeKey.SINGLE_SELECT -> FieldValue.SingleSelectValue(map["value"] as String)
      FieldTypeKey.MULTI_SELECT -> {
        @Suppress("UNCHECKED_CAST") val values = map["values"] as List<String>
        FieldValue.MultiSelectValue(values)
      }
    }
  }

  fun taskToMap(task: Task): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    map["taskID"] = task.taskID
    map["templateId"] = task.templateId
    map["projectId"] = task.projectId
    map["title"] = task.title
    map["description"] = task.description
    map["status"] = task.status.name
    map["assignedUserIds"] = task.assignedUserIds
    map["dueDate"] = task.dueDate
    map["attachmentUrls"] = task.attachmentUrls
    map["customData"] = customDataToMap(task.customData)
    map["createdBy"] = task.createdBy
    return map
  }

  fun mapToTask(map: Map<String, Any>): Task {
    @Suppress("UNCHECKED_CAST")
    return Task(
        taskID = map["taskID"] as? String ?: "",
        templateId = map["templateId"] as? String ?: "",
        projectId = map["projectId"] as? String ?: "",
        title = map["title"] as? String ?: "",
        description = map["description"] as? String ?: "",
        status = (map["status"] as? String)?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
        assignedUserIds = (map["assignedUserIds"] as? List<String>) ?: emptyList(),
        dueDate = map["dueDate"] as? Timestamp,
        attachmentUrls = (map["attachmentUrls"] as? List<String>) ?: emptyList(),
        customData =
            (map["customData"] as? Map<String, Any>)?.let { mapToCustomData(it) }
                ?: TaskCustomData(),
        createdBy = map["createdBy"] as? String ?: "")
  }

  fun taskTemplateToMap(template: TaskTemplate): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    map["templateID"] = template.templateID
    map["projectId"] = template.projectId
    map["title"] = template.title
    map["description"] = template.description
    map["definedFields"] = schemaToMap(template.definedFields)
    map["createdBy"] = template.createdBy
    return map
  }

  fun mapToTaskTemplate(map: Map<String, Any>): TaskTemplate {
    @Suppress("UNCHECKED_CAST")
    return TaskTemplate(
        templateID = map["templateID"] as? String ?: "",
        projectId = map["projectId"] as? String ?: "",
        title = map["title"] as? String ?: "",
        description = map["description"] as? String ?: "",
        definedFields =
            (map["definedFields"] as? Map<String, Any>)?.let { mapToSchema(it) }
                ?: TaskTemplateSchema(),
        createdBy = map["createdBy"] as? String ?: "")
  }
}

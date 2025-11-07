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

  fun schemaToMap(schema: TaskTemplateSchema): Map<String, Any> =
      mapOf("fields" to schema.fields.map { fieldDefinitionToMap(it) })

  fun mapToSchema(map: Map<String, Any>): TaskTemplateSchema {
    @Suppress("UNCHECKED_CAST")
    val fieldsList = map["fields"] as? List<Map<String, Any>> ?: emptyList()
    val fields = fieldsList.map { mapToFieldDefinition(it) }
    return TaskTemplateSchema(fields)
  }

  fun customDataToMap(customData: TaskCustomData): Map<String, Any> =
      customData.data.mapValues { (_, value) -> fieldValueToMap(value) }

  fun mapToCustomData(map: Map<String, Any>): TaskCustomData {
    @Suppress("UNCHECKED_CAST")
    val data = map.mapValues { (_, value) -> mapToFieldValue(value as Map<String, Any>) }
    return TaskCustomData(data)
  }

  private fun fieldDefinitionToMap(field: FieldDefinition): Map<String, Any> = buildMap {
    put("id", field.id)
    put("label", field.label)
    put("type", fieldTypeToMap(field.type))
    put("required", field.required)
    field.description?.let { put("description", it) }
    field.defaultValue?.let { put("defaultValue", fieldValueToMap(it)) }
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

  private fun fieldTypeToMap(fieldType: FieldType): Map<String, Any> = buildMap {
    put("typeKey", fieldType.typeKey.name)

    when (fieldType) {
      is FieldType.Text -> {
        fieldType.maxLength?.let { put("maxLength", it) }
        fieldType.minLength?.let { put("minLength", it) }
        fieldType.pattern?.let { put("pattern", it) }
        fieldType.placeholder?.let { put("placeholder", it) }
      }
      is FieldType.Number -> {
        fieldType.min?.let { put("min", it) }
        fieldType.max?.let { put("max", it) }
        fieldType.step?.let { put("step", it) }
        fieldType.decimals?.let { put("decimals", it) }
        fieldType.unit?.let { put("unit", it) }
      }
      is FieldType.Date -> {
        fieldType.minDate?.let { put("minDate", it) }
        fieldType.maxDate?.let { put("maxDate", it) }
        put("includeTime", fieldType.includeTime)
        fieldType.format?.let { put("format", it) }
      }
      is FieldType.SingleSelect -> {
        put("options", fieldType.options.map { selectOptionToMap(it) })
        put("allowCustom", fieldType.allowCustom)
      }
      is FieldType.MultiSelect -> {
        put("options", fieldType.options.map { selectOptionToMap(it) })
        fieldType.minSelections?.let { put("minSelections", it) }
        fieldType.maxSelections?.let { put("maxSelections", it) }
        put("allowCustom", fieldType.allowCustom)
      }
    }
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

  private fun selectOptionToMap(option: SelectOption): Map<String, Any> = buildMap {
    put("value", option.value)
    put("label", option.label)
    option.description?.let { put("description", it) }
  }

  private fun mapToSelectOption(map: Map<String, Any>): SelectOption {
    return SelectOption(
        value = map["value"] as String,
        label = map["label"] as String,
        description = map["description"] as? String)
  }

  private fun fieldValueToMap(fieldValue: FieldValue): Map<String, Any> = buildMap {
    put("typeKey", fieldValue.typeKey.name)

    when (fieldValue) {
      is FieldValue.TextValue -> put("value", fieldValue.value)
      is FieldValue.NumberValue -> put("value", fieldValue.value)
      is FieldValue.DateValue -> put("value", fieldValue.value)
      is FieldValue.SingleSelectValue -> put("value", fieldValue.value)
      is FieldValue.MultiSelectValue -> put("values", fieldValue.values)
    }
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

  fun taskToMap(task: Task): Map<String, Any?> = buildMap {
    put("taskID", task.taskID)
    put("templateId", task.templateId)
    put("projectId", task.projectId)
    put("title", task.title)
    put("description", task.description)
    put("status", task.status.name)
    put("assignedUserIds", task.assignedUserIds)
    put("dueDate", task.dueDate)
    put("attachmentUrls", task.attachmentUrls)
    put("customData", customDataToMap(task.customData))
    put("createdBy", task.createdBy)
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

  fun taskTemplateToMap(template: TaskTemplate): Map<String, Any> = buildMap {
    put("templateID", template.templateID)
    put("projectId", template.projectId)
    put("title", template.title)
    put("description", template.description)
    put("definedFields", schemaToMap(template.definedFields))
    put("createdBy", template.createdBy)
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

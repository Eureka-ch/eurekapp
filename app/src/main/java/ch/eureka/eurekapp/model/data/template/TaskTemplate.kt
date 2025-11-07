package ch.eureka.eurekapp.model.data.template

/**
 * Data class representing a task template within a project.
 *
 * Templates provide a reusable structure for creating tasks with predefined fields and
 * configurations. When a task is created from a template, the template's defined fields are used to
 * populate the task's customData.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property templateID Unique identifier for the template.
 * @property projectId ID of the project this template belongs to.
 * @property title The name of the template.
 * @property description Description of what this template is used for.
 * @property definedFields Strongly-typed schema defining custom fields for this template.
 * @property createdBy User ID of the person who created this template.
 */
data class TaskTemplate(
    val templateID: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val definedFields: TaskTemplateSchema = TaskTemplateSchema(),
    val createdBy: String = ""
)

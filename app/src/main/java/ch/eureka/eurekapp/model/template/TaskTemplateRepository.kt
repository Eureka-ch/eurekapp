package ch.eureka.eurekapp.model.template

import kotlinx.coroutines.flow.Flow

interface TaskTemplateRepository {
  /** Get template by ID with real-time updates */
  fun getTemplateById(workspaceId: String, templateId: String): Flow<TaskTemplate?>

  /** Get all templates in workspace with real-time updates */
  fun getTemplatesInWorkspace(workspaceId: String): Flow<List<TaskTemplate>>

  /** Get templates for specific context (workspace, group, or project) with real-time updates */
  fun getTemplatesForContext(
      workspaceId: String,
      contextId: String,
      contextType: TemplateContextType
  ): Flow<List<TaskTemplate>>

  /** Create a new template */
  suspend fun createTemplate(template: TaskTemplate): Result<String>

  /** Update template */
  suspend fun updateTemplate(template: TaskTemplate): Result<Unit>

  /** Delete template */
  suspend fun deleteTemplate(workspaceId: String, templateId: String): Result<Unit>
}

package ch.eureka.eurekapp.model.data.template

import kotlinx.coroutines.flow.Flow

interface TaskTemplateRepository {
  /** Get template by ID with real-time updates */
  fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?>

  /** Get all templates in project with real-time updates */
  fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>>

  /** Create a new template */
  suspend fun createTemplate(template: TaskTemplate): Result<String>

  /** Update template */
  suspend fun updateTemplate(template: TaskTemplate): Result<Unit>

  /** Delete template */
  suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit>
}

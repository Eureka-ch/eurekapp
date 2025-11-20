// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private class PreviewRepository : TaskTemplateRepository {
  override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
      flowOf(null)

  override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> =
      flowOf(emptyList())

  override suspend fun createTemplate(template: TaskTemplate): Result<String> {
    // Simulate validation failure for preview
    return Result.failure(Exception("Validation failed - title required"))
  }

  override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> = Result.success(Unit)

  override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
      Result.success(Unit)
}

@Preview(name = "Empty State", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CreateTemplateScreenEmptyPreview() {
  val viewModel = CreateTemplateViewModel(PreviewRepository())

  CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
}

@Preview(name = "With Field Added", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CreateTemplateScreenWithFieldPreview() {
  val viewModel = CreateTemplateViewModel(PreviewRepository())

  // Add a field to simulate user action
  viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))

  CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
}

@Preview(
    name = "After Validation (Empty Title)", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CreateTemplateScreenAfterValidationPreview() {
  val viewModel = CreateTemplateViewModel(PreviewRepository())

  // Add a field
  viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))

  // Trigger validation
  viewModel.validateAll()

  CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
}

@Preview(name = "With Valid Data", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CreateTemplateScreenValidDataPreview() {
  val viewModel = CreateTemplateViewModel(PreviewRepository())

  // Set valid title
  viewModel.updateTitle("My Template")

  // Add fields
  viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))
  viewModel.addField(FieldDefinition(id = "field2", label = "Due Date", type = FieldType.Date()))

  CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
}

@Preview(
    name = "With Errors After Validation", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CreateTemplateScreenWithErrorsPreview() {
  val viewModel = CreateTemplateViewModel(PreviewRepository())

  // Add a field with blank label (will fail validation)
  viewModel.addField(FieldDefinition(id = "field1", label = "Name", type = FieldType.Text()))

  // Trigger validation which will show title error
  viewModel.validateAll()

  CreateTemplateScreen(onNavigateBack = {}, onTemplateCreated = {}, viewModel = viewModel)
}

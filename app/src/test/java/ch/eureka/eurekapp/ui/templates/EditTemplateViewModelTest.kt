package ch.eureka.eurekapp.ui.templates

import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditTemplateViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockRepository: MockTaskTemplateRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockRepository = MockTaskTemplateRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun viewModel_afterLoading_isNotLoading() = runTest {
    mockRepository.setTemplate(null)
    val viewModel = EditTemplateViewModel(mockRepository, "proj1", "template1")
    advanceUntilIdle()
    assertFalse(viewModel.isLoading.first())
  }

  @Test
  fun viewModel_loadsTemplate_successfully() = runTest {
    val existingField = FieldDefinition(id = "f1", label = "Name", type = FieldType.Text())
    val template =
        TaskTemplate(
            templateID = "template1",
            projectId = "proj1",
            title = "Test Template",
            description = "Test desc",
            definedFields = TaskTemplateSchema(listOf(existingField)),
            createdBy = "user1")
    mockRepository.setTemplate(template)

    val viewModel = EditTemplateViewModel(mockRepository, "proj1", "template1")
    advanceUntilIdle()

    assertFalse(viewModel.isLoading.first())
    assertNull(viewModel.loadError.first())

    val state = viewModel.state.first()
    assertEquals("Test Template", state.title)
    assertEquals("Test desc", state.description)
    assertEquals(1, state.fields.size)
    assertEquals("Name", state.fields[0].label)
  }

  @Test
  fun viewModel_templateNotFound_setsError() = runTest {
    mockRepository.setTemplate(null)

    val viewModel = EditTemplateViewModel(mockRepository, "proj1", "template1")
    advanceUntilIdle()

    assertFalse(viewModel.isLoading.first())
    assertEquals("Template not found", viewModel.loadError.first())
  }

  @Test
  fun updateTitle_updatesState() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Old",
            description = "",
            definedFields = TaskTemplateSchema(listOf()),
            createdBy = "")
    mockRepository.setTemplate(template)

    val viewModel = EditTemplateViewModel(mockRepository, "p1", "t1")
    advanceUntilIdle()

    viewModel.updateTitle("New Title")
    assertEquals("New Title", viewModel.state.first().title)
  }

  @Test
  fun addField_addsToList() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields = TaskTemplateSchema(listOf()),
            createdBy = "")
    mockRepository.setTemplate(template)

    val viewModel = EditTemplateViewModel(mockRepository, "p1", "t1")
    advanceUntilIdle()

    val newField = FieldDefinition(id = "f1", label = "Field", type = FieldType.Text())
    viewModel.addField(newField)

    assertEquals(1, viewModel.state.first().fields.size)
  }

  @Test
  fun save_callsUpdateTemplate() = runTest {
    val existingField = FieldDefinition(id = "f1", label = "Name", type = FieldType.Text())
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields = TaskTemplateSchema(listOf(existingField)),
            createdBy = "")
    mockRepository.setTemplate(template)

    val viewModel = EditTemplateViewModel(mockRepository, "p1", "t1")
    advanceUntilIdle()

    viewModel.updateTitle("Updated Title")
    val result = viewModel.save()

    assertTrue(result.isSuccess)
    assertEquals(1, mockRepository.updateTemplateCalls.size)
    assertEquals("Updated Title", mockRepository.updateTemplateCalls[0].title)
  }

  @Test
  fun save_withInvalidData_fails() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            projectId = "p1",
            title = "Test",
            description = "",
            definedFields = TaskTemplateSchema(listOf()),
            createdBy = "")
    mockRepository.setTemplate(template)

    val viewModel = EditTemplateViewModel(mockRepository, "p1", "t1")
    advanceUntilIdle()

    viewModel.updateTitle("")
    val result = viewModel.save()

    assertTrue(result.isFailure)
    assertEquals(0, mockRepository.updateTemplateCalls.size)
  }

  private class MockTaskTemplateRepository : TaskTemplateRepository {
    val updateTemplateCalls = mutableListOf<TaskTemplate>()
    private val templateFlow = MutableStateFlow<TaskTemplate?>(null)

    fun setTemplate(template: TaskTemplate?) {
      templateFlow.value = template
    }

    override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
        templateFlow

    override fun getTemplatesInProject(projectId: String) =
        MutableStateFlow(emptyList<TaskTemplate>())

    override suspend fun createTemplate(template: TaskTemplate) =
        Result.success(template.templateID)

    override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> {
      updateTemplateCalls.add(template)
      return Result.success(Unit)
    }

    override suspend fun deleteTemplate(projectId: String, templateId: String) =
        Result.success(Unit)
  }
}

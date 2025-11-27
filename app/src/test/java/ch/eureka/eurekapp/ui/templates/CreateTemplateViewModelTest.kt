// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTemplateViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockRepository: MockTaskTemplateRepository
  private lateinit var viewModel: CreateTemplateViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockRepository = MockTaskTemplateRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockRepository.reset()
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertNull(state.projectId)
    assertEquals(emptyList<FieldDefinition>(), state.fields)
    assertNull(state.editingFieldId)
    assertNull(state.titleError)
    assertEquals(emptyMap<String, String>(), state.fieldErrors)
    assertFalse(state.isSaving)
    assertNull(state.saveError)
  }

  @Test
  fun viewModel_initialState_withProjectId_setsProjectId() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository, initialProjectId = "proj1")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("proj1", state.projectId)
  }

  @Test
  fun updateTitle_updatesStateAndValidates() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("New Template")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("New Template", state.title)
    assertNull(state.titleError)
  }

  @Test
  fun updateTitle_withBlankTitle_setsTitleError() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("  ")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("  ", state.title)
    assertEquals("Title is required", state.titleError)
  }

  @Test
  fun updateDescription_updatesState() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateDescription("Template description")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("Template description", state.description)
  }

  @Test
  fun addField_addsFieldAndValidates() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())

    viewModel.addField(field)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(1, state.fields.size)
    assertEquals("field1", state.fields[0].id)
    assertEquals("Name", state.fields[0].label)
    assertFalse(state.fieldErrors.containsKey("field1"))
  }

  @Test
  fun addField_withMultipleFields_addsAllFields() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val field1 = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    val field2 = FieldDefinition(id = "field2", label = "Age", type = FieldType.Number())

    viewModel.addField(field1)
    viewModel.addField(field2)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(2, state.fields.size)
    assertEquals("Name", state.fields[0].label)
    assertEquals("Age", state.fields[1].label)
  }

  @Test
  fun updateField_updatesFieldAndValidates() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val originalField = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(originalField)
    advanceUntilIdle()

    val updatedField = originalField.copy(label = "Full Name")
    viewModel.updateField("field1", updatedField)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(1, state.fields.size)
    assertEquals("Full Name", state.fields[0].label)
    assertFalse(state.fieldErrors.containsKey("field1"))
  }

  @Test
  fun updateField_withRequiredChange_updatesField() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val originalField = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(originalField)
    advanceUntilIdle()

    val updatedField = originalField.copy(required = true)
    viewModel.updateField("field1", updatedField)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(1, state.fields.size)
    assertTrue(state.fields[0].required)
  }

  @Test
  fun removeField_removesField() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    viewModel.removeField("field1")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(0, state.fields.size)
  }

  @Test
  fun duplicateField_createsNewFieldWithCopySuffix() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    viewModel.duplicateField("field1")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals(2, state.fields.size)
    assertTrue(state.fields[1].label.endsWith(" (copy)"))
    assertNotNull(state.editingFieldId)
    assertEquals(state.fields[1].id, state.editingFieldId)
  }

  @Test
  fun reorderFields_reordersFieldsList() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    val field1 = FieldDefinition(id = "field1", label = "First", type = FieldType.Text())
    val field2 = FieldDefinition(id = "field2", label = "Second", type = FieldType.Text())
    val field3 = FieldDefinition(id = "field3", label = "Third", type = FieldType.Text())

    viewModel.addField(field1)
    viewModel.addField(field2)
    viewModel.addField(field3)
    advanceUntilIdle()

    viewModel.reorderFields(0, 2)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("Second", state.fields[0].label)
    assertEquals("Third", state.fields[1].label)
    assertEquals("First", state.fields[2].label)
  }

  @Test
  fun setEditingFieldId_updatesState() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.setEditingFieldId("field1")
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertEquals("field1", state.editingFieldId)
  }

  @Test
  fun setEditingFieldId_withNull_clearsEditingField() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.setEditingFieldId("field1")
    advanceUntilIdle()
    viewModel.setEditingFieldId(null)
    advanceUntilIdle()

    val state = viewModel.state.first()
    assertNull(state.editingFieldId)
  }

  @Test
  fun validateAll_withValidData_returnsTrue() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("Valid Template")
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    val result = viewModel.validateAll()
    advanceUntilIdle()

    assertTrue(result)
    val state = viewModel.state.first()
    assertNull(state.titleError)
    assertEquals(0, state.fieldErrors.size)
  }

  @Test
  fun validateAll_withBlankTitle_returnsFalse() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("")
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    val result = viewModel.validateAll()
    advanceUntilIdle()

    assertFalse(result)
    val state = viewModel.state.first()
    assertEquals("Title is required", state.titleError)
  }

  @Test
  fun validateAll_withNoFields_returnsFalse() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("Valid Template")
    advanceUntilIdle()

    val result = viewModel.validateAll()
    advanceUntilIdle()

    assertFalse(result)
  }

  @Test
  fun validateAll_checksAllValidationRules() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("Valid Template")
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    val result = viewModel.validateAll()
    advanceUntilIdle()

    assertTrue(result)
    val state = viewModel.state.first()
    assertNull(state.titleError)
    assertEquals(0, state.fieldErrors.size)
  }

  @Test
  fun save_withValidData_succeeds() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository, initialProjectId = "proj1")
    advanceUntilIdle()

    viewModel.updateTitle("Test Template")
    viewModel.updateDescription("Test description")
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    mockRepository.setCreateResult(Result.success("template1"))

    var successResult: String? = null
    var failureResult: String? = null
    viewModel.save(onSuccess = { successResult = it }, onFailure = { failureResult = it })
    advanceUntilIdle()

    assertNotNull(successResult)
    assertNull(failureResult)
    assertEquals("template1", successResult)
    assertEquals(1, mockRepository.createTemplateCalls.size)

    val savedTemplate = mockRepository.createTemplateCalls[0]
    assertEquals("Test Template", savedTemplate.title)
    assertEquals("Test description", savedTemplate.description)
    assertEquals("proj1", savedTemplate.projectId)
    assertEquals(1, savedTemplate.definedFields.fields.size)
  }

  @Test
  fun save_withInvalidData_fails() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository)
    advanceUntilIdle()

    viewModel.updateTitle("")
    advanceUntilIdle()

    var successResult: String? = null
    var failureResult: String? = null
    viewModel.save(onSuccess = { successResult = it }, onFailure = { failureResult = it })
    advanceUntilIdle()

    assertNull(successResult)
    assertNotNull(failureResult)
    assertEquals(0, mockRepository.createTemplateCalls.size)
  }

  @Test
  fun save_setsAndClearsSavingState() = runTest {
    viewModel = CreateTemplateViewModel(mockRepository, initialProjectId = "proj1")
    advanceUntilIdle()

    viewModel.updateTitle("Test Template")
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    viewModel.addField(field)
    advanceUntilIdle()

    mockRepository.setCreateResult(Result.success("template1"))

    viewModel.save(onSuccess = {}, onFailure = {})
    advanceUntilIdle()

    val finalState = viewModel.state.first()
    assertFalse(finalState.isSaving)
  }

  @Test
  fun templateEditorState_errorCount_countsAllErrors() {
    val state =
        TemplateEditorState(
            titleError = "Title error",
            fieldErrors = mapOf("field1" to "Error 1", "field2" to "Error 2"))

    assertEquals(3, state.errorCount)
  }

  @Test
  fun templateEditorState_errorCount_withNoErrors_isZero() {
    val state = TemplateEditorState()

    assertEquals(0, state.errorCount)
  }

  @Test
  fun templateEditorState_canSave_withValidState_isTrue() {
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    val state = TemplateEditorState(title = "Valid Title", fields = listOf(field))

    assertTrue(state.canSave)
  }

  @Test
  fun templateEditorState_canSave_withBlankTitle_isFalse() {
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    val state = TemplateEditorState(title = "", fields = listOf(field))

    assertFalse(state.canSave)
  }

  @Test
  fun templateEditorState_canSave_withNoFields_isFalse() {
    val state = TemplateEditorState(title = "Valid Title", fields = emptyList())

    assertFalse(state.canSave)
  }

  @Test
  fun templateEditorState_canSave_withErrors_isFalse() {
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    val state =
        TemplateEditorState(
            title = "Valid Title", fields = listOf(field), titleError = "Title error")

    assertFalse(state.canSave)
  }

  @Test
  fun templateEditorState_canSave_whileSaving_isFalse() {
    val field = FieldDefinition(id = "field1", label = "Name", type = FieldType.Text())
    val state = TemplateEditorState(title = "Valid Title", fields = listOf(field), isSaving = true)

    assertFalse(state.canSave)
  }

  private class MockTaskTemplateRepository : TaskTemplateRepository {
    val createTemplateCalls = mutableListOf<TaskTemplate>()
    val updateTemplateCalls = mutableListOf<TaskTemplate>()
    val deleteTemplateCalls = mutableListOf<Pair<String, String>>()

    private var createResult: Result<String> = Result.success("")

    fun setCreateResult(result: Result<String>) {
      createResult = result
    }

    fun reset() {
      createTemplateCalls.clear()
      updateTemplateCalls.clear()
      deleteTemplateCalls.clear()
      createResult = Result.success("")
    }

    override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> {
      return flowOf(null)
    }

    override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> {
      return flowOf(emptyList())
    }

    override suspend fun createTemplate(template: TaskTemplate): Result<String> {
      createTemplateCalls.add(template)
      return createResult
    }

    override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> {
      updateTemplateCalls.add(template)
      return Result.success(Unit)
    }

    override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> {
      deleteTemplateCalls.add(projectId to templateId)
      return Result.success(Unit)
    }
  }
}

package ch.eureka.eurekapp.model.template

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

class TaskTemplateRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: TaskTemplateRepository
  private val testWorkspaceId = "workspace_template_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/taskTemplates")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreTaskTemplateRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createTemplate_shouldCreateTemplateInFirestore() = runBlocking {
    val template =
        TaskTemplate(
            templateID = "template1",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = TemplateContextType.WORKSPACE,
            title = "Bug Fix Template",
            description = "Template for fixing bugs",
            definedFields = mapOf("priority" to "high", "type" to "bug"))

    val result = repository.createTemplate(template)

    assertTrue(result.isSuccess)
    assertEquals("template1", result.getOrNull())

    val savedTemplate =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("taskTemplates")
            .document("template1")
            .get()
            .await()
            .toObject(TaskTemplate::class.java)

    assertNotNull(savedTemplate)
    assertEquals(template.templateID, savedTemplate?.templateID)
    assertEquals(template.title, savedTemplate?.title)
    assertEquals(template.description, savedTemplate?.description)
  }

  @Test
  fun getTemplateById_shouldReturnTemplateWhenExists() = runBlocking {
    val template =
        TaskTemplate(
            templateID = "template2",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = TemplateContextType.WORKSPACE,
            title = "Feature Template",
            description = "Template for new features",
            definedFields = emptyMap())
    repository.createTemplate(template)

    val flow = repository.getTemplateById(testWorkspaceId, "template2")
    val retrievedTemplate = flow.first()

    assertNotNull(retrievedTemplate)
    assertEquals(template.templateID, retrievedTemplate?.templateID)
    assertEquals(template.title, retrievedTemplate?.title)
  }

  @Test
  fun getTemplateById_shouldReturnNullWhenTemplateDoesNotExist() = runBlocking {
    val flow = repository.getTemplateById(testWorkspaceId, "non_existent_template")
    val retrievedTemplate = flow.first()

    assertNull(retrievedTemplate)
  }

  @Test
  fun getTemplatesInWorkspace_shouldReturnAllTemplates() = runBlocking {
    val template1 =
        TaskTemplate(
            templateID = "template3",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = TemplateContextType.WORKSPACE,
            title = "Template 3",
            description = "",
            definedFields = emptyMap())
    val template2 =
        TaskTemplate(
            templateID = "template4",
            workspaceId = testWorkspaceId,
            contextId = "group1",
            contextType = TemplateContextType.GROUP,
            title = "Template 4",
            description = "",
            definedFields = emptyMap())
    repository.createTemplate(template1)
    repository.createTemplate(template2)

    val flow = repository.getTemplatesInWorkspace(testWorkspaceId)
    val templates = flow.first()

    assertEquals(2, templates.size)
    assertTrue(templates.any { it.templateID == "template3" })
    assertTrue(templates.any { it.templateID == "template4" })
  }

  @Test
  fun getTemplatesInWorkspace_shouldReturnEmptyListWhenNoTemplates() = runBlocking {
    val flow = repository.getTemplatesInWorkspace(testWorkspaceId)
    val templates = flow.first()

    assertTrue(templates.isEmpty())
  }

  @Test
  fun getTemplatesForContext_shouldReturnTemplatesForSpecificContext() = runBlocking {
    val template1 =
        TaskTemplate(
            templateID = "template5",
            workspaceId = testWorkspaceId,
            contextId = "project1",
            contextType = TemplateContextType.PROJECT,
            title = "Project Template 1",
            description = "",
            definedFields = emptyMap())
    val template2 =
        TaskTemplate(
            templateID = "template6",
            workspaceId = testWorkspaceId,
            contextId = "project2",
            contextType = TemplateContextType.PROJECT,
            title = "Project Template 2",
            description = "",
            definedFields = emptyMap())
    repository.createTemplate(template1)
    repository.createTemplate(template2)

    val flow =
        repository.getTemplatesForContext(testWorkspaceId, "project1", TemplateContextType.PROJECT)
    val templates = flow.first()

    assertEquals(1, templates.size)
    assertEquals("template5", templates[0].templateID)
  }

  @Test
  fun updateTemplate_shouldUpdateTemplateDetails() = runBlocking {
    val template =
        TaskTemplate(
            templateID = "template7",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = TemplateContextType.WORKSPACE,
            title = "Original Title",
            description = "Original Description",
            definedFields = emptyMap())
    repository.createTemplate(template)

    val updatedTemplate =
        template.copy(
            title = "Updated Title",
            description = "Updated Description",
            definedFields = mapOf("status" to "active"))
    val result = repository.updateTemplate(updatedTemplate)

    assertTrue(result.isSuccess)

    val savedTemplate =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("taskTemplates")
            .document("template7")
            .get()
            .await()
            .toObject(TaskTemplate::class.java)

    assertNotNull(savedTemplate)
    assertEquals("Updated Title", savedTemplate?.title)
    assertEquals("Updated Description", savedTemplate?.description)
    assertEquals(1, savedTemplate?.definedFields?.size)
  }

  @Test
  fun deleteTemplate_shouldDeleteTemplateFromFirestore() = runBlocking {
    val template =
        TaskTemplate(
            templateID = "template8",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = TemplateContextType.WORKSPACE,
            title = "To Delete",
            description = "",
            definedFields = emptyMap())
    repository.createTemplate(template)

    val result = repository.deleteTemplate(testWorkspaceId, "template8")

    assertTrue(result.isSuccess)

    val deletedTemplate =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("taskTemplates")
            .document("template8")
            .get()
            .await()
            .toObject(TaskTemplate::class.java)

    assertNull(deletedTemplate)
  }
}

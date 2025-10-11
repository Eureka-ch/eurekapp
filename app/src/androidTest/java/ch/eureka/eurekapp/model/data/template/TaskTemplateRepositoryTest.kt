package ch.eureka.eurekapp.model.data.template

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

/**
 * Test suite for TaskTemplateRepository implementation.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class TaskTemplateRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: TaskTemplateRepository
  private val testProjectId = "project_template_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("projects/$testProjectId/taskTemplates")
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
    val projectId = "project_template_1"
    setupTestProject(projectId)

    val template =
        TaskTemplate(
            templateID = "template1",
            projectId = projectId,
            title = "Bug Fix Template",
            description = "Template for fixing bugs",
            definedFields = mapOf("priority" to "high", "type" to "bug"),
            createdBy = testUserId)

    val result = repository.createTemplate(template)

    assertTrue(result.isSuccess)
    assertEquals("template1", result.getOrNull())

    val savedTemplate =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
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
    val projectId = "project_template_2"
    setupTestProject(projectId)

    val template =
        TaskTemplate(
            templateID = "template2",
            projectId = projectId,
            title = "Feature Template",
            description = "Template for new features",
            definedFields = emptyMap(),
            createdBy = testUserId)
    repository.createTemplate(template)

    val flow = repository.getTemplateById(projectId, "template2")
    val retrievedTemplate = flow.first()

    assertNotNull(retrievedTemplate)
    assertEquals(template.templateID, retrievedTemplate?.templateID)
    assertEquals(template.title, retrievedTemplate?.title)
  }

  @Test
  fun getTemplateById_shouldReturnNullWhenTemplateDoesNotExist() = runBlocking {
    val projectId = "project_template_3"
    setupTestProject(projectId)

    val flow = repository.getTemplateById(projectId, "non_existent_template")
    val retrievedTemplate = flow.first()

    assertNull(retrievedTemplate)
  }

  @Test
  fun getTemplatesInProject_shouldReturnAllTemplates() = runBlocking {
    val projectId = "project_template_4"
    setupTestProject(projectId)

    val template1 =
        TaskTemplate(
            templateID = "template3",
            projectId = projectId,
            title = "Template 3",
            description = "",
            definedFields = emptyMap(),
            createdBy = testUserId)
    val template2 =
        TaskTemplate(
            templateID = "template4",
            projectId = projectId,
            title = "Template 4",
            description = "",
            definedFields = emptyMap(),
            createdBy = testUserId)
    repository.createTemplate(template1)
    repository.createTemplate(template2)

    val flow = repository.getTemplatesInProject(projectId)
    val templates = flow.first()

    assertEquals(2, templates.size)
    assertTrue(templates.any { it.templateID == "template3" })
    assertTrue(templates.any { it.templateID == "template4" })
  }

  @Test
  fun getTemplatesInProject_shouldReturnEmptyListWhenNoTemplates() = runBlocking {
    val projectId = "project_template_5"
    setupTestProject(projectId)

    val flow = repository.getTemplatesInProject(projectId)
    val templates = flow.first()

    assertTrue(templates.isEmpty())
  }

  @Test
  fun updateTemplate_shouldUpdateTemplateDetails() = runBlocking {
    val projectId = "project_template_6"
    setupTestProject(projectId)

    val template =
        TaskTemplate(
            templateID = "template7",
            projectId = projectId,
            title = "Original Title",
            description = "Original Description",
            definedFields = emptyMap(),
            createdBy = testUserId)
    repository.createTemplate(template)

    val updatedTemplate =
        template.copy(
            title = "Updated Title",
            description = "Updated Description",
            definedFields = mapOf("status" to "active"),
            createdBy = testUserId)
    val result = repository.updateTemplate(updatedTemplate)

    assertTrue(result.isSuccess)

    val savedTemplate =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
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
    val projectId = "project_template_7"
    setupTestProject(projectId)

    val template =
        TaskTemplate(
            templateID = "template8",
            projectId = projectId,
            title = "To Delete",
            description = "",
            definedFields = emptyMap(),
            createdBy = testUserId)
    repository.createTemplate(template)

    val result = repository.deleteTemplate(projectId, "template8")

    assertTrue(result.isSuccess)

    val deletedTemplate =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("taskTemplates")
            .document("template8")
            .get()
            .await()
            .toObject(TaskTemplate::class.java)

    assertNull(deletedTemplate)
  }
}

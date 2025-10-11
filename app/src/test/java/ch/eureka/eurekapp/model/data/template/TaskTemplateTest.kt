package ch.eureka.eurekapp.model.data.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Test suite for TaskTemplate model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class TaskTemplateTest {

  @Test
  fun taskTemplate_defaultConstructor_createsEmptyTaskTemplate() {
    val template = TaskTemplate()

    assertEquals("", template.templateID)
    assertEquals("", template.projectId)
    assertEquals("", template.title)
    assertEquals("", template.description)
    assertEquals(emptyMap<String, Any>(), template.definedFields)
  }

  @Test
  fun taskTemplate_withParameters_setsCorrectValues() {
    val fields = mapOf("priority" to "high", "estimate" to 5)
    val template =
        TaskTemplate(
            templateID = "tmpl123",
            projectId = "prj123",
            title = "Bug Fix Template",
            description = "Template for bug fixes",
            definedFields = fields)

    assertEquals("tmpl123", template.templateID)
    assertEquals("prj123", template.projectId)
    assertEquals("Bug Fix Template", template.title)
    assertEquals("Template for bug fixes", template.description)
    assertEquals(fields, template.definedFields)
  }

  @Test
  fun taskTemplate_copy_createsNewInstance() {
    val template =
        TaskTemplate(templateID = "tmpl123", projectId = "prj123", title = "Bug Fix Template")
    val copiedTemplate = template.copy(title = "Feature Template")

    assertEquals("tmpl123", copiedTemplate.templateID)
    assertEquals("prj123", copiedTemplate.projectId)
    assertEquals("Feature Template", copiedTemplate.title)
  }

  @Test
  fun taskTemplate_equals_comparesCorrectly() {
    val template1 =
        TaskTemplate(templateID = "tmpl123", projectId = "prj123", title = "Bug Fix Template")
    val template2 =
        TaskTemplate(templateID = "tmpl123", projectId = "prj123", title = "Bug Fix Template")
    val template3 =
        TaskTemplate(templateID = "tmpl456", projectId = "prj456", title = "Feature Template")

    assertEquals(template1, template2)
    assertNotEquals(template1, template3)
  }

  @Test
  fun taskTemplate_hashCode_isConsistent() {
    val template1 =
        TaskTemplate(templateID = "tmpl123", projectId = "prj123", title = "Bug Fix Template")
    val template2 =
        TaskTemplate(templateID = "tmpl123", projectId = "prj123", title = "Bug Fix Template")

    assertEquals(template1.hashCode(), template2.hashCode())
  }

  @Test
  fun taskTemplate_toString_containsAllFields() {
    val template =
        TaskTemplate(
            templateID = "tmpl123",
            projectId = "prj123",
            title = "Bug Fix Template",
            description = "Template for bug fixes")
    val templateString = template.toString()

    assert(templateString.contains("tmpl123"))
    assert(templateString.contains("prj123"))
    assert(templateString.contains("Bug Fix Template"))
  }
}

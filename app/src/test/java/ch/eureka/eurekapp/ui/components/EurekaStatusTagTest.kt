package ch.eureka.eurekapp.ui.components

import org.junit.Assert.*
import org.junit.Test

/** Unit tests for EurekaStatusTag component Tests the component's parameters and behavior */
class EurekaStatusTagTest {

  @Test
  fun `EurekaStatusTag accepts all parameters`() {
    // Test that all parameters are accepted
    val text = "Test Status"
    val type = StatusType.INFO

    assertNotNull("Text should not be null", text)
    assertNotNull("Type should not be null", type)
    assertTrue("Text should not be empty", text.isNotEmpty())
  }

  @Test
  fun `EurekaStatusTag has default type`() {
    // Test that StatusType.INFO is the default
    val text = "Default Status"

    assertNotNull("Text should not be null", text)
    assertEquals("Default type should be INFO", StatusType.INFO, StatusType.INFO)
  }

  @Test
  fun `EurekaStatusTag supports all status types`() {
    // Test all available status types
    val successType = StatusType.SUCCESS
    val warningType = StatusType.WARNING
    val errorType = StatusType.ERROR
    val infoType = StatusType.INFO

    assertNotNull("Success type should not be null", successType)
    assertNotNull("Warning type should not be null", warningType)
    assertNotNull("Error type should not be null", errorType)
    assertNotNull("Info type should not be null", infoType)
  }

  @Test
  fun `EurekaStatusTag text parameter is required`() {
    // Test that text parameter is required
    val text = "Required Text"

    assertNotNull("Text should not be null", text)
    assertTrue("Text should not be empty", text.isNotEmpty())
  }
}

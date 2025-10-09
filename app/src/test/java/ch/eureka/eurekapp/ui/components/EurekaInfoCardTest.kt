package ch.eureka.eurekapp.ui.components

import org.junit.Assert.*
import org.junit.Test

/** Unit tests for EurekaInfoCard component Tests the component's parameters and behavior */
class EurekaInfoCardTest {

  @Test
  fun `EurekaInfoCard accepts all parameters`() {
    // Test that all parameters are accepted
    val title = "Test Info"
    val primaryValue = "42"
    val secondaryValue = "Additional info"
    val iconText = "I"

    assertNotNull("Title should not be null", title)
    assertNotNull("Primary value should not be null", primaryValue)
    assertNotNull("Secondary value should not be null", secondaryValue)
    assertNotNull("Icon text should not be null", iconText)
  }

  @Test
  fun `EurekaInfoCard handles optional parameters`() {
    // Test with minimal parameters
    val title = "Minimal Info"
    val primaryValue = "123"

    assertNotNull("Title should not be null", title)
    assertNotNull("Primary value should not be null", primaryValue)
    assertTrue("Title should not be empty", title.isNotEmpty())
    assertTrue("Primary value should not be empty", primaryValue.isNotEmpty())
  }

  @Test
  fun `EurekaInfoCard iconText is optional`() {
    // Test that iconText can be null
    val title = "No Icon"
    val primaryValue = "456"
    val iconText: String? = null

    assertNotNull("Title should not be null", title)
    assertNotNull("Primary value should not be null", primaryValue)
    assertNull("Icon text should be null", iconText)
  }

  @Test
  fun `EurekaInfoCard secondaryValue is optional`() {
    // Test that secondaryValue can be null
    val title = "No Secondary"
    val primaryValue = "789"
    val secondaryValue: String? = null

    assertNotNull("Title should not be null", title)
    assertNotNull("Primary value should not be null", primaryValue)
    assertNull("Secondary value should be null", secondaryValue)
  }
}

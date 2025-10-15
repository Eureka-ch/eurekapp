package ch.eureka.eurekapp.ui.components

import org.junit.Assert.*
import org.junit.Test

class SimpleUIComponentsTest {

  @Test
  fun `NavItem data class works correctly`() {
    val navItem = NavItem("Test", null)
    assertEquals("Test", navItem.label)
    assertNull(navItem.icon)
  }

  @Test
  fun `NavItem with icon works correctly`() {
    val navItem = NavItem("Home", null)
    assertEquals("Home", navItem.label)
    assertNull(navItem.icon)
  }
}

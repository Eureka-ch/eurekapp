package ch.eureka.eurekapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavItemTest {

  @Test
  fun `NavItem creates correct data class`() {
    val navItem = NavItem("Test", Icons.Default.Home)

    assertEquals("Test", navItem.label)
    assertNotNull(navItem.icon)
  }
}

class StatusTypeTest {

  @Test
  fun `StatusType enum has all expected values`() {
    val values = StatusType.values()

    assertEquals(4, values.size)
    assertEquals(StatusType.SUCCESS, values[0])
    assertEquals(StatusType.WARNING, values[1])
    assertEquals(StatusType.ERROR, values[2])
    assertEquals(StatusType.INFO, values[3])
  }

  @Test
  fun `StatusType enum values are accessible`() {
    assertNotNull(StatusType.SUCCESS)
    assertNotNull(StatusType.WARNING)
    assertNotNull(StatusType.ERROR)
    assertNotNull(StatusType.INFO)
  }
}

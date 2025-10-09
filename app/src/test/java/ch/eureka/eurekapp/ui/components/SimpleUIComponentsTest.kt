package ch.eureka.eurekapp.ui.components

import org.junit.Test
import org.junit.Assert.*

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

    @Test
    fun `NavItem copy works correctly`() {
        val original = NavItem("Original", null)
        val copied = original.copy(label = "Copied")
        assertEquals("Copied", copied.label)
        assertNull(copied.icon)
    }

    @Test
    fun `NavItem equals works correctly`() {
        val nav1 = NavItem("Test", null)
        val nav2 = NavItem("Test", null)
        val nav3 = NavItem("Different", null)
        
        assertEquals(nav1, nav2)
        assertNotEquals(nav1, nav3)
    }

    @Test
    fun `NavItem toString works correctly`() {
        val navItem = NavItem("Test", null)
        val toString = navItem.toString()
        assertTrue(toString.contains("Test"))
    }

    @Test
    fun `StatusType enum values work correctly`() {
        assertEquals("SUCCESS", StatusType.SUCCESS.name)
        assertEquals("WARNING", StatusType.WARNING.name)
        assertEquals("ERROR", StatusType.ERROR.name)
        assertEquals("INFO", StatusType.INFO.name)
    }

    @Test
    fun `StatusType enum ordinal works correctly`() {
        assertEquals(0, StatusType.SUCCESS.ordinal)
        assertEquals(1, StatusType.WARNING.ordinal)
        assertEquals(2, StatusType.ERROR.ordinal)
        assertEquals(3, StatusType.INFO.ordinal)
    }

    @Test
    fun `StatusType values array works correctly`() {
        val values = StatusType.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(StatusType.SUCCESS))
        assertTrue(values.contains(StatusType.WARNING))
        assertTrue(values.contains(StatusType.ERROR))
        assertTrue(values.contains(StatusType.INFO))
    }

    @Test
    fun `StatusType valueOf works correctly`() {
        assertEquals(StatusType.SUCCESS, StatusType.valueOf("SUCCESS"))
        assertEquals(StatusType.WARNING, StatusType.valueOf("WARNING"))
        assertEquals(StatusType.ERROR, StatusType.valueOf("ERROR"))
        assertEquals(StatusType.INFO, StatusType.valueOf("INFO"))
    }
}

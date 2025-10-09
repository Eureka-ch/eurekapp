package ch.eureka.eurekapp.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit tests for EurekaStatusTag component Tests the component's enum and business
 * logic
 */
class EurekaStatusTagComprehensiveTest {

  @Test
  fun `StatusType enum has correct values`() {
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

  @Test
  fun `StatusType enum valueOf works correctly`() {
    assertEquals(StatusType.SUCCESS, StatusType.valueOf("SUCCESS"))
    assertEquals(StatusType.WARNING, StatusType.valueOf("WARNING"))
    assertEquals(StatusType.ERROR, StatusType.valueOf("ERROR"))
    assertEquals(StatusType.INFO, StatusType.valueOf("INFO"))
  }

  @Test
  fun `StatusType enum name property works correctly`() {
    assertEquals("SUCCESS", StatusType.SUCCESS.name)
    assertEquals("WARNING", StatusType.WARNING.name)
    assertEquals("ERROR", StatusType.ERROR.name)
    assertEquals("INFO", StatusType.INFO.name)
  }

  @Test
  fun `StatusType enum ordinal property works correctly`() {
    assertEquals(0, StatusType.SUCCESS.ordinal)
    assertEquals(1, StatusType.WARNING.ordinal)
    assertEquals(2, StatusType.ERROR.ordinal)
    assertEquals(3, StatusType.INFO.ordinal)
  }

  @Test
  fun `StatusType enum toString works correctly`() {
    assertEquals("SUCCESS", StatusType.SUCCESS.toString())
    assertEquals("WARNING", StatusType.WARNING.toString())
    assertEquals("ERROR", StatusType.ERROR.toString())
    assertEquals("INFO", StatusType.INFO.toString())
  }

  @Test
  fun `StatusType enum comparison works correctly`() {
    assertTrue(StatusType.SUCCESS < StatusType.WARNING)
    assertTrue(StatusType.WARNING < StatusType.ERROR)
    assertTrue(StatusType.ERROR < StatusType.INFO)
    assertTrue(StatusType.SUCCESS < StatusType.ERROR)
    assertTrue(StatusType.SUCCESS < StatusType.INFO)
  }

  @Test
  fun `StatusType enum equality works correctly`() {
    assertEquals(StatusType.SUCCESS, StatusType.SUCCESS)
    assertEquals(StatusType.WARNING, StatusType.WARNING)
    assertEquals(StatusType.ERROR, StatusType.ERROR)
    assertEquals(StatusType.INFO, StatusType.INFO)

    assertNotEquals(StatusType.SUCCESS, StatusType.WARNING)
    assertNotEquals(StatusType.WARNING, StatusType.ERROR)
    assertNotEquals(StatusType.ERROR, StatusType.INFO)
  }

  @Test
  fun `StatusType enum hashCode works correctly`() {
    assertEquals(StatusType.SUCCESS.hashCode(), StatusType.SUCCESS.hashCode())
    assertEquals(StatusType.WARNING.hashCode(), StatusType.WARNING.hashCode())
    assertEquals(StatusType.ERROR.hashCode(), StatusType.ERROR.hashCode())
    assertEquals(StatusType.INFO.hashCode(), StatusType.INFO.hashCode())
  }

  @Test
  fun `StatusType enum iteration works correctly`() {
    val expectedOrder =
        listOf(StatusType.SUCCESS, StatusType.WARNING, StatusType.ERROR, StatusType.INFO)
    val actualOrder = StatusType.values().toList()

    assertEquals(expectedOrder, actualOrder)
  }

  @Test
  fun `StatusType enum filtering works correctly`() {
    val allTypes = StatusType.values().toList()
    val errorTypes = allTypes.filter { it == StatusType.ERROR }
    val successTypes = allTypes.filter { it == StatusType.SUCCESS }

    assertEquals(1, errorTypes.size)
    assertEquals(1, successTypes.size)
    assertEquals(StatusType.ERROR, errorTypes[0])
    assertEquals(StatusType.SUCCESS, successTypes[0])
  }

  @Test
  fun `StatusType enum mapping works correctly`() {
    val allTypes = StatusType.values().toList()
    val names = allTypes.map { it.name }
    val ordinals = allTypes.map { it.ordinal }

    assertEquals(listOf("SUCCESS", "WARNING", "ERROR", "INFO"), names)
    assertEquals(listOf(0, 1, 2, 3), ordinals)
  }

  @Test
  fun `StatusType enum grouping works correctly`() {
    val allTypes = StatusType.values().toList()
    val groupedByOrdinal = allTypes.groupBy { it.ordinal }

    assertEquals(4, groupedByOrdinal.size)
    assertEquals(1, groupedByOrdinal[0]?.size)
    assertEquals(1, groupedByOrdinal[1]?.size)
    assertEquals(1, groupedByOrdinal[2]?.size)
    assertEquals(1, groupedByOrdinal[3]?.size)
  }

  @Test
  fun `StatusType enum contains works correctly`() {
    val allTypes = StatusType.values().toList()

    assertTrue(allTypes.contains(StatusType.SUCCESS))
    assertTrue(allTypes.contains(StatusType.WARNING))
    assertTrue(allTypes.contains(StatusType.ERROR))
    assertTrue(allTypes.contains(StatusType.INFO))
  }

  @Test
  fun `StatusType enum indexOf works correctly`() {
    val allTypes = StatusType.values().toList()

    assertEquals(0, allTypes.indexOf(StatusType.SUCCESS))
    assertEquals(1, allTypes.indexOf(StatusType.WARNING))
    assertEquals(2, allTypes.indexOf(StatusType.ERROR))
    assertEquals(3, allTypes.indexOf(StatusType.INFO))
  }

  @Test
  fun `StatusType enum lastIndexOf works correctly`() {
    val allTypes = StatusType.values().toList()

    assertEquals(0, allTypes.lastIndexOf(StatusType.SUCCESS))
    assertEquals(1, allTypes.lastIndexOf(StatusType.WARNING))
    assertEquals(2, allTypes.lastIndexOf(StatusType.ERROR))
    assertEquals(3, allTypes.lastIndexOf(StatusType.INFO))
  }
}

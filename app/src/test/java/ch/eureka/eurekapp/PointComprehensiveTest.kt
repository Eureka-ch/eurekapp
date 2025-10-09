package ch.eureka.eurekapp

import kotlin.math.sqrt
import org.junit.Assert.*
import org.junit.Test

/** Tests complets pour la classe Point */
class PointComprehensiveTest {

  @Test
  fun `Point can be created with coordinates`() {
    val point = Point(3.0, 4.0)
    assertNotNull(point)
  }

  @Test
  fun `Point distance to itself is zero`() {
    val point = Point(5.0, 5.0)
    assertEquals(0.0, point.distanceTo(point), 0.001)
  }

  @Test
  fun `Point distance is symmetric`() {
    val p1 = Point(1.0, 1.0)
    val p2 = Point(4.0, 5.0)

    assertEquals(p1.distanceTo(p2), p2.distanceTo(p1), 0.001)
  }

  @Test
  fun `Point distance with negative coordinates`() {
    val p1 = Point(-2.0, -3.0)
    val p2 = Point(1.0, 1.0)

    val expected = sqrt(9.0 + 16.0)
    assertEquals(expected, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance horizontal line`() {
    val p1 = Point(0.0, 5.0)
    val p2 = Point(3.0, 5.0)

    assertEquals(3.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance vertical line`() {
    val p1 = Point(5.0, 0.0)
    val p2 = Point(5.0, 4.0)

    assertEquals(4.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance with zero coordinates`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(3.0, 4.0)

    assertEquals(5.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance with decimal values`() {
    val p1 = Point(1.5, 2.5)
    val p2 = Point(4.5, 6.5)

    val expected = sqrt(9.0 + 16.0)
    assertEquals(expected, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance large values`() {
    val p1 = Point(100.0, 100.0)
    val p2 = Point(103.0, 104.0)

    val expected = sqrt(9.0 + 16.0)
    assertEquals(expected, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance Pythagorean triple 3-4-5`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(3.0, 4.0)

    assertEquals(5.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance Pythagorean triple 5-12-13`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(5.0, 12.0)

    assertEquals(13.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance Pythagorean triple 8-15-17`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(8.0, 15.0)

    assertEquals(17.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point handles very small distances`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(0.001, 0.001)

    val expected = sqrt(0.000001 + 0.000001)
    assertEquals(expected, p1.distanceTo(p2), 0.0001)
  }

  @Test
  fun `Point distance formula is correct`() {
    val p1 = Point(1.0, 2.0)
    val p2 = Point(4.0, 6.0)

    // Distance = sqrt((4-1)² + (6-2)²) = sqrt(9 + 16) = sqrt(25) = 5
    assertEquals(5.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point with same x different y`() {
    val p1 = Point(5.0, 3.0)
    val p2 = Point(5.0, 8.0)

    assertEquals(5.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point with same y different x`() {
    val p1 = Point(2.0, 7.0)
    val p2 = Point(8.0, 7.0)

    assertEquals(6.0, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance is always positive`() {
    val p1 = Point(-5.0, -5.0)
    val p2 = Point(5.0, 5.0)

    val distance = p1.distanceTo(p2)
    assertTrue(distance > 0)
  }

  @Test
  fun `Point distance with mixed positive and negative`() {
    val p1 = Point(-3.0, 4.0)
    val p2 = Point(3.0, -4.0)

    val expected = sqrt(36.0 + 64.0)
    assertEquals(expected, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance diagonal`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(7.0, 7.0)

    val expected = 7.0 * sqrt(2.0)
    assertEquals(expected, p1.distanceTo(p2), 0.001)
  }

  @Test
  fun `Point distance unit diagonal`() {
    val p1 = Point(0.0, 0.0)
    val p2 = Point(1.0, 1.0)

    assertEquals(sqrt(2.0), p1.distanceTo(p2), 0.001)
  }
}

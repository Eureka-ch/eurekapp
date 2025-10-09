package ch.eureka.eurekapp.resources

import org.junit.Assert.*
import org.junit.Test

/** Tests pour l'objet C (constants/resources) */
class CTest {

  @Test
  fun `C object exists`() {
    assertNotNull(C)
  }

  @Test
  fun `C Tag object exists`() {
    assertNotNull(C.Tag)
  }

  @Test
  fun `greeting tag has correct value`() {
    assertEquals("main_screen_greeting", C.Tag.greeting)
  }

  @Test
  fun `greeting_robo tag has correct value`() {
    assertEquals("second_screen_greeting", C.Tag.greeting_robo)
  }

  @Test
  fun `main_screen_container tag has correct value`() {
    assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun `second_screen_container tag has correct value`() {
    assertEquals("second_screen_container", C.Tag.second_screen_container)
  }

  @Test
  fun `all tags are unique`() {
    val tags =
        setOf(
            C.Tag.greeting,
            C.Tag.greeting_robo,
            C.Tag.main_screen_container,
            C.Tag.second_screen_container)
    assertEquals(4, tags.size)
  }

  @Test
  fun `greeting tag is not empty`() {
    assertTrue(C.Tag.greeting.isNotEmpty())
  }

  @Test
  fun `greeting_robo tag is not empty`() {
    assertTrue(C.Tag.greeting_robo.isNotEmpty())
  }

  @Test
  fun `main_screen_container tag is not empty`() {
    assertTrue(C.Tag.main_screen_container.isNotEmpty())
  }

  @Test
  fun `second_screen_container tag is not empty`() {
    assertTrue(C.Tag.second_screen_container.isNotEmpty())
  }

  @Test
  fun `greeting tags contain screen prefix`() {
    assertTrue(C.Tag.greeting.contains("screen"))
    assertTrue(C.Tag.greeting_robo.contains("screen"))
  }

  @Test
  fun `container tags contain container suffix`() {
    assertTrue(C.Tag.main_screen_container.contains("container"))
    assertTrue(C.Tag.second_screen_container.contains("container"))
  }

  @Test
  fun `tags follow naming convention`() {
    assertTrue(C.Tag.greeting.contains("_"))
    assertTrue(C.Tag.greeting_robo.contains("_"))
  }

  @Test
  fun `C Tag constants are accessible`() {
    val greeting = C.Tag.greeting
    val greetingRobo = C.Tag.greeting_robo
    val mainContainer = C.Tag.main_screen_container
    val secondContainer = C.Tag.second_screen_container

    assertNotNull(greeting)
    assertNotNull(greetingRobo)
    assertNotNull(mainContainer)
    assertNotNull(secondContainer)
  }
}

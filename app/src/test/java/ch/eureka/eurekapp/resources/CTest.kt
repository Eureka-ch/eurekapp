package ch.eureka.eurekapp.resources

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests pour l'objet C (constants/resources)
 *
 * This code was written with help of Claude.
 */
class CTest {

  @Test
  fun cObject_exists() {
    assertNotNull(C)
  }

  @Test
  fun cTagObject_exists() {
    assertNotNull(C.Tag)
  }

  @Test
  fun greetingTag_hasCorrectValue() {
    assertEquals("main_screen_greeting", C.Tag.greeting)
  }

  @Test
  fun greetingRoboTag_hasCorrectValue() {
    assertEquals("second_screen_greeting", C.Tag.greeting_robo)
  }

  @Test
  fun mainScreenContainerTag_hasCorrectValue() {
    assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun secondScreenContainerTag_hasCorrectValue() {
    assertEquals("second_screen_container", C.Tag.second_screen_container)
  }

  @Test
  fun allTags_areUnique() {
    val tags =
        setOf(
            C.Tag.greeting,
            C.Tag.greeting_robo,
            C.Tag.main_screen_container,
            C.Tag.second_screen_container)
    assertEquals(4, tags.size)
  }

  @Test
  fun greetingTag_isNotEmpty() {
    assertTrue(C.Tag.greeting.isNotEmpty())
  }

  @Test
  fun greetingRoboTag_isNotEmpty() {
    assertTrue(C.Tag.greeting_robo.isNotEmpty())
  }

  @Test
  fun mainScreenContainerTag_isNotEmpty() {
    assertTrue(C.Tag.main_screen_container.isNotEmpty())
  }

  @Test
  fun secondScreenContainerTag_isNotEmpty() {
    assertTrue(C.Tag.second_screen_container.isNotEmpty())
  }

  @Test
  fun greetingTags_containScreenPrefix() {
    assertTrue(C.Tag.greeting.contains("screen"))
    assertTrue(C.Tag.greeting_robo.contains("screen"))
  }

  @Test
  fun containerTags_containContainerSuffix() {
    assertTrue(C.Tag.main_screen_container.contains("container"))
    assertTrue(C.Tag.second_screen_container.contains("container"))
  }

  @Test
  fun tags_followNamingConvention() {
    assertTrue(C.Tag.greeting.contains("_"))
    assertTrue(C.Tag.greeting_robo.contains("_"))
  }

  @Test
  fun cTagConstants_areAccessible() {
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

package ch.eureka.eurekapp.resources

import org.junit.Assert.*
import org.junit.Test

/** Tests pour l'objet C (constants/resources) */
class CTest {

  @Test
  fun cObjectExists() {
    assertNotNull(C)
  }

  @Test
  fun cTagObjectExists() {
    assertNotNull(C.Tag)
  }

  @Test
  fun greetingTagHasCorrectValue() {
    assertEquals("main_screen_greeting", C.Tag.greeting)
  }

  @Test
  fun greetingRoboTagHasCorrectValue() {
    assertEquals("second_screen_greeting", C.Tag.greeting_robo)
  }

  @Test
  fun mainScreenContainerTagHasCorrectValue() {
    assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun secondScreenContainerTagHasCorrectValue() {
    assertEquals("second_screen_container", C.Tag.second_screen_container)
  }

  @Test
  fun allTagsAreUnique() {
    val tags =
        setOf(
            C.Tag.greeting,
            C.Tag.greeting_robo,
            C.Tag.main_screen_container,
            C.Tag.second_screen_container)
    assertEquals(4, tags.size)
  }

  @Test
  fun greetingTagIsNotEmpty() {
    assertTrue(C.Tag.greeting.isNotEmpty())
  }

  @Test
  fun greetingRoboTagIsNotEmpty() {
    assertTrue(C.Tag.greeting_robo.isNotEmpty())
  }

  @Test
  fun mainScreenContainerTagIsNotEmpty() {
    assertTrue(C.Tag.main_screen_container.isNotEmpty())
  }

  @Test
  fun secondScreenContainerTagIsNotEmpty() {
    assertTrue(C.Tag.second_screen_container.isNotEmpty())
  }

  @Test
  fun greetingTagsContainScreenPrefix() {
    assertTrue(C.Tag.greeting.contains("screen"))
    assertTrue(C.Tag.greeting_robo.contains("screen"))
  }

  @Test
  fun containerTagsContainContainerSuffix() {
    assertTrue(C.Tag.main_screen_container.contains("container"))
    assertTrue(C.Tag.second_screen_container.contains("container"))
  }

  @Test
  fun tagsFollowNamingConvention() {
    assertTrue(C.Tag.greeting.contains("_"))
    assertTrue(C.Tag.greeting_robo.contains("_"))
  }

  @Test
  fun cTagConstantsAreAccessible() {
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

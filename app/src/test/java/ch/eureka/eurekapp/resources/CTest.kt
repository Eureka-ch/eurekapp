package ch.eureka.eurekapp.resources

import org.junit.Assert.*
import org.junit.Test

/** Tests pour l'objet C (constants/resources) */
class CTest {

  @Test
  fun c_objectExists() {
    assertNotNull(C)
  }

  @Test
  fun cTag_objectExists() {
    assertNotNull(C.Tag)
  }

  @Test
  fun cTag_greetingTagHasCorrectValue() {
    assertEquals("main_screen_greeting", C.Tag.greeting)
  }

  @Test
  fun cTag_greetingRoboTagHasCorrectValue() {
    assertEquals("second_screen_greeting", C.Tag.greeting_robo)
  }

  @Test
  fun cTag_mainScreenContainerTagHasCorrectValue() {
    assertEquals("main_screen_container", C.Tag.main_screen_container)
  }

  @Test
  fun cTag_secondScreenContainerTagHasCorrectValue() {
    assertEquals("second_screen_container", C.Tag.second_screen_container)
  }

  @Test
  fun cTag_allTagsAreUnique() {
    val tags =
        setOf(
            C.Tag.greeting,
            C.Tag.greeting_robo,
            C.Tag.main_screen_container,
            C.Tag.second_screen_container)
    assertEquals(4, tags.size)
  }

  @Test
  fun cTag_greetingTagIsNotEmpty() {
    assertTrue(C.Tag.greeting.isNotEmpty())
  }

  @Test
  fun cTag_greetingRoboTagIsNotEmpty() {
    assertTrue(C.Tag.greeting_robo.isNotEmpty())
  }

  @Test
  fun cTag_mainScreenContainerTagIsNotEmpty() {
    assertTrue(C.Tag.main_screen_container.isNotEmpty())
  }

  @Test
  fun cTag_secondScreenContainerTagIsNotEmpty() {
    assertTrue(C.Tag.second_screen_container.isNotEmpty())
  }

  @Test
  fun cTag_greetingTagsContainScreenPrefix() {
    assertTrue(C.Tag.greeting.contains("screen"))
    assertTrue(C.Tag.greeting_robo.contains("screen"))
  }

  @Test
  fun cTag_containerTagsContainContainerSuffix() {
    assertTrue(C.Tag.main_screen_container.contains("container"))
    assertTrue(C.Tag.second_screen_container.contains("container"))
  }

  @Test
  fun cTag_tagsFollowNamingConvention() {
    assertTrue(C.Tag.greeting.contains("_"))
    assertTrue(C.Tag.greeting_robo.contains("_"))
  }

  @Test
  fun cTag_constantsAreAccessible() {
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

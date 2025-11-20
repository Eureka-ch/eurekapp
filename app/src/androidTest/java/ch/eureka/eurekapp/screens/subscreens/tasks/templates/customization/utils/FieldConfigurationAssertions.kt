package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

/** Custom assertion helpers for field configuration tests. */
object FieldConfigurationAssertions {

  /**
   * Assert that a nullable value is not null and equals expected value.
   *
   * @param expected the expected value
   * @param actual the actual nullable value
   * @param message optional message for assertion failure
   */
  fun <T> assertUpdateEquals(expected: T, actual: T?, message: String = "Values do not match") {
    assertNotNull("Update was null", actual)
    assertEquals(message, expected, actual)
  }

  /**
   * Assert that a boolean property is true.
   *
   * @param value the object containing the property
   * @param propertyGetter lambda to get the boolean property
   * @param message optional message for assertion failure
   */
  fun <T> assertBooleanTrue(
      value: T?,
      propertyGetter: (T) -> Boolean,
      message: String = "Expected true"
  ) {
    assertNotNull("Value was null", value)
    assertTrue(message, propertyGetter(value!!))
  }

  /**
   * Assert that a boolean property is false.
   *
   * @param value the object containing the property
   * @param propertyGetter lambda to get the boolean property
   * @param message optional message for assertion failure
   */
  fun <T> assertBooleanFalse(
      value: T?,
      propertyGetter: (T) -> Boolean,
      message: String = "Expected false"
  ) {
    assertNotNull("Value was null", value)
    assertFalse(message, propertyGetter(value!!))
  }

  /**
   * Assert that a nullable property is null.
   *
   * @param value the object containing the property
   * @param propertyGetter lambda to get the property
   * @param propertyName name of the property for error messages
   */
  fun <T, P> assertPropertyNull(
      value: T?,
      propertyGetter: (T) -> P?,
      propertyName: String = "property"
  ) {
    assertNotNull("Value was null", value)
    assertNull("$propertyName should be null", propertyGetter(value!!))
  }

  /**
   * Assert that a nullable property equals expected value.
   *
   * @param expected the expected property value
   * @param value the object containing the property
   * @param propertyGetter lambda to get the property
   * @param propertyName name of the property for error messages
   */
  fun <T, P> assertPropertyEquals(
      expected: P,
      value: T?,
      propertyGetter: (T) -> P?,
      propertyName: String = "property"
  ) {
    assertNotNull("Value was null", value)
    assertEquals("$propertyName does not match", expected, propertyGetter(value!!))
  }

  /**
   * Assert that a list property is preserved.
   *
   * @param expected the expected list
   * @param value the object containing the list property
   * @param propertyGetter lambda to get the list property
   * @param propertyName name of the property for error messages
   */
  fun <T, E> assertListPreserved(
      expected: List<E>,
      value: T?,
      propertyGetter: (T) -> List<E>,
      propertyName: String = "list"
  ) {
    assertNotNull("Value was null", value)
    assertEquals("$propertyName was not preserved", expected, propertyGetter(value!!))
  }
}

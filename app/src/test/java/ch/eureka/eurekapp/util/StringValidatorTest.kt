package ch.eureka.eurekapp.util

import org.junit.Assert.*
import org.junit.Test

class StringValidatorTest {

  @Test
  fun isValidEmail_validEmail_returnsTrue() {
    assertTrue(StringValidator.isValidEmail("test@example.com"))
    assertTrue(StringValidator.isValidEmail("user.name@example.co.uk"))
    assertTrue(StringValidator.isValidEmail("user+tag@domain.com"))
  }

  @Test
  fun isValidEmail_invalidEmail_returnsFalse() {
    assertFalse(StringValidator.isValidEmail(""))
    assertFalse(StringValidator.isValidEmail("   "))
    assertFalse(StringValidator.isValidEmail("invalid.email"))
    assertFalse(StringValidator.isValidEmail("@example.com"))
    assertFalse(StringValidator.isValidEmail("user@"))
    assertFalse(StringValidator.isValidEmail("user@domain"))
  }

  @Test
  fun isValidPhone_validPhone_returnsTrue() {
    assertTrue(StringValidator.isValidPhone("1234567890"))
    assertTrue(StringValidator.isValidPhone("+41123456789"))
    assertTrue(StringValidator.isValidPhone("123 456 7890"))
    assertTrue(StringValidator.isValidPhone("+1 234 567 8901"))
  }

  @Test
  fun isValidPhone_invalidPhone_returnsFalse() {
    assertFalse(StringValidator.isValidPhone(""))
    assertFalse(StringValidator.isValidPhone("   "))
    assertFalse(StringValidator.isValidPhone("123"))
    assertFalse(StringValidator.isValidPhone("abcd1234567"))
    assertFalse(StringValidator.isValidPhone("12345678901234567890"))
  }

  @Test
  fun isStrongPassword_strongPassword_returnsTrue() {
    assertTrue(StringValidator.isStrongPassword("Password1"))
    assertTrue(StringValidator.isStrongPassword("MyP@ssw0rd"))
    assertTrue(StringValidator.isStrongPassword("Abcdefg1"))
  }

  @Test
  fun isStrongPassword_weakPassword_returnsFalse() {
    assertFalse(StringValidator.isStrongPassword(""))
    assertFalse(StringValidator.isStrongPassword("short"))
    assertFalse(StringValidator.isStrongPassword("alllowercase123"))
    assertFalse(StringValidator.isStrongPassword("ALLUPPERCASE123"))
    assertFalse(StringValidator.isStrongPassword("NoDigitsHere"))
    assertFalse(StringValidator.isStrongPassword("12345678"))
  }

  @Test
  fun sanitize_removesSpecialCharacters() {
    assertEquals("Hello World", StringValidator.sanitize("Hello World!"))
    assertEquals("Test123", StringValidator.sanitize("Test@123#"))
    assertEquals("", StringValidator.sanitize("@#$%^&*()"))
    assertEquals("UserName", StringValidator.sanitize("User_Name"))
  }

  @Test
  fun sanitize_emptyString_returnsEmptyString() {
    assertEquals("", StringValidator.sanitize(""))
  }

  @Test
  fun sanitize_preservesAlphanumeric() {
    assertEquals("abc123 XYZ", StringValidator.sanitize("abc123 XYZ"))
  }
}

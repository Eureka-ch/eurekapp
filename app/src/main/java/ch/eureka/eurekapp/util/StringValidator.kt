package ch.eureka.eurekapp.util

/**
 * Utility class for validating strings.
 *
 * This is a dummy class created to test coverage reporting.
 */
object StringValidator {

  /**
   * Checks if a string is a valid email address.
   *
   * @param email The email string to validate.
   * @return true if the email is valid, false otherwise.
   */
  fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
  }

  /**
   * Checks if a string is a valid phone number (simple validation).
   *
   * @param phone The phone string to validate.
   * @return true if the phone number is valid, false otherwise.
   */
  fun isValidPhone(phone: String): Boolean {
    if (phone.isBlank()) return false

    val phoneRegex = "^[+]?[0-9]{10,15}$".toRegex()
    return phoneRegex.matches(phone.replace("\\s".toRegex(), ""))
  }

  /**
   * Checks if a string is a strong password.
   *
   * @param password The password string to validate.
   * @return true if the password is strong, false otherwise.
   */
  fun isStrongPassword(password: String): Boolean {
    if (password.length < 8) return false

    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }

    return hasUpperCase && hasLowerCase && hasDigit
  }

  /**
   * Sanitizes a string by removing special characters.
   *
   * @param input The string to sanitize.
   * @return The sanitized string.
   */
  fun sanitize(input: String): String {
    return input.replace("[^A-Za-z0-9\\s]".toRegex(), "")
  }
}

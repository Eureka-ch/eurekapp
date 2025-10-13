package ch.eureka.eurekapp.model.data

/**
 * Generic helper function to convert a string to an enum value.
 *
 * This function performs a case-insensitive lookup of enum values by name, eliminating the need for
 * repetitive fromString() methods in each enum.
 *
 * @param T The enum type to convert to.
 * @param value The string value to convert.
 * @return The matching enum constant.
 * @throws IllegalArgumentException if no matching enum constant is found.
 *
 * Example usage:
 * ```
 * val status = enumFromString<TaskStatus>("todo") // Returns TaskStatus.TODO
 * val role = enumFromString<MeetingRole>("HOST") // Returns MeetingRole.HOST
 * ```
 *
 * Note: This file was co-authored by Claude Code.
 */
inline fun <reified T : Enum<T>> enumFromString(value: String): T {
  return enumValues<T>().find { it.name.equals(value, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid ${T::class.simpleName}: $value")
}

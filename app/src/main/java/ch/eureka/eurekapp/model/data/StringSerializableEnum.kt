package ch.eureka.eurekapp.model.data
/**
 * Interface for enums that can be serialized to/from strings.
 *
 * This interface provides a standard way to convert enum values to display strings (lowercase)
 * while maintaining the default uppercase toString() behavior for Firebase serialization.
 *
 * Note: This file was co-authored by Claude Code.
 */
interface StringSerializableEnum {
  /** The name of the enum constant. */
  val name: String

  /**
   * Converts the enum to a lowercase display string.
   *
   * @return The enum name in lowercase for UI display purposes.
   */
  fun toDisplayString(): String = name.lowercase()
}

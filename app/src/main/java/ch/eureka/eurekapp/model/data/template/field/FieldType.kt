/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class FieldTypeKey {
  TEXT,
  NUMBER,
  DATE,
  SINGLE_SELECT,
  MULTI_SELECT
}

@Serializable
sealed interface FieldType {
  val typeKey: FieldTypeKey

  /**
   * Validates the field type configuration without throwing exceptions.
   *
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateConfiguration(): Result<Unit>

  @Serializable
  @SerialName("text")
  data class Text(
      val maxLength: Int? = null,
      val minLength: Int? = null,
      val pattern: String? = null,
      val placeholder: String? = null
  ) : FieldType {
    override val typeKey: FieldTypeKey = FieldTypeKey.TEXT

    init {
      require(maxLength == null || maxLength > 0) { "maxLength must be positive" }
      require(minLength == null || minLength >= 0) { "minLength must be non-negative" }
      require(maxLength == null || minLength == null || maxLength >= minLength) {
        "maxLength must be >= minLength"
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (maxLength != null && maxLength <= 0) {
        return Result.failure(IllegalArgumentException("maxLength must be positive"))
      }
      if (minLength != null && minLength < 0) {
        return Result.failure(IllegalArgumentException("minLength must be non-negative"))
      }
      if (maxLength != null && minLength != null && maxLength < minLength) {
        return Result.failure(IllegalArgumentException("maxLength must be >= minLength"))
      }
      return Result.success(Unit)
    }
  }

  @Serializable
  @SerialName("number")
  data class Number(
      val min: Double? = null,
      val max: Double? = null,
      val step: Double? = null,
      val decimals: Int? = null,
      val unit: String? = null
  ) : FieldType {
    override val typeKey: FieldTypeKey = FieldTypeKey.NUMBER

    init {
      require(min == null || max == null || max >= min) { "max must be >= min" }
      require(step == null || step > 0) { "step must be positive" }
      require(decimals == null || decimals >= 0) { "decimals must be non-negative" }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (min != null && max != null && max < min) {
        return Result.failure(IllegalArgumentException("max must be >= min"))
      }
      if (step != null && step <= 0) {
        return Result.failure(IllegalArgumentException("step must be positive"))
      }
      if (decimals != null && decimals < 0) {
        return Result.failure(IllegalArgumentException("decimals must be non-negative"))
      }
      return Result.success(Unit)
    }
  }

  @Serializable
  @SerialName("date")
  data class Date(
      val minDate: String? = null,
      val maxDate: String? = null,
      val includeTime: Boolean = false,
      val format: String? = null
  ) : FieldType {
    override val typeKey: FieldTypeKey = FieldTypeKey.DATE

    override fun validateConfiguration(): Result<Unit> {
      return Result.success(Unit)
    }
  }

  @Serializable
  @SerialName("single_select")
  data class SingleSelect(val options: List<SelectOption>, val allowCustom: Boolean = false) :
      FieldType {
    override val typeKey: FieldTypeKey = FieldTypeKey.SINGLE_SELECT

    init {
      require(options.isNotEmpty()) { "options must not be empty" }
      require(options.map { it.value }.distinct().size == options.size) {
        "option values must be unique"
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (options.isEmpty()) {
        return Result.failure(IllegalArgumentException("options must not be empty"))
      }
      if (options.map { it.value }.distinct().size != options.size) {
        return Result.failure(IllegalArgumentException("option values must be unique"))
      }
      return Result.success(Unit)
    }
  }

  @Serializable
  @SerialName("multi_select")
  data class MultiSelect(
      val options: List<SelectOption>,
      val minSelections: Int? = null,
      val maxSelections: Int? = null,
      val allowCustom: Boolean = false
  ) : FieldType {
    override val typeKey: FieldTypeKey = FieldTypeKey.MULTI_SELECT

    init {
      require(options.isNotEmpty()) { "options must not be empty" }
      require(options.map { it.value }.distinct().size == options.size) {
        "option values must be unique"
      }
      require(minSelections == null || minSelections >= 0) { "minSelections must be non-negative" }
      require(maxSelections == null || maxSelections > 0) { "maxSelections must be positive" }
      require(minSelections == null || maxSelections == null || maxSelections >= minSelections) {
        "maxSelections must be >= minSelections"
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (options.isEmpty()) {
        return Result.failure(IllegalArgumentException("options must not be empty"))
      }
      if (options.map { it.value }.distinct().size != options.size) {
        return Result.failure(IllegalArgumentException("option values must be unique"))
      }
      if (minSelections != null && minSelections < 0) {
        return Result.failure(IllegalArgumentException("minSelections must be non-negative"))
      }
      if (maxSelections != null && maxSelections <= 0) {
        return Result.failure(IllegalArgumentException("maxSelections must be positive"))
      }
      if (minSelections != null && maxSelections != null && maxSelections < minSelections) {
        return Result.failure(IllegalArgumentException("maxSelections must be >= minSelections"))
      }
      return Result.success(Unit)
    }
  }
}

@Serializable
data class SelectOption(val value: String, val label: String, val description: String? = null) {
  init {
    require(value.isNotBlank()) { "value must not be blank" }
    require(label.isNotBlank()) { "label must not be blank" }
  }
}

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

object FieldTypeErrorMessages {
  const val MAX_LENGTH_POSITIVE = "maxLength must be positive"
  const val MIN_LENGTH_NON_NEGATIVE = "minLength must be non-negative"
  const val MAX_LENGTH_GTE_MIN_LENGTH = "maxLength must be >= minLength"

  const val MAX_GTE_MIN = "max must be >= min"
  const val STEP_POSITIVE = "step must be positive"
  const val DECIMALS_NON_NEGATIVE = "decimals must be non-negative"

  const val OPTIONS_NOT_EMPTY = "options must not be empty"
  const val OPTION_VALUES_UNIQUE = "option values must be unique"

  const val MIN_SELECTIONS_NON_NEGATIVE = "minSelections must be non-negative"
  const val MAX_SELECTIONS_POSITIVE = "maxSelections must be positive"
  const val MAX_SELECTIONS_GTE_MIN_SELECTIONS = "maxSelections must be >= minSelections"

  const val VALUE_NOT_BLANK = "value must not be blank"
  const val LABEL_NOT_BLANK = "label must not be blank"
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
      require(maxLength == null || maxLength > 0) { FieldTypeErrorMessages.MAX_LENGTH_POSITIVE }
      require(minLength == null || minLength >= 0) {
        FieldTypeErrorMessages.MIN_LENGTH_NON_NEGATIVE
      }
      require(maxLength == null || minLength == null || maxLength >= minLength) {
        FieldTypeErrorMessages.MAX_LENGTH_GTE_MIN_LENGTH
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (maxLength != null && maxLength <= 0) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.MAX_LENGTH_POSITIVE))
      }
      if (minLength != null && minLength < 0) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.MIN_LENGTH_NON_NEGATIVE))
      }
      if (maxLength != null && minLength != null && maxLength < minLength) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.MAX_LENGTH_GTE_MIN_LENGTH))
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
      require(min == null || max == null || max >= min) { FieldTypeErrorMessages.MAX_GTE_MIN }
      require(step == null || step > 0) { FieldTypeErrorMessages.STEP_POSITIVE }
      require(decimals == null || decimals >= 0) { FieldTypeErrorMessages.DECIMALS_NON_NEGATIVE }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (min != null && max != null && max < min) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.MAX_GTE_MIN))
      }
      if (step != null && step <= 0) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.STEP_POSITIVE))
      }
      if (decimals != null && decimals < 0) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.DECIMALS_NON_NEGATIVE))
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
      require(options.isNotEmpty()) { FieldTypeErrorMessages.OPTIONS_NOT_EMPTY }
      require(options.map { it.value }.distinct().size == options.size) {
        FieldTypeErrorMessages.OPTION_VALUES_UNIQUE
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (options.isEmpty()) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.OPTIONS_NOT_EMPTY))
      }
      if (options.map { it.value }.distinct().size != options.size) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.OPTION_VALUES_UNIQUE))
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
      require(options.isNotEmpty()) { FieldTypeErrorMessages.OPTIONS_NOT_EMPTY }
      require(options.map { it.value }.distinct().size == options.size) {
        FieldTypeErrorMessages.OPTION_VALUES_UNIQUE
      }
      require(minSelections == null || minSelections >= 0) {
        FieldTypeErrorMessages.MIN_SELECTIONS_NON_NEGATIVE
      }
      require(maxSelections == null || maxSelections > 0) {
        FieldTypeErrorMessages.MAX_SELECTIONS_POSITIVE
      }
      require(minSelections == null || maxSelections == null || maxSelections >= minSelections) {
        FieldTypeErrorMessages.MAX_SELECTIONS_GTE_MIN_SELECTIONS
      }
    }

    override fun validateConfiguration(): Result<Unit> {
      if (options.isEmpty()) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.OPTIONS_NOT_EMPTY))
      }
      if (options.map { it.value }.distinct().size != options.size) {
        return Result.failure(IllegalArgumentException(FieldTypeErrorMessages.OPTION_VALUES_UNIQUE))
      }
      if (minSelections != null && minSelections < 0) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.MIN_SELECTIONS_NON_NEGATIVE))
      }
      if (maxSelections != null && maxSelections <= 0) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.MAX_SELECTIONS_POSITIVE))
      }
      if (minSelections != null && maxSelections != null && maxSelections < minSelections) {
        return Result.failure(
            IllegalArgumentException(FieldTypeErrorMessages.MAX_SELECTIONS_GTE_MIN_SELECTIONS))
      }
      return Result.success(Unit)
    }
  }
}

@Serializable
data class SelectOption(val value: String, val label: String, val description: String? = null) {
  init {
    require(value.isNotBlank()) { FieldTypeErrorMessages.VALUE_NOT_BLANK }
    require(label.isNotBlank()) { FieldTypeErrorMessages.LABEL_NOT_BLANK }
  }
}

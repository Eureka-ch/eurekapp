/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface FieldValue {
  val typeKey: FieldTypeKey

  @Serializable
  @SerialName("text")
  data class TextValue(val value: String) : FieldValue {
    override val typeKey: FieldTypeKey = FieldTypeKey.TEXT
  }

  @Serializable
  @SerialName("number")
  data class NumberValue(val value: Double) : FieldValue {
    override val typeKey: FieldTypeKey = FieldTypeKey.NUMBER
  }

  @Serializable
  @SerialName("date")
  data class DateValue(val value: String) : FieldValue {
    override val typeKey: FieldTypeKey = FieldTypeKey.DATE
  }

  @Serializable
  @SerialName("single_select")
  data class SingleSelectValue(val value: String) : FieldValue {
    override val typeKey: FieldTypeKey = FieldTypeKey.SINGLE_SELECT

    init {
      require(value.isNotBlank()) { "value must not be blank" }
    }
  }

  @Serializable
  @SerialName("multi_select")
  data class MultiSelectValue(values: List<String>) : FieldValue {
    val values: List<String> = values.toList()
    override val typeKey: FieldTypeKey = FieldTypeKey.MULTI_SELECT

    init {
      require(this.values.all { it.isNotBlank() }) { "all values must not be blank" }
      require(this.values.distinct().size == this.values.size) { "values must be unique" }
    }
  }
}

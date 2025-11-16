package ch.eureka.eurekapp.model.data

interface StringSerializableEnum {
  val name: String

  fun toDisplayString(): String = name.lowercase()
}

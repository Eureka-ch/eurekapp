package ch.eureka.eurekapp.model.data

inline fun <reified T : Enum<T>> enumFromString(value: String): T {
  return enumValues<T>().find { it.name.equals(value, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid ${T::class.simpleName}: $value")
}

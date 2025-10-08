package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Restricts string length in generated Firestore rules.
 *
 * Use -1 to indicate no limit.
 * Example: @Length(min = 1, max = 100)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Length(
    val min: Int = -1,
    val max: Int = -1
)

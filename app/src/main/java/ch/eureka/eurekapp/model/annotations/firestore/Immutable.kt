package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Marks a property that cannot be changed after creation.
 *
 * The rules generator will ensure the field value matches the existing resource data on update
 * operations.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Immutable

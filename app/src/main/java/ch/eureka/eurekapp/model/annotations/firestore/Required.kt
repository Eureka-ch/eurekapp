package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Marks a property as required in Firestore documents.
 *
 * The rules generator will emit hasAll() checks to ensure this field is present on create and
 * update operations.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Required

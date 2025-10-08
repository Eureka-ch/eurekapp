package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Marks a timestamp field as server-managed.
 *
 * When writing to Firestore, use FieldValue.serverTimestamp().
 * The rules generator will treat this as a special system field.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServerTimestamp

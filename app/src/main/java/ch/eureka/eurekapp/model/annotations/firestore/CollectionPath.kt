package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Defines the Firestore collection path for this entity.
 *
 * Examples:
 * - Top-level collection: @CollectionPath("users")
 * - Nested collection: @CollectionPath("projects/{projectId}/tasks")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CollectionPath(val path: String)

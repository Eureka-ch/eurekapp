package ch.eureka.eurekapp.model.annotations.firestore

/**
 * Defines Firestore security rules for this entity or field.
 *
 * Use CEL expressions for conditions. Common patterns:
 * - "request.auth != null" - User must be authenticated
 * - "request.auth.uid == resource.data.uid" - User owns the resource
 * - "request.auth.uid in resource.data.members" - User is a member
 * - "true" - Allow all
 * - "false" - Deny all
 *
 * Empty string defaults to "request.auth != null"
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Rules(
    val read: String = "request.auth != null",
    val create: String = "request.auth != null",
    val update: String = "request.auth != null",
    val delete: String = "request.auth != null"
)

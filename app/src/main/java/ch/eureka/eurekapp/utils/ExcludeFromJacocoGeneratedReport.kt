package ch.eureka.eurekapp.utils

// Source - https://stackoverflow.com/a/74715572
// Posted by Mohamed Anees A
// Retrieved 2025-11-12, License - CC BY-SA 4.0

/**
 * Annotation to exclude methods from Jacoco coverage reports.
 *
 * This is particularly useful for Compose Preview functions and other generated code that should
 * not be included in test coverage metrics.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@ExcludeFromJacocoGeneratedReport
annotation class ExcludeFromJacocoGeneratedReport

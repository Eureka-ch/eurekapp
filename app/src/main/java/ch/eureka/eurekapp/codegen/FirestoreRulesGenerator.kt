package ch.eureka.eurekapp.codegen

import ch.eureka.eurekapp.model.annotations.firestore.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Main class for generating Firestore security rules from annotated data classes.
 *
 * Usage:
 * ```
 * val generator = FirestoreRulesGenerator()
 * generator.addClass(User::class)
 * generator.addClass(Workspace::class)
 * val rulesContent = generator.generate()
 * ```
 */
class FirestoreRulesGenerator {
  private val entityClasses = mutableListOf<KClass<*>>()

  /** Add a data class to be processed for rule generation. */
  fun addClass(clazz: KClass<*>): FirestoreRulesGenerator {
    entityClasses.add(clazz)
    return this
  }

  /** Add multiple data classes to be processed. */
  fun addClasses(vararg classes: KClass<*>): FirestoreRulesGenerator {
    entityClasses.addAll(classes)
    return this
  }

  /** Generate the complete Firestore rules file content. */
  fun generate(): String {
    val builder = StringBuilder()

    // Header
    builder.appendLine("rules_version = '2';")
    builder.appendLine()
    builder.appendLine("service cloud.firestore {")
    builder.appendLine("  match /databases/{database}/documents {")
    builder.appendLine()

    // Generate rules for each entity
    for (entityClass in entityClasses) {
      val collectionPath = entityClass.findAnnotation<CollectionPath>() ?: continue

      val rules = entityClass.findAnnotation<Rules>()

      generateMatchBlock(builder, collectionPath.path, entityClass, rules)
      builder.appendLine()
    }

    // Footer
    builder.appendLine("  }")
    builder.appendLine("}")

    return builder.toString()
  }

  private fun generateMatchBlock(
      builder: StringBuilder,
      path: String,
      entityClass: KClass<*>,
      rules: Rules?,
      indentLevel: Int = 2
  ) {
    val indent = "  ".repeat(indentLevel)

    // Convert path to Firestore match format
    val matchPath = convertToMatchPath(path)

    builder.appendLine("$indent// ${entityClass.simpleName}")
    builder.appendLine("${indent}match /$matchPath {")

    // Generate allow rules
    if (rules != null) {
      generateAllowRule(builder, "get", rules.read, indentLevel + 1)
      generateAllowRule(builder, "list", rules.read, indentLevel + 1)
      generateAllowRule(builder, "create", rules.create, entityClass, indentLevel + 1)
      generateAllowRule(builder, "update", rules.update, entityClass, indentLevel + 1)
      generateAllowRule(builder, "delete", rules.delete, indentLevel + 1)
    }

    builder.appendLine("$indent}")
  }

  private fun convertToMatchPath(path: String): String {
    // Split the path by '/' and filter empty segments
    val segments = path.split("/").filter { it.isNotEmpty() }
    val result = mutableListOf<String>()

    var i = 0
    while (i < segments.size) {
      val segment = segments[i]

      if (segment.startsWith("{") && segment.endsWith("}")) {
        // Path variable - this is already a document ID
        // The previous segment should be a collection name
        // Don't add another ID wildcard
        result.add(segment)
      } else {
        // Collection name
        result.add(segment)

        // Check if next segment is a path variable
        if (i + 1 < segments.size &&
            segments[i + 1].startsWith("{") &&
            segments[i + 1].endsWith("}")) {
          // Next segment is a path variable, use it as-is
          // Don't add a wildcard, let the next iteration handle the path variable
        } else {
          // No path variable follows, add a document wildcard
          // Generate a unique document ID variable based on collection name
          // e.g., "users" -> "userId", "chatChannels" -> "channelId"
          val singularName = segment.removeSuffix("s")
          result.add("{${singularName}Id}")
        }
      }
      i++
    }

    return result.joinToString("/")
  }

  private fun generateAllowRule(
      builder: StringBuilder,
      operation: String,
      condition: String,
      indentLevel: Int
  ) {
    val indent = "  ".repeat(indentLevel)

    if (condition.isNotBlank() && condition != "false") {
      builder.appendLine("${indent}allow $operation: if $condition;")
    }
  }

  private fun generateAllowRule(
      builder: StringBuilder,
      operation: String,
      condition: String,
      entityClass: KClass<*>,
      indentLevel: Int
  ) {
    val indent = "  ".repeat(indentLevel)

    if (condition.isBlank() || condition == "false") {
      return
    }

    val validationChecks = mutableListOf<String>()

    // Add base condition
    validationChecks.add(condition)

    // Add field validations for create/update
    if (operation == "create" || operation == "update") {
      val fieldChecks = generateFieldValidations(entityClass, operation)
      if (fieldChecks.isNotEmpty()) {
        validationChecks.addAll(fieldChecks)
      }
    }

    // Combine all checks
    val fullCondition =
        if (validationChecks.size == 1) {
          validationChecks[0]
        } else {
          validationChecks.joinToString(" && \n${indent}         ")
        }

    builder.appendLine("${indent}allow $operation: if $fullCondition;")
  }

  private fun generateFieldValidations(entityClass: KClass<*>, operation: String): List<String> {
    val checks = mutableListOf<String>()
    val requiredFields = mutableListOf<String>()

    for (property in entityClass.memberProperties) {
      @Suppress("UNCHECKED_CAST") val prop = property as KProperty1<Any, *>
      val isRequired = prop.findAnnotation<Required>() != null

      // Check for @Required
      if (isRequired) {
        requiredFields.add(property.name)
      }

      // Check for @Length on strings
      val length = prop.findAnnotation<Length>()
      if (length != null) {
        val fieldRef = "request.resource.data.${property.name}"

        // For optional fields with @Length, only validate if the field exists
        val lengthChecks = mutableListOf<String>()
        if (length.min > 0) {
          lengthChecks.add("$fieldRef.size() >= ${length.min}")
        }
        if (length.max > 0) {
          lengthChecks.add("$fieldRef.size() <= ${length.max}")
        }

        if (lengthChecks.isNotEmpty()) {
          val combinedCheck = lengthChecks.joinToString(" && ")
          // If field is optional, wrap in existence check
          if (!isRequired) {
            checks.add(
                "(!request.resource.data.keys().hasAny(['${property.name}']) || ($combinedCheck))")
          } else {
            checks.add(combinedCheck)
          }
        }
      }

      // Check for @Immutable on updates
      if (operation == "update" && prop.findAnnotation<Immutable>() != null) {
        checks.add("request.resource.data.${property.name} == resource.data.${property.name}")
      }
    }

    // Add hasAll check for required fields
    if (requiredFields.isNotEmpty()) {
      val fieldsList = requiredFields.joinToString("', '", prefix = "['", postfix = "']")
      checks.add(0, "request.resource.data.keys().hasAll($fieldsList)")
    }

    return checks
  }
}

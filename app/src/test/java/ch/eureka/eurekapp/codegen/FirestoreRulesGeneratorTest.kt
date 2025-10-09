package ch.eureka.eurekapp.codegen

import ch.eureka.eurekapp.model.annotations.firestore.*
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit tests for the FirestoreRulesGenerator.
 *
 * These tests verify that the generator correctly processes annotations and produces valid
 * Firestore security rules.
 */
class FirestoreRulesGeneratorTest {

  // Test data classes

  @CollectionPath("users")
  @Rules(
      read = "request.auth != null && request.auth.uid == resource.id",
      create = "request.auth != null && request.auth.uid == request.resource.id",
      update = "request.auth != null && request.auth.uid == resource.id",
      delete = "false")
  data class TestUser(
      @Required @Immutable val uid: String = "",
      @Required @Length(min = 1, max = 100) val displayName: String = "",
      @Required val email: String = "",
      @ServerTimestamp val createdAt: Timestamp = Timestamp(0, 0)
  )

  @CollectionPath("workspaces/{workspaceId}/tasks")
  @Rules(
      read = "request.auth != null",
      create = "request.auth != null",
      update = "request.auth != null",
      delete = "request.auth != null")
  data class TestTask(
      @Required val taskId: String = "",
      @Required @Length(min = 1, max = 200) val title: String = "",
      @Length(max = 5000) val description: String = "",
      @Immutable val workspaceId: String = "",
      @ServerTimestamp val createdAt: Timestamp = Timestamp(0, 0),
      @ServerTimestamp val updatedAt: Timestamp = Timestamp(0, 0)
  )

  @CollectionPath("simple")
  @Rules(read = "true", create = "true", update = "true", delete = "true")
  data class TestSimple(@Required val id: String = "", @Required val name: String = "")

  @CollectionPath("public") data class TestNoRules(@Required val id: String = "")

  @CollectionPath("posts") @Rules(read = "true") data class TestEmptyCreate(val id: String = "")

  // Test suite

  @Test
  fun generate_shouldIncludeRulesVersionAndServiceHeader() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class)

    val output = generator.generate()

    assertTrue(output.contains("rules_version = '2';"))
    assertTrue(output.contains("service cloud.firestore {"))
    assertTrue(output.contains("match /databases/{database}/documents {"))
  }

  @Test
  fun generate_shouldGenerateSimpleCollectionMatch() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class)

    val output = generator.generate()

    assertTrue(output.contains("// TestSimple"))
    assertTrue(output.contains("match /simple/{simpleId} {"))
  }

  @Test
  fun generate_shouldGenerateBasicAllowRules() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class)

    val output = generator.generate()

    assertTrue(output.contains("allow get: if true;"))
    assertTrue(output.contains("allow list: if true;"))
    assertTrue(output.contains("allow create: if true"))
    assertTrue(output.contains("allow update: if true"))
    assertTrue(output.contains("allow delete: if true;"))
  }

  @Test
  fun generate_shouldHandleRequiredFields() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class)

    val output = generator.generate()

    assertTrue(
        output.contains("request.resource.data.keys().hasAll(['id', 'name'])") ||
            output.contains("request.resource.data.keys().hasAll(['name', 'id'])"))
  }

  @Test
  fun generate_shouldHandleLengthValidation() {
    val generator = FirestoreRulesGenerator().addClass(TestUser::class)

    val output = generator.generate()

    assertTrue(output.contains("request.resource.data.displayName.size() >= 1"))
    assertTrue(output.contains("request.resource.data.displayName.size() <= 100"))
  }

  @Test
  fun generate_shouldHandleOptionalFieldsWithLength() {
    val generator = FirestoreRulesGenerator().addClass(TestTask::class)

    val output = generator.generate()

    // description is optional but has max length validation
    assertTrue(
        output.contains(
            "(!request.resource.data.keys().hasAny(['description']) || (request.resource.data.description.size() <= 5000))"))
  }

  @Test
  fun generate_shouldHandleImmutableFields() {
    val generator = FirestoreRulesGenerator().addClass(TestUser::class)

    val output = generator.generate()

    // uid is immutable, should be checked on update
    assertTrue(
        output.contains("request.resource.data.uid == resource.data.uid") ||
            output.contains("allow update: if") && output.contains("uid"))
  }

  @Test
  fun generate_shouldNotValidateServerTimestampFields() {
    val generator = FirestoreRulesGenerator().addClass(TestUser::class)

    val output = generator.generate()

    // @ServerTimestamp fields should not have validation rules to avoid timing issues
    assertTrue(!output.contains("createdAt == request.time"))
  }

  @Test
  fun generate_shouldHandleNestedCollectionPaths() {
    val generator = FirestoreRulesGenerator().addClass(TestTask::class)

    val output = generator.generate()

    assertTrue(output.contains("// TestTask"))
    assertTrue(output.contains("match /workspaces/{workspaceId}/tasks/{taskId} {"))
  }

  @Test
  fun generate_shouldNotGenerateRulesForFalseConditions() {
    @CollectionPath("blocked")
    @Rules(read = "false", create = "false", update = "false", delete = "false")
    data class TestBlocked(val id: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestBlocked::class)

    val output = generator.generate()

    // Should contain the match block but no allow rules
    assertTrue(output.contains("match /blocked/{blockedId} {"))
    assertTrue(!output.contains("allow get:"))
    assertTrue(!output.contains("allow create:"))
  }

  @Test
  fun generate_shouldNotGenerateRulesForEmptyConditions() {
    @CollectionPath("empty")
    @Rules(read = "", create = "", update = "", delete = "")
    data class TestEmpty(val id: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestEmpty::class)

    val output = generator.generate()

    // Should contain the match block but no allow rules
    assertTrue(output.contains("match /empty/{emptyId} {"))
    assertTrue(!output.contains("allow get:"))
    assertTrue(!output.contains("allow create:"))
  }

  @Test
  fun generate_shouldHandleClassWithoutRulesAnnotation() {
    val generator = FirestoreRulesGenerator().addClass(TestNoRules::class)

    val output = generator.generate()

    // Should generate match block but no allow rules inside
    assertTrue(output.contains("// TestNoRules"))
    assertTrue(output.contains("match /public/{publicId} {"))
    // But should not have any allow rules
    val matchBlock = output.substringAfter("// TestNoRules").substringBefore("}")
    assertTrue(!matchBlock.contains("allow"))
  }

  @Test
  fun generate_shouldHandleMultipleClasses() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class).addClass(TestUser::class)

    val output = generator.generate()

    assertTrue(output.contains("// TestSimple"))
    assertTrue(output.contains("match /simple/{simpleId} {"))
    assertTrue(output.contains("// TestUser"))
    assertTrue(output.contains("match /users/{userId} {"))
  }

  @Test
  fun generate_shouldHandleAddClassesVariadic() {
    val generator = FirestoreRulesGenerator().addClasses(TestSimple::class, TestUser::class)

    val output = generator.generate()

    assertTrue(output.contains("// TestSimple"))
    assertTrue(output.contains("// TestUser"))
  }

  @Test
  fun generate_shouldPreserveRuleOrder() {
    val generator = FirestoreRulesGenerator().addClass(TestSimple::class)

    val output = generator.generate()

    val getIndex = output.indexOf("allow get:")
    val listIndex = output.indexOf("allow list:")
    val createIndex = output.indexOf("allow create:")
    val updateIndex = output.indexOf("allow update:")
    val deleteIndex = output.indexOf("allow delete:")

    assertTrue(getIndex > 0)
    assertTrue(listIndex > getIndex)
    assertTrue(createIndex > listIndex)
    assertTrue(updateIndex > createIndex)
    assertTrue(deleteIndex > updateIndex)
  }

  @Test
  fun generate_shouldCombineMultipleValidations() {
    val generator = FirestoreRulesGenerator().addClass(TestUser::class)

    val output = generator.generate()

    // Create rule should have: base condition + required fields + length checks
    val createRuleSection = output.substringAfter("allow create:")
    assertTrue(createRuleSection.contains("request.auth != null"))
    assertTrue(createRuleSection.contains("hasAll"))
    assertTrue(createRuleSection.contains("displayName.size()"))
  }

  @Test
  fun generate_shouldHandleDifferentRulesPerOperation() {
    @CollectionPath("mixed")
    @Rules(read = "true", create = "request.auth != null", update = "false", delete = "false")
    data class TestMixed(val id: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestMixed::class)

    val output = generator.generate()

    assertTrue(output.contains("allow get: if true;"))
    assertTrue(output.contains("allow list: if true;"))
    assertTrue(output.contains("allow create: if request.auth != null"))
    assertTrue(!output.contains("allow update:"))
    assertTrue(!output.contains("allow delete:"))
  }

  @Test
  fun convertToMatchPath_shouldHandleSimplePath() {
    val generator = FirestoreRulesGenerator()
    @CollectionPath("users") @Rules(read = "true") data class Test(val id: String = "")

    val output = generator.addClass(Test::class).generate()

    assertTrue(output.contains("match /users/{userId} {"))
  }

  @Test
  fun convertToMatchPath_shouldHandleNestedPathWithExplicitVariable() {
    val generator = FirestoreRulesGenerator()
    @CollectionPath("workspaces/{workspaceId}/projects")
    @Rules(read = "true")
    data class Test(val id: String = "")

    val output = generator.addClass(Test::class).generate()

    assertTrue(output.contains("match /workspaces/{workspaceId}/projects/{projectId} {"))
  }

  @Test
  fun convertToMatchPath_shouldHandleDeeplyNestedPath() {
    val generator = FirestoreRulesGenerator()
    @CollectionPath("workspaces/{workspaceId}/projects/{projectId}/tasks")
    @Rules(read = "true")
    data class Test(val id: String = "")

    val output = generator.addClass(Test::class).generate()

    assertTrue(
        output.contains("match /workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId} {"))
  }

  @Test
  fun generate_shouldHandleComplexRealWorldExample() {
    @CollectionPath("workspaces/{workspaceId}/chatChannels/{channelId}/messages")
    @Rules(
        read = "request.auth != null",
        create = "request.auth != null && request.auth.uid == request.resource.data.senderId",
        update = "request.auth != null && request.auth.uid == resource.data.senderId",
        delete = "request.auth != null && request.auth.uid == resource.data.senderId")
    data class TestMessage(
        @Required val messageId: String = "",
        @Required @Length(min = 1, max = 5000) val text: String = "",
        @Required @Immutable val senderId: String = "",
        @ServerTimestamp val createdAt: Timestamp = Timestamp(0, 0)
    )

    val generator = FirestoreRulesGenerator().addClass(TestMessage::class)

    val output = generator.generate()

    assertTrue(
        output.contains(
            "match /workspaces/{workspaceId}/chatChannels/{channelId}/messages/{messageId} {"))
    assertTrue(output.contains("allow get: if request.auth != null;"))
    assertTrue(
        output.contains("request.auth.uid == request.resource.data.senderId") ||
            output.contains("senderId"))
    assertTrue(output.contains("request.resource.data.keys().hasAll("))
    assertTrue(output.contains("text.size() >= 1"))
    assertTrue(output.contains("text.size() <= 5000"))
    assertTrue(output.contains("senderId == resource.data.senderId"))
  }

  @Test
  fun generate_shouldProduceValidFirestoreRulesSyntax() {
    val generator =
        FirestoreRulesGenerator().addClasses(TestSimple::class, TestUser::class, TestTask::class)

    val output = generator.generate()

    // Verify basic syntax structure
    assertEquals(1, output.split("rules_version = '2';").size - 1)
    assertEquals(1, output.split("service cloud.firestore {").size - 1)

    // Count opening and closing braces - they should match
    val openBraces = output.count { it == '{' }
    val closeBraces = output.count { it == '}' }
    assertEquals(openBraces, closeBraces)

    // Verify it ends properly
    assertTrue(output.trim().endsWith("}"))
  }

  @Test
  fun generate_shouldHandleMinLengthOnly() {
    @CollectionPath("test")
    @Rules(create = "true")
    data class TestMinOnly(@Required @Length(min = 5) val name: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestMinOnly::class)

    val output = generator.generate()

    assertTrue(output.contains("request.resource.data.name.size() >= 5"))
    assertTrue(!output.contains("request.resource.data.name.size() <="))
  }

  @Test
  fun generate_shouldHandleMaxLengthOnly() {
    @CollectionPath("test")
    @Rules(create = "true")
    data class TestMaxOnly(@Required @Length(max = 100) val name: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestMaxOnly::class)

    val output = generator.generate()

    assertTrue(output.contains("request.resource.data.name.size() <= 100"))
    assertTrue(!output.contains("request.resource.data.name.size() >="))
  }

  @Test
  fun generate_shouldIgnoreLengthWithNegativeValues() {
    @CollectionPath("test")
    @Rules(create = "true")
    data class TestNoLength(@Required @Length(min = -1, max = -1) val name: String = "")

    val generator = FirestoreRulesGenerator().addClass(TestNoLength::class)

    val output = generator.generate()

    // Should not generate any size() checks
    assertTrue(!output.contains("name.size()"))
  }

  @Test
  fun generate_shouldHandleMultipleRequiredFields() {
    @CollectionPath("test")
    @Rules(create = "true")
    data class TestMultiRequired(
        @Required val field1: String = "",
        @Required val field2: String = "",
        @Required val field3: String = "",
        val optional: String = ""
    )

    val generator = FirestoreRulesGenerator().addClass(TestMultiRequired::class)

    val output = generator.generate()

    val hasAllSection = output.substringAfter("hasAll(").substringBefore(")")
    assertTrue(hasAllSection.contains("field1"))
    assertTrue(hasAllSection.contains("field2"))
    assertTrue(hasAllSection.contains("field3"))
    assertTrue(!hasAllSection.contains("optional"))
  }

  @Test
  fun generate_shouldHandleMultipleImmutableFields() {
    @CollectionPath("test")
    @Rules(update = "true")
    data class TestMultiImmutable(
        @Immutable val id: String = "",
        @Immutable val creatorId: String = "",
        val mutableField: String = ""
    )

    val generator = FirestoreRulesGenerator().addClass(TestMultiImmutable::class)

    val output = generator.generate()

    assertTrue(output.contains("request.resource.data.id == resource.data.id"))
    assertTrue(output.contains("request.resource.data.creatorId == resource.data.creatorId"))
  }
}

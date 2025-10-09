package ch.eureka.eurekapp.codegen

import java.io.File
import org.junit.Test

/** Test for Firestore rules generation. This test will generate the rules file. */
class FirestoreRulesGeneratorTest {

  @Test
  fun generateFirestoreRules() {
    println("🔥 Generating Firestore Rules...")

    val outputPath = File("../../firestore.rules").canonicalPath
    val outputFile = GeneratorStandalone.generateToFile(outputPath)

    println("✅ Generated successfully!")
    println("📄 Location: ${outputFile.absolutePath}")
    println()

    GeneratorStandalone.printRules()
  }
}

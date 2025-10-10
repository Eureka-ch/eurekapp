package ch.eureka.eurekapp.codegen

import java.io.File
import org.junit.Test

/** Test that generates the actual Firestore rules file. */
class GenerateRulesTest {

  @Test
  fun generateFirestoreRulesFile() {
    println("🔥 Generating Firestore Rules...")

    val outputPath = File("firestore.rules").absolutePath
    val outputFile = GeneratorStandalone.generateToFile(outputPath)

    println("✅ Generated successfully!")
    println("📄 Location: ${outputFile.absolutePath}")
    println()

    GeneratorStandalone.printRules()
  }
}

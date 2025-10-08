package ch.eureka.eurekapp.codegen

import ch.eureka.eurekapp.model.data.*
import java.io.File

/**
 * Main entry point for generating Firestore rules.
 *
 * Run this to generate firestore.rules file from annotated data classes.
 *
 * Usage:
 * - From IDE: Run this main function
 * - From command line: ./gradlew run -PmainClass=ch.eureka.eurekapp.codegen.RulesGeneratorMainKt
 */
fun main() {
    println("🔥 Generating Firestore Rules...")

    val generator = FirestoreRulesGenerator()
        .addClasses(
            User::class,
            Workspace::class,
            Group::class,
            Project::class,
            TaskTemplate::class,
            Task::class,
            Meeting::class,
            ChatChannel::class,
            Message::class
        )

    val rulesContent = generator.generate()

    // Determine output path
    val outputPath = System.getProperty("output.path")
        ?: "firestore.rules"

    val outputFile = File(outputPath)
    outputFile.writeText(rulesContent)

    println("✅ Firestore rules generated successfully!")
    println("📄 Output: ${outputFile.absolutePath}")
    println()
    println("Preview:")
    println("─".repeat(80))
    println(rulesContent)
    println("─".repeat(80))
}

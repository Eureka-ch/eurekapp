package ch.eureka.eurekapp.codegen

import ch.eureka.eurekapp.model.annotations.firestore.*
import ch.eureka.eurekapp.model.chat.ChatChannel
import ch.eureka.eurekapp.model.chat.Message
import ch.eureka.eurekapp.model.group.Group
import ch.eureka.eurekapp.model.meeting.Meeting
import ch.eureka.eurekapp.model.project.Project
import ch.eureka.eurekapp.model.task.Task
import ch.eureka.eurekapp.model.template.TaskTemplate
import ch.eureka.eurekapp.model.user.User
import ch.eureka.eurekapp.model.workspace.Workspace
import java.io.File

/**
 * Standalone generator that can be run directly from IDE or tests.
 *
 * This avoids complex Gradle configuration issues. Just call generateRules() from anywhere in your
 * code.
 */
object GeneratorStandalone {

  /** Generate Firestore rules and return as String. */
  fun generateRules(): String {
    return FirestoreRulesGenerator()
        .addClasses(
            User::class,
            Workspace::class,
            Group::class,
            Project::class,
            TaskTemplate::class,
            Task::class,
            Meeting::class,
            ChatChannel::class,
            Message::class)
        .generate()
  }

  /** Generate and save to file. */
  fun generateToFile(outputPath: String = "firestore.rules"): File {
    val rulesContent = generateRules()
    val file = File(outputPath)
    file.writeText(rulesContent)
    return file
  }

  /** Print rules to console. */
  fun printRules() {
    println("═".repeat(80))
    println("FIRESTORE RULES")
    println("═".repeat(80))
    println(generateRules())
    println("═".repeat(80))
  }
}

/** Simple main function for easy IDE execution. Right-click and run this file. */
fun main() {
  println("🔥 Generating Firestore Rules...")

  val outputFile = GeneratorStandalone.generateToFile("firestore.rules")

  println("✅ Generated successfully!")
  println("📄 Location: ${outputFile.absolutePath}")
  println()

  GeneratorStandalone.printRules()
}

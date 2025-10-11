package ch.eureka.eurekapp.codegen

import ch.eureka.eurekapp.model.chat.ChatChannel
import ch.eureka.eurekapp.model.chat.Message
import ch.eureka.eurekapp.model.meeting.Meeting
import ch.eureka.eurekapp.model.project.Project
import ch.eureka.eurekapp.model.task.Task
import ch.eureka.eurekapp.model.template.TaskTemplate
import ch.eureka.eurekapp.model.user.User
import java.io.File
import org.junit.Test

/** Test that generates the actual Firestore rules file. */
class GenerateRulesTest {

  @Test
  fun generateFirestoreRulesFile() {
    println("🔥 Generating Firestore Rules...")

    val generator =
        FirestoreRulesGenerator()
            .addClasses(
                User::class,
                Project::class,
                TaskTemplate::class,
                Task::class,
                Meeting::class,
                ChatChannel::class,
                Message::class)

    val rulesContent = generator.generate()
    val outputFile = File("firestore.rules")
    outputFile.writeText(rulesContent)

    println("✅ Generated successfully!")
    println("📄 Location: ${outputFile.absolutePath}")
    println()
    println("Preview:")
    println("=".repeat(80))
    println(rulesContent)
    println("=".repeat(80))
  }
}

import ch.eureka.eurekapp.codegen.GeneratorStandalone
import java.io.File

fun main() {
    println("Generating Firestore Rules...")

    val rulesContent = GeneratorStandalone.generateRules()

    val outputFile = File("firestore.rules")
    outputFile.writeText(rulesContent)

    println("Generated successfully!")
    println("Location: ${outputFile.absolutePath}")
    println()
    println(rulesContent)
}

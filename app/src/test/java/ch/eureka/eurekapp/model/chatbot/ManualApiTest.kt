package ch.eureka.eurekapp.model.chatbot

import ch.eureka.eurekapp.BuildConfig
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * Manual test to verify DeepSeek API works with real queries. Only runs if API key is configured.
 *
 * WARNING: This test makes real API calls and consumes API credits. Do not run frequently. Only run
 * manually when needed to verify API integration.
 */
@RunWith(RobolectricTestRunner::class)
class ManualApiTest {

  @Before
  fun setup() {
    ShadowLog.stream = System.out
    assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())
  }

  @Test
  fun manualApi_determinantQuestion() =
      runTest(timeout = 90.seconds) {
        println("API Key present: ${BuildConfig.DEEPSEEK_API_KEY.isNotBlank()}")

        val dataSource =
            ChatbotApiDataSource(
                apiUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = BuildConfig.DEEPSEEK_API_KEY)

        try {
          val context =
              """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments.
          """
                  .trimIndent()
          val response =
              dataSource.sendMessage("Summarize this meeting and extract action items.", context)

          println("==================== RESPONSE ====================")
          println(response)
          println("==================================================")
        } catch (e: Exception) {
          println("==================== ERROR ====================")
          println("Error type: ${e::class.simpleName}")
          println("Error message: ${e.message}")
          e.printStackTrace()
          println("===============================================")
          throw e
        }
      }
}

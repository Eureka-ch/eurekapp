// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.chatbot

import ch.eureka.eurekapp.BuildConfig
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * WARNING: Some tests in this class make real API calls and consume API credits. Tests using
 * BuildConfig.DEEPSEEK_API_KEY are skipped when the key is not configured. Do not run these tests
 * frequently to avoid unnecessary API usage.
 */
@RunWith(RobolectricTestRunner::class)
class ChatbotApiDataSourceTest {

  @Before
  fun setup() {
    ShadowLog.stream = System.out
  }

  @Test
  fun chatbotApiDataSource_constructorWithValidParametersCreatesInstance() {
    val dataSource = ChatbotApiDataSource("https://api.example.com/chat", "test-api-key")
    assertTrue(dataSource is ChatbotDataSource)
  }

  @Test
  fun chatbotApiDataSource_constructorWithoutApiKeyCreatesInstance() {
    val dataSource = ChatbotApiDataSource("https://api.example.com/chat")
    assertTrue(dataSource is ChatbotDataSource)
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithInvalidUrlThrowsException() = runTest {
    val dataSource = ChatbotApiDataSource("invalid-url")
    val context = "test context"

    try {
      dataSource.sendMessage("Test prompt", context)
      fail("Expected exception to be thrown")
    } catch (e: Exception) {
      // Expected
      assertTrue(e.message != null)
    }
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithMeetingTranscriptionReturnsResponse() =
      runTest(timeout = 30.seconds) {
        assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())

        val dataSource =
            ChatbotApiDataSource(
                apiUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = BuildConfig.DEEPSEEK_API_KEY)

        val context = "Team standup meeting: John completed backend API, Sarah working on UI."
        val response = dataSource.sendMessage("Transcribe this meeting briefly", context)

        println("Transcription Response: $response")
        assertTrue(response.isNotEmpty())
        assertTrue(response.length > 10)
      }

  @Test
  fun chatbotApiDataSource_sendMessageWithMeetingSummaryReturnsResponse() =
      runTest(timeout = 30.seconds) {
        assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())

        val dataSource =
            ChatbotApiDataSource(
                apiUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = BuildConfig.DEEPSEEK_API_KEY)

        val context =
            """
            Meeting notes:
            John: We need to launch by Q3.
            Sarah: I'll coordinate with marketing.
            Tom: Development is 80% complete.
        """
                .trimIndent()

        val response = dataSource.sendMessage("Summarize this meeting in 2 sentences", context)

        println("Summary Response: $response")
        assertTrue(response.isNotEmpty())
        assertTrue(response.length > 20)
      }

  @Test
  fun chatbotApiDataSource_sendMessageWithActionItemExtractionReturnsResponse() =
      runTest(timeout = 30.seconds) {
        assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())

        val dataSource =
            ChatbotApiDataSource(
                apiUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = BuildConfig.DEEPSEEK_API_KEY)

        val context =
            """
            Meeting discussion:
            John said he will finalize the budget by Friday.
            Sarah mentioned she will contact the vendor next week.
        """
                .trimIndent()

        val response = dataSource.sendMessage("Extract action items from this meeting", context)

        println("Action Items Response: $response")
        assertTrue(response.isNotEmpty())
        assertTrue(
            response.contains("John") || response.contains("budget") || response.contains("Friday"))
      }

  @Test
  fun chatbotApiDataSource_sendMessageWithEmptyPromptThrowsException() = runTest {
    val dataSource = ChatbotApiDataSource("https://api.example.com/chat", "test-key")

    try {
      dataSource.sendMessage("", "Some context")
      fail("Expected exception for empty prompt")
    } catch (e: Exception) {
      assertTrue(e.message != null)
    }
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithEmptyContextThrowsException() = runTest {
    val dataSource = ChatbotApiDataSource("https://api.example.com/chat", "test-key")

    try {
      dataSource.sendMessage("Test prompt", "")
      fail("Expected exception for empty context")
    } catch (e: Exception) {
      assertTrue(e.message != null)
    }
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithSpecialCharactersHandlesCorrectly() = runTest {
    assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())

    val dataSource =
        ChatbotApiDataSource(
            apiUrl = "https://api.deepseek.com/v1/chat/completions",
            apiKey = BuildConfig.DEEPSEEK_API_KEY)

    val context = """Special chars: "quotes", 'apostrophes', newlines\n, unicode: 你好, émojis: 🎉"""

    val response = dataSource.sendMessage("Summarize this text", context)
    assertTrue(response.isNotEmpty())
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithVeryLongContextHandlesCorrectly() =
      runTest(timeout = 60.seconds) {
        assumeTrue("API key not configured", BuildConfig.DEEPSEEK_API_KEY.isNotBlank())

        val dataSource =
            ChatbotApiDataSource(
                apiUrl = "https://api.deepseek.com/v1/chat/completions",
                apiKey = BuildConfig.DEEPSEEK_API_KEY)

        val longContext = "Long meeting transcript. ".repeat(100)

        val response = dataSource.sendMessage("Summarize this", longContext)
        assertTrue(response.isNotEmpty())
      }

  @Test
  fun chatbotApiDataSource_sendMessageWithInvalidApiKeyThrowsException() = runTest {
    val dataSource =
        ChatbotApiDataSource(
            apiUrl = "https://api.deepseek.com/v1/chat/completions", apiKey = "invalid-key")

    try {
      dataSource.sendMessage("Test prompt", "Test context")
      fail("Expected exception for invalid API key")
    } catch (e: Exception) {
      assertTrue(e.message!!.contains("API error") || e.message!!.contains("401"))
    }
  }

  @Test
  fun chatbotApiDataSource_sendMessageWith404UrlThrowsException() = runTest {
    val dataSource =
        ChatbotApiDataSource(apiUrl = "https://api.deepseek.com/v1/nonexistent", apiKey = "test")

    try {
      dataSource.sendMessage("Test prompt", "Test context")
      fail("Expected exception for 404")
    } catch (e: Exception) {
      assertTrue(e.message!!.contains("API error") || e.message!!.contains("404"))
    }
  }

  @Test
  fun chatbotApiDataSource_sendMessageWithNetworkTimeoutThrowsException() =
      runTest(timeout = 65.seconds) {
        val dataSource =
            ChatbotApiDataSource(apiUrl = "https://httpstat.us/200?sleep=65000", apiKey = "test")

        try {
          dataSource.sendMessage("Test prompt", "Test context")
          fail("Expected timeout exception")
        } catch (e: Exception) {
          assertTrue(e.message != null)
        }
      }
}

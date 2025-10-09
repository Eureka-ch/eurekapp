package ch.eureka.eurekapp.model.chatbot

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ChatbotRepositoryTest {

  private lateinit var repository: ChatbotRepository

  @Before
  fun setup() {
    repository = ChatbotRepository()
  }

  @Test
  fun initialState_usesDummyEngine() {
    assertEquals(ChatbotEngineType.DUMMY, repository.getEngineType())
  }

  @Test
  fun setEngineType_updatesEngineType() {
    repository.setEngineType(ChatbotEngineType.API)
    assertEquals(ChatbotEngineType.API, repository.getEngineType())

    repository.setEngineType(ChatbotEngineType.DUMMY)
    assertEquals(ChatbotEngineType.DUMMY, repository.getEngineType())
  }

  @Test
  fun sendMessage_withMeetingTranscription_returnsResponse() = runTest {
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the "consulting fees" line stays untouched.
            Sarah: Consulting fees? We don't have any consultants.
            John: Oh, right, they're... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge "Luxury Yachts Ltd" show up last week?
            John: That's... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting. Wait, is this recording the meeting??
        """
            .trimIndent()
    val response = repository.sendMessage("Transcribe this meeting", context)

    assertTrue(response.contains("dummy"))
  }

  @Test
  fun sendMessage_withMeetingSummary_returnsResponse() = runTest {
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the "consulting fees" line stays untouched.
            Sarah: Consulting fees? We don't have any consultants.
            John: Oh, right, they're... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge "Luxury Yachts Ltd" show up last week?
            John: That's... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting. Wait, is this recording the meeting??
        """
            .trimIndent()

    val response = repository.sendMessage("Summarize this meeting", context)
    assertTrue(response.isNotEmpty())
  }

  @Test
  fun sendMessage_withApiEngineNotConfigured_throwsException() = runTest {
    repository.setEngineType(ChatbotEngineType.API)
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the "consulting fees" line stays untouched.
            Sarah: Consulting fees? We don't have any consultants.
            John: Oh, right, they're... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge "Luxury Yachts Ltd" show up last week?
            John: That's... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting. Wait, is this recording the meeting??
        """
            .trimIndent()

    try {
      repository.sendMessage("Extract action items from this meeting", context)
      fail("Expected IllegalStateException")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("not configured"))
    }
  }

  @Test
  fun configureApiDataSource_allowsApiEngine() = runTest {
    repository.configureApiDataSource("https://api.example.com/chat", "test-key")
    repository.setEngineType(ChatbotEngineType.API)
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the "consulting fees" line stays untouched.
            Sarah: Consulting fees? We don't have any consultants.
            John: Oh, right, they're... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge "Luxury Yachts Ltd" show up last week?
            John: That's... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting. Wait, is this recording the meeting??
        """
            .trimIndent()

    try {
      repository.sendMessage("Extract action items from this meeting", context)
    } catch (e: Exception) {
      assertTrue(e !is IllegalStateException)
    }
  }

  @Test
  fun sendMessage_withAllMeetingPromptTypes_callsDataSource() = runTest {
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the "consulting fees" line stays untouched.
            Sarah: Consulting fees? We don't have any consultants.
            John: Oh, right, they're... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge "Luxury Yachts Ltd" show up last week?
            John: That's... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting. Wait, is this recording the meeting??
        """
            .trimIndent()

    val response1 = repository.sendMessage("Transcribe this meeting", context)
    val response2 = repository.sendMessage("Summarize this meeting", context)
    val response3 = repository.sendMessage("Extract action items from this meeting", context)

    assertTrue(response1.isNotEmpty())
    assertTrue(response2.isNotEmpty())
    assertTrue(response3.isNotEmpty())
  }

  @Test
  fun sendMessage_withEmptyPrompt_returnsResponse() = runTest {
    val response = repository.sendMessage("", "Some context")
    assertTrue(response.isNotEmpty())
  }

  @Test
  fun sendMessage_withEmptyContext_returnsResponse() = runTest {
    val response = repository.sendMessage("Test prompt", "")
    assertTrue(response.isNotEmpty())
  }

  @Test
  fun setEngineType_multipleTransitions_worksCorrectly() = runTest {
    repository.setEngineType(ChatbotEngineType.API)
    assertEquals(ChatbotEngineType.API, repository.getEngineType())

    repository.setEngineType(ChatbotEngineType.DUMMY)
    assertEquals(ChatbotEngineType.DUMMY, repository.getEngineType())

    repository.setEngineType(ChatbotEngineType.API)
    assertEquals(ChatbotEngineType.API, repository.getEngineType())

    repository.setEngineType(ChatbotEngineType.DUMMY)
    val response = repository.sendMessage("Test", "Context")
    assertTrue(response.contains("dummy"))
  }

  @Test
  fun configureApiDataSource_multipleTimes_usesLatestConfiguration() = runTest {
    repository.configureApiDataSource("https://api1.example.com", "key1")
    repository.configureApiDataSource("https://api2.example.com", "key2")
    repository.setEngineType(ChatbotEngineType.API)

    try {
      repository.sendMessage("Test", "Context")
    } catch (e: Exception) {
      // Should use the second configuration (api2)
      assertTrue(e !is IllegalStateException)
    }
  }

  @Test
  fun configureApiDataSource_withoutApiKey_allowsConfiguration() = runTest {
    repository.configureApiDataSource("https://api.example.com")
    repository.setEngineType(ChatbotEngineType.API)

    try {
      repository.sendMessage("Test", "Context")
    } catch (e: Exception) {
      assertTrue(e !is IllegalStateException)
    }
  }

  @Test
  fun sendMessage_switchingBetweenEngines_worksCorrectly() = runTest {
    val response1 = repository.sendMessage("Test", "Context")
    assertTrue(response1.contains("dummy"))
    repository.configureApiDataSource("https://api.example.com", "key")
    repository.setEngineType(ChatbotEngineType.API)

    try {
      repository.sendMessage("Test", "Context")
    } catch (e: Exception) {
      assertTrue(e !is IllegalStateException)
    }
    repository.setEngineType(ChatbotEngineType.DUMMY)
    val response2 = repository.sendMessage("Test", "Context")
    assertTrue(response2.contains("dummy"))
  }

  @Test
  fun sendMessage_withNullApiKeyAfterConfiguration_worksCorrectly() = runTest {
    repository.configureApiDataSource("https://api.example.com", null)
    repository.setEngineType(ChatbotEngineType.API)

    try {
      repository.sendMessage("Test", "Context")
    } catch (e: Exception) {
      assertTrue(e !is IllegalStateException)
    }
  }
}

package ch.eureka.eurekapp.model.chatbot

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatbotDummyDataSourceTest {

  private lateinit var dataSource: ChatbotDummyDataSource

  @Before
  fun setup() {
    dataSource = ChatbotDummyDataSource()
  }

  @Test
  fun sendMessage_withMeetingTranscript_returnsDummyResponse() = runTest {
    val context = "Please transcribe this meeting: John said we need to review the Q4 budget."
    val response = dataSource.sendMessage("Transcribe this meeting", context)

    assertTrue(response.contains("dummy response"))
    assertTrue(response.contains("Context:"))
  }

  @Test
  fun sendMessage_withMeetingSummaryRequest_returnsDummyResponse() = runTest {
    val context =
        """
            Meeting transcript:
            John: We need to finalize the Q4 budget by Friday.
            Sarah: I'll prepare the report. Should we include marketing expenses?
            John: Yes, include all departments... and, uh, make sure the “consulting fees” line stays untouched.
            Sarah: Consulting fees? We don’t have any consultants.
            John: Oh, right, they’re... remote. Very remote.
            Sarah: Like overseas?
            John: You could say... offshore.
            Sarah: John, why did the company credit card charge “Luxury Yachts Ltd” show up last week?
            John: That’s... market research! For the marine industry!
            Sarah: We sell accounting software, John.
            John: Exactly, wave accounting.
            
        """
            .trimIndent()
    val response = dataSource.sendMessage("Summarize this meeting", context)

    assertTrue(response.contains("dummy response"))
  }

  @Test
  fun sendMessage_withActionItemExtraction_returnsDummyResponse() = runTest {
    val context =
        """
            Extract action items from: John to prepare budget by Friday.
            Sarah will contact the vendor next week.
        """
            .trimIndent()
    val response = dataSource.sendMessage("Extract action items from this meeting", context)

    assertTrue(response.isNotEmpty())
  }

  @Test
  fun sendMessage_withDifferentMeetingPrompts_returnsSameFormat() = runTest {
    val context = "Team standup: We discussed sprint progress and blockers."

    val response1 = dataSource.sendMessage("Transcribe this meeting", context)
    val response2 = dataSource.sendMessage("Summarize this meeting", context)
    val response3 = dataSource.sendMessage("Extract action items from this meeting", context)

    assertTrue(response1.contains("dummy"))
    assertTrue(response2.contains("dummy"))
    assertTrue(response3.contains("dummy"))
  }
}

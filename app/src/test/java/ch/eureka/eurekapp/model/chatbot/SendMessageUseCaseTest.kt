package ch.eureka.eurekapp.model.chatbot

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

  private lateinit var repository: ChatbotRepository
  private lateinit var useCase: SendMessageUseCase

  @Before
  fun setup() {
    repository = ChatbotRepository()
    useCase = SendMessageUseCase(repository)
  }

  @Test
  fun invoke_withMeetingTranscription_returnsSuccess() = runTest {
    val context = "Transcribe: John said the project deadline is next Monday."
    val result = useCase("Transcribe this meeting", context)

    assertTrue(result.isSuccess)
    val response = result.getOrNull()
    assertTrue(response != null)
  }

  @Test
  fun invoke_withBlankContext_returnsFailure() = runTest {
    val context = ""
    val result = useCase("Transcribe this meeting", context)

    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(exception is IllegalArgumentException)
    assertEquals("Context cannot be blank", exception?.message)
  }

  @Test
  fun invoke_withMeetingSummary_returnsSuccess() = runTest {
    val context =
        """
            Summarize this meeting:
            John: We need to launch the product by Q3.
            Sarah: I'll coordinate with marketing.
            Tom: Development is 80% complete.
        """
            .trimIndent()
    val result = useCase("Summarize this meeting", context)

    assertTrue(result.isSuccess)
    val response = result.getOrNull()
    assertTrue(response != null)
  }

  @Test
  fun invoke_withActionItemExtraction_returnsSuccess() = runTest {
    val context =
        """
            Extract action items:
            - John to finalize budget by Friday
            - Sarah will schedule client meeting next week
            - Team to review proposals by end of month
        """
            .trimIndent()
    val result = useCase("Extract action items from this meeting", context)

    assertTrue(result.isSuccess)
    val response = result.getOrNull()
    assertTrue(response != null)
  }

  @Test
  fun invoke_withApiEngineNotConfigured_returnsFailure() = runTest {
    repository.setEngineType(ChatbotEngineType.API)
    val context = "Test context"

    val result = useCase("Extract action items from this meeting", context)

    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(exception is IllegalStateException)
  }

  @Test
  fun invoke_withDifferentMeetingTypes_eachReturnsSuccess() = runTest {
    val standup = "Standup: Completed tasks, no blockers."
    val retrospective = "Retro: What went well, what to improve."
    val planning = "Planning: Sprint goals and capacity."

    val result1 = useCase("Transcribe this meeting", standup)
    val result2 = useCase("Summarize this meeting", retrospective)
    val result3 = useCase("Extract action items from this meeting", planning)

    assertTrue(result1.isSuccess)
    assertTrue(result2.isSuccess)
    assertTrue(result3.isSuccess)
  }

  @Test
  fun invoke_withLongMeetingTranscript_returnsSuccess() = runTest {
    val context =
        """
            Meeting participants: John (PM), Sarah (Dev), Tom (Design)

            John: Let's review the sprint progress.
            Sarah: Backend API is complete, working on integration.
            Tom: UI mockups approved, starting implementation.
            John: Any blockers?
            Sarah: Need access to staging environment.
            John: I'll arrange that by EOD.
            Tom: No blockers on my end.
            John: Great, next standup tomorrow 9 AM.
        """
            .trimIndent()
    val result = useCase("Summarize this meeting", context)

    assertTrue(result.isSuccess)
    val response = result.getOrThrow()
    assertTrue(response.isNotEmpty())
  }
}

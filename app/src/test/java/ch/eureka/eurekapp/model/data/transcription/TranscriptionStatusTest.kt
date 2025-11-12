package ch.eureka.eurekapp.model.data.transcription

import junit.framework.TestCase.assertEquals
import org.junit.Test

/** Test suite for TranscriptionStatus enum. */
class TranscriptionStatusTest {

  @Test
  fun transcriptionStatus_shouldHaveThreeValues() {
    val values = TranscriptionStatus.values()
    assertEquals(3, values.size)
  }

  @Test
  fun transcriptionStatus_shouldHavePendingValue() {
    val pending = TranscriptionStatus.valueOf("PENDING")
    assertEquals(TranscriptionStatus.PENDING, pending)
  }

  @Test
  fun transcriptionStatus_shouldHaveCompletedValue() {
    val completed = TranscriptionStatus.valueOf("COMPLETED")
    assertEquals(TranscriptionStatus.COMPLETED, completed)
  }

  @Test
  fun transcriptionStatus_shouldHaveFailedValue() {
    val failed = TranscriptionStatus.valueOf("FAILED")
    assertEquals(TranscriptionStatus.FAILED, failed)
  }

  @Test
  fun transcriptionStatus_shouldReturnCorrectName() {
    assertEquals("PENDING", TranscriptionStatus.PENDING.name)
    assertEquals("COMPLETED", TranscriptionStatus.COMPLETED.name)
    assertEquals("FAILED", TranscriptionStatus.FAILED.name)
  }
}

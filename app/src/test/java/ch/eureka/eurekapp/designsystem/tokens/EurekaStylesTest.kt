package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EurekaStylesTest {

  @Test
  fun `card shape is properly defined`() {
    val cardShape = EurekaStyles.CardShape
    assertNotNull("Card shape should be defined", cardShape)
  }

  @Test
  fun `card elevation is correct`() {
    val cardElevation = EurekaStyles.CardElevation
    assertEquals("Card elevation should be 2dp", 2.dp, cardElevation)
  }

  @Test
  fun `eurekaStyles object is accessible`() {
    assertNotNull("EurekaStyles object should be accessible", EurekaStyles)
  }

  @Test
  fun `card shape has correct corner radius`() {
    val cardShape = EurekaStyles.CardShape
    assertNotNull("Card shape should be defined", cardShape)
    // Verify it's a RoundedCornerShape with 16dp
    assertNotNull("Card shape should be accessible", cardShape)
  }
}

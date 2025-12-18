package ch.eureka.eurekapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import kotlinx.coroutines.delay

object ThinkingBubbleTestTags {
  const val THINKING_BUBBLE = "thinkingBubble"
  const val THINKING_TEXT = "thinkingText"
  const val THINKING_LINE = "thinkingLine"
}

/**
 * A special message bubble that displays AI reasoning steps with auto-typing animation.
 * 
 * @param thinkingSteps List of reasoning steps to display progressively
 * @param onComplete Callback when all thinking steps are displayed
 * @param modifier Optional modifier
 */
@Composable
fun ThinkingBubble(
    thinkingSteps: List<String>,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  var currentStepIndex by remember { mutableStateOf(0) }
  var displayedText by remember { mutableStateOf("") }
  var isComplete by remember { mutableStateOf(false) }
  
  val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
  val contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
  
  // Animate opacity for smooth appearance
  val alpha by animateFloatAsState(
    targetValue = if (isComplete) 0f else 1f,
    animationSpec = tween(durationMillis = 300),
    label = "thinkingAlpha"
  )

  // Auto-type each step
  LaunchedEffect(currentStepIndex) {
    if (currentStepIndex < thinkingSteps.size) {
      val step = thinkingSteps[currentStepIndex]
      displayedText = ""
      
      // Type each character
      for (i in step.indices) {
        displayedText = step.substring(0, i + 1)
        delay(30) // Typing speed: 30ms per character
      }
      
      // Wait before showing next step
      delay(400)
      currentStepIndex++
    } else {
      // All steps displayed, wait a bit then complete
      delay(500)
      isComplete = true
      delay(300) // Wait for fade out
      onComplete()
    }
  }

  if (!isComplete) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(vertical = Spacing.sm)
        .alpha(alpha),
      contentAlignment = Alignment.CenterStart
    ) {
      Surface(
        shape = EurekaStyles.CardShape,
        color = containerColor,
        tonalElevation = EurekaStyles.CardElevation,
        modifier = Modifier
          .widthIn(max = 280.dp)
          .testTag(ThinkingBubbleTestTags.THINKING_BUBBLE)
      ) {
        Column(
          modifier = Modifier.padding(Spacing.md),
          verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
          // Display all completed steps
          thinkingSteps.take(currentStepIndex).forEach { step ->
            ThinkingLine(
              text = step,
              contentColor = contentColor
            )
          }
          
          // Current typing step (if any)
          if (currentStepIndex < thinkingSteps.size && displayedText.isNotEmpty()) {
            ThinkingLine(
              text = displayedText,
              contentColor = contentColor,
              showCursor = true
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ThinkingLine(
    text: String,
    contentColor: Color,
    showCursor: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(ThinkingBubbleTestTags.THINKING_LINE),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = if (showCursor) "$text▊" else text,
      style = MaterialTheme.typography.bodySmall,
      color = contentColor,
      textAlign = TextAlign.Start,
      modifier = Modifier.testTag(ThinkingBubbleTestTags.THINKING_TEXT)
    )
  }
}



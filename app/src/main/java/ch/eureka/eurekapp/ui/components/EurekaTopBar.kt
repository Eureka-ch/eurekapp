package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Portions of this code were generated with the help of Grok.

/** Top header bar that appears on all screens with gradient red background and rounded corners */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EurekaTopBar(
    modifier: Modifier = Modifier,
    title: String = "EUREKA",
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
  // Gradient colors: from intense red to darker red
  val gradientStart = MaterialTheme.colorScheme.primary // #E83E3E
  val gradientEnd = Color(0xFFC62828) // Darker red for gradient effect
  val barHeight = 64.dp

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .height(barHeight)
              .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
              .background(
                  brush = Brush.linearGradient(colors = listOf(gradientStart, gradientEnd)))) {
        TopAppBar(
            title = {
              Text(
                  text = title,
                  style =
                      MaterialTheme.typography.titleLarge.copy(
                          fontWeight = FontWeight.SemiBold,
                          fontFamily = FontFamily.SansSerif,
                          letterSpacing = 0.5.sp),
                  color = Color.White)
            },
            navigationIcon = {
              // Ensure navigation icon is white
              if (navigationIcon != null) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                  navigationIcon()
                }
              }
            },
            actions = {
              // Ensure all actions are white
              CompositionLocalProvider(LocalContentColor provides Color.White) { actions() }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Transparent to show gradient
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(barHeight))
      }
}

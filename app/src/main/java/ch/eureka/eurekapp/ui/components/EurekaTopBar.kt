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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Compact red gradient top bar with visible white title/actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EurekaTopBar(
    modifier: Modifier = Modifier,
    title: String = "EUREKA",
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
  val gradient = Brush.verticalGradient(listOf(Color(0xFFE53935), Color(0xFFC62828)))
  val barHeight = 52.dp

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .height(barHeight)
              .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
              .background(gradient)) {
        TopAppBar(
            title = {
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = Color.White)
            },
            navigationIcon = {
              navigationIcon?.let {
                CompositionLocalProvider(LocalContentColor provides Color.White) { it() }
              }
            },
            actions = {
              CompositionLocalProvider(LocalContentColor provides Color.White) { actions() }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(barHeight))
      }
}

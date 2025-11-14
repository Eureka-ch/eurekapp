package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

// Portions of this code were generated with the help of Grok.

/** Top header bar that appears on all screens */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EurekaTopBar(
    modifier: Modifier = Modifier,
    title: String = "EUREKA",
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
  TopAppBar(
      title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary)
      },
      navigationIcon = navigationIcon ?: {},
      actions = actions,
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = modifier.fillMaxWidth())
}

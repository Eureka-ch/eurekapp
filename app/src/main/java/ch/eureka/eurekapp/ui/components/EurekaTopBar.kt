package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/** Top header bar that appears on all screens */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EurekaTopBar(title: String = "EUREKA", modifier: Modifier = Modifier) {
  TopAppBar(
      title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary)
      },
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = modifier.fillMaxWidth())
}

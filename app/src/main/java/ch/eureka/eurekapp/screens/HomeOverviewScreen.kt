package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/**
 * Temporary placeholder for the Home overview entry screen.
 *
 * The real content will be implemented in follow-up milestones. For now we expose the basic CTAs so
 * navigation keeps working after switching the start destination.
 */
@Composable
fun HomeOverviewScreen(
    modifier: Modifier = Modifier,
    onOpenProjects: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenMeetings: () -> Unit = {},
) {
  Column(
      modifier = modifier.fillMaxSize().padding(Spacing.lg),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically)) {
        Text(text = "Home overview", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "This placeholder keeps navigation functional while we build the final UI.",
            style = MaterialTheme.typography.bodyMedium)

        Button(onClick = onOpenProjects, modifier = Modifier.padding(top = 16.dp)) {
          Text("Browse projects")
        }
        Button(onClick = onOpenTasks) { Text("View tasks") }
        Button(onClick = onOpenMeetings) { Text("View meetings") }
      }
}


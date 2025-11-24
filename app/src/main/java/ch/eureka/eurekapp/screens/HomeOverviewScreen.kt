package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.home.HomeOverviewViewModel

/**
 * Home overview entry screen.
 *
 * Currently displays lightweight summaries powered by [HomeOverviewViewModel]. Further UI polish
 * will arrive in later milestones.
 */
@Composable
fun HomeOverviewScreen(
    modifier: Modifier = Modifier,
    onOpenProjects: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenMeetings: () -> Unit = {},
    homeOverviewViewModel: HomeOverviewViewModel = viewModel(),
) {
  val uiState by homeOverviewViewModel.uiState.collectAsState()

  Column(
      modifier = modifier.fillMaxSize().padding(Spacing.lg),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically)) {
        Text(
            text =
                if (uiState.currentUserName.isNotEmpty()) "Hello ${uiState.currentUserName}"
                else "Welcome back",
            style = MaterialTheme.typography.headlineSmall)
        if (uiState.isLoading) {
          CircularProgressIndicator()
        } else {
          SummaryRow(label = "Upcoming tasks", value = uiState.upcomingTasks.size)
          SummaryRow(label = "Next meetings", value = uiState.upcomingMeetings.size)
          SummaryRow(label = "Recent projects", value = uiState.recentProjects.size)
        }
        uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = onOpenProjects) { Text("Browse projects") }
        Button(onClick = onOpenTasks) { Text("View tasks") }
        Button(onClick = onOpenMeetings) { Text("View meetings") }
      }
}

@Composable
private fun SummaryRow(label: String, value: Int) {
  Text(
      text = "$label: $value",
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant)
}


package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object OverviewProjectsScreenTestTags {
  const val OVERVIEW_PROJECTS_SCREEN_TEXT = "OverviewProjectsScreenText" // The text of the overview
  // project screen
}

/**
 * The Overview Project screen is the screen where the user will be able to see all the projects in
 * which they have access to
 * *
 */
@Composable
fun OverviewProjectScreen(projectId: String) {
  Text(
      "Overview Projects Screen",
      modifier = Modifier.testTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT))
  // Camera()
  Camera()
}

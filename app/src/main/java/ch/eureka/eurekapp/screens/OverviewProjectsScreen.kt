package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.InteractiveHelpEntryPoint
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

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
  Box(modifier = Modifier.fillMaxSize().padding(Spacing.md)) {
    Column(modifier = Modifier.fillMaxSize()) {
      Text(
          "Overview Projects Screen: $projectId",
          modifier = Modifier.testTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT))
      Camera()
    }
    InteractiveHelpEntryPoint(
        helpContext = HelpContext.PROJECTS,
        modifier = Modifier.align(Alignment.BottomEnd))
  }
}

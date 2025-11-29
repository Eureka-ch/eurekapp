package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.ScreenWithHelp
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
  ScreenWithHelp(
      helpContext = HelpContext.PROJECTS,
      content = {
        Column(modifier = Modifier.fillMaxSize().padding(Spacing.md)) {
          Text(
              "Overview Projects Screen: $projectId",
              modifier =
                  Modifier.testTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT))
          Camera()
        }
      })
}

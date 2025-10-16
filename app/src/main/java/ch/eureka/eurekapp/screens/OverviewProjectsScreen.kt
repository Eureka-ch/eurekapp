package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.screens.subscreens.overview_project_subscreens.CreateInvitationSubscreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen

object OverviewProjectsScreenTestTags {
  const val OVERVIEW_PROJECTS_SCREEN_TEXT = "OverviewProjectsScreenText"
}

@Composable
fun OverviewProjectsScreen(navigationController: NavHostController = rememberNavController()) {
  Text(
      "Overview Projects Screen",
      modifier = Modifier.testTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT))
  // Camera()
    CreateInvitationSubscreen(
        projectId = "KWBupYqndWMhMAlR1AnD",
        onInvitationCreate = {}
    )
}

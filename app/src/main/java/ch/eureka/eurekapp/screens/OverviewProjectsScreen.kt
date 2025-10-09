package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

enum class OverviewProjectsScreenTestTags {
  OVERVIEW_PROJECTS_SCREEN_TEXT
}

@Composable
fun OverviewProjectsScreen(navigationController: NavHostController = rememberNavController()) {
  Text(
      "Overview Projects Screen",
      modifier =
          Modifier.testTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT.name))
}

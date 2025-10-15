package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object ProjectSelectionScreenTestTags {
  const val PROJECT_SELECTION_TEXT = "ProjectSelectionText"
}

@Composable
fun ProjectSelectionScreen(navigationController: NavHostController = rememberNavController()) {
  Text(
      "Project Selection Screen",
      modifier = Modifier.testTag(ProjectSelectionScreenTestTags.PROJECT_SELECTION_TEXT))

}

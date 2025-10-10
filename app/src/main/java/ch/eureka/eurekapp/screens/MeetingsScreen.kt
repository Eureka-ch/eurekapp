package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object MeetingsScreenTestTags {
  const val MEETINGS_SCREEN_TEXT = "MeetingsScreenText"
}

@Composable
fun MeetingsScreen(navigationController: NavHostController = rememberNavController()) {
  Text("Meetings Screen", modifier = Modifier.testTag(MeetingsScreenTestTags.MEETINGS_SCREEN_TEXT))
}

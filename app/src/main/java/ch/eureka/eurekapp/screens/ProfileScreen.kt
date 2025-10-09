package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object ProfileScreenTestTags {
  const val PROFILE_SCREEN_TEXT = "ProfileScreenText"
}

@Composable
fun ProfileScreen(navigationController: NavHostController = rememberNavController()) {
  Text(
      "Profile Screen", modifier = Modifier.testTag(ProfileScreenTestTags.PROFILE_SCREEN_TEXT))
}

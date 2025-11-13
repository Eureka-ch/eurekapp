package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object IdeasScreenTestTags {
  const val IDEAS_SCREEN_TEXT = "IdeasScreenText"
}

@Composable
fun IdeasScreen() {
  Text("Ideas Screen", modifier = Modifier.testTag(IdeasScreenTestTags.IDEAS_SCREEN_TEXT))
}

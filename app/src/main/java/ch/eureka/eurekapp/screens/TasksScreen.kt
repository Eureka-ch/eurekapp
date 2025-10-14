package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object TasksScreenTestTags {
  const val TASKS_SCREEN_TEXT = "TasksScreenText"
}

@Composable
fun TasksScreen(navigationController: NavHostController = rememberNavController()) {
  Text("Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
}

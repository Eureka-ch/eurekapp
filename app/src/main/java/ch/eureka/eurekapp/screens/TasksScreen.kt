package ch.eureka.eurekapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.navigation.TaskSpecificScreens
import ch.eureka.eurekapp.navigation.navigationFunction
import ch.eureka.eurekapp.ui.designsystem.tokens.*

object TasksScreenTestTags {
  const val TASKS_SCREEN_TEXT = "TasksScreenText"
  const val CREATE_TASK_BUTTON = "CreateTaskButton"
}

@Composable
fun TasksScreen(navigationController: NavHostController = rememberNavController()) {

  Box(modifier = Modifier.fillMaxSize()) {
    Button(
        onClick = {
          navigationFunction(
              navigationController = navigationController,
              destination = TaskSpecificScreens.CreateTaskScreen,
              args = arrayOf("1234"))
        },
        colors = EurekaStyles.PrimaryButtonColors(),
        modifier =
            Modifier.align(Alignment.TopEnd).testTag(TasksScreenTestTags.CREATE_TASK_BUTTON)) {
          Row {
            Icon(Icons.Filled.Add, "Add Icon")
            Text("Add new task")
          }
        }

    Text(
        "Tasks Screen",
        modifier = Modifier.align(Alignment.Center).testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
  }
}

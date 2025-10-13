package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object CreateTaskScreenTestTags {
    const val CREATE_TASK_TEXT = "CreateTaskText"
}

@Composable
fun CreateTaskScreen(navigationController: NavHostController = rememberNavController()) {
    Text("Create task screen",
        modifier = Modifier.testTag(CreateTaskScreenTestTags.CREATE_TASK_TEXT))
}
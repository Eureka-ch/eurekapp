package ch.eureka.eurekapp.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.ui.tasks.TasksScreen as UITasksScreen

@Composable
fun TasksScreen(navigationController: NavHostController = rememberNavController()) {
  UITasksScreen(onNavigate = { route -> navigationController.navigate(route) })
}

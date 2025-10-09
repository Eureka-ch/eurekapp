package ch.eureka.eurekapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProjectSelectionScreen(navigationController: NavHostController = rememberNavController()){
    Text("Project Selection Screen")
}
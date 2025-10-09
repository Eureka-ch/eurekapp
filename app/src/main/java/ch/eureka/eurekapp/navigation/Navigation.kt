package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.screens.IdeasScreen
import ch.eureka.eurekapp.screens.MeetingsScreen
import ch.eureka.eurekapp.screens.OverviewProjectsScreen
import ch.eureka.eurekapp.screens.ProfileScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen


enum class MainNavigationScreens(title: String, isMainPage:Boolean  = true){

    //The general page where the user will select the project he wants to see or accept an invitation
    ProjectSelectionScreen("General Overview"),
    //The page where the user will have a general overview of the project he selected
    OverviewProjectScreen("Overview Project Screen"),

    //The page where the user will be able to see their profile details (and edit them)
    ProfileScreen("Profile Screen"),

    //The page where the user will be able to see the meetings
    MeetingsScreen("Meetings Screen"),

    //The Screen where the user will be able to see ideas
    IdeasScreen("Ideas Screen"),

    //The screen where the user will see all the tasks
    TasksScreen("Tasks Screen"),
}

enum class TaskSpecificScreens(title: String, isMainPage:Boolean  = false) {
    //The screen where the user will be able to see a specific task's detail
    TasksDetailScreen("Task Detail Screen"),
    //The screen where the user will be able to edit a task
    TasksEditScreen("Task Edit Screen"),
    //The screen with AI suggestions on how to share tasks and time between members of the team
    AutoTaskAssignmentScreen("Task Assignment Screen"),
    //The screen showing dependences between tasks.
    TaskDependencePage("Task Dependence Page")
}

enum class IdeasSpecificScreens(title: String, isMainPage:Boolean  = false){
    //The screen where we could get ideas from our chat and AI suggestions
    CreateIdeasScreen("Create Ideas Screen"),
}

enum class MeetingsSpecificScreens(title: String, isMainPage:Boolean  = false){
    //Add Meeting Screen
    AddMeetingScreen("Add Meeting Screen"),
    //Sub page to the meetings Screen where the user will be able to take pictures
    CameraScreen("Camera Screen"),
    //Sub page to the meetings screen where the user will be able to start the recording
    // in order to make ann audio transcript
    AudioTranscriptScreen("Audio Transcript Screen"),
}

@Composable
fun NavigationMenu(isUserSignedIn: Boolean){
    val navigationController = rememberNavController()
    Scaffold(
        bottomBar = {

        }

    ) {innerPadding ->
        NavHost(modifier = Modifier.padding(innerPadding),navController = navigationController, startDestination = MainNavigationScreens.ProjectSelectionScreen.name){
            composable(MainNavigationScreens.ProjectSelectionScreen.name) {
                ProjectSelectionScreen(navigationController)
            }
            composable(MainNavigationScreens.ProfileScreen.name) {
                ProfileScreen(navigationController)
            }
            composable(MainNavigationScreens.MeetingsScreen.name) {
                MeetingsScreen(navigationController)
            }
            composable(MainNavigationScreens.IdeasScreen.name) {
                IdeasScreen(navigationController)
            }
            composable(MainNavigationScreens.OverviewProjectScreen.name) {
                OverviewProjectsScreen(navigationController)
            }
            composable(MainNavigationScreens.TasksScreen.name) {
                TasksScreen(navigationController)
            }
        }
    }
}

fun navigationFunction(navigationController: NavController , goBack: Boolean = false, destination: String){
    if(goBack){
        navigationController.popBackStack()
    }else{
        navigationController.navigate(destination){
            launchSingleTop = true
            restoreState = true
            popUpTo(navigationController.graph.startDestinationId){
                saveState = true
            }
        }
    }
}
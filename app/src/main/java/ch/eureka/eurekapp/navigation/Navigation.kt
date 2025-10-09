package ch.eureka.eurekapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.screens.IdeasScreen
import ch.eureka.eurekapp.screens.MeetingsScreen
import ch.eureka.eurekapp.screens.OverviewProjectsScreen
import ch.eureka.eurekapp.screens.ProfileScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen

abstract class Screen(val title: String);

class MainScreen(title: String): Screen(title)

class SubScreen(title: String, val parentScreen: MainScreen): Screen(title)

object MainScreens {
    //The general page where the user will select the project he wants to see or accept an invitation
    val ProjectSelectionScreen = MainScreen("General Overview")
    //The page where the user will have a general overview of the project he selected
    val OverviewProjectScreen = MainScreen("Overview Project Screen")
    //The page where the user will be able to see their profile details (and edit them)
    val ProfileScreen = MainScreen("Profile Screen")
    //The page where the user will be able to see the meetings
    val MeetingsScreen = MainScreen("Meetings Screen")
    //The Screen where the user will be able to see ideas
    val IdeasScreen = MainScreen("Ideas Screen")
    //The screen where the user will see all the tasks
    val TasksScreen = MainScreen("Tasks Screen")
}

object TaskSpecificScreens{
    // The screen where the user will be able to see a specific task's detail
    val TasksDetailScreen = SubScreen("Task Detail Screen", MainScreens.TasksScreen)

    // The screen where the user will be able to edit a task
    val TasksEditScreen = SubScreen("Task Edit Screen",MainScreens.TasksScreen)

    // The screen with AI suggestions on how to share tasks and time between members of the team
    val AutoTaskAssignmentScreen = SubScreen("Task Assignment Screen",MainScreens.TasksScreen)

    // The screen showing dependences between tasks.
    val TaskDependencePage = SubScreen("Task Dependence Page",MainScreens.TasksScreen)
}

object IdeasSpecificScreens {
    //The screen where we could get ideas from our chat and AI suggestions
    val CreateIdeasScreen = SubScreen("Create Ideas Screen", MainScreens.IdeasScreen)
}

object MeetinsSpecificScreens {
    //Add Meeting Screen
    val AddMeetingScreen = SubScreen("Add Meeting Screen", MainScreens.MeetingsScreen)
    //Sub page to the meetings Screen where the user will be able to take pictures
    val CameraScreen = SubScreen("Camera Screen", MainScreens.MeetingsScreen)
    //Sub page to the meetings screen where the user will be able to start the recording
    // in order to make an audio transcript
    val AudioTranscriptScreen = SubScreen("Audio Transcript Screen", MainScreens.MeetingsScreen)
}

private val titleToScreensMap = mapOf<String, Screen>(
    MainScreens.MeetingsScreen.title to MainScreens.MeetingsScreen,
    MainScreens.TasksScreen.title to MainScreens.TasksScreen,
    MainScreens.OverviewProjectScreen.title to MainScreens.OverviewProjectScreen,
    MainScreens.ProfileScreen.title to MainScreens.ProfileScreen,
    MainScreens.IdeasScreen.title to MainScreens.IdeasScreen,
    MainScreens.ProjectSelectionScreen.title to MainScreens.ProjectSelectionScreen,

)

@Composable
fun NavigationMenu(isUserSignedIn: Boolean){
    val navigationController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val navBackStackEntry by navigationController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var currentScreen by remember { mutableStateOf<Screen>(MainScreens.OverviewProjectScreen) }

    LaunchedEffect(currentRoute) {
        if(currentRoute != null){
            currentScreen = titleToScreensMap[currentRoute]!!

        }else{
            currentScreen = MainScreens.OverviewProjectScreen
        }
    }
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomBarNavigationComponent(
                navigationController = navigationController,
                currentScreen = currentScreen,
                screenTotalHeight = screenHeight
            )
        }

    ) {innerPadding ->
        NavHost(modifier = Modifier.padding(innerPadding),navController = navigationController, startDestination = MainScreens.ProjectSelectionScreen.title){
            composable(MainScreens.ProjectSelectionScreen.title) {
                ProjectSelectionScreen(navigationController)
            }
            composable(MainScreens.ProfileScreen.title) {
                ProfileScreen(navigationController)
            }
            composable(MainScreens.MeetingsScreen.title) {
                MeetingsScreen(navigationController)
            }
            composable(MainScreens.IdeasScreen.title) {
                IdeasScreen(navigationController)
            }
            composable(MainScreens.OverviewProjectScreen.title) {
                OverviewProjectsScreen(navigationController)
            }
            composable(MainScreens.TasksScreen.title) {
                TasksScreen(navigationController)
            }
        }
    }
}

fun navigationFunction(navigationController: NavController , goBack: Boolean = false, destination: Screen){
    if(goBack){
        navigationController.popBackStack()
    }else{
        navigationController.navigate(destination.title){
            launchSingleTop = true
            restoreState = true
            popUpTo(navigationController.graph.startDestinationId){
                saveState = true
            }
        }
    }
}
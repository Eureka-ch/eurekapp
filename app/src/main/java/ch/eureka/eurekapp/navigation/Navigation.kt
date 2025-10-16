package ch.eureka.eurekapp.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CreateTaskScreen
import ch.eureka.eurekapp.screens.IdeasScreen
import ch.eureka.eurekapp.screens.MeetingsScreen
import ch.eureka.eurekapp.screens.OverviewProjectsScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.profile.ProfileScreen

abstract class Screen(val title: String)

class MainScreen(title: String) : Screen(title)

class SubScreen(title: String, val parentScreen: MainScreen) : Screen(title)

class SharedScreen(title: String) : Screen(title)

object MainScreens {
  // The general page where the user will select the project he wants to see or accept an invitation
  val ProjectSelectionScreen = MainScreen("General Overview")
  // The page where the user will have a general overview of the project he selected
  val OverviewProjectScreen = MainScreen("Overview Project Screen")
  // The page where the user will be able to see their profile details (and edit them)
  val ProfileScreen = MainScreen("Profile Screen")
  // The page where the user will be able to see the meetings
  val MeetingsScreen = MainScreen("Meetings Screen")
  // The Screen where the user will be able to see ideas
  val IdeasScreen = MainScreen("Ideas Screen")
  // The screen where the user will see all the tasks
  val TasksScreen = MainScreen("Tasks Screen")
}

object TaskSpecificScreens {
  // The screen where the user will be able to see a specific task's detail
  val TasksDetailScreen = SubScreen("Task Detail Screen", MainScreens.TasksScreen)

  // The screen where the user will be able to edit a task
  val TasksEditScreen = SubScreen("Task Edit Screen", MainScreens.TasksScreen)

  // The screen with AI suggestions on how to share tasks and time between members of the team
  val AutoTaskAssignmentScreen = SubScreen("Task Assignment Screen", MainScreens.TasksScreen)

  // The screen showing dependences between tasks.
  val TaskDependencePage = SubScreen("Task Dependence Page", MainScreens.TasksScreen)

  val CreateTaskScreen = SubScreen("Create task screen", MainScreens.TasksScreen)
}

object IdeasSpecificScreens {
  // The screen where we could get ideas from our chat and AI suggestions
  val CreateIdeasScreen = SubScreen("Create Ideas Screen", MainScreens.IdeasScreen)
}

object MeetingsSpecificScreens {
  // Add Meeting Screen
  val AddMeetingScreen = SubScreen("Add Meeting Screen", MainScreens.MeetingsScreen)
  // Sub page to the meetings screen where the user will be able to start the recording
  // in order to make an audio transcript
  val AudioTranscriptScreen = SubScreen("Audio Transcript Screen", MainScreens.MeetingsScreen)
}

object SharedScreens {
  // Page where the user will be able to take pictures related to both tasks and meetings
  val CameraScreen = SharedScreen("Camera Screen")
}

object ProjectSelectionSpecificScreens {
  // Create Project Screen
  val CreateProjectScreen = SubScreen("Create Project Screen", MainScreens.ProjectSelectionScreen)
}

private val titleToScreensMap =
    mapOf<String, Screen>(
        MainScreens.MeetingsScreen.title to MainScreens.MeetingsScreen,
        MainScreens.TasksScreen.title to MainScreens.TasksScreen,
        MainScreens.OverviewProjectScreen.title to MainScreens.OverviewProjectScreen,
        MainScreens.ProfileScreen.title to MainScreens.ProfileScreen,
        MainScreens.IdeasScreen.title to MainScreens.IdeasScreen,
        MainScreens.ProjectSelectionScreen.title to MainScreens.ProjectSelectionScreen,

        // Task specific screens
        TaskSpecificScreens.TasksEditScreen.title to TaskSpecificScreens.TasksEditScreen,
        TaskSpecificScreens.TasksDetailScreen.title to TaskSpecificScreens.TasksDetailScreen,
        TaskSpecificScreens.AutoTaskAssignmentScreen.title to
            TaskSpecificScreens.AutoTaskAssignmentScreen,
        TaskSpecificScreens.TaskDependencePage.title to TaskSpecificScreens.TaskDependencePage,
        TaskSpecificScreens.CreateTaskScreen.title to TaskSpecificScreens.CreateTaskScreen,

        // Ideas Specific Screens
        IdeasSpecificScreens.CreateIdeasScreen.title to IdeasSpecificScreens.CreateIdeasScreen,

        // Meetings specific screens
        SharedScreens.CameraScreen.title to SharedScreens.CameraScreen,
        MeetingsSpecificScreens.AddMeetingScreen.title to MeetingsSpecificScreens.AddMeetingScreen,
        MeetingsSpecificScreens.AudioTranscriptScreen.title to
            MeetingsSpecificScreens.AudioTranscriptScreen,

        // Project selection specific screens
        ProjectSelectionSpecificScreens.CreateProjectScreen.title to
            ProjectSelectionSpecificScreens.CreateProjectScreen)

@Composable
fun NavigationMenu() {
  val navigationController = rememberNavController()

  val navBackStackEntry by navigationController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val currentScreen = remember { mutableStateOf<Screen>(MainScreens.OverviewProjectScreen) }

  LaunchedEffect(currentRoute) {
    val screenRoute = currentRoute?.substringBefore('/') ?: currentRoute
    if (screenRoute != null) {
      currentScreen.value = titleToScreensMap[screenRoute]!!
    } else {
      currentScreen.value = MainScreens.OverviewProjectScreen
    }
  }
  Scaffold(
      containerColor = Color.White,
      bottomBar = {
        BottomBarNavigationComponent(
            navigationController = navigationController,
            currentScreen = currentScreen,
        )
      }) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navigationController,
            startDestination = MainScreens.ProjectSelectionScreen.title) {
              composable(MainScreens.ProjectSelectionScreen.title) {
                ProjectSelectionScreen(navigationController)
              }
              composable(MainScreens.MeetingsScreen.title) {
                MeetingScreen("1234")
              } // TODO : change this after "Create project" is implemented
              composable(MainScreens.ProfileScreen.title) { ProfileScreen() }
              composable(MainScreens.IdeasScreen.title) { IdeasScreen(navigationController) }
              composable(MainScreens.OverviewProjectScreen.title) {
                OverviewProjectsScreen(navigationController)
              }
              composable(MainScreens.TasksScreen.title) { TasksScreen(navigationController) }
              composable("${TaskSpecificScreens.CreateTaskScreen.title}/{projectId}") {
                  backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId")
                val context = LocalContext.current

                projectId?.let { CreateTaskScreen(projectId, navigationController) }
                    ?: run {
                      Toast.makeText(context, "Project ID is null", Toast.LENGTH_SHORT).show()
                    }
              }
              composable(SharedScreens.CameraScreen.title) { Camera(navigationController) }
            }
      }
}

fun navigationFunction(
    navigationController: NavController,
    goBack: Boolean = false,
    destination: Screen?,
    vararg args: String
) {
  if (goBack) {
    navigationController.popBackStack()
  } else {
    if (destination == null) return

    val route =
        if (args.isNotEmpty()) {
          "${destination.title}/${args.joinToString("/")}"
        } else {
          destination.title
        }
    navigationController.navigate(route) {
      launchSingleTop = true
      popUpTo(route) {
        saveState = true
        inclusive = true
      }
    }
  }
}

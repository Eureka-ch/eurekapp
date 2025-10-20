package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CreateTaskScreen
import ch.eureka.eurekapp.screens.IdeasScreen
import ch.eureka.eurekapp.screens.OverviewProjectsScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens.CreateProjectScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.profile.ProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable

// Type-safe navigation routes using Kotlin Serialization
sealed interface Route {
  // Main screens
  @Serializable data object ProjectSelection : Route

  @Serializable data object OverviewProject : Route

  @Serializable data object Profile : Route

  @Serializable data object Meetings : Route

  @Serializable data object Ideas : Route

  @Serializable data object Tasks : Route

  // Task specific screens
  @Serializable data class CreateTask(val projectId: String) : Route

  @Serializable data class TaskDetail(val taskId: String) : Route

  @Serializable data class TaskEdit(val taskId: String) : Route

  @Serializable data object AutoTaskAssignment : Route

  @Serializable data object TaskDependence : Route

  // Ideas specific screens
  @Serializable data object CreateIdeas : Route

  // Meetings specific screens
  @Serializable data object AddMeeting : Route

  @Serializable data object AudioTranscript : Route

  // Project selection specific screens
  @Serializable data object CreateProject : Route

  // Overview project specific screens
  @Serializable data object CreateInvitation : Route

  // Shared screens
  @Serializable data object Camera : Route
}

// Legacy Screen classes for backward compatibility during migration
@Deprecated("Use Route sealed interface instead") abstract class Screen(val title: String)

@Deprecated("Use Route sealed interface instead") class MainScreen(title: String) : Screen(title)

@Deprecated("Use Route sealed interface instead")
class SubScreen(title: String, val parentScreen: MainScreen) : Screen(title)

@Deprecated("Use Route sealed interface instead") class SharedScreen(title: String) : Screen(title)

// Legacy screen object definitions - deprecated in favor of Route sealed interface
@Deprecated("Use Route sealed interface instead")
object MainScreens {
  val ProjectSelectionScreen = MainScreen("General Overview")
  val OverviewProjectScreen = MainScreen("Overview Project Screen")
  val ProfileScreen = MainScreen("Profile Screen")
  val MeetingsScreen = MainScreen("Meetings Screen")
  val IdeasScreen = MainScreen("Ideas Screen")
  val TasksScreen = MainScreen("Tasks Screen")
}

@Deprecated("Use Route sealed interface instead")
object OverviewProjectSpecificScreens {
  val CreateInvitationScreen =
      SubScreen("Create Invitation Screen", MainScreens.OverviewProjectScreen)
}

@Deprecated("Use Route sealed interface instead")
object TaskSpecificScreens {
  val TasksDetailScreen = SubScreen("Task Detail Screen", MainScreens.TasksScreen)
  val TasksEditScreen = SubScreen("Task Edit Screen", MainScreens.TasksScreen)
  val AutoTaskAssignmentScreen = SubScreen("Task Assignment Screen", MainScreens.TasksScreen)
  val TaskDependencePage = SubScreen("Task Dependence Page", MainScreens.TasksScreen)
  val CreateTaskScreen = SubScreen("Create task screen", MainScreens.TasksScreen)
}

@Deprecated("Use Route sealed interface instead")
object IdeasSpecificScreens {
  val CreateIdeasScreen = SubScreen("Create Ideas Screen", MainScreens.IdeasScreen)
}

@Deprecated("Use Route sealed interface instead")
object MeetingsSpecificScreens {
  val AddMeetingScreen = SubScreen("Add Meeting Screen", MainScreens.MeetingsScreen)
  val AudioTranscriptScreen = SubScreen("Audio Transcript Screen", MainScreens.MeetingsScreen)
  val CameraScreen = SubScreen("Camera Screen", MainScreens.MeetingsScreen)
}

@Deprecated("Use Route sealed interface instead")
object SharedScreens {
  val CameraScreen = SharedScreen("Camera Screen")
}

@Deprecated("Use Route sealed interface instead")
object ProjectSelectionSpecificScreens {
  val CreateProjectScreen = SubScreen("Create Project Screen", MainScreens.ProjectSelectionScreen)
}

@Composable
fun NavigationMenu() {
  val navigationController = rememberNavController()
  val projectRepository = FirestoreRepositoriesProvider.projectRepository
  val navBackStackEntry by navigationController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val currentScreen = remember { mutableStateOf<Screen>(MainScreens.OverviewProjectScreen) }
  val auth = Firebase.auth
  val testProjectId = "test-project-id"
  // this is hardcoded for current release
  val testProject =
      Project(
          projectId = testProjectId,
          name = "Test Project",
          description = "This is a test project",
          status = ProjectStatus.OPEN,
          createdBy = auth.currentUser?.uid ?: "unknown",
          memberIds = listOf(auth.currentUser?.uid ?: "unknown"),
      )

  LaunchedEffect(Unit) {
    projectRepository.createProject(
        project = testProject,
        creatorRole = ProjectRole.OWNER,
        creatorId = auth.currentUser?.uid ?: "unknown")
  }

  // Legacy currentScreen tracking for BottomBarNavigationComponent
  // TODO: Refactor BottomBarNavigationComponent to use Route instead of Screen
  LaunchedEffect(currentRoute) {
    val screenRoute = currentRoute?.substringBefore('/') ?: currentRoute
    currentScreen.value =
        when {
          screenRoute?.contains("ProjectSelection") == true -> MainScreens.ProjectSelectionScreen
          screenRoute?.contains("OverviewProject") == true -> MainScreens.OverviewProjectScreen
          screenRoute?.contains("Profile") == true -> MainScreens.ProfileScreen
          screenRoute?.contains("Meetings") == true -> MainScreens.MeetingsScreen
          screenRoute?.contains("Ideas") == true -> MainScreens.IdeasScreen
          screenRoute?.contains("Tasks") == true -> MainScreens.TasksScreen
          else -> MainScreens.OverviewProjectScreen
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
            startDestination = Route.ProjectSelection) {
              // Main screens
              composable<Route.ProjectSelection> { ProjectSelectionScreen(navigationController) }
              composable<Route.Meetings> {
                MeetingScreen("1234")
              } // TODO : change this after "Create project" is implemented
              composable<Route.Profile> { ProfileScreen() }
              composable<Route.Ideas> { IdeasScreen(navigationController) }
              composable<Route.OverviewProject> { OverviewProjectsScreen() }
              composable<Route.Tasks> {
                TasksScreen(
                    onCreateTaskClick = {
                      navigationController.navigate(Route.CreateTask(projectId = testProjectId))
                    })
              }

              // Task screens
              composable<Route.CreateTask> { backStackEntry ->
                val createTaskRoute = backStackEntry.toRoute<Route.CreateTask>()
                CreateTaskScreen(createTaskRoute.projectId, navigationController)
              }

              // Shared screens
              composable<Route.Camera> { Camera(navigationController) }

              // Project screens
              composable<Route.CreateProject> { CreateProjectScreen() }

              // Overview project screens
              composable<Route.CreateInvitation> {
                ch.eureka.eurekapp.screens.subscreens.overview_project_subscreens
                    .CreateInvitationSubscreen(projectId = testProjectId, onInvitationCreate = {})
              }
            }
      }
}

/**
 * Legacy navigation helper function.
 *
 * @deprecated Use navController.navigate(Route.SomeRoute) for forward navigation and
 *   navController.popBackStack() for back navigation instead.
 */
@Deprecated(
    "Use navController.navigate(Route.SomeRoute) or navController.popBackStack() instead",
    ReplaceWith(
        "if (goBack) navigationController.popBackStack() else navigationController.navigate(destination)"))
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

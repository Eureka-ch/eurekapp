package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.IdeasScreen
import ch.eureka.eurekapp.screens.OverviewProjectsScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.subscreens.projects.creation.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.projects.invitation.CreateInvitationSubscreen
import ch.eureka.eurekapp.screens.subscreens.tasks.creation.CreateTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.profile.ProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

// Type-safe navigation routes using Kotlin Serialization
sealed interface Route {
  // Main screens
  @Serializable data object ProjectSelection : Route

  @Serializable data object OverviewProject : Route

  @Serializable data object Profile : Route

  // Tasks section - all task-related screens
  sealed interface TasksSection : Route {
    companion object {
      val routes: Set<KClass<out TasksSection>>
        get() = TasksSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Tasks : TasksSection

    @Serializable data object CreateTask : TasksSection

    @Serializable data class TaskDetail(val taskId: String) : TasksSection

    @Serializable data class TaskEdit(val projectId: String, val taskId: String) : TasksSection

    @Serializable data object AutoTaskAssignment : TasksSection

    @Serializable data object TaskDependence : TasksSection
  }

  // Ideas section - all ideas-related screens
  sealed interface IdeasSection : Route {
    companion object {
      val routes: Set<KClass<out IdeasSection>>
        get() = IdeasSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Ideas : IdeasSection

    @Serializable data object CreateIdeas : IdeasSection
  }

  // Meetings section - all meetings-related screens
  sealed interface MeetingsSection : Route {
    companion object {
      val routes: Set<KClass<out MeetingsSection>>
        get() = MeetingsSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Meetings : MeetingsSection

    @Serializable data object AddMeeting : MeetingsSection

    @Serializable data object AudioTranscript : MeetingsSection
  }

  // Project selection section
  sealed interface ProjectSelectionSection : Route {
    companion object {
      val routes: Set<KClass<out ProjectSelectionSection>>
        get() = ProjectSelectionSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object CreateProject : ProjectSelectionSection
  }

  // Overview project section
  sealed interface OverviewProjectSection : Route {
    companion object {
      val routes: Set<KClass<out OverviewProjectSection>>
        get() = OverviewProjectSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object CreateInvitation : OverviewProjectSection
  }

  // Shared screens (used across multiple sections)
  @Serializable data object Camera : Route
}

@Composable
fun NavigationMenu() {
  val navigationController = rememberNavController()
  val projectRepository = FirestoreRepositoriesProvider.projectRepository
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

  Scaffold(
      containerColor = Color.White,
      bottomBar = { BottomBarNavigationComponent(navigationController = navigationController) }) {
          innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navigationController,
            startDestination = Route.ProjectSelection) {
              // Main screens
              composable<Route.ProjectSelection> { ProjectSelectionScreen(navigationController) }
              composable<Route.Profile> { ProfileScreen() }
              composable<Route.OverviewProject> { OverviewProjectsScreen() }

              // Tasks section
              composable<Route.TasksSection.Tasks> {
                TasksScreen(
                    onCreateTaskClick = {
                      navigationController.navigate(Route.TasksSection.CreateTask)
                    },
                    onTaskClick = { taskId, projectId ->
                      navigationController.navigate(
                          Route.TasksSection.TaskEdit(projectId = projectId, taskId = taskId))
                    })
              }
              composable<Route.TasksSection.CreateTask> { CreateTaskScreen(navigationController) }

              composable<Route.TasksSection.TaskEdit> { backStackEntry ->
                val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.TaskEdit>()
                EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navigationController)
              }

              // Ideas section
              composable<Route.IdeasSection.Ideas> { IdeasScreen(navigationController) }

              // Meetings section
              composable<Route.MeetingsSection.Meetings> {
                MeetingScreen("1234")
              } // TODO : change this after "Create project" is implemented

              // Project selection section
              composable<Route.ProjectSelectionSection.CreateProject> { CreateProjectScreen() }

              // Overview project section
              composable<Route.OverviewProjectSection.CreateInvitation> {
                CreateInvitationSubscreen(projectId = testProjectId, onInvitationCreate = {})
              }

              // Shared screens
              composable<Route.Camera> { Camera(navigationController) }
            }
      }
}

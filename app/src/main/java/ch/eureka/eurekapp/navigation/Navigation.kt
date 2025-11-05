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
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingTranscriptViewScreen
import ch.eureka.eurekapp.screens.subscreens.projects.creation.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.projects.invitation.CreateInvitationSubscreen
import ch.eureka.eurekapp.screens.subscreens.tasks.creation.CreateTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreen
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreen
import ch.eureka.eurekapp.ui.meeting.DateTimeVoteScreen
import ch.eureka.eurekapp.ui.meeting.MeetingDetailActionsConfig
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.profile.ProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

sealed interface Route {
  // Main screens
  @Serializable data object ProjectSelection : Route

  @Serializable data object OverviewProject : Route

  @Serializable data object Profile : Route

  sealed interface TasksSection : Route {
    companion object {
      val routes: Set<KClass<out TasksSection>>
        get() = TasksSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Tasks : TasksSection

    @Serializable data object CreateTask : TasksSection

    @Serializable data class ViewTask(val projectId: String, val taskId: String) : TasksSection

    @Serializable data class EditTask(val projectId: String, val taskId: String) : TasksSection

    @Serializable data object AutoTaskAssignment : TasksSection

    @Serializable data object TaskDependence : TasksSection
  }

  sealed interface IdeasSection : Route {
    companion object {
      val routes: Set<KClass<out IdeasSection>>
        get() = IdeasSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Ideas : IdeasSection

    @Serializable data object CreateIdeas : IdeasSection
  }

  sealed interface MeetingsSection : Route {
    companion object {
      val routes: Set<KClass<out MeetingsSection>>
        get() = MeetingsSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Meetings : MeetingsSection

    @Serializable data class MeetingsOverview(val projectId: String) : MeetingsSection

    @Serializable data class CreateMeeting(val projectId: String) : MeetingsSection

    @Serializable
    data class DateTimeVotes(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class MeetingDetail(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class AudioRecording(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class AudioTranscript(val projectId: String, val meetingId: String) : MeetingsSection
  }

  sealed interface ProjectSelectionSection : Route {
    companion object {
      val routes: Set<KClass<out ProjectSelectionSection>>
        get() = ProjectSelectionSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object CreateProject : ProjectSelectionSection
  }

  sealed interface OverviewProjectSection : Route {
    companion object {
      val routes: Set<KClass<out OverviewProjectSection>>
        get() = OverviewProjectSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object CreateInvitation : OverviewProjectSection
  }

  @Serializable data object Camera : Route
}

@Composable
fun NavigationMenu() {
  val navigationController = rememberNavController()
  val projectRepository = FirestoreRepositoriesProvider.projectRepository
  val meetingRepository = FirestoreRepositoriesProvider.meetingRepository
  val auth = Firebase.auth
  val testProjectId = "test-project-id"
  val testMeetingId = "test-meeting"
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
                          Route.TasksSection.ViewTask(projectId = testProjectId, taskId = taskId))
                    })
              }
              composable<Route.TasksSection.CreateTask> { CreateTaskScreen(navigationController) }

              composable<Route.TasksSection.EditTask> { backStackEntry ->
                val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.EditTask>()
                EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navigationController)
              }

              composable<Route.TasksSection.ViewTask> { backStackEntry ->
                val taskDetailRoute = backStackEntry.toRoute<Route.TasksSection.ViewTask>()
                ViewTaskScreen(
                    taskDetailRoute.projectId, taskDetailRoute.taskId, navigationController)
              }

              // Ideas section
              composable<Route.IdeasSection.Ideas> { IdeasScreen(navigationController) }

              // Meetings section
              composable<Route.MeetingsSection.Meetings> {
                MeetingScreen(
                    projectId = testProjectId,
                    onCreateMeeting = {
                      navigationController.navigate(
                          Route.MeetingsSection.CreateMeeting(testProjectId))
                    },
                    onMeetingClick = { projectId, meetingId ->
                      navigationController.navigate(
                          Route.MeetingsSection.MeetingDetail(
                              projectId = projectId, meetingId = meetingId))
                    },
                    onVoteForDateTimeClick = { projectId, meetingId ->
                      navigationController.navigate(
                          Route.MeetingsSection.DateTimeVotes(
                              projectId = projectId, meetingId = meetingId))
                    })
              }

              composable<Route.MeetingsSection.MeetingDetail> { backStackEntry ->
                val meetingDetailRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.MeetingDetail>()
                MeetingDetailScreen(
                    projectId = meetingDetailRoute.projectId,
                    meetingId = meetingDetailRoute.meetingId,
                    actionsConfig =
                        MeetingDetailActionsConfig(
                            onNavigateBack = { navigationController.popBackStack() },
                            onRecordMeeting = { projectId, meetingId ->
                              navigationController.navigate(
                                  Route.MeetingsSection.AudioRecording(
                                      projectId = projectId, meetingId = meetingId))
                            },
                            onViewTranscript = { projectId, meetingId ->
                              navigationController.navigate(
                                  Route.MeetingsSection.AudioTranscript(
                                      projectId = projectId, meetingId = meetingId))
                            }),
                )
              }

              composable<Route.MeetingsSection.CreateMeeting> { backStackEntry ->
                val createMeetingCreationRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.CreateMeeting>()
                CreateMeetingScreen(
                    createMeetingCreationRoute.projectId,
                    { navigationController.navigate(Route.MeetingsSection.Meetings) })
              }

              composable<Route.MeetingsSection.DateTimeVotes> { backStackEntry ->
                val dateTimeVotesRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.DateTimeVotes>()
                DateTimeVoteScreen(
                    projectId = dateTimeVotesRoute.projectId,
                    meetingId = dateTimeVotesRoute.meetingId,
                    onDone = { navigationController.navigate(Route.MeetingsSection.Meetings) },
                )
              }

              composable<Route.MeetingsSection.AudioRecording> { backStackEntry ->
                val audioRecordingRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.AudioRecording>()
                MeetingAudioRecordingScreen(
                    projectId = audioRecordingRoute.projectId,
                    meetingId = audioRecordingRoute.meetingId,
                    onNavigateToTranscript = { projectId, meetingId ->
                      navigationController.navigate(
                          Route.MeetingsSection.AudioTranscript(
                              projectId = projectId, meetingId = meetingId))
                    })
              }

              composable<Route.MeetingsSection.AudioTranscript> { backStackEntry ->
                val audioTranscriptRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.AudioTranscript>()
                MeetingTranscriptViewScreen(
                    projectId = audioTranscriptRoute.projectId,
                    meetingId = audioTranscriptRoute.meetingId,
                    onNavigateBack = { navigationController.popBackStack() })
              }

              composable<Route.ProjectSelectionSection.CreateProject> { CreateProjectScreen() }

              composable<Route.OverviewProjectSection.CreateInvitation> {
                CreateInvitationSubscreen(projectId = testProjectId, onInvitationCreate = {})
              }

              composable<Route.Camera> { Camera(navigationController) }
            }
      }
}

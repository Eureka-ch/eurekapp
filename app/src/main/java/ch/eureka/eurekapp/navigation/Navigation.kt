/* This code was partially written by Claude Sonnet 4.5, Gemini, chatGPT-5 and Grok. */
package ch.eureka.eurekapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.model.data.mcp.FirebaseMcpTokenRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.model.notifications.NotificationType
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.FilesManagementScreen
import ch.eureka.eurekapp.screens.HomeOverviewActions
import ch.eureka.eurekapp.screens.HomeOverviewScreen
import ch.eureka.eurekapp.screens.OverviewProjectScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingTranscriptViewScreen
import ch.eureka.eurekapp.screens.subscreens.projects.creation.CreateProjectScreen
import ch.eureka.eurekapp.screens.subscreens.projects.invitation.CreateInvitationSubscreen
import ch.eureka.eurekapp.screens.subscreens.projects.members.ProjectMembersScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.AutoAssignResultScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.creation.CreateTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreen
import ch.eureka.eurekapp.ui.activity.ActivityDetailScreen
import ch.eureka.eurekapp.ui.activity.ActivityFeedScreen
import ch.eureka.eurekapp.ui.authentication.TokenEntryScreen
import ch.eureka.eurekapp.ui.conversation.ConversationDetailScreen
import ch.eureka.eurekapp.ui.conversation.ConversationListScreen
import ch.eureka.eurekapp.ui.conversation.CreateConversationScreen
import ch.eureka.eurekapp.ui.ideas.IdeasScreen
import ch.eureka.eurekapp.ui.map.MeetingLocationSelectionScreen
import ch.eureka.eurekapp.ui.mcp.McpTokenScreen
import ch.eureka.eurekapp.ui.mcp.McpTokenViewModel
import ch.eureka.eurekapp.ui.meeting.CreateDateTimeFormatProposalForMeetingScreen
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreen
import ch.eureka.eurekapp.ui.meeting.CreateMeetingViewModel
import ch.eureka.eurekapp.ui.meeting.MeetingDetailActionsConfig
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreen
import ch.eureka.eurekapp.ui.meeting.MeetingNavigationScreen
import ch.eureka.eurekapp.ui.meeting.MeetingProposalVoteScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreen
import ch.eureka.eurekapp.ui.meeting.MeetingScreenConfig
import ch.eureka.eurekapp.ui.notes.SelfNotesScreen
import ch.eureka.eurekapp.ui.notifications.NotificationPreferencesScreen
import ch.eureka.eurekapp.ui.profile.ProfileScreen
import ch.eureka.eurekapp.ui.templates.CreateTemplateScreen
import ch.eureka.eurekapp.ui.templates.CreateTemplateViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable

/**
 * The duration in milliseconds between two ping of the app to firestore for last active tracking.
 */
const val HEARTBEAT_DURATION = 180000L // 3 minutes

sealed interface Route {
  // Main screens
  @Serializable data object ProjectSelection : Route

  @Serializable data object HomeOverview : Route

  @Serializable data class OverviewProject(val projectId: String) : Route

  @Serializable data object Profile : Route

  @Serializable data object McpTokens : Route

  @Serializable data object SelfNotes : Route

  @Serializable data object ActivityFeed : Route

  sealed interface ActivitySection : Route {
    @Serializable data class ActivityDetail(val activityId: String) : ActivitySection
  }

  @Serializable data object NotificationPreferences : Route

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

    @Serializable data object FilesManagement : TasksSection

    @Serializable
    data class TaskDependence(val projectId: String, val taskId: String) : TasksSection

    @Serializable data class CreateTemplate(val projectId: String) : TasksSection
  }

  sealed interface MeetingsSection : Route {
    companion object {
      val routes: Set<KClass<out MeetingsSection>>
        get() = MeetingsSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Meetings : MeetingsSection

    @Serializable data object CreateMeeting : MeetingsSection

    @Serializable
    data class MeetingProposalVotes(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class CreateDateTimeFormatMeetingProposalForMeeting(
        val projectId: String,
        val meetingId: String
    ) : MeetingsSection

    @Serializable
    data class MeetingDetail(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class AudioRecording(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class AudioTranscript(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable
    data class MeetingNavigation(val projectId: String, val meetingId: String) : MeetingsSection

    @Serializable data object MeetingLocationSelection : MeetingsSection
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

    @Serializable data class CreateInvitation(val projectId: String) : OverviewProjectSection

    @Serializable data object TokenEntry : OverviewProjectSection

    @Serializable data class ProjectMembers(val projectId: String) : OverviewProjectSection
  }

  @Serializable data object Camera : Route

  sealed interface ConversationsSection : Route {
    companion object {
      val routes: Set<KClass<out ConversationsSection>>
        get() = ConversationsSection::class.sealedSubclasses.toSet()
    }

    @Serializable data object Conversations : ConversationsSection

    @Serializable data class ConversationDetail(val conversationId: String) : ConversationsSection

    @Serializable data class CreateConversation(val projectId: String) : ConversationsSection
  }

  sealed interface IdeasSection : Route {
    companion object {
      val routes: Set<KClass<out IdeasSection>>
        get() = IdeasSection::class.sealedSubclasses.toSet()
    }

    @Serializable data class Ideas(val projectId: String? = null) : IdeasSection
  }
}

@Composable
fun NavigationMenu(
    notificationType: String? = null,
    notificationId: String? = null,
    notificationProjectId: String? = null,
) {
  val navigationController = rememberNavController()
  val testProjectId = "test-project-id"

  HandleNotificationNavigation(
      notificationType = notificationType,
      notificationId = notificationId,
      notificationProjectId = notificationProjectId,
      navigationController = navigationController)

  RepositoriesProvider.projectRepository
  val auth = Firebase.auth
  val userRepository = RepositoriesProvider.userRepository
  val currentUser = auth.currentUser
  requireNotNull(currentUser)

  UserHeartbeatEffect(userRepository, currentUser)

  Scaffold(
      containerColor = Color.White,
      bottomBar = { BottomBarNavigationComponent(navigationController = navigationController) }) {
          innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navigationController,
            startDestination = Route.HomeOverview) {
              composable<Route.HomeOverview> {
                HomeOverviewScreen(
                    actions =
                        HomeOverviewActions(
                            onOpenProjects = {
                              navigationController.navigate(Route.ProjectSelection)
                            },
                            onOpenTasks = {
                              navigationController.navigate(Route.TasksSection.Tasks)
                            },
                            onOpenMeetings = {
                              navigationController.navigate(Route.MeetingsSection.Meetings)
                            },
                            onTaskSelected = { projectId, taskId ->
                              navigationController.navigate(
                                  Route.TasksSection.ViewTask(
                                      projectId = projectId, taskId = taskId))
                            },
                            onMeetingSelected = { projectId, meetingId ->
                              navigationController.navigate(
                                  Route.MeetingsSection.MeetingDetail(
                                      projectId = projectId, meetingId = meetingId))
                            },
                            onProjectSelected = { projectId ->
                              navigationController.navigate(
                                  Route.OverviewProject(projectId = projectId))
                            }))
              }
              // Main screens
              composable<Route.ProjectSelection> {
                ProjectSelectionScreen(
                    onCreateProjectRequest = {
                      navigationController.navigate(Route.ProjectSelectionSection.CreateProject)
                    },
                    onInputTokenRequest = {
                      navigationController.navigate(Route.OverviewProjectSection.TokenEntry)
                    },
                    onGenerateInviteRequest = { projectId ->
                      navigationController.navigate(
                          Route.OverviewProjectSection.CreateInvitation(projectId = projectId))
                    },
                    onSeeProjectMembers = { projectId ->
                      navigationController.navigate(
                          Route.OverviewProjectSection.ProjectMembers(projectId = projectId))
                    })
              }
              composable<Route.OverviewProjectSection.TokenEntry> {
                TokenEntryScreen(
                    onTokenValidated = { navigationController.navigate(Route.ProjectSelection) },
                    onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.OverviewProjectSection.ProjectMembers> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.OverviewProjectSection.ProjectMembers>()
                ProjectMembersScreen(
                    projectId = route.projectId,
                    onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.Profile> {
                ProfileScreen(
                    onNavigateToActivityFeed = {
                      navigationController.navigate(Route.ActivityFeed)
                    },
                    onNavigateToMcpTokens = { navigationController.navigate(Route.McpTokens) },
                    onNavigateToPreferences = {
                      navigationController.navigate(Route.NotificationPreferences)
                    })
              }
              composable<Route.McpTokens> {
                val viewModel = McpTokenViewModel(FirebaseMcpTokenRepository())
                McpTokenScreen(
                    viewModel = viewModel, onNavigateBack = { navigationController.popBackStack() })
              }
              composable<Route.NotificationPreferences> {
                NotificationPreferencesScreen(
                    onFinishedSettingNotifications = { navigationController.popBackStack() })
              }
              composable<Route.SelfNotes> {
                SelfNotesScreen(onNavigateBack = { navigationController.popBackStack() })
              }
              composable<Route.ActivityFeed> {
                ActivityFeedScreen(
                    onActivityClick = { activityId, _ ->
                      navigationController.navigate(
                          Route.ActivitySection.ActivityDetail(activityId = activityId))
                    })
              }
              composable<Route.ActivitySection.ActivityDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.ActivitySection.ActivityDetail>()
                ActivityDetailScreen(
                    activityId = route.activityId,
                    onNavigateBack = { navigationController.popBackStack() },
                    onNavigateToEntity = { entityType, entityId, projectId ->
                      when (entityType) {
                        EntityType.MEETING -> {
                          navigationController.navigate(
                              Route.MeetingsSection.MeetingDetail(projectId, entityId))
                        }
                        EntityType.TASK -> {
                          navigationController.navigate(
                              Route.TasksSection.ViewTask(projectId, entityId))
                        }
                        EntityType.MESSAGE -> {
                          navigationController.navigate(Route.ConversationsSection.Conversations)
                        }
                        EntityType.PROJECT -> {
                          navigationController.navigate(Route.ProjectSelection)
                        }
                        else -> {
                          // FILE and MEMBER types have no detail screen
                        }
                      }
                    })
              }
              composable<Route.IdeasSection.Ideas> {
                IdeasScreen(onNavigateBack = { navigationController.popBackStack() })
              }
              composable<Route.OverviewProject> { backStackEntry ->
                val overviewProjectScreenRoute = backStackEntry.toRoute<Route.OverviewProject>()
                OverviewProjectScreen(projectId = overviewProjectScreenRoute.projectId)
              }

              // Tasks section
              composable<Route.TasksSection.Tasks> {
                TasksScreen(
                    onCreateTaskClick = {
                      navigationController.navigate(Route.TasksSection.CreateTask)
                    },
                    onAutoAssignClick = {
                      navigationController.navigate(Route.TasksSection.AutoTaskAssignment)
                    },
                    onTaskClick = { taskId, projectId ->
                      navigationController.navigate(
                          Route.TasksSection.ViewTask(projectId = projectId, taskId = taskId))
                    },
                    onFilesManagementClick = {
                      navigationController.navigate(Route.TasksSection.FilesManagement)
                    })
              }
              composable<Route.TasksSection.CreateTask> { CreateTaskScreen(navigationController) }
              composable<Route.TasksSection.AutoTaskAssignment> {
                AutoAssignResultScreen(navigationController)
              }

              composable<Route.TasksSection.EditTask> { backStackEntry ->
                val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.EditTask>()
                EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navigationController)
              }

              composable<Route.TasksSection.ViewTask> { backStackEntry ->
                val taskDetailRoute = backStackEntry.toRoute<Route.TasksSection.ViewTask>()
                ViewTaskScreen(
                    taskDetailRoute.projectId, taskDetailRoute.taskId, navigationController)
              }
              composable<Route.TasksSection.FilesManagement> {
                FilesManagementScreen(onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.TasksSection.TaskDependence> { backStackEntry ->
                val dependenceRoute = backStackEntry.toRoute<Route.TasksSection.TaskDependence>()
                TaskDependenciesScreen(
                    projectId = dependenceRoute.projectId,
                    taskId = dependenceRoute.taskId,
                    navigationController = navigationController)
              }

              composable<Route.TasksSection.CreateTemplate> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.TasksSection.CreateTemplate>()
                val templateViewModel =
                    CreateTemplateViewModel(
                        repository = RepositoriesProvider.taskTemplateRepository,
                        initialProjectId = route.projectId)
                CreateTemplateScreen(
                    onNavigateBack = { navigationController.popBackStack() },
                    onTemplateCreated = { templateId ->
                      navigationController.previousBackStackEntry
                          ?.savedStateHandle
                          ?.set("createdTemplateId", templateId)
                      navigationController.popBackStack()
                    },
                    viewModel = templateViewModel)
              }

              // Meetings section
              composable<Route.MeetingsSection.Meetings> {
                MeetingScreen(
                    config =
                        MeetingScreenConfig(
                            onCreateMeeting = { isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(Route.MeetingsSection.CreateMeeting)
                              }
                            },
                            onMeetingClick = { projectId, meetingId ->
                              navigationController.navigate(
                                  Route.MeetingsSection.MeetingDetail(
                                      projectId = projectId, meetingId = meetingId))
                            },
                            onVoteForMeetingProposalClick = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.MeetingProposalVotes(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            },
                            onNavigateToMeeting = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.MeetingNavigation(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            },
                            onViewTranscript = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.AudioTranscript(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            },
                            onRecord = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.AudioRecording(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            }))
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
                            onRecordMeeting = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.AudioRecording(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            },
                            onViewTranscript = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.AudioTranscript(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            },
                            onNavigateToMeeting = { isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.MeetingNavigation(
                                        projectId = meetingDetailRoute.projectId,
                                        meetingId = meetingDetailRoute.meetingId))
                              }
                            },
                            onVoteForMeetingProposalClick = { projectId, meetingId, isConnected ->
                              navigateIfConditionSatisfied(isConnected) {
                                navigationController.navigate(
                                    Route.MeetingsSection.MeetingProposalVotes(
                                        projectId = projectId, meetingId = meetingId))
                              }
                            }),
                )
              }

              // Project selection section
              composable<Route.ProjectSelectionSection.CreateProject> {
                CreateProjectScreen(
                    onProjectCreated = { navigationController.navigate(Route.ProjectSelection) },
                    onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.MeetingsSection.CreateMeeting> { backStackEntry ->
                val viewModel = viewModel<CreateMeetingViewModel>()

                val selectedLocation: Location? =
                    backStackEntry.savedStateHandle["selected_location"]

                if (selectedLocation != null) {
                  viewModel.setLocation(selectedLocation)
                  viewModel.setLocationQuery(selectedLocation.name)
                  backStackEntry.savedStateHandle.remove<Location>("selected_location")
                }

                CreateMeetingScreen(
                    onDone = { navigationController.navigate(Route.MeetingsSection.Meetings) },
                    createMeetingViewModel = viewModel,
                    onPickLocationOnMap = {
                      navigationController.navigate(Route.MeetingsSection.MeetingLocationSelection)
                    },
                    onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.MeetingsSection.MeetingLocationSelection> {
                MeetingLocationSelectionScreen(
                    onLocationSelected = { location ->
                      navigationController.previousBackStackEntry
                          ?.savedStateHandle
                          ?.set("selected_location", location)

                      navigationController.popBackStack()
                    },
                    onBack = { navigationController.popBackStack() })
              }

              composable<Route.MeetingsSection.MeetingProposalVotes> { backStackEntry ->
                val meetingProposalVotesRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.MeetingProposalVotes>()
                MeetingProposalVoteScreen(
                    projectId = meetingProposalVotesRoute.projectId,
                    meetingId = meetingProposalVotesRoute.meetingId,
                    onDone = { navigationController.navigate(Route.MeetingsSection.Meetings) },
                    onBackClick = { navigationController.popBackStack() },
                    onCreateDateTimeFormatProposalForMeeting = {
                      navigationController.navigate(
                          Route.MeetingsSection.CreateDateTimeFormatMeetingProposalForMeeting(
                              projectId = meetingProposalVotesRoute.projectId,
                              meetingId = meetingProposalVotesRoute.meetingId))
                    },
                )
              }

              composable<Route.MeetingsSection.CreateDateTimeFormatMeetingProposalForMeeting> {
                  backStackEntry ->
                val createDateTimeFormatMeetingProposalForMeetingVotesRoute =
                    backStackEntry.toRoute<
                        Route.MeetingsSection.CreateDateTimeFormatMeetingProposalForMeeting>()
                CreateDateTimeFormatProposalForMeetingScreen(
                    projectId = createDateTimeFormatMeetingProposalForMeetingVotesRoute.projectId,
                    meetingId = createDateTimeFormatMeetingProposalForMeetingVotesRoute.meetingId,
                    onDone = {
                      navigationController.navigate(
                          Route.MeetingsSection.MeetingProposalVotes(
                              projectId =
                                  createDateTimeFormatMeetingProposalForMeetingVotesRoute.projectId,
                              meetingId =
                                  createDateTimeFormatMeetingProposalForMeetingVotesRoute
                                      .meetingId))
                    },
                )
              }

              composable<Route.MeetingsSection.MeetingNavigation> { backStackEntry ->
                val meetingNavigationRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.MeetingNavigation>()
                MeetingNavigationScreen(
                    projectId = meetingNavigationRoute.projectId,
                    meetingId = meetingNavigationRoute.meetingId,
                    onNavigateBack = { navigationController.popBackStack() })
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
                    },
                    onBackClick = { navigationController.popBackStack() })
              }

              composable<Route.MeetingsSection.AudioTranscript> { backStackEntry ->
                val audioTranscriptRoute =
                    backStackEntry.toRoute<Route.MeetingsSection.AudioTranscript>()
                MeetingTranscriptViewScreen(
                    projectId = audioTranscriptRoute.projectId,
                    meetingId = audioTranscriptRoute.meetingId,
                    onNavigateBack = { navigationController.popBackStack() })
              }

              composable<Route.OverviewProjectSection.CreateInvitation> { backStackEntry ->
                val createInvitationRoute =
                    backStackEntry.toRoute<Route.OverviewProjectSection.CreateInvitation>()
                CreateInvitationSubscreen(
                    projectId = createInvitationRoute.projectId, onInvitationCreate = {})
              }

              composable<Route.Camera> {
                Camera(
                    onBackClick = { navigationController.popBackStack() },
                    onPhotoSaved = { uri ->
                      navigationController.previousBackStackEntry
                          ?.savedStateHandle
                          ?.set("photoUri", uri)
                      navigationController.popBackStack()
                    })
              }

              // Conversations section
              composable<Route.ConversationsSection.Conversations> {
                ConversationListScreen(
                    onConversationClick = { conversationId ->
                      // If clicking on "To Self" conversation, navigate to SelfNotesScreen
                      if (conversationId ==
                          ch.eureka.eurekapp.ui.conversation.TO_SELF_CONVERSATION_ID) {
                        navigationController.navigate(Route.SelfNotes)
                      } else {
                        navigationController.navigate(
                            Route.ConversationsSection.ConversationDetail(conversationId))
                      }
                    },
                    onCreateConversation = {
                      navigationController.navigate(
                          Route.ConversationsSection.CreateConversation(projectId = testProjectId))
                    })
              }

              composable<Route.ConversationsSection.ConversationDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.ConversationsSection.ConversationDetail>()
                ConversationDetailScreen(
                    conversationId = route.conversationId,
                    onNavigateBack = { navigationController.popBackStack() })
              }

              composable<Route.ConversationsSection.CreateConversation> {
                CreateConversationScreen(
                    onNavigateToConversation = { conversationId ->
                      navigationController.navigate(
                          Route.ConversationsSection.ConversationDetail(conversationId))
                    })
              }
            }
      }
}

/**
 * Handles navigation events triggered by notifications.
 *
 * @param notificationType The type of the notification (e.g., meeting, message).
 * @param notificationId The ID associated with the notification (e.g., meeting ID).
 * @param notificationProjectId The project ID associated with the notification.
 * @param navigationController The [NavHostController] used to perform navigation.
 */
@Composable
private fun HandleNotificationNavigation(
    notificationType: String?,
    notificationId: String?,
    notificationProjectId: String?,
    navigationController: NavHostController
) {
  // Handle the case where the app has been started by a notification
  LaunchedEffect(notificationType, notificationId, notificationProjectId) {
    if (notificationType != null) {
      when (notificationType) {
        NotificationType.MEETING_NOTIFICATION.backendTypeString -> {
          if (notificationId != null && notificationProjectId != null) {
            navigationController.navigate(
                Route.MeetingsSection.MeetingDetail(
                    projectId = notificationProjectId, meetingId = notificationId))
          }
        }
        NotificationType.MESSAGE_NOTIFICATION.backendTypeString -> {
          Log.d("Navigation", "Not yet implemented")
        }
        else -> {
          // Do nothing
        }
      }
    }
  }
}

/**
 * Initiates a periodic heartbeat to update the user's last active timestamp in the repository.
 *
 * @param userRepository The repository used to update the user's status.
 * @param currentUser The currently authenticated [FirebaseUser].
 */
@Composable
private fun UserHeartbeatEffect(userRepository: UserRepository, currentUser: FirebaseUser) {
  LaunchedEffect(Unit) {
    while (isActive) {
      try {
        userRepository.updateLastActive(currentUser.uid)
      } catch (c: CancellationException) {
        throw c
      } catch (e: Exception) {
        Log.e("Navigation", "Failed to ping Firestore (heartbeat)", e)
      }
      // Wait before the next ping
      delay(HEARTBEAT_DURATION)
    }
  }
}

private inline fun navigateIfConditionSatisfied(condition: Boolean, navigate: () -> Unit) {
  if (condition) {
    navigate()
  }
}

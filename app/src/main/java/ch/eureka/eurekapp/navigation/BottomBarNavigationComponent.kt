package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors

/*
Co-author: GPT-5 Codex
*/

object BottomBarNavigationTestTags {
  const val TASKS_SCREEN_BUTTON = "TasksScreenButton"
  const val CONVERSATIONS_SCREEN_BUTTON = "ConversationsScreenButton"
  const val PROJECTS_SCREEN_BUTTON = "ProjectsScreenButton"
  const val OVERVIEW_SCREEN_BUTTON = "OverviewScreenButton"
  const val MEETINGS_SCREEN_BUTTON = "MeetingsScreenButton"
  const val NOTES_SCREEN_BUTTON = "NotesScreenButton"
  const val PROFILE_SCREEN_BUTTON = "ProfileScreenButton"
}

@Composable
fun BottomBarNavigationComponent(navigationController: NavController) {
  val navBackStackEntry by navigationController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  val isHomeScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          currentDestination?.hierarchy?.any { it.hasRoute(Route.HomeOverview::class) } ?: false
        }
      }

  fun navigateToTab(route: Any) {
    navigationController.navigate(route) {
      popUpTo(navigationController.graph.startDestinationId) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToHome() {
    navigationController.navigate(Route.HomeOverview) {
      popUpTo(navigationController.graph.startDestinationId) { saveState = false }
      launchSingleTop = true
      restoreState = false
    }
  }

  val isTasksPressed by
      remember(currentDestination) {
        derivedStateOf {
          Route.TasksSection.routes.any { routeClass ->
            currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
          }
        }
      }

  // Check if conversations section is currently active
  val isConversationsScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          Route.ConversationsSection.routes.any { routeClass ->
            currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
          }
        }
      }

  val isProfileScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          currentDestination?.hierarchy?.any { it.hasRoute(Route.Profile::class) } == true
        }
      }

  val isProjectsScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          currentDestination?.hierarchy?.any { it.hasRoute(Route.ProjectSelection::class) } ?: false
        }
      }

  val isMeetingScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          Route.MeetingsSection.routes.any { routeClass ->
            currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
          }
        }
      }

  val isNotesScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          currentDestination?.hierarchy?.any { it.hasRoute(Route.SelfNotes::class) } == true
        }
      }

  BottomAppBar(
      containerColor = EColors.light.surface,
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 0.dp, vertical = 0.dp)
              .windowInsetsPadding(WindowInsets.navigationBars)
              .clip(RoundedCornerShape(25.dp)),
      tonalElevation = 8.dp,
      actions = {
        Row() {
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON),
              "Tasks",
              onClick = { navigateToTab(Route.TasksSection.Tasks) },
              iconVector = Icons.Outlined.AssignmentTurnedIn,
              pressedIconVector = Icons.Filled.AssignmentTurnedIn,
              isPressed = isTasksPressed)
          // Conversations tab - between Tasks and Project (left of Home)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f)
                      .testTag(BottomBarNavigationTestTags.CONVERSATIONS_SCREEN_BUTTON),
              "Chats",
              onClick = { navigateToTab(Route.ConversationsSection.Conversations) },
              iconVector = Icons.AutoMirrored.Outlined.Chat,
              pressedIconVector = Icons.AutoMirrored.Filled.Chat,
              isPressed = isConversationsScreenPressed)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON),
              "Project",
              onClick = { navigateToTab(Route.ProjectSelection) },
              iconVector = Icons.Outlined.Folder,
              pressedIconVector = Icons.Filled.Folder,
              isPressed = isProjectsScreenPressed)
          Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically) {
                Box() {
                  HomeIconButton(
                      modifier =
                          Modifier.testTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON),
                      isPressed = isHomeScreenPressed,
                      onClick = { navigateToHome() })
                }
              }
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON),
              "Meetings",
              onClick = { navigateToTab(Route.MeetingsSection.Meetings) },
              iconVector = Icons.Default.CalendarToday,
              pressedIconVector = Icons.Filled.CalendarToday,
              isPressed = isMeetingScreenPressed)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON),
              "Notes",
              onClick = { navigateToTab(Route.SelfNotes) },
              iconVector = Icons.Outlined.Edit,
              pressedIconVector = Icons.Filled.Edit,
              isPressed = isNotesScreenPressed)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON),
              "Profile",
              onClick = { navigateToTab(Route.Profile) },
              iconVector = Icons.Outlined.AccountCircle,
              pressedIconVector = Icons.Filled.AccountCircle,
              isPressed = isProfileScreenPressed)
        }
      })
}

@Composable
fun CustomIconButtonComposable(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    iconVector: ImageVector,
    pressedIconVector: ImageVector,
    isPressed: Boolean
) {
  IconButton(onClick = onClick, modifier = modifier.padding(0.dp)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Icon(
              if (isPressed) pressedIconVector else iconVector,
              contentDescription = null,
              tint = if (isPressed) EColors.light.primary else EColors.light.onSurfaceVariant)
          Text(title, style = TextStyle(fontSize = 10.sp))
        }
  }
}

@Composable
fun HomeIconButton(modifier: Modifier = Modifier, isPressed: Boolean, onClick: () -> Unit) {
  IconButton(
      modifier =
          modifier
              .padding(0.dp)
              .background(
                  if (isPressed) EColors.light.primary else EColors.light.onSurfaceVariant,
                  shape = CircleShape),
      onClick = onClick) {
        Icon(
            Icons.Outlined.Home,
            contentDescription = null,
            tint = if (isPressed) Color.White else EColors.light.surface)
      }
}

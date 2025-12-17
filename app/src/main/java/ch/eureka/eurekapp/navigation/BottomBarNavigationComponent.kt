package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.ui.draw.shadow
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
  const val IDEAS_SCREEN_BUTTON = "IdeasScreenButton"
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

  val isIdeasScreenPressed by
      remember(currentDestination) {
        derivedStateOf {
          Route.IdeasSection.routes.any { routeClass ->
            currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
          }
        }
      }

  BottomAppBar(
      containerColor = Color.Transparent,
      modifier =
          Modifier.fillMaxWidth()
              .windowInsetsPadding(WindowInsets.navigationBars)
              .padding(horizontal = 12.dp, vertical = 0.dp),
      tonalElevation = 0.dp,
      actions = {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = CircleShape)
                    .background(color = Color.White.copy(alpha = 0.9f), shape = CircleShape)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              CustomIconButtonComposable(
                  modifier =
                      Modifier.weight(1f).testTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON),
                  "Tasks",
                  onClick = { navigateToTab(Route.TasksSection.Tasks) },
                  iconVector = Icons.Outlined.AssignmentTurnedIn,
                  pressedIconVector = Icons.Filled.AssignmentTurnedIn,
                  isPressed = isTasksPressed)
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
                      Modifier.weight(1f)
                          .testTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON),
                  "Project",
                  onClick = { navigateToTab(Route.ProjectSelection) },
                  iconVector = Icons.Outlined.Folder,
                  pressedIconVector = Icons.Filled.Folder,
                  isPressed = isProjectsScreenPressed)
              Box(
                  modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                  contentAlignment = Alignment.Center) {
                    HomeIconButton(
                        modifier =
                            Modifier.offset(y = (-14).dp)
                                .testTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON),
                        isPressed = isHomeScreenPressed,
                        onClick = { navigateToHome() })
                  }
              CustomIconButtonComposable(
                  modifier =
                      Modifier.weight(1f)
                          .testTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON),
                  "Meetings",
                  onClick = { navigateToTab(Route.MeetingsSection.Meetings) },
                  iconVector = Icons.Default.CalendarToday,
                  pressedIconVector = Icons.Filled.CalendarToday,
                  isPressed = isMeetingScreenPressed)
              CustomIconButtonComposable(
                  modifier =
                      Modifier.weight(1f).testTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON),
                  "Ideas",
                  onClick = { navigateToTab(Route.IdeasSection.Ideas()) },
                  iconVector = Icons.Outlined.Lightbulb,
                  pressedIconVector = Icons.Filled.Lightbulb,
                  isPressed = isIdeasScreenPressed)
              CustomIconButtonComposable(
                  modifier =
                      Modifier.weight(1f)
                          .testTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON),
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
  IconButton(onClick = onClick, modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(0.dp)) {
          Icon(
              if (isPressed) pressedIconVector else iconVector,
              contentDescription = null,
              tint = if (isPressed) EColors.light.primary else EColors.light.onSurfaceVariant,
              modifier = Modifier.size(24.dp))
          Text(
              title,
              style = TextStyle(fontSize = 10.sp),
              color = if (isPressed) EColors.light.primary else EColors.light.onSurfaceVariant)
        }
  }
}

@Composable
fun HomeIconButton(modifier: Modifier = Modifier, isPressed: Boolean, onClick: () -> Unit) {
  Box(
      modifier =
          modifier
              .size(56.dp)
              .shadow(elevation = 6.dp, shape = CircleShape)
              .background(color = Color(0xFFE53935), shape = CircleShape),
      contentAlignment = Alignment.Center) {
        IconButton(modifier = Modifier.size(56.dp), onClick = onClick) {
          Icon(
              Icons.Filled.Home,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(28.dp))
        }
      }
}

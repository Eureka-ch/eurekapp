package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors

object BottomBarNavigationTestTags {
  const val TASKS_SCREEN_BUTTON = "TasksScreenButton"
  const val IDEAS_SCREEN_BUTTON = "IdeasScreenButton"
  const val OVERVIEW_SCREEN_BUTTON = "OverviewScreenButton"
  const val MEETINGS_SCREEN_BUTTON = "MeetingsScreenButton"
  const val PROFILE_SCREEN_BUTTON = "ProfileScreenButton"
}

@Composable
fun BottomBarNavigationComponent(
    navigationController: NavController,
    currentScreen: MutableState<Screen>
) {
  val configuration = LocalConfiguration.current
  val screenTotalHeight = configuration.screenHeightDp
  val isTasksPressed by
      remember(currentScreen) {
        derivedStateOf {
          (currentScreen.value == MainScreens.TasksScreen ||
              (currentScreen.value is SubScreen &&
                  (currentScreen.value as SubScreen).parentScreen == MainScreens.TasksScreen))
        }
      }

  val isProfileScreenPressed by
      remember(currentScreen) {
        derivedStateOf {
          (currentScreen.value == MainScreens.ProfileScreen ||
              (currentScreen.value is SubScreen &&
                  (currentScreen.value as SubScreen).parentScreen == MainScreens.ProfileScreen))
        }
      }

  /**
   * val isOverviewProjectScreenPressed by remember(currentScreen) { derivedStateOf {
   * (currentScreen.value == MainScreens.OverviewProjectScreen || (currentScreen.value is SubScreen
   * && (currentScreen.value as SubScreen).parentScreen == MainScreens.OverviewProjectScreen)) } }
   */
  val isIdeasScreenPressed by
      remember(currentScreen) {
        derivedStateOf {
          (currentScreen.value == MainScreens.IdeasScreen ||
              (currentScreen.value is SubScreen &&
                  (currentScreen.value as SubScreen).parentScreen == MainScreens.IdeasScreen))
        }
      }

  /**
   * val isProjectSelectionScreenPressed by remember(currentScreen) { derivedStateOf {
   * (currentScreen.value == MainScreens.ProjectSelectionScreen || (currentScreen.value is SubScreen
   * && (currentScreen.value as SubScreen).parentScreen == MainScreens.ProjectSelectionScreen)) } }
   */
  val isMeetingScreenPressed by remember {
    derivedStateOf {
      (currentScreen.value == MainScreens.MeetingsScreen ||
          (currentScreen.value is SubScreen &&
              (currentScreen.value as SubScreen).parentScreen == MainScreens.MeetingsScreen))
    }
  }

  BottomAppBar(
      containerColor = EColors.light.surface,
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 15.dp, vertical = 10.dp)
              .height((screenTotalHeight * 0.08f).dp)
              .clip(RoundedCornerShape(25.dp)),
      tonalElevation = 8.dp,
      actions = {
        Row() {
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON),
              "Tasks",
              onClick = { navigationController.navigate(Route.Tasks) },
              iconVector = Icons.Outlined.AssignmentTurnedIn,
              pressedIconVector = Icons.Filled.AssignmentTurnedIn,
              isPressed = isTasksPressed)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON),
              "Ideas",
              onClick = { navigationController.navigate(Route.Ideas) },
              iconVector = Icons.Outlined.Lightbulb,
              pressedIconVector = Icons.Filled.Lightbulb,
              isPressed = isIdeasScreenPressed)
          Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically) {
                Box() {
                  HomeIconButton(
                      modifier =
                          Modifier.testTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON),
                      onClick = { navigationController.navigate(Route.OverviewProject) })
                }
              }
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON),
              "Meetings",
              onClick = { navigationController.navigate(Route.Meetings) },
              iconVector = Icons.Default.CalendarToday,
              pressedIconVector = Icons.Filled.CalendarToday,
              isPressed = isMeetingScreenPressed)
          CustomIconButtonComposable(
              modifier =
                  Modifier.weight(1f).testTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON),
              "Profile",
              onClick = { navigationController.navigate(Route.Profile) },
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
fun HomeIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  IconButton(
      modifier = modifier.padding(0.dp).background(EColors.light.primary, shape = CircleShape),
      onClick = onClick) {
        Icon(Icons.Outlined.Home, contentDescription = null, tint = Color.White)
      }
}

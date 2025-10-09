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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ch.eureka.eurekapp.ui.theme.DarkerGray
import ch.eureka.eurekapp.ui.theme.LightGrey
import ch.eureka.eurekapp.ui.theme.LightRed


@Composable
fun BottomBarNavigationComponent(navigationController: NavController, currentScreen: Screen, screenTotalHeight: Int){

    val isTasksPressed =
      (currentScreen == MainScreens.TasksScreen ||
              (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.TasksScreen))



    val isProfileScreenPressed =
        (currentScreen == MainScreens.ProfileScreen ||
                (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.ProfileScreen))


    val isOverviewProjectScreenPressed =
        (currentScreen == MainScreens.OverviewProjectScreen ||
                (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.OverviewProjectScreen))


    val isIdeasScreenPressed =
        (currentScreen == MainScreens.IdeasScreen ||
                (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.IdeasScreen))


    val isProjectSelectionScreenPressed =
        (currentScreen == MainScreens.ProjectSelectionScreen||
                (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.ProjectSelectionScreen))


    val isMeetingScreenPressed =
        (currentScreen == MainScreens.MeetingsScreen||
                (currentScreen is SubScreen && currentScreen.parentScreen == MainScreens.MeetingsScreen))



    BottomAppBar(
        containerColor = LightGrey,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp).height((screenTotalHeight*0.08f).dp).clip(RoundedCornerShape(25.dp)),
        tonalElevation = 8.dp,
        actions = {
            Row(){
                CustomIconButtonComposable(
                    modifier = Modifier.weight(1f),
                    "Tasks",
                    onClick = {navigationFunction(navigationController, destination = MainScreens.TasksScreen)},
                    iconVector = Icons.Outlined.AssignmentTurnedIn,
                    pressedIconVector = Icons.Filled.AssignmentTurnedIn,
                    isPressed = isTasksPressed
                )
                CustomIconButtonComposable(
                    modifier = Modifier.weight(1f),
                    "Ideas",
                    onClick = {navigationFunction(navigationController, destination = MainScreens.IdeasScreen)},
                    iconVector = Icons.Outlined.Lightbulb,
                    pressedIconVector = Icons.Filled.Lightbulb,
                    isPressed = isIdeasScreenPressed
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(){
                        HomeIconButton(
                            onClick = {navigationFunction(navigationController, destination = MainScreens.OverviewProjectScreen)}
                        )
                    }
                }
                CustomIconButtonComposable(
                    modifier = Modifier.weight(1f),
                    "Meetings",
                    onClick = {navigationFunction(navigationController, destination = MainScreens.MeetingsScreen)},
                    iconVector = Icons.Default.CalendarToday,
                    pressedIconVector = Icons.Filled.CalendarToday,
                    isPressed = isMeetingScreenPressed
                )
                CustomIconButtonComposable(
                    modifier = Modifier.weight(1f),
                    "Profile",
                    onClick = {navigationFunction(navigationController, destination = MainScreens.ProfileScreen)},
                    iconVector = Icons.Outlined.AccountCircle,
                    pressedIconVector = Icons.Filled.AccountCircle,
                    isPressed = isProfileScreenPressed
                )






            }
        }
    )
}

@Composable
fun CustomIconButtonComposable(modifier: Modifier = Modifier, title: String, onClick: () -> Unit, iconVector: ImageVector, pressedIconVector: ImageVector, isPressed: Boolean){
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(if(isPressed) pressedIconVector else iconVector, contentDescription = null, tint = if (isPressed) LightRed else DarkerGray)
            Text(title, style = TextStyle(fontSize = 10.sp))
        }
    }
}

@Composable
fun HomeIconButton(modifier: Modifier = Modifier, onClick: () -> Unit){
    IconButton(
        modifier = modifier.padding(0.dp).background(LightRed, shape = CircleShape),
        onClick = onClick
    ) {
        Icon(Icons.Outlined.Home, contentDescription = null, tint = Color.White)
    }
}
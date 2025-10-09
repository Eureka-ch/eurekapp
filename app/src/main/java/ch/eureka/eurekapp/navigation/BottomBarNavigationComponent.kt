package ch.eureka.eurekapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ch.eureka.eurekapp.ui.theme.DarkerGray
import ch.eureka.eurekapp.ui.theme.LightGrey
import ch.eureka.eurekapp.ui.theme.LightRed


@Composable
fun BottomBarNavigationComponent(navigationController: NavController, currentScreenName: String, screenTotalHeight: Int){
    val navBackStackEntry by navigationController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTasksPressed by remember {derivedStateOf {
      currentRoute != null && (
              currentRoute == MainNavigationScreens.TasksScreen.name ||
              currentRoute == TaskSpecificScreens.TasksEditScreen.name ||
              currentRoute == TaskSpecificScreens.TasksDetailScreen.name ||
              currentRoute == TaskSpecificScreens.TaskDependencePage.name ||
              currentRoute == TaskSpecificScreens.AutoTaskAssignmentScreen.name
              )
    }}

    val isProfileScreenPressed by remember { derivedStateOf {
        currentRoute != null &&
                currentRoute == MainNavigationScreens.ProfileScreen.name
    } }

    val isOverviewProjectScreen by remember { derivedStateOf {
        currentRoute != null && currentRoute == MainNavigationScreens.OverviewProjectScreen.name
    } }


    BottomAppBar(
        containerColor = LightGrey,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp).height((screenTotalHeight*0.08f).dp).clip(RoundedCornerShape(25.dp)),
        tonalElevation = 8.dp,
        actions = {
            Row(){
                CustomIconButtonCompposable(
                    modifier = Modifier.weight(1f),
                    "Tasks",
                    onClick = {navigationFunction(navigationController, destination = MainNavigationScreens.TasksScreen.name)},
                    iconVector = Icons.Outlined.AssignmentTurnedIn,
                    pressedIconVector = Icons.Filled.AssignmentTurnedIn
                )
                CustomIconButtonCompposable(
                    modifier = Modifier.weight(1f),
                    "Ideas",
                    onClick = {navigationFunction(navigationController, destination = MainNavigationScreens.IdeasScreen.name)},
                    iconVector = Icons.Outlined.Lightbulb,
                    pressedIconVector = Icons.Filled.Lightbulb
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(){
                        HomeIconButton(
                            onClick = {navigationFunction(navigationController, destination = MainNavigationScreens.OverviewProjectScreen.name)}
                        )
                    }
                }
                CustomIconButtonCompposable(
                    modifier = Modifier.weight(1f),
                    "Meetings",
                    onClick = {navigationFunction(navigationController, destination = MainNavigationScreens.MeetingsScreen.name)},
                    iconVector = Icons.Default.CalendarToday,
                    pressedIconVector = Icons.Filled.CalendarToday
                )
                CustomIconButtonCompposable(
                    modifier = Modifier.weight(1f),
                    "Profile",
                    onClick = {navigationFunction(navigationController, destination = MainNavigationScreens.ProfileScreen.name)},
                    iconVector = Icons.Outlined.AccountCircle,
                    pressedIconVector = Icons.Filled.AccountCircle
                )






            }
        }
    )
}

@Composable
fun CustomIconButtonCompposable(modifier: Modifier = Modifier, title: String, onClick: () -> Unit, iconVector: ImageVector, pressedIconVector: ImageVector, isPressed: Boolean){
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(iconVector, contentDescription = null, tint = if (isPressed) LightRed else DarkerGray)
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
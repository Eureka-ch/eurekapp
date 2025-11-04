package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.tasks.TaskDependenciesViewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.DarkBackground
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.GrayTextColor2
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography

@Composable
fun TaskDependencesScreen(
    projectId: String,
    taskId: String,
    taskDependenciesViewModel: TaskDependenciesViewModel = viewModel()
){
    val projectUsers = taskDependenciesViewModel.getProjectUsers(projectId).collectAsState(
        emptyList())

    val task = taskDependenciesViewModel.getTaskFromRepository(projectId, taskId).collectAsState(null)

    Column(modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ){
            Text(
                text = "Dependencies",
                style = Typography.titleLarge,
                fontWeight = FontWeight(600))
            Text(
                text =
                    "Visualise order and detect blocking",
                style = Typography.titleMedium,
                color = GrayTextColor2)
        }


        /*
        Filter section
        **/
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Surface(
                modifier =
                    Modifier.border(
                        border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                        shape = RoundedCornerShape(16.dp))
                        .fillMaxWidth(0.95f),
                shadowElevation = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 7.dp)
                ){
                    Text("Filtering", style = Typography.titleMedium,
                        fontWeight = FontWeight(500), modifier = Modifier.padding(horizontal = 13.dp))

                    FlowRow(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        projectUsers.value.mapNotNull { userFlow ->
                            val user = userFlow.collectAsState(null)
                            if(user.value == null){
                                return@mapNotNull null
                            }
                            else{
                                OutlinedButton(
                                    onClick = {},
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = DarkColorScheme.background,
                                        containerColor = LightColorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(width = 1.dp, BorderGrayColor)
                                ){
                                    Text(user.value!!.displayName, style = Typography.labelSmall)
                                }
                            }

                        }
                    }
                }
            }
        }

        /*
        Dependencies screen
        **/
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Surface(
                modifier =
                    Modifier.border(
                        border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                        shape = RoundedCornerShape(16.dp))
                        .fillMaxWidth(0.95f),
                shadowElevation = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Dependencies")
                if(task.value != null){
                Row(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp)
                ){
                    TreeView(
                        isParent = true,
                        projectId = projectId,
                        task = task.value,
                        taskDependenciesViewModel = taskDependenciesViewModel
                    )
                }
                }
            }
        }
    }
}

@Composable
fun TreeView(isParent: Boolean = false, projectId: String, task: Task?,
             taskDependenciesViewModel: TaskDependenciesViewModel){
    if(task != null){
        var tasksDependentOn = taskDependenciesViewModel.getDependentTasksForTask(projectId,task)
        Row(){
            if(!isParent){
                Text("->")
            }
            TaskSurfaceComponent(task)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                for(task in tasksDependentOn){
                    val taskState = task.collectAsState(null)
                    TreeView(
                        isParent = false,
                        projectId = projectId,
                        task = taskState.value,
                        taskDependenciesViewModel = taskDependenciesViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TaskSurfaceComponent(task: Task, filterName: String = "All"){
    val taskContainsFilterName = task.assignedUserIds.contains(filterName)
    OutlinedButton(
        onClick = {},
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DarkColorScheme.background,
            containerColor = if(taskContainsFilterName) LightColorScheme.primary
            else LightColorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = 1.dp, BorderGrayColor)
    ){
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                task.title,
                style = Typography.labelSmall,
                color = if(taskContainsFilterName) LightColorScheme.onPrimary
                else DarkBackground
            )
        }
    }
}
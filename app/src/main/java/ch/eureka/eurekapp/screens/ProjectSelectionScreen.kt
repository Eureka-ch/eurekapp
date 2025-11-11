package ch.eureka.eurekapp.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectSelectionScreenViewModel
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.LightingBlue
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.SuccessGreen
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.WarningOrange
import ch.eureka.eurekapp.ui.theme.DarkColorScheme

object ProjectSelectionScreenTestTags {
  const val PROJECT_SELECTION_TEXT = "ProjectSelectionText"
}

@Composable
fun ProjectSelectionScreen(
    onCreateProjectRequest: () -> Unit,
    onProjectSelectRequest: (Project) -> Unit,
    projectSelectionScreenViewModel: ProjectSelectionScreenViewModel = viewModel()
) {
    val projectsList = projectSelectionScreenViewModel.getProjectsForUser()
        .collectAsState(listOf())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            CustomElevatedButton(
                onClick = {
                    onCreateProjectRequest()
                },
                text = "+ Create Project",
                typography = Typography.titleLarge
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(projectsList.value){ project ->
                ProjectCard(project, projectSelectionScreenViewModel, onProjectSelectRequest)
            }
        }
    }
}

@Composable
private fun CustomElevatedButton(onClick: () -> Unit, text: String, typography: TextStyle){
    ElevatedButton(
        modifier = Modifier.padding(vertical = 10.dp),
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = LightingBlue
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp
        ),
    ) {
        Text(text, style = typography,
            color = LightColorScheme.surface, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProjectCard(project: Project,
                        projectSelectionScreenViewModel: ProjectSelectionScreenViewModel,
                        onProjectSelectRequest: (Project) -> Unit){
    Card(
        modifier = Modifier.fillMaxWidth(0.9f).height(220.dp).padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        border = BorderStroke(1.dp, BorderGrayColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            //Show name
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Text(project.name, style = Typography.titleLarge,
                    textAlign = TextAlign.Center, fontWeight = FontWeight(600))
            }

            val projectUsers = projectSelectionScreenViewModel
                .getProjectUsersInformation(project.projectId)
                .collectAsState(listOf())


            //Description users and status
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
            ){
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ){
                    Row(
                        modifier = Modifier.weight(2f)
                    ){
                        InformationContainer(project.description,
                            iconVector = Icons.Default.Description, iconColor = WarningOrange)}
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ){
                        CustomElevatedButton(
                            onClick = {
                                onProjectSelectRequest(project)
                            },
                            text = "Navigate",
                            typography = Typography.titleSmall
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ){
                    Row(
                        modifier = Modifier.weight(2f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(projectUsers.value){ user ->
                                InformationContainer(user.displayName,
                                    iconVector = Icons.Default.Person,
                                    iconColor = DarkColorScheme.background)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        ProjectStatusDisplay(project.status)
                    }
                }
            }
        }
    }
}
@Composable
private fun InformationContainer(text: String, iconVector: ImageVector, iconColor: Color){
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ){
            Icon(iconVector, null, tint = iconColor)
        }
        Row(
            modifier = Modifier.fillMaxHeight().padding(vertical = 5.dp, horizontal = 3.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ){
            Text(text, style = Typography.labelMedium)
        }
    }
}

private val colorMap = mapOf(
    ProjectStatus.OPEN to SuccessGreen,
    ProjectStatus.IN_PROGRESS to LightColorScheme.tertiary,
    ProjectStatus.ARCHIVED to WarningOrange,
    ProjectStatus.COMPLETED to LightColorScheme.onSurfaceVariant
)
@Composable
private fun ProjectStatusDisplay(projectStatus: ProjectStatus){
    Surface(
        color = colorMap.get(projectStatus)!!,
        modifier = Modifier.height(30.dp)
            .width(130.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
    ){
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(projectStatus.name, style = Typography.labelMedium,
                color = LightColorScheme.surface)
        }
    }
}
